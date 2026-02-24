package com.buildings.dto.response.maintenance;

import com.buildings.entity.enums.ScheduleProposedBy;
import com.buildings.entity.enums.ScheduleStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaintenanceScheduleResponse {
    private String id;
    private String maintenanceRequestId;
    private LocalDateTime proposedTime;
    private Integer estimatedDuration;
    private String note;
    private ScheduleStatus status;
    private ScheduleProposedBy proposedByRole;
    private String proposedById;
    private String proposedByName;
    private String parentScheduleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
