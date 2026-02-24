package com.buildings.dto.request.maintenance;

import com.buildings.entity.enums.MaintenanceCategory;
import com.buildings.entity.enums.PaymentStatus;
import com.buildings.entity.enums.RequestPriority;
import com.buildings.entity.enums.RequestScope;
import com.buildings.entity.enums.RequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRequestUpdateRequest {
    private String title;
    private String description;
    private RequestScope scope;
    private MaintenanceCategory category;
    private RequestPriority priority;
    private LocalDateTime preferredTime;
    private Boolean isBillable;
    private RequestStatus status;
    private PaymentStatus paymentStatus;
    private UUID staffId;
}
