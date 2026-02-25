package com.buildings.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity biểu giá dịch vụ theo thời kỳ
 * Cho phép thay đổi giá theo thời gian mà không ảnh hưởng lịch sử
 */
@Entity
@Table(name = "service_tariffs")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServiceTariff extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "service_id", insertable = false, updatable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID serviceId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price; // Đơn giá (dùng cho FIXED, AREA, METER)

    @Column(length = 10)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom; // Ngày bắt đầu hiệu lực

    @Column(name = "effective_to")
    private LocalDate effectiveTo; // Ngày kết thúc (null = vô thời hạn)

    @Column(name = "vat_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal vatRate = new BigDecimal("10.00"); // % VAT

    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServiceTariffTier> tiers = new ArrayList<>(); // Bậc thang giá (nếu billing_method = TIER)
}