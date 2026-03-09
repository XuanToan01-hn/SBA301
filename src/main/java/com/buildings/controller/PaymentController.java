package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.request.payment.PayOSWebhookRequest;
import com.buildings.dto.response.payment.PaymentResponse;
import com.buildings.entity.Bill;
import com.buildings.entity.PaymentTransaction;
import com.buildings.entity.enums.BillStatus;
import com.buildings.repository.BillRepository;
import com.buildings.repository.PaymentTransactionRepository;
import com.buildings.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.buildings.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BillRepository billRepository;

    // Tạo payment link + trả QR
    @PostMapping("/{billId}")
    public ApiResponse<PaymentResponse> createPayment(
            @PathVariable UUID billId) throws Exception {

//        return ApiResponse.<PaymentResponse>builder()
//                .result(paymentService.createPaymentDemo())
//                .build();

        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createPayment(billId))
                .build();
    }

//    @PostMapping("/payos-webhook")
//    public ResponseEntity<?> handleWebhook(@RequestBody PayOSWebhookRequest request) {
//
//        System.out.println("===== PAYMENT SUCCESS =====");
//        System.out.println("OrderCode: " + request.getData().getOrderCode());
//        System.out.println("Amount: " + request.getData().getAmount());
//        System.out.println("Description: " + request.getData().getDescription());
//
//        Long orderCode = request.getData().getOrderCode();
//
//        PaymentTransaction tx =
//                paymentTransactionRepository.findByOrderCode((orderCode));
//
//        tx.setStatus(PaymentStatus.PARTIAL);
//        tx.setPaidAt(LocalDateTime.now());
//
//        paymentTransactionRepository.save(tx);
//
//
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/payos-webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody PayOSWebhookRequest request) {

        System.out.println("===== PAYMENT SUCCESS =====");

        Long orderCode = request.getData().getOrderCode();
        Long amount = request.getData().getAmount();

        System.out.println("OrderCode: " + orderCode);
        System.out.println("Amount: " + amount);

        PaymentTransaction tx = paymentTransactionRepository
                .findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Update transaction
        tx.setStatus(PaymentStatus.SUCCESS);
        tx.setPaidAt(LocalDateTime.now());

        paymentTransactionRepository.save(tx);

        // ==========================
        // UPDATE BILL
        // ==========================

        Bill bill = tx.getBill();

        BigDecimal totalPaid =
                paymentTransactionRepository.sumSuccessAmountByBill(bill.getId());

        if (totalPaid.compareTo(bill.getTotalAmount()) >= 0) {
            bill.setStatus(BillStatus.PAID);
        }

        billRepository.save(bill);

        return ResponseEntity.ok().build();
    }

}
