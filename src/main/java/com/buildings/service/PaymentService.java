package com.buildings.service;

import com.buildings.dto.PageResponse;
import com.buildings.dto.request.payment.PaymentWebhookDTO;
import com.buildings.dto.response.payment.PaymentResponse;
import com.buildings.dto.response.payment.PaymentStatisticsDTO;
import com.buildings.dto.response.payment.PaymentTransactionDTO;
import com.buildings.dto.response.payment.PaymentTransactionDetailDTO;
import com.buildings.dto.response.payment.UploadProofResponse;
import com.buildings.entity.enums.PaymentTransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPayment(UUID billId);

    void handleWebhook(PaymentWebhookDTO webhookDTO);

    // Lịch sử giao dịch (phân trang, filter theo status và billId)
    Page<PaymentTransactionDTO> getTransactionHistory(int page, int size, PaymentTransactionStatus status, UUID billId);

    // Đồng bộ trạng thái từ PayOS (khi webhook bị miss)
    PaymentTransactionDTO syncFromPayOS(Long orderCode);

    // Admin xác nhận thanh toán thủ công
    PaymentTransactionDTO manualConfirm(UUID transactionId, UUID adminId);

    // Thống kê dashboard
    PaymentStatisticsDTO getStatistics();

    // Resident upload bằng chứng thanh toán
    UploadProofResponse uploadProof(UUID transactionId, UUID userId, MultipartFile file);

    // Admin duyệt giao dịch có bằng chứng
    PaymentTransactionDetailDTO approveTransaction(UUID transactionId, UUID adminId);

    // Admin từ chối giao dịch
    PaymentTransactionDetailDTO rejectTransaction(UUID transactionId, String reason, UUID adminId);

    // Admin xem chi tiết đầy đủ của 1 giao dịch
    PaymentTransactionDetailDTO getTransactionDetail(UUID transactionId);

    // Admin xem danh sách giao dịch PENDING có proof (chờ duyệt)
    Page<PaymentTransactionDetailDTO> getPendingProofTransactions(int page, int size);
}
