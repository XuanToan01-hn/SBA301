package com.buildings.service;

import com.buildings.dto.response.payment.PaymentResponse;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse createPaymentDemo();
    PaymentResponse createPayment(UUID billId) throws Exception;
}
