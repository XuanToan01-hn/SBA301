package com.buildings.dto.response.maintenance;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StaffWorkloadResponse {
    private UUID staffId;
    private String staffName;
    private long totalAssigned;
    private long inProgress;
    private long completed;
    private long cancelled;
    private Double avgRating;
    private long overdueCount;
}
