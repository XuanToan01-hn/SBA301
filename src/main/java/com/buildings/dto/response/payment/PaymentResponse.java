package com.buildings.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private UUID billId;
    private String billCode;
    private Long amount;
    private String status;
    private String checkoutUrl;
    private String qrCode;
}
