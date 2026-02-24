package com.buildings.dto.request.maintenance;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceQuotationUpdateRequest {

    @NotBlank
    private String title;

    private String description;

    private String note;

    private LocalDateTime validUntil;

    private List<MaintenanceItemRequest> items;
}
