package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.request.payment.PayOSWebhookRequest;
import com.buildings.dto.response.payment.PaymentResponse;
import com.buildings.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // Tạo payment link + trả QR
    @PostMapping("/{billId}")
    public ApiResponse<PaymentResponse> createPayment(
            @PathVariable UUID billId) throws Exception {

        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createPaymentDemo())
                .build();
    }

    @PostMapping("/payos-webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody PayOSWebhookRequest request) {

        System.out.println("===== PAYMENT SUCCESS =====");
        System.out.println("OrderCode: " + request.getData().getOrderCode());
        System.out.println("Amount: " + request.getData().getAmount());
        System.out.println("Description: " + request.getData().getDescription());

        return ResponseEntity.ok().build();
    }
}
