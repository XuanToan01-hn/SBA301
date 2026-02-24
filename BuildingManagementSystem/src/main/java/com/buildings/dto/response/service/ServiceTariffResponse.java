package com.buildings.dto.response.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTariffResponse {

    private UUID id;
    private UUID serviceId;
    private BigDecimal price;
    private String currency;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private BigDecimal vatRate;
    private Boolean isActive; // True nếu đang trong thời gian hiệu lực
    private LocalDateTime createdAt;

    // Danh sách bậc thang (nếu billing_method = TIER)
    private List<TierResponse> tiers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TierResponse {
        private UUID id;
        private BigDecimal minVal;
        private BigDecimal maxVal;
        private BigDecimal price;
    }
}
