package com.buildings.dto.response.maintenance;

import com.buildings.entity.enums.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MaintenanceRequestResponse {
    private String id;
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
    private String requesterId;
    private String requesterName;
    private String staffId;
    private String staffName;
    private String apartmentId;
    private String apartmentCode;
    private String buildingId;
    private String buildingName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
