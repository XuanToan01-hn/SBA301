package com.buildings.dto.response.maintenance;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaintenanceLogResponse {
    private String id;
    private String action;
    private String note;
    private String actorId;
    private LocalDateTime createdAt;
}
