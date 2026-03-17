package com.buildings.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionDetailDTO {

    private UUID transactionId;
    private Long orderCode;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String proofUrl;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime verifiedAt;
    private String rejectedReason;

    private BillInfo bill;
    private ResidentInfo resident;
    private ApartmentInfo apartment;

    // ===================== Nested DTOs =====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillInfo {
        private UUID billId;
        private Integer month;
        private Integer year;
        private Double totalAmount;
        private LocalDateTime dueDate;
        private String status;
        private List<BillItemInfo> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillItemInfo {
        private String name;
        private Double amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResidentInfo {
        private UUID userId;
        private String fullName;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApartmentInfo {
        private UUID apartmentId;
        private String roomNumber;
        private Integer floor;
        private String building;
    }
}
