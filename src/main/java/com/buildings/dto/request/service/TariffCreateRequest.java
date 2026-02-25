package com.buildings.dto.request.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffCreateRequest {

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;

    @Builder.Default
    private String currency = "VND";

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo; // null = vô thời hạn

    @Builder.Default
    private BigDecimal vatRate = new BigDecimal("10.00");

    @Valid
    private List<TierRequest> tiers; // Chỉ dùng khi billing_method = TIER

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TierRequest {

        @NotNull(message = "Min value is required")
        @DecimalMin(value = "0.0", message = "Min value must be non-negative")
        private BigDecimal minVal;

        private BigDecimal maxVal; // null = vô cùng

        @NotNull(message = "Tier price is required")
        @DecimalMin(value = "0.0", message = "Tier price must be non-negative")
        private BigDecimal price;
    }
}
