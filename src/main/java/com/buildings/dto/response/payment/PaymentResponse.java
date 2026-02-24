package com.buildings.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String billId;
    private String billCode;
    private Long amount;
    private String status;
    private String checkoutUrl;
    private String qrCode;
}
