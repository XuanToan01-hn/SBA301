package com.buildings.dto.response.maintenance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffWorkloadResponse {
    private String staffId;
    private String staffName;
    private long totalAssigned;
    private long inProgress;
    private long completed;
    private long cancelled;
    private Double avgRating;
    private long overdueCount;
}
