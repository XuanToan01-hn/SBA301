package com.buildings.entity;

import com.buildings.entity.enums.BillStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills")
@Getter
@Setter
public class Bill extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillStatus status = BillStatus.UNPAID;


    // ===============================
    // RELATIONSHIP (THIS IS REQUIRED)
    // ===============================

    @ManyToOne
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;


    // Liên kết với quotation
//    @OneToOne
//    @JoinColumn(name = "quotation_id", nullable = false)
//    private MaintenanceQuotation quotation;

    // Danh sách item
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillItem> items = new ArrayList<>();

    // =========================
    // BUSINESS METHODS
    // =========================

    public void addItem(BillItem item) {
        item.setBill(this);
        this.items.add(item);
        recalculateTotal();
    }

    public void removeItem(BillItem item) {
        this.items.remove(item);
        item.setBill(null);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(BillItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
