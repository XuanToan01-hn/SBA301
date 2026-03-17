package com.buildings.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatisticsDTO {

    private long totalTransactions;
    private long pendingCount;
    private long successCount;
    private long cancelledCount;
    private long failedCount;
    private BigDecimal totalRevenue;
    private List<MonthlyRevenueSummary> monthlyRevenue;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenueSummary {
        private String month;       // format: "2026-03"
        private BigDecimal revenue;
        private long count;
    }
}
