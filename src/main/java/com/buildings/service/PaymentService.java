package com.buildings.service;

import com.buildings.dto.request.payment.PaymentWebhookDTO;
import com.buildings.dto.response.payment.PaymentResponse;
import com.buildings.dto.response.payment.PaymentStatisticsDTO;
import com.buildings.dto.response.payment.PaymentTransactionDTO;
import com.buildings.entity.enums.PaymentTransactionStatus;
import org.springframework.data.domain.Page;

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
}
