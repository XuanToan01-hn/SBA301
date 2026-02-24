package com.buildings.dto.request.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để cập nhật tariff đã tồn tại.
 * Chỉ cho phép sửa price, vatRate, currency và tiers (không sửa
 * effectiveFrom/effectiveTo
 * vì đó là key xác định thời kỳ hiệu lực).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffUpdateRequest {

    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;

    private String currency;

    @Builder.Default
    private BigDecimal vatRate = new BigDecimal("10.00");

    @Valid
    private List<TierRequest> tiers; // Bậc thang – chỉ dùng khi billing_method = TIER

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TierRequest {

        @DecimalMin(value = "0.0", message = "Min value must be non-negative")
        private BigDecimal minVal;

        private BigDecimal maxVal; // null = vô cùng

        @DecimalMin(value = "0.0", message = "Tier price must be non-negative")
        private BigDecimal price;
    }
}
