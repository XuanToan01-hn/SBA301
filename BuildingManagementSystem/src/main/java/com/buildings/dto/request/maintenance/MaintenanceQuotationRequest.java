package com.buildings.dto.request.maintenance;

import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceQuotationRequest {
    @NotBlank
    private String title;

    private String description; // staff note

    private String note; // resident note

    private LocalDateTime validUntil;

    private List<MaintenanceItemRequest> items;
}
