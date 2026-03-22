package com.buildings.service.impl;

import com.buildings.entity.*;
import com.buildings.entity.enums.ApartmentStatus;
import com.buildings.entity.enums.MeterReadingStatus;
import com.buildings.entity.enums.BillingMethod;
import com.buildings.entity.enums.QuotationStatus;
import com.buildings.entity.enums.RequestScope;
import com.buildings.entity.enums.RequestStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.MonthlyBillMapper;
import com.buildings.repository.*;
import com.buildings.service.MonthlyBillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyBillServiceImpl implements MonthlyBillService {

    private final ApartmentRepository apartmentRepository;
    private final MaintenanceQuotationRepository maintenanceQuotationRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final MonthlyBillsRepository monthlyBillsRepository;
    private final MonthlyBillMapper monthlyBillMapper;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final BillDetailRepository billDetailRepository;

    @Override
    public void generateMonthlyBills(YearMonth billingPeriod) {
        log.info("Starting monthly billing process for period: {}", billingPeriod);
        
        LocalDateTime periodStart = billingPeriod.atDay(1).atStartOfDay();
        LocalDateTime periodEnd = billingPeriod.atEndOfMonth().atTime(23, 59, 59);
        String periodCode = billingPeriod.toString(); // YYYY-MM

        List<Apartment> activeApartments = apartmentRepository.findByStatus(ApartmentStatus.OCCUPIED);
        
        for (Apartment apartment : activeApartments) {
            try {
                processBillForApartment(apartment, periodStart, periodEnd, periodCode);
            } catch (Exception e) {
                log.error("Error generating bill for apartment {}: {}", apartment.getCode(), e.getMessage(), e);
            }
        }
        
        log.info("Finished monthly billing process for period: {}", billingPeriod);
    }

    @Transactional
    public void processBillForApartment(Apartment apartment, LocalDateTime periodStart, LocalDateTime periodEnd, String periodCode) {
        List<BillDetail> details = new ArrayList<>();
        double totalAmount = 0.0;
        double subtotal = 0.0;
        double taxTotal = 0.0;

        MonthlyBills bill = MonthlyBills.builder()
                .apartment(apartment)
                .periodFrom(periodStart)
                .periodTo(periodEnd)
                .periodCode(periodCode)
                .status("UNPAID")
                .issuedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(10))
                .locked(false)
                .details(new ArrayList<>())
                .build();

        // 1. Process Maintenance Quotations
        List<MaintenanceQuotation> quotations = maintenanceQuotationRepository.findQuotationsForBilling(apartment.getId(), periodStart, periodEnd);
        for (MaintenanceQuotation quotation : quotations) {
            MaintenanceRequest maintenanceRequest = quotation.getMaintenanceRequest();
            if (maintenanceRequest == null || maintenanceRequest.getRequestStatus() != RequestStatus.RESIDENT_ACCEPTED) {
                continue;
            }

            String requestCode = maintenanceRequest.getCode();
            if (requestCode != null
                    && billDetailRepository.existsMaintenanceChargeByApartmentAndRequestCode(apartment.getId(), requestCode)) {
                continue;
            }

            List<BillDetail> maintenanceDetails = buildMaintenanceDetails(quotation, requestCode);
            for (BillDetail detail : maintenanceDetails) {
                detail.setBill(bill);
                details.add(detail);
                subtotal += detail.getAmount();
                totalAmount += detail.getTotalLine();
            }
        }

        // 2. Process Meter Readings
        List<MeterReading> readings = meterReadingRepository.findByApartmentIdAndPeriodAndStatus(apartment.getId(), periodCode, MeterReadingStatus.CONFIRMED);
        for (MeterReading reading : readings) {
            BillDetail detail = monthlyBillMapper.toBillDetailBase(reading);
            
            calculateTieredPrice(detail, reading);
            
            detail.setBill(bill);
            details.add(detail);
            
            subtotal += detail.getAmount();
            taxTotal += (detail.getAmount() * detail.getTaxRate() / 100);
            totalAmount += detail.getTotalLine();
            
            reading.setStatus(MeterReadingStatus.LOCKED);
            meterReadingRepository.save(reading);
        }

        if (details.isEmpty()) {
            return;
        }

        bill.getDetails().addAll(details);
        bill.setSubtotal(subtotal);
        bill.setTaxTotal(taxTotal);
        bill.setTotalAmount(totalAmount);

        monthlyBillsRepository.save(bill);
    }

    private void calculateTieredPrice(BillDetail detail, MeterReading reading) {
        ServiceTariff tariff = reading.getService().getTariffs().stream()
                .filter(t -> t.getEffectiveTo() == null || !t.getEffectiveTo().isBefore(reading.getTakenAt().toLocalDate()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active tariff found for service: " + reading.getService().getCode()));

        BigDecimal remainingConsumption = reading.getConsumption();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        detail.setTaxRate(tariff.getVatRate().doubleValue());

        if (reading.getService().getBillingMethod() == BillingMethod.TIER) {
            List<ServiceTariffTier> tiers = new ArrayList<>(tariff.getTiers());
            tiers.sort((t1, t2) -> t1.getMinVal().compareTo(t2.getMinVal()));

            for (ServiceTariffTier tier : tiers) {
                if (remainingConsumption.compareTo(BigDecimal.ZERO) <= 0) break;

                BigDecimal capacity;
                if (tier.getMaxVal() == null) {
                    capacity = remainingConsumption;
                } else {
                    if (tier.getMinVal().compareTo(BigDecimal.ZERO) == 0) {
                        capacity = tier.getMaxVal();
                    } else {
                        capacity = tier.getMaxVal().subtract(tier.getMinVal()).add(BigDecimal.ONE);
                    }
                }

                BigDecimal consumableInTier = remainingConsumption.min(capacity);
                totalAmount = totalAmount.add(consumableInTier.multiply(tier.getPrice()));
                remainingConsumption = remainingConsumption.subtract(consumableInTier);
            }
        } else {
            totalAmount = remainingConsumption.multiply(tariff.getPrice());
        }

        detail.setUnitPrice(tariff.getPrice() != null ? tariff.getPrice().doubleValue() : 0.0);
        detail.setAmount(totalAmount.doubleValue());
        
        BigDecimal vatAmount = totalAmount.multiply(tariff.getVatRate()).divide(new BigDecimal(100));
        detail.setTotalLine(totalAmount.add(vatAmount).doubleValue());
    }

    private List<BillDetail> buildMaintenanceDetails(MaintenanceQuotation quotation, String requestCode) {
        if (quotation.getItems() != null && !quotation.getItems().isEmpty()) {
            return quotation.getItems().stream()
                    .map(item -> monthlyBillMapper.toBillDetail(item, requestCode))
                    .toList();
        }
        return List.of(monthlyBillMapper.toBillDetail(quotation));
    }

    private org.springframework.data.domain.Pageable createPageable(int page, int size, String sortBy, String unSort) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {
            sort = org.springframework.data.domain.Sort.by(sortBy).ascending();
        } else if (unSort != null && !unSort.isEmpty()) {
            sort = org.springframework.data.domain.Sort.by(unSort).descending();
        } else {
            sort = org.springframework.data.domain.Sort.by("periodCode").descending(); // Default sorting
        }
        return org.springframework.data.domain.PageRequest.of(page > 0 ? page - 1 : 0, size, sort);
    }

    @Override
    public com.buildings.dto.PageResponse<com.buildings.dto.response.bill.BillDTO> getAllBills(int page, int size, String sortBy, String unSort) {
        org.springframework.data.domain.Page<MonthlyBills> billPage = monthlyBillsRepository.findAll(createPageable(page, size, sortBy, unSort));
        return buildPageResponse(billPage);
    }

    @Override
    public com.buildings.dto.PageResponse<com.buildings.dto.response.bill.BillDTO> getAllBillsWithFilters(String status, String periodCode, String buildingCode, int page, int size, String sortBy, String unSort) {
        String statusParam = (status != null && !status.isBlank()) ? status : null;
        String periodParam = (periodCode != null && !periodCode.isBlank()) ? periodCode : null;
        String buildingParam = (buildingCode != null && !buildingCode.isBlank()) ? buildingCode : null;
        org.springframework.data.domain.Page<MonthlyBills> billPage = monthlyBillsRepository.findAllWithFilters(statusParam, periodParam, buildingParam, createPageable(page, size, sortBy, unSort));
        return buildPageResponse(billPage);
    }

    @Override
    public com.buildings.dto.response.bill.BillDTO getBillDetails(java.util.UUID id) {
        MonthlyBills bill = monthlyBillsRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + id));
        com.buildings.dto.response.bill.BillDTO billDTO = monthlyBillMapper.toDto(bill);
        
        if (bill.getDetails() != null) {
            billDTO.setDetails(bill.getDetails().stream()
                    .map(monthlyBillMapper::toDetailDto)
                    .toList());
        }
        return billDTO;
    }

    @Override
    @Transactional
    public com.buildings.dto.response.bill.BillDTO getPayableMaintenanceBill(java.util.UUID requestId, java.util.UUID residentId) {
        MaintenanceRequest maintenanceRequest = maintenanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance request not found"));

        if (maintenanceRequest.getRequester() == null
                || maintenanceRequest.getRequester().getId() == null
                || !maintenanceRequest.getRequester().getId().equals(residentId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (maintenanceRequest.getRequestStatus() != RequestStatus.RESIDENT_ACCEPTED) {
            return null;
        }

        String requestCode = maintenanceRequest.getCode();
        if (requestCode == null || requestCode.isBlank()) {
            return null;
        }

        org.springframework.data.domain.Page<MonthlyBills> billPage = findPayableMaintenanceBillPage(residentId, requestCode);

        // Backfill for old accepted requests: if bill is missing, generate/attach maintenance charge on-demand.
        if (billPage.isEmpty()) {
            createOrAttachMaintenanceBill(maintenanceRequest);
            billPage = findPayableMaintenanceBillPage(residentId, requestCode);
        }

        if (billPage.isEmpty()) {
            return null;
        }

        MonthlyBills bill = billPage.getContent().get(0);
        return monthlyBillMapper.toDto(bill);
    }

    private org.springframework.data.domain.Page<MonthlyBills> findPayableMaintenanceBillPage(java.util.UUID residentId, String requestCode) {
        return monthlyBillsRepository.findByResidentAndMaintenanceRequestCode(
                residentId,
                requestCode,
                org.springframework.data.domain.PageRequest.of(0, 1)
        );
    }

    private void createOrAttachMaintenanceBill(MaintenanceRequest maintenanceRequest) {
        if (maintenanceRequest.getScope() != RequestScope.PRIVATE || maintenanceRequest.getApartment() == null) {
            return;
        }

        MaintenanceQuotation approvedQuotation = maintenanceQuotationRepository
                .findFirstByMaintenanceRequestIdAndStatusOrderByCreatedAtDesc(
                        maintenanceRequest.getId(),
                        QuotationStatus.APPROVED)
                .orElse(null);

        if (approvedQuotation == null || approvedQuotation.getTotalAmount() == null) {
            return;
        }

        String requestCode = maintenanceRequest.getCode();
        if (requestCode != null
                && billDetailRepository.existsMaintenanceChargeByApartmentAndRequestCode(
                maintenanceRequest.getApartment().getId(),
                requestCode)) {
            return;
        }

        YearMonth now = YearMonth.now();
        String periodCode = now.toString();
        MonthlyBills bill = monthlyBillsRepository
                .findFirstByApartmentIdAndPeriodCode(maintenanceRequest.getApartment().getId(), periodCode)
                .orElseGet(() -> MonthlyBills.builder()
                        .apartment(maintenanceRequest.getApartment())
                        .periodFrom(now.atDay(1).atStartOfDay())
                        .periodTo(now.atEndOfMonth().atTime(23, 59, 59))
                        .periodCode(periodCode)
                        .status("UNPAID")
                        .issuedAt(LocalDateTime.now())
                        .dueDate(LocalDateTime.now().plusDays(10))
                        .locked(false)
                        .details(new ArrayList<>())
                        .build());

        if (bill.getDetails() == null) {
            bill.setDetails(new ArrayList<>());
        }

        List<BillDetail> maintenanceDetails = buildMaintenanceDetails(approvedQuotation, requestCode);
        for (BillDetail detail : maintenanceDetails) {
            detail.setBill(bill);
            bill.getDetails().add(detail);
        }

        double subtotal = bill.getDetails().stream().mapToDouble(d -> d.getAmount() != null ? d.getAmount() : 0.0).sum();
        double total = bill.getDetails().stream().mapToDouble(d -> d.getTotalLine() != null ? d.getTotalLine() : 0.0).sum();
        bill.setSubtotal(subtotal);
        bill.setTaxTotal(total - subtotal);
        bill.setTotalAmount(total);

        monthlyBillsRepository.save(bill);
    }

    @Override
    public com.buildings.dto.PageResponse<com.buildings.dto.response.bill.BillDTO> getBillsForCurrentMonth(int page, int size, String sortBy, String unSort) {
        String currentPeriodCode = YearMonth.now().toString();
        org.springframework.data.domain.Page<MonthlyBills> billPage = monthlyBillsRepository.findByPeriodCode(currentPeriodCode, createPageable(page, size, sortBy, unSort));
        return buildPageResponse(billPage);
    }

    @Override
    public com.buildings.dto.PageResponse<com.buildings.dto.response.bill.BillDTO> getBillsByUser(java.util.UUID userId, String status, String periodCode, String apartmentCode, int page, int size, String sortBy, String unSort) {
        org.springframework.data.domain.Page<MonthlyBills> billPage = monthlyBillsRepository.findByUserId(userId, status, periodCode, apartmentCode, createPageable(page, size, sortBy, unSort));
        return buildPageResponse(billPage);
    }

    private com.buildings.dto.PageResponse<com.buildings.dto.response.bill.BillDTO> buildPageResponse(org.springframework.data.domain.Page<MonthlyBills> page) {
        return com.buildings.dto.PageResponse.<com.buildings.dto.response.bill.BillDTO>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(monthlyBillMapper::toDto)
                        .toList())
                .build();
    }
}
