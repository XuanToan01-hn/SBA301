package com.buildings.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity bậc thang giá cho dịch vụ tính theo bậc (TIER)
 * VD: Điện bậc 1: 0-50 kWh = 1.678đ, bậc 2: 51-100 kWh = 1.734đ...
 */
@Entity
@Table(name = "service_tariff_tiers")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServiceTariffTier extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private ServiceTariff tariff;

    @Column(name = "tariff_id", insertable = false, updatable = false)
    private UUID tariffId;

    @Column(name = "min_val", nullable = false, precision = 10, scale = 2)
    private BigDecimal minVal; // Giá trị tối thiểu của bậc (VD: 0, 51, 101...)

    @Column(name = "max_val", precision = 10, scale = 2)
    private BigDecimal maxVal; // Giá trị tối đa của bậc (null = vô cùng)

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price; // Đơn giá cho bậc này
}
