package com.buildings.dto.response.maintenance;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MaintenanceStatisticsResponse {
    private long totalRequests;
    private Map<String, Long> byStatus;
    private Map<String, Long> byCategory;
    private Map<String, Long> byPriority;
    private double avgResolutionDays;
    private double avgRating;
    private long overdueCount;
    private long pendingCount;
    private long inProgressCount;
    private long completedCount;
    private long cancelledCount;
}
