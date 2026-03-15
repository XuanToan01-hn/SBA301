package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.PageResponse;
import com.buildings.dto.request.payment.RejectTransactionRequest;
import com.buildings.dto.response.payment.PaymentResponse;
import com.buildings.dto.response.payment.PaymentStatisticsDTO;
import com.buildings.dto.response.payment.PaymentTransactionDTO;
import com.buildings.dto.response.payment.PaymentTransactionDetailDTO;
import com.buildings.dto.response.payment.UploadProofResponse;
import com.buildings.entity.User;
import com.buildings.entity.enums.PaymentTransactionStatus;
import com.buildings.repository.UserRepository;
import com.buildings.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

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
        Optional<User> user = userRepository.findByEmail(authentication.getName());
        UUID adminId = user.get().getId();
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

    // Resident upload bằng chứng thanh toán
    // POST /api/payments/transactions/{transactionId}/proof
    @PostMapping("/transactions/{transactionId}/proof")
    public ApiResponse<UploadProofResponse> uploadProof(
            @PathVariable UUID transactionId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Optional<User> user = userRepository.findByEmail(authentication.getName());
        UUID userId = user.get().getId();
        return ApiResponse.<UploadProofResponse>builder()
                .result(paymentService.uploadProof(transactionId, userId, file))
                .build();
    }

    // ===================== ADMIN ENDPOINTS =====================

    // Danh sách giao dịch PENDING có bằng chứng, chờ admin duyệt
    // GET /api/payments/admin/pending-proof?page=0&size=20
    @GetMapping("/admin/pending-proof")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<PaymentTransactionDetailDTO>> getPendingProofTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<Page<PaymentTransactionDetailDTO>>builder()
                .result(paymentService.getPendingProofTransactions(page, size))
                .build();
    }

    // Chi tiết đầy đủ 1 giao dịch (bill, cư dân, căn hộ, ảnh bằng chứng)
    // GET /api/payments/admin/transactions/{transactionId}/detail
    @GetMapping("/admin/transactions/{transactionId}/detail")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PaymentTransactionDetailDTO> getTransactionDetail(
            @PathVariable UUID transactionId) {
        return ApiResponse.<PaymentTransactionDetailDTO>builder()
                .result(paymentService.getTransactionDetail(transactionId))
                .build();
    }

    // Admin duyệt giao dịch → SUCCESS, bill → PAID
    // POST /api/payments/admin/transactions/{transactionId}/approve
    @PostMapping("/admin/transactions/{transactionId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PaymentTransactionDetailDTO> approveTransaction(
            @PathVariable UUID transactionId,
            Authentication authentication) {
        Optional<User> user =  userRepository.findByEmail(authentication.getName());
        UUID adminId = user.get().getId();
        return ApiResponse.<PaymentTransactionDetailDTO>builder()
                .result(paymentService.approveTransaction(transactionId, adminId))
                .build();
    }

    // Admin từ chối giao dịch → CANCELLED, lưu lý do
    // POST /api/payments/admin/transactions/{transactionId}/reject
    @PostMapping("/admin/transactions/{transactionId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PaymentTransactionDetailDTO> rejectTransaction(
            @PathVariable UUID transactionId,
            @RequestBody RejectTransactionRequest request,
            Authentication authentication) {
        Optional<User> user = userRepository.findByEmail(authentication.getName());
        UUID adminId = user.get().getId();
        return ApiResponse.<PaymentTransactionDetailDTO>builder()
                .result(paymentService.rejectTransaction(transactionId, request.getReason(), adminId))
                .build();
    }
}
