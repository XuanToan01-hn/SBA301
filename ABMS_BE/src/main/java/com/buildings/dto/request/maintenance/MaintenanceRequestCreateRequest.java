package com.buildings.dto.request.maintenance;

import com.buildings.entity.enums.MaintenanceCategory;
import com.buildings.entity.enums.RequestPriority;
import com.buildings.entity.enums.RequestScope;
import jakarta.validation.constraints.NotBlank;
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
public class MaintenanceRequestCreateRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private RequestScope scope;

    private MaintenanceCategory category;

    private RequestPriority priority;

    private LocalDateTime preferredTime;

    private Boolean isBillable;

    private UUID apartmentId;

    private UUID buildingId;
}
