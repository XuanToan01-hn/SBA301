package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.response.payment.PaymentResponse;
import com.buildings.dto.response.payment.PaymentStatisticsDTO;
import com.buildings.dto.response.payment.PaymentTransactionDTO;
import com.buildings.entity.enums.PaymentTransactionStatus;
import com.buildings.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Tạo payment link
    @PostMapping("/{billId}")
    public ApiResponse<PaymentResponse> createPayment(@PathVariable UUID billId) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createPayment(billId))
                .build();
    }

    // Lịch sử giao dịch (admin)
    // GET /api/payments/transactions?page=0&size=20&status=SUCCESS&billId=xxx
    @GetMapping("/transactions")
    public ApiResponse<Page<PaymentTransactionDTO>> getTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PaymentTransactionStatus status,
            @RequestParam(required = false) UUID billId) {
        return ApiResponse.<Page<PaymentTransactionDTO>>builder()
                .result(paymentService.getTransactionHistory(page, size, status, billId))
                .build();
    }

    // Đồng bộ trạng thái từ PayOS (admin - khi webhook bị miss)
    // POST /api/payments/sync/123456789
    @PostMapping("/sync/{orderCode}")
    public ApiResponse<PaymentTransactionDTO> syncFromPayOS(@PathVariable Long orderCode) {
        return ApiResponse.<PaymentTransactionDTO>builder()
                .result(paymentService.syncFromPayOS(orderCode))
                .build();
    }

    // Admin xác nhận thanh toán thủ công
    // POST /api/payments/manual-confirm/xxx
    @PostMapping("/manual-confirm/{transactionId}")
    public ApiResponse<PaymentTransactionDTO> manualConfirm(
            @PathVariable UUID transactionId,
            Authentication authentication) {
        UUID adminId = UUID.fromString(authentication.getName());
        return ApiResponse.<PaymentTransactionDTO>builder()
                .result(paymentService.manualConfirm(transactionId, adminId))
                .build();
    }

    // Dashboard thống kê thanh toán (admin)
    // GET /api/payments/statistics
    @GetMapping("/statistics")
    public ApiResponse<PaymentStatisticsDTO> getStatistics() {
        return ApiResponse.<PaymentStatisticsDTO>builder()
                .result(paymentService.getStatistics())
                .build();
    }
}
