package com.buildings.controller;

import com.buildings.dto.request.payment.PaymentWebhookDTO;
import com.buildings.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentService paymentService;

    @PostMapping("/payos-webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody PaymentWebhookDTO webhookDTO) {
        log.info("PayOS webhook received: orderCode={}, status={}", webhookDTO.getData().getOrderCode(), webhookDTO.getCode());
        paymentService.handleWebhook(webhookDTO);
        return ResponseEntity.ok().build();
    }
}
