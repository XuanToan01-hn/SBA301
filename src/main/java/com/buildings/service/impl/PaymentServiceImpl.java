package com.buildings.service.impl;

import com.buildings.dto.response.payment.PaymentResponse;
import com.buildings.entity.Bill;
import com.buildings.repository.BillRepository;
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
    private final BillRepository billRepository;

    private static final boolean TEST_MODE = true;
    public PaymentResponse createPaymentDemo() {
        // 🔥 HARD CODE DATA
        UUID billId = UUID.randomUUID();
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

    public PaymentResponse createPayment(UUID billId) throws Exception {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        System.out.println("Creating payment for bill: " + bill.getCode());

        Long orderCode = System.currentTimeMillis();

        // ✅ Lấy số tiền gốc
        Long originalAmount = bill.getTotalAmount().longValue();

        // ✅ Nếu test mode → chỉ thanh toán 1%
        Long amountToPay = TEST_MODE ? originalAmount / 100 : originalAmount;

        // Tránh trường hợp số tiền quá nhỏ bị = 0
        if (amountToPay <= 0) {
            amountToPay = 1000L; // tối thiểu 1000đ để test
        }

        System.out.println("Original amount: " + originalAmount);
        System.out.println("Amount to pay (TEST 1%): " + amountToPay);

        // ✅ Lưu orderCode vào bill (rất quan trọng cho webhook sau này)
        bill.setCode(String.valueOf(orderCode));
        billRepository.save(bill);

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Hoa don " + bill.getCode())
                .quantity(1)
                .price(amountToPay)
                .build();

        CreatePaymentLinkRequest request =
                CreatePaymentLinkRequest.builder()
                        .orderCode(orderCode)
                        .amount(amountToPay)
                        .description(String.valueOf(orderCode))
                        .items(List.of(item))
                        .returnUrl("http://localhost:5172/payment-success")
                        .cancelUrl("http://localhost:5172/payment-cancel")
                        .build();

        CreatePaymentLinkResponse response =
                payOS.paymentRequests().create(request);

        return PaymentResponse.builder()
                .billId(UUID.fromString(bill.getId().toString()))
                .billCode(bill.getCode())
                .amount(amountToPay) // trả về số tiền test
                .status(bill.getStatus().name())
                .checkoutUrl(response.getCheckoutUrl())
                .qrCode(response.getQrCode())
                .build();
    }

    public Bill getBillDetail(UUID billId) {
        System.out.println("Incoming UUID: [" + billId + "]");
        System.out.println("Total bills in DB: " + billRepository.count());

        return billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
    }
}

//    public Bill getBillDetail(UUID billId) {
//        System.out.println("Incoming UUID: [" + billId + "]");
//        System.out.println("Total bills in DB: " + billRepository.count());
//
//        return billRepository.findById(billId)
//                .orElseThrow(() -> new RuntimeException("Bill not found"));
//    }

