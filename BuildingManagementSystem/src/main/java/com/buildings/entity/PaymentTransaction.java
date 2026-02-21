//package com.buildings.entity;
//
//import com.buildings.entity.enums.PaymentStatus;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "payment_transactions")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class PaymentTransaction extends BaseEntity {
//
//    // ===============================
//    // 🔹 Many Transactions → One Bill
//    // ===============================
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "bill_id", nullable = false)
//    private Bill bill;
//
//    // ===============================
//    // 🔹 Amount
//    // ===============================
//    @Column(nullable = false, precision = 12, scale = 2)
//    private BigDecimal amount;
//
//    // ===============================
//    // 🔹 Payment Method
//    // ===============================
//    @Column(length = 50)
//    private String method; // BANK_TRANSFER | CASH | GATEWAY
//
//    // ===============================
//    // 🔹 Currency
//    // ===============================
//    @Column(length = 10)
//    private String currency;
//
//    // ===============================
//    // 🔹 Proof Image URL
//    // ===============================
//    @Column(length = 500)
//    private String proofUrl;
//
//    // ===============================
//    // 🔹 Bank Reference
//    // ===============================
//    @Column(length = 100)
//    private String referenceNo;
//
//    // ===============================
//    // 🔹 Status
//    // ===============================
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private PaymentStatus status;
//
//    // ===============================
//    // 🔹 Paid Time
//    // ===============================
//    private LocalDateTime paidAt;
//
//    // ===============================
//    // 🔹 Admin who verified
//    // ===============================
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "posted_by")
//    private User postedBy;
//
//    // ===============================
//    // 🔹 Reject Reason
//    // ===============================
//    @Column(length = 500)
//    private String rejectedReason;
//
//    // ===============================
//    // 🔹 Verified Time
//    // ===============================
//    private LocalDateTime verifiedAt;
//}