package com.buildings.dto.response.maintenance;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MaintenanceProgressResponse {
    private UUID id;
    private UUID maintenanceRequestId;
    private String note;
    private Integer progressPercent;
    private UUID updatedById;
    private String updatedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
