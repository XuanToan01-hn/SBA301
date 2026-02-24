package com.buildings.service.impl;

import com.buildings.dto.response.payment.PaymentResponse;
import com.buildings.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PayOS payOS;

    public PaymentResponse createPaymentDemo() {
        // 🔥 HARD CODE DATA
        String billId = UUID.randomUUID().toString();
        String billCode = "HD-2026-001";
        long originalAmount = 1500000L;

        // 👉 Test 1% nếu muốn
        long testAmount = originalAmount / 100; // 15.000 VND

        Long orderCode = System.currentTimeMillis();

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Hoa don " + billCode)
                .quantity(1)
                .price(testAmount)
                .build();

        CreatePaymentLinkRequest request =
                CreatePaymentLinkRequest.builder()
                        .orderCode(orderCode)
                        .amount(testAmount)
                        .description(billCode)
                        .items(List.of(item))
                        .returnUrl("http://localhost:5172/payment-success")
                        .cancelUrl("http://localhost:5172/payment-cancel")
                        .build();

        CreatePaymentLinkResponse response =
                payOS.paymentRequests().create(request);

        return PaymentResponse.builder()
                .billId(billId)
                .billCode(billCode)
                .amount(testAmount)
                .status("PENDING")
                .checkoutUrl(response.getCheckoutUrl())
                .qrCode(response.getQrCode())
                .build();
    }
}

//    public Bill getBillDetail(UUID billId) {
//        System.out.println("Incoming UUID: [" + billId + "]");
//        System.out.println("Total bills in DB: " + billRepository.count());
//
//        return billRepository.findById(billId)
//                .orElseThrow(() -> new RuntimeException("Bill not found"));
//    }

