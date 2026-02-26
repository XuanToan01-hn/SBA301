package com.buildings.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import com.buildings.entity.enums.BillingMethod;

/**
 * Entity đại diện cho loại dịch vụ (Điện, Nước, Phí quản lý, Phí gửi xe...)
 */
@Entity
@Table(name = "services")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Service extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String code; // ELECTRICITY, WATER, MGMT_FEE, PARKING_FEE, CLEANING_FEE...

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết dịch vụ

    @Column(length = 20)
    private String unit; // kWh, m3, m2, xe/tháng...

    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = true; // Dịch vụ định kỳ hàng tháng

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_method", nullable = false)
    private BillingMethod billingMethod;

    @Column(name = "taxable")
    @Builder.Default
    private Boolean taxable = true; // Có tính thuế VAT không

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true; // Trạng thái hoạt động (false = deactivated, khác với isDeleted)

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServiceTariff> tariffs = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MeterReading> meterReadings = new ArrayList<>();
}