package com.buildings.entity;

import com.buildings.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentTransaction extends BaseEntity {

    // ===============================
    // 🔹 Many Transactions → One Bill
    // ===============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private MonthlyBills bill;

    // ===============================
    // 🔹 Payment Amount
    // ===============================
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // ===============================
    // 🔹 Payment Method
    // ===============================
    @Column(name = "method", length = 50)
    private String method; // BANK_TRANSFER | CASH | GATEWAY

    // ===============================
    // 🔹 Currency
    // ===============================
    @Column(name = "currency", length = 10)
    private String currency;

    // ===============================
    // 🔹 Proof Image (bill transfer screenshot)
    // ===============================
    @Column(name = "proof_url", length = 500)
    private String proofUrl;

    // ===============================
    // 🔹 Bank Transfer Reference
    // ===============================
    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    // ===============================
    // 🔹 Transaction Status
    // ===============================
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    // ===============================
    // 🔹 Time Resident Paid
    // ===============================
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // ===============================
    // 🔹 Admin who verified payment
    // ===============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by")
    private User postedBy;

    // ===============================
    // 🔹 Reject reason (if payment invalid)
    // ===============================
    @Column(name = "rejected_reason", length = 500)
    private String rejectedReason;

    // ===============================
    // 🔹 Admin verification time
    // ===============================
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
}