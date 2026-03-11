package com.buildings.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "monthly_bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MonthlyBills extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @Column(name = "period_from")
    private LocalDateTime periodFrom;

    @Column(name = "period_to")
    private LocalDateTime periodTo;

    @Column(name = "period_code")
    private String periodCode;

    private Double subtotal;

    @Column(name = "tax_total")
    private Double taxTotal;

    @Column(name = "total_amount")
    private Double totalAmount;

    private String status;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    private Boolean locked;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BillDetail> details;

    // ===============================
    // One Bill → Many Payment Transactions
    // ===============================
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentTransaction> paymentTransactions;
}