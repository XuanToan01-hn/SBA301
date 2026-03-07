package com.buildings.dto.response.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 *  Thống kê tổng hợp chỉ số công tơ theo kỳ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodSummaryResponse {

    private String period;

    // Tổng số readings
    private long totalReadings;
    private long draftCount;
    private long confirmedCount;
    private long lockedCount;

    // Thống kê tiêu thụ
    private BigDecimal totalConsumption;
    private BigDecimal avgConsumption;
    private BigDecimal maxConsumption;
    private BigDecimal minConsumption;

    // Cảnh báo
    private long highUsageCount; // Số readings có tiêu thụ bất thường

    // Tiến độ - tỷ lệ đã confirm/lock vs tổng
    private double completionRate; // % readings đã confirmed hoặc locked
}
