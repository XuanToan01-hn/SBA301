package com.buildings.service.impl;

import com.buildings.dto.request.payment.PaymentWebhookDTO;
import com.buildings.dto.response.payment.PaymentResponse;
import com.buildings.dto.response.payment.PaymentStatisticsDTO;
import com.buildings.dto.response.payment.PaymentTransactionDTO;
import com.buildings.entity.MonthlyBills;
import com.buildings.entity.PaymentTransaction;
import com.buildings.entity.User;
import com.buildings.entity.enums.PaymentTransactionStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.PaymentTransactionMapper;
import com.buildings.repository.MonthlyBillsRepository;
import com.buildings.repository.PaymentTransactionRepository;
import com.buildings.repository.UserRepository;
import com.buildings.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PayOS payOS;
    private final MonthlyBillsRepository monthlyBillsRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentTransactionMapper paymentTransactionMapper;
    private final UserRepository userRepository;

    @Value("${app.payment.return-url:http://localhost:5173/payment-success}")
    private String returnUrl;

    @Value("${app.payment.cancel-url:http://localhost:5173/payment-cancel}")
    private String cancelUrl;

    @Override
    @Transactional
    public PaymentResponse createPayment(UUID billId) {
        MonthlyBills bill = monthlyBillsRepository.findById(billId)
                .orElseThrow(() -> new AppException(ErrorCode.BILL_NOT_FOUND));

        // Nếu đã có PENDING transaction → trả lại checkoutUrl cũ (tránh tạo mới khi reload)
        Optional<PaymentTransaction> existingPending =
                paymentTransactionRepository.findFirstByBillIdAndStatusOrderByCreatedAtDesc(billId, PaymentTransactionStatus.PENDING);

        if (existingPending.isPresent()) {
            PaymentTransaction existing = existingPending.get();
            log.info("Bill {} already has a PENDING transaction, returning existing checkout URL", billId);
            return PaymentResponse.builder()
                    .billId(billId)
                    .amount(existing.getAmount().longValue())
                    .checkoutUrl(existing.getCheckoutUrl())
                    .qrCode(existing.getQrCode())
                    .build();
        }

        long amount = bill.getTotalAmount() != null
                ? bill.getTotalAmount().longValue()
                : 0L;

        Long orderCode = System.currentTimeMillis();

        String description = "HD-" + bill.getPeriodCode();
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Hoa don " + bill.getPeriodCode())
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
                .orderCode(orderCode)
                .checkoutUrl(payosResponse.getCheckoutUrl())
                .qrCode(payosResponse.getQrCode())
                .build();

        paymentTransactionRepository.save(transaction);
        log.info("Created new PayOS payment for bill {}, orderCode {}", billId, orderCode);

        return PaymentResponse.builder()
                .billId(billId)
                .amount(amount)
                .checkoutUrl(payosResponse.getCheckoutUrl())
                .qrCode(payosResponse.getQrCode())
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
                                                              UUID billId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return paymentTransactionRepository
                .findAllWithFilters(status, billId, pageable)
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

        MonthlyBills bill = transaction.getBill();
        bill.setStatus("PAID");
        monthlyBillsRepository.save(bill);

        log.info("Manual confirm by admin {} for transaction {}, bill {} marked as PAID",
                adminId, transactionId, bill.getId());

        return paymentTransactionMapper.toDto(transaction);
    }

    // ===================== THỐNG KÊ =====================

    @Override
    @Transactional(readOnly = true)
    public PaymentStatisticsDTO getStatistics() {
        long pending   = paymentTransactionRepository.countByStatus(PaymentTransactionStatus.PENDING);
        long success   = paymentTransactionRepository.countByStatus(PaymentTransactionStatus.SUCCESS);
        long cancelled = paymentTransactionRepository.countByStatus(PaymentTransactionStatus.CANCELLED);
        long failed    = paymentTransactionRepository.countByStatus(PaymentTransactionStatus.FAILED);
        BigDecimal totalRevenue = paymentTransactionRepository.getTotalRevenue();

        List<PaymentStatisticsDTO.MonthlyRevenueSummary> monthly =
                paymentTransactionRepository.getMonthlyRevenueSummary().stream()
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

    // ===================== HELPER =====================

    private void markTransactionSuccess(PaymentTransaction transaction) {
        transaction.setStatus(PaymentTransactionStatus.SUCCESS);
        transaction.setPaidAt(LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        MonthlyBills bill = transaction.getBill();
        bill.setStatus("PAID");
        monthlyBillsRepository.save(bill);
    }
}
