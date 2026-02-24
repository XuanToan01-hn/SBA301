package com.buildings.dto.response.maintenance;

import com.buildings.entity.enums.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MaintenanceRequestResponse {
    private UUID id;
    private String code;
    private String title;
    private String description;
    private Boolean isBillable;
    private LocalDateTime preferredTime;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime closedAt;
    private RequestScope scope;
    private MaintenanceCategory category;
    private RequestStatus requestStatus;
    private RequestPriority priority;
    private PaymentStatus paymentStatus;
    private UUID requesterId;
    private String requesterName;
    private UUID staffId;
    private String staffName;
    private UUID apartmentId;
    private String apartmentCode;
    private UUID buildingId;
    private String buildingName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
