package com.buildings.dto.response.maintenance;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MaintenanceLogResponse {
    private UUID id;
    private String action;
    private String note;
    private UUID actorId;
    private LocalDateTime createdAt;
}
