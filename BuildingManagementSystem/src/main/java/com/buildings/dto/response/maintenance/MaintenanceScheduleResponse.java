package com.buildings.dto.response.maintenance;

import com.buildings.entity.enums.ScheduleProposedBy;
import com.buildings.entity.enums.ScheduleStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MaintenanceScheduleResponse {
    private UUID id;
    private UUID maintenanceRequestId;
    private LocalDateTime proposedTime;
    private Integer estimatedDuration;
    private String note;
    private ScheduleStatus status;
    private ScheduleProposedBy proposedByRole;
    private UUID proposedById;
    private String proposedByName;
    private UUID parentScheduleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
