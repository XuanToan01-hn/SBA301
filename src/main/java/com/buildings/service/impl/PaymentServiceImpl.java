package com.buildings.service.impl;

import com.buildings.dto.PageResponse;
import com.buildings.dto.request.payment.PaymentWebhookDTO;
import com.buildings.dto.response.payment.PaymentResponse;
import com.buildings.dto.response.payment.PaymentStatisticsDTO;
import com.buildings.dto.response.payment.PaymentTransactionDTO;
import com.buildings.dto.response.payment.PaymentTransactionDetailDTO;
import com.buildings.dto.response.payment.UploadProofResponse;
import com.buildings.entity.ApartmentResident;
import com.buildings.entity.BillDetail;
import com.buildings.entity.MaintenanceRequest;
import com.buildings.entity.MonthlyBills;
import com.buildings.entity.PaymentTransaction;
import com.buildings.entity.User;
import com.buildings.entity.enums.RequestStatus;
import com.buildings.entity.enums.PaymentTransactionStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.PaymentTransactionMapper;
import com.buildings.repository.MonthlyBillsRepository;
import com.buildings.repository.MaintenanceRequestRepository;
import com.buildings.repository.PaymentTransactionRepository;
import com.buildings.repository.UserRepository;
import com.buildings.service.FileStorageService;
import com.buildings.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PayOS payOS;
    private final MonthlyBillsRepository monthlyBillsRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentTransactionMapper paymentTransactionMapper;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.payment.return-url:http://localhost:5173/payment-success}")
    private String returnUrl;

    @Value("${app.payment.cancel-url:http://localhost:5173/payment-cancel}")
    private String cancelUrl;

    @Override
    @Transactional
        public PaymentResponse createPayment(UUID billId, UUID maintenanceRequestId) {
        MonthlyBills bill = monthlyBillsRepository.findByIdWithDetails(billId)
                .orElseThrow(() -> new AppException(ErrorCode.BILL_NOT_FOUND));

        String paymentReference = null;
        long amount;
        String itemName;
        String description;

        if (maintenanceRequestId != null) {
            MaintenanceRequest maintenanceRequest = maintenanceRequestRepository.findById(maintenanceRequestId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance request not found"));

            if (maintenanceRequest.getRequestStatus() != RequestStatus.RESIDENT_ACCEPTED) {
            throw new AppException(ErrorCode.INVALID_KEY, "Maintenance request must be RESIDENT_ACCEPTED to pay");
            }

            String requestCode = maintenanceRequest.getCode();
            if (requestCode == null || requestCode.isBlank()) {
            throw new AppException(ErrorCode.INVALID_KEY, "Maintenance request code is invalid");
            }

            amount = resolveMaintenanceAmountFromBill(bill, requestCode);
            if (amount <= 0) {
            throw new AppException(ErrorCode.INVALID_KEY, "No payable maintenance fee found in this bill");
            }

            paymentReference = requestCode;
            itemName = "Phi bao tri " + requestCode;
            description = ("BT-" + requestCode);
        } else {
            amount = bill.getTotalAmount() != null ? bill.getTotalAmount().longValue() : 0L;
            itemName = "Hoa don " + bill.getPeriodCode();
            description = "HD-" + bill.getPeriodCode();
        }

        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        // Nếu đã có PENDING transaction → trả lại checkoutUrl cũ (tránh tạo mới khi reload)
        Optional<PaymentTransaction> existingPending = paymentReference == null
            ? paymentTransactionRepository.findFirstByBillIdAndStatusAndReferenceNoIsNullOrderByCreatedAtDesc(
                billId,
                PaymentTransactionStatus.PENDING)
            : paymentTransactionRepository.findFirstByBillIdAndStatusAndReferenceNoOrderByCreatedAtDesc(
                billId,
                PaymentTransactionStatus.PENDING,
                paymentReference);

        if (existingPending.isPresent()) {
            PaymentTransaction existing = existingPending.get();
            log.info("Bill {} already has a PENDING transaction, returning existing checkout URL", billId);

            String lastRejectedReason = paymentReference == null
                ? paymentTransactionRepository
                .findFirstByBillIdAndStatusAndReferenceNoIsNullOrderByCreatedAtDesc(
                    billId,
                    PaymentTransactionStatus.CANCELLED)
                .map(PaymentTransaction::getRejectedReason)
                .orElse(null)
                : paymentTransactionRepository
                .findFirstByBillIdAndStatusAndReferenceNoOrderByCreatedAtDesc(
                    billId,
                    PaymentTransactionStatus.CANCELLED,
                    paymentReference)
                .map(PaymentTransaction::getRejectedReason)
                .orElse(null);

            return PaymentResponse.builder()
                    .transactionId(existing.getId())
                    .billId(billId)
                    .amount(existing.getAmount().longValue())
                    .checkoutUrl(existing.getCheckoutUrl())
                    .qrCode(existing.getQrCode())
                    .rejectedReason(lastRejectedReason)
                    .build();
        }

        Long orderCode = System.currentTimeMillis();

        PaymentLinkItem item = PaymentLinkItem.builder()
            .name(itemName)
                .quantity(1)
                .price(amount)
                .build();

        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description(description)
                .items(List.of(item))
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();

        CreatePaymentLinkResponse payosResponse = payOS.paymentRequests().create(request);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .bill(bill)
                .amount(BigDecimal.valueOf(amount))
                .currency("VND")
                .status(PaymentTransactionStatus.PENDING)
                .referenceNo(paymentReference)
                .orderCode(orderCode)
                .checkoutUrl(payosResponse.getCheckoutUrl())
                .qrCode(payosResponse.getQrCode())
                .build();

        paymentTransactionRepository.save(transaction);
        log.info("Created new PayOS payment for bill {}, orderCode {}", billId, orderCode);

        // Lấy rejectedReason từ lần bị từ chối gần nhất (nếu có) để frontend hiển thị
        String lastRejectedReason = paymentReference == null
            ? paymentTransactionRepository
            .findFirstByBillIdAndStatusAndReferenceNoIsNullOrderByCreatedAtDesc(
                billId,
                PaymentTransactionStatus.CANCELLED)
            .map(PaymentTransaction::getRejectedReason)
            .orElse(null)
            : paymentTransactionRepository
            .findFirstByBillIdAndStatusAndReferenceNoOrderByCreatedAtDesc(
                billId,
                PaymentTransactionStatus.CANCELLED,
                paymentReference)
            .map(PaymentTransaction::getRejectedReason)
            .orElse(null);

        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .billId(billId)
                .amount(amount)
                .checkoutUrl(payosResponse.getCheckoutUrl())
                .qrCode(payosResponse.getQrCode())
                .rejectedReason(lastRejectedReason)
                .build();
    }

    @Override
    @Transactional
    public void handleWebhook(PaymentWebhookDTO webhookDTO) {
        if (webhookDTO.getData() == null || webhookDTO.getData().getOrderCode() == null) {
            log.warn("Webhook received with missing data/orderCode, payload: {}", webhookDTO);
            return;
        }

        Long orderCode = webhookDTO.getData().getOrderCode();

        PaymentTransaction transaction = paymentTransactionRepository
                .findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_TRANSACTION_NOT_FOUND));

        if ("00".equals(webhookDTO.getCode())) {
            markTransactionSuccess(transaction);
            log.info("Payment SUCCESS via webhook for orderCode {}, bill {} marked as PAID",
                    orderCode, transaction.getBill().getId());
        } else {
            transaction.setStatus(PaymentTransactionStatus.CANCELLED);
            paymentTransactionRepository.save(transaction);
            log.info("Payment non-success webhook for orderCode {}, code={}", orderCode, webhookDTO.getCode());
        }
    }

    // ===================== LỊCH SỬ GIAO DỊCH =====================

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDTO> getTransactionHistory(int page, int size,
                                                             PaymentTransactionStatus status,
                                                             UUID billId,
                                                             Integer month,
                                                             Integer year) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return paymentTransactionRepository
                .findAllWithFilters(status, billId, month, year, pageable)
                .map(paymentTransactionMapper::toDto);
    }

    // ===================== ĐỒNG BỘ TỪ PAYOS =====================

    @Override
    @Transactional
    public PaymentTransactionDTO syncFromPayOS(Long orderCode) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_TRANSACTION_NOT_FOUND));

        if (transaction.getStatus() != PaymentTransactionStatus.PENDING) {
            log.info("Transaction {} already processed (status={}), skip sync", orderCode, transaction.getStatus());
            return paymentTransactionMapper.toDto(transaction);
        }

        try {
            PaymentLink paymentData = payOS.paymentRequests().get(orderCode);
            String payosStatus = paymentData.getStatus().getValue();
            log.info("PayOS status for orderCode {}: {}", orderCode, payosStatus);

            if ("PAID".equalsIgnoreCase(payosStatus)) {
                markTransactionSuccess(transaction);
                log.info("Synced SUCCESS from PayOS for orderCode {}", orderCode);
            } else if ("CANCELLED".equalsIgnoreCase(payosStatus) || "EXPIRED".equalsIgnoreCase(payosStatus)) {
                transaction.setStatus(PaymentTransactionStatus.CANCELLED);
                paymentTransactionRepository.save(transaction);
                log.info("Synced CANCELLED from PayOS for orderCode {}", orderCode);
            } else {
                log.info("PayOS status is still {}, no update needed", payosStatus);
            }
        } catch (Exception e) {
            log.error("Failed to sync from PayOS for orderCode {}: {}", orderCode, e.getMessage());
            throw new AppException(ErrorCode.PAYMENT_SYNC_FAILED);
        }

        return paymentTransactionMapper.toDto(transaction);
    }

    // ===================== XÁC NHẬN THỦ CÔNG =====================

    @Override
    @Transactional
    public PaymentTransactionDTO manualConfirm(UUID transactionId, UUID adminId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_TRANSACTION_NOT_FOUND));

        if (transaction.getStatus() != PaymentTransactionStatus.PENDING) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        User admin = userRepository.findById(adminId).orElse(null);

        transaction.setStatus(PaymentTransactionStatus.SUCCESS);
        transaction.setPaidAt(LocalDateTime.now());
        transaction.setVerifiedAt(LocalDateTime.now());
        transaction.setPostedBy(admin);
        paymentTransactionRepository.save(transaction);

        updateBillStatusByPaidAmount(transaction.getBill());

        log.info("Manual confirm by admin {} for transaction {}, bill {} marked as PAID",
            adminId, transactionId, transaction.getBill().getId());

        return paymentTransactionMapper.toDto(transaction);
    }

    // ===================== THỐNG KÊ =====================

    @Override
    @Transactional(readOnly = true)
    public PaymentStatisticsDTO getStatistics(Integer month, Integer year) {
        long pending   = paymentTransactionRepository.countByStatusAndPeriod(PaymentTransactionStatus.PENDING,   month, year);
        long success   = paymentTransactionRepository.countByStatusAndPeriod(PaymentTransactionStatus.SUCCESS,   month, year);
        long cancelled = paymentTransactionRepository.countByStatusAndPeriod(PaymentTransactionStatus.CANCELLED, month, year);
        long failed    = paymentTransactionRepository.countByStatusAndPeriod(PaymentTransactionStatus.FAILED,    month, year);
        BigDecimal totalRevenue = paymentTransactionRepository.getTotalRevenue(month, year);

        List<PaymentStatisticsDTO.MonthlyRevenueSummary> monthly =
                paymentTransactionRepository.getMonthlyRevenueSummary(year).stream()
                        .map(row -> PaymentStatisticsDTO.MonthlyRevenueSummary.builder()
                                .month((String) row[0])
                                .revenue(new BigDecimal(row[1].toString()))
                                .count(((Number) row[2]).longValue())
                                .build())
                        .collect(Collectors.toList());

        return PaymentStatisticsDTO.builder()
                .totalTransactions(pending + success + cancelled + failed)
                .pendingCount(pending)
                .successCount(success)
                .cancelledCount(cancelled)
                .failedCount(failed)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .monthlyRevenue(monthly)
                .build();
    }

    // ===================== UPLOAD BẰNG CHỨNG =====================

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png"
    );
    private static final long MAX_PROOF_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    @Transactional
    public UploadProofResponse uploadProof(UUID transactionId, UUID userId, MultipartFile file) {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new AppException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        if (file.getSize() > MAX_PROOF_SIZE) {
            throw new AppException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_TRANSACTION_NOT_FOUND));

        try {
            String objectName = fileStorageService.uploadFile(file, "payment-proofs");
            String proofUrl = fileStorageService.getFileUrl(objectName);

            transaction.setProofUrl(proofUrl);
            paymentTransactionRepository.save(transaction);

            log.info("Proof uploaded for transaction {}, url: {}", transactionId, proofUrl);

            return UploadProofResponse.builder()
                    .transactionId(transactionId)
                    .proofUrl(proofUrl)
                    .status(transaction.getStatus().name())
                    .build();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload proof for transaction {}: {}", transactionId, e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // ===================== ADMIN DUYỆT / TỪ CHỐI =====================

    @Override
    @Transactional
    public PaymentTransactionDetailDTO approveTransaction(UUID transactionId, UUID adminId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_TRANSACTION_NOT_FOUND));

        if (transaction.getStatus() != PaymentTransactionStatus.PENDING || transaction.getProofUrl() == null) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_AWAITING_PROOF);
        }

        User admin = userRepository.findById(adminId).orElse(null);

        transaction.setStatus(PaymentTransactionStatus.SUCCESS);
        transaction.setPaidAt(LocalDateTime.now());
        transaction.setVerifiedAt(LocalDateTime.now());
        transaction.setPostedBy(admin);
        paymentTransactionRepository.save(transaction);

        updateBillStatusByPaidAmount(transaction.getBill());

        log.info("Transaction {} approved by admin {}, bill {} payment status recalculated", transactionId, adminId, transaction.getBill().getId());

        return buildTransactionDetail(transaction);
    }

    @Override
    @Transactional
    public PaymentTransactionDetailDTO rejectTransaction(UUID transactionId, String reason, UUID adminId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_TRANSACTION_NOT_FOUND));

        if (transaction.getStatus() != PaymentTransactionStatus.PENDING || transaction.getProofUrl() == null) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_AWAITING_PROOF);
        }

        transaction.setStatus(PaymentTransactionStatus.CANCELLED);
        transaction.setRejectedReason(reason);
        transaction.setVerifiedAt(LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        log.info("Transaction {} rejected by admin {}, reason: {}", transactionId, adminId, reason);

        return buildTransactionDetail(transaction);
    }

    // ===================== CHI TIẾT GIAO DỊCH =====================

    @Override
    @Transactional(readOnly = true)
    public PaymentTransactionDetailDTO getTransactionDetail(UUID transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_TRANSACTION_NOT_FOUND));
        return buildTransactionDetail(transaction);
    }

    // ===================== DANH SÁCH CHỜ DUYỆT =====================

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDetailDTO> getPendingProofTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentTransaction> transactions = paymentTransactionRepository.findPendingWithProof(pageable);
        List<PaymentTransactionDetailDTO> dtos = transactions.getContent().stream()
                .map(this::buildTransactionDetail)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, transactions.getTotalElements());
    }

    // ===================== HELPER =====================

    private void markTransactionSuccess(PaymentTransaction transaction) {
        transaction.setStatus(PaymentTransactionStatus.SUCCESS);
        transaction.setPaidAt(LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        updateBillStatusByPaidAmount(transaction.getBill());
    }

    private long resolveMaintenanceAmountFromBill(MonthlyBills bill, String requestCode) {
        if (bill.getDetails() == null || requestCode == null) {
            return 0L;
        }

        String marker = requestCode.toLowerCase();
        double amount = bill.getDetails().stream()
                .filter(d -> d.getDescription() != null && d.getDescription().toLowerCase().contains(marker))
                .mapToDouble(d -> d.getTotalLine() != null ? d.getTotalLine() : 0.0)
                .sum();

        return Math.round(amount);
    }

    private void updateBillStatusByPaidAmount(MonthlyBills bill) {
        BigDecimal paidAmount = paymentTransactionRepository.getPaidAmountByBillId(bill.getId());
        BigDecimal totalAmount = bill.getTotalAmount() != null
                ? BigDecimal.valueOf(bill.getTotalAmount())
                : BigDecimal.ZERO;

        if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            bill.setStatus("UNPAID");
        } else if (paidAmount.compareTo(totalAmount) >= 0) {
            bill.setStatus("PAID");
        } else {
            bill.setStatus("PARTIAL");
        }

        monthlyBillsRepository.save(bill);
    }

    private PaymentTransactionDetailDTO buildTransactionDetail(PaymentTransaction tx) {
        MonthlyBills bill = tx.getBill();

        // Bill items
        List<PaymentTransactionDetailDTO.BillItemInfo> items = bill.getDetails() != null
                ? bill.getDetails().stream()
                        .map(d -> PaymentTransactionDetailDTO.BillItemInfo.builder()
                                .name(d.getDescription())
                                .amount(d.getTotalLine())
                                .build())
                        .collect(Collectors.toList())
                : List.of();

        // Month/Year từ periodFrom
        Integer month = bill.getPeriodFrom() != null ? bill.getPeriodFrom().getMonthValue() : null;
        Integer year  = bill.getPeriodFrom() != null ? bill.getPeriodFrom().getYear() : null;

        PaymentTransactionDetailDTO.BillInfo billInfo = PaymentTransactionDetailDTO.BillInfo.builder()
                .billId(bill.getId())
                .month(month)
                .year(year)
                .totalAmount(bill.getTotalAmount())
                .dueDate(bill.getDueDate())
                .status(bill.getStatus())
                .items(items)
                .build();

        // Resident: lấy cư dân đang ở (movedOutAt == null)
        PaymentTransactionDetailDTO.ResidentInfo residentInfo = null;
        if (bill.getApartment() != null && bill.getApartment().getResidents() != null) {
            residentInfo = bill.getApartment().getResidents().stream()
                    .filter(r -> r.getMovedOutAt() == null)
                    .findFirst()
                    .map(r -> PaymentTransactionDetailDTO.ResidentInfo.builder()
                            .userId(r.getUser().getId())
                            .fullName(r.getUser().getFullName())
                            .email(r.getUser().getEmail())
                            .phone(r.getUser().getPhone())
                            .build())
                    .orElse(null);
        }

        // Apartment
        PaymentTransactionDetailDTO.ApartmentInfo apartmentInfo = null;
        if (bill.getApartment() != null) {
            apartmentInfo = PaymentTransactionDetailDTO.ApartmentInfo.builder()
                    .apartmentId(bill.getApartment().getId())
                    .roomNumber(bill.getApartment().getCode())
                    .floor(bill.getApartment().getFloorNumber())
                    .building(bill.getApartment().getBuilding() != null
                            ? bill.getApartment().getBuilding().getName() : null)
                    .build();
        }

        return PaymentTransactionDetailDTO.builder()
                .transactionId(tx.getId())
                .orderCode(tx.getOrderCode())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .status(tx.getStatus().name())
            .proofUrl(tx.getProofUrl() != null ? fileStorageService.getFileUrl(tx.getProofUrl()) : null)
                .createdAt(tx.getCreatedAt())
                .paidAt(tx.getPaidAt())
                .verifiedAt(tx.getVerifiedAt())
                .rejectedReason(tx.getRejectedReason())
                .bill(billInfo)
                .resident(residentInfo)
                .apartment(apartmentInfo)
                .build();
    }
}
