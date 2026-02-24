package com.buildings.dto.response.maintenance;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaintenanceProgressResponse {
    private String id;
    private String maintenanceRequestId;
    private String note;
    private Integer progressPercent;
    private String updatedById;
    private String updatedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
