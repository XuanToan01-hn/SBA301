package com.buildings.dto.response.payment;

import com.buildings.entity.enums.PaymentTransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionDTO {

    private UUID id;
    private UUID billId;
    private String billPeriodCode;
    private String apartmentCode;
    private BigDecimal amount;
    private String method;
    private String currency;
    private PaymentTransactionStatus status;
    private Long orderCode;
    private String checkoutUrl;
    private String qrCode;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
