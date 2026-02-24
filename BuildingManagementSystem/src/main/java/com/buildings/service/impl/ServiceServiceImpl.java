package com.buildings.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.buildings.dto.request.service.ServiceCreateRequest;
import com.buildings.dto.request.service.ServiceUpdateRequest;
import com.buildings.dto.request.service.TariffCreateRequest;
import com.buildings.dto.request.service.TariffUpdateRequest;
import com.buildings.dto.response.service.ServiceResponse;
import com.buildings.dto.response.service.ServiceTariffResponse;
import com.buildings.entity.ServiceTariff;
import com.buildings.entity.ServiceTariffTier;
import com.buildings.entity.enums.BillingMethod;
import com.buildings.entity.enums.MeterReadingStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.ServiceMapper;
import com.buildings.repository.MeterReadingRepository;
import com.buildings.repository.ServiceRepository;
import com.buildings.repository.ServiceTariffRepository;
import com.buildings.repository.ServiceTariffTierRepository;
import com.buildings.service.ServiceService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceTariffRepository tariffRepository;
    private final ServiceTariffTierRepository tierRepository;
    private final ServiceMapper serviceMapper;
    private final MeterReadingRepository meterReadingRepository;

    @Override
    public ServiceResponse create(ServiceCreateRequest request) {
        // Check duplicate code
        if (serviceRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.SERVICE_CODE_EXISTED);
        }

        com.buildings.entity.Service service = serviceMapper.toEntity(request);
        service = serviceRepository.save(service);

        log.info("Created service: {} - {}", service.getCode(), service.getName());
        return serviceMapper.toResponse(service);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getById(UUID id) {
        com.buildings.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));
        return serviceMapper.toResponse(service);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getByIdWithTariffs(UUID id) {
        com.buildings.entity.Service service = serviceRepository.findByIdWithTariffs(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        ServiceResponse response = serviceMapper.toResponse(service);
        response.setTariffs(serviceMapper.toTariffResponseList(service.getTariffs()));

        // Set current tariff
        tariffRepository.findCurrentTariff(id)
                .ifPresent(tariff -> response.setCurrentTariff(serviceMapper.toTariffResponse(tariff)));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getAll() {
        return serviceMapper.toResponseList(serviceRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllActive() {
        return serviceMapper.toResponseList(serviceRepository.findAllActive());
    }

    @Override
    public ServiceResponse update(UUID id, ServiceUpdateRequest request) {
        com.buildings.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        serviceMapper.updateEntity(service, request);
        service = serviceRepository.save(service);

        log.info("Updated service: {}", service.getCode());
        return serviceMapper.toResponse(service);
    }

    @Override
    public void delete(UUID id) {
        com.buildings.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        service.setIsDeleted(true);
        serviceRepository.save(service);

        log.info("Soft deleted service: {}", service.getCode());
    }

    @Override
    public ServiceResponse deactivate(UUID id) {
        com.buildings.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        // BR-03: Check if already inactive
        if (Boolean.FALSE.equals(service.getIsActive())) {
            throw new AppException(ErrorCode.SERVICE_ALREADY_INACTIVE);
        }

        // BR-03: Check for active meter readings in current period
        String currentPeriod = java.time.YearMonth.now().toString(); // vd: "2025-02"
        boolean hasActiveReadings = meterReadingRepository
                .findByPeriodAndStatus(currentPeriod, MeterReadingStatus.DRAFT).stream()
                .anyMatch(mr -> mr.getService().getId().equals(id));
        if (!hasActiveReadings) {
            // Also check CONFIRMED status
            hasActiveReadings = meterReadingRepository
                    .findByPeriodAndStatus(currentPeriod, MeterReadingStatus.CONFIRMED).stream()
                    .anyMatch(mr -> mr.getService().getId().equals(id));
        }

        if (hasActiveReadings) {
            throw new AppException(ErrorCode.SERVICE_HAS_ACTIVE_READINGS);
        }

        service.setIsActive(false);
        service = serviceRepository.save(service);

        log.info("Deactivated service: {}", service.getCode());
        return serviceMapper.toResponse(service);
    }

    @Override
    public ServiceResponse activate(UUID id) {
        com.buildings.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        if (Boolean.TRUE.equals(service.getIsActive())) {
            throw new AppException(ErrorCode.SERVICE_ALREADY_ACTIVE);
        }

        service.setIsActive(true);
        service = serviceRepository.save(service);

        log.info("Activated service: {}", service.getCode());
        return serviceMapper.toResponse(service);
    }
    // ==================== TARIFF MANAGEMENT ====================

    @Override
    public ServiceTariffResponse addTariff(UUID serviceId, TariffCreateRequest request) {
        com.buildings.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        // BR-03: Validate effectiveFrom không trùng tariff hiện tại
        boolean dateConflict = tariffRepository.findByServiceIdOrderByEffectiveFromDesc(serviceId)
                .stream()
                .anyMatch(t -> t.getEffectiveFrom().equals(request.getEffectiveFrom()));
        if (dateConflict) {
            throw new AppException(ErrorCode.TARIFF_OVERLAPPING);
        }

        // BR-01: Auto-close tariff cũ nhất đang active (effectiveTo = null)
        // Set effectiveTo = effectiveFrom mới - 1 ngày
        tariffRepository.findCurrentTariff(serviceId).ifPresent(currentTariff -> {
            if (currentTariff.getEffectiveTo() == null) {
                currentTariff.setEffectiveTo(request.getEffectiveFrom().minusDays(1));
                tariffRepository.save(currentTariff);
                log.info("Auto-closed tariff {} with effectiveTo: {}",
                        currentTariff.getId(), currentTariff.getEffectiveTo());
            }
        });

        ServiceTariff tariff = serviceMapper.toTariffEntity(request);
        tariff.setService(service);

        tariff = tariffRepository.save(tariff);

        // Add tiers if billing method is TIER
        if (service.getBillingMethod() == BillingMethod.TIER && request.getTiers() != null) {
            final ServiceTariff savedTariff = tariff;
            List<ServiceTariffTier> tiers = request.getTiers().stream()
                    .map(tierRequest -> {
                        ServiceTariffTier tier = serviceMapper.toTierEntity(tierRequest);
                        tier.setTariff(savedTariff);
                        return tier;
                    })
                    .toList();
            tierRepository.saveAll(tiers);
            tariff.setTiers(tiers);
        }

        log.info("Added tariff for service: {}, effective from: {}", service.getCode(), request.getEffectiveFrom());
        return serviceMapper.toTariffResponse(tariff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceTariffResponse> getTariffsByServiceId(UUID serviceId) {
        // Verify service exists
        if (!serviceRepository.existsById(serviceId)) {
            throw new AppException(ErrorCode.SERVICE_NOT_FOUND);
        }

        List<ServiceTariff> tariffs = tariffRepository.findByServiceIdOrderByEffectiveFromDesc(serviceId);
        return serviceMapper.toTariffResponseList(tariffs);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceTariffResponse getCurrentTariff(UUID serviceId) {
        return tariffRepository.findCurrentTariff(serviceId)
                .map(serviceMapper::toTariffResponse)
                .orElseThrow(() -> new AppException(ErrorCode.TARIFF_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceTariffResponse getTariffByDate(UUID serviceId, LocalDate date) {
        return tariffRepository.findActiveTariff(serviceId, date)
                .map(serviceMapper::toTariffResponse)
                .orElseThrow(() -> new AppException(ErrorCode.TARIFF_NOT_FOUND));
    }

    @Override
    public ServiceTariffResponse updateTariff(UUID tariffId, TariffUpdateRequest request) {
        ServiceTariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new AppException(ErrorCode.TARIFF_NOT_FOUND));

        // Cập nhật các field được phép thay đổi
        if (request.getPrice() != null) {
            tariff.setPrice(request.getPrice());
        }
        if (request.getVatRate() != null) {
            tariff.setVatRate(request.getVatRate());
        }
        if (request.getCurrency() != null) {
            tariff.setCurrency(request.getCurrency());
        }

        tariff = tariffRepository.save(tariff);

        // Cập nhật tiers nếu có
        if (request.getTiers() != null) {
            // Xóa tiers cũ và thêm tiers mới
            tierRepository.deleteByTariffId(tariffId);
            final ServiceTariff savedTariff = tariff;
            List<ServiceTariffTier> newTiers = request.getTiers().stream()
                    .map(tierReq -> {
                        ServiceTariffTier tier = new ServiceTariffTier();
                        tier.setMinVal(tierReq.getMinVal());
                        tier.setMaxVal(tierReq.getMaxVal());
                        tier.setPrice(tierReq.getPrice());
                        tier.setTariff(savedTariff);
                        return tier;
                    })
                    .toList();
            tierRepository.saveAll(newTiers);
            tariff.setTiers(newTiers);
        }

        log.info("Updated tariff: {}", tariffId);
        return serviceMapper.toTariffResponse(tariff);
    }

    @Override
    public void deleteTariff(UUID tariffId) {
        ServiceTariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new AppException(ErrorCode.TARIFF_NOT_FOUND));

        tierRepository.deleteByTariffId(tariffId);
        tariffRepository.delete(tariff);

        log.info("Deleted tariff: {}", tariffId);
    }

    // ==================== PRICE CALCULATION ====================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculatePrice(UUID serviceId, BigDecimal consumption, LocalDate date) {
        com.buildings.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        ServiceTariff tariff = tariffRepository.findActiveTariff(serviceId, date)
                .orElseThrow(() -> new AppException(ErrorCode.TARIFF_NOT_FOUND));

        if (service.getBillingMethod() == BillingMethod.TIER) {
            return calculateTieredPrice(tariff.getId(), consumption);
        }

        // For FIXED, AREA, METER: simple multiplication
        return tariff.getPrice().multiply(consumption);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTieredPrice(UUID tariffId, BigDecimal consumption) {
        List<ServiceTariffTier> tiers = tierRepository.findByTariffIdOrderByMinValAsc(tariffId);

        if (tiers.isEmpty()) {
            throw new AppException(ErrorCode.TARIFF_NOT_FOUND);
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal remainingConsumption = consumption;

        for (ServiceTariffTier tier : tiers) {
            if (remainingConsumption.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal tierMin = tier.getMinVal();
            BigDecimal tierMax = tier.getMaxVal() != null ? tier.getMaxVal() : consumption;

            // Calculate consumption in this tier
            BigDecimal tierRange = tierMax.subtract(tierMin);
            BigDecimal consumptionInTier = remainingConsumption.min(tierRange);

            if (consumptionInTier.compareTo(BigDecimal.ZERO) > 0) {
                totalPrice = totalPrice.add(consumptionInTier.multiply(tier.getPrice()));
                remainingConsumption = remainingConsumption.subtract(consumptionInTier);
            }
        }

        return totalPrice;
    }
}
