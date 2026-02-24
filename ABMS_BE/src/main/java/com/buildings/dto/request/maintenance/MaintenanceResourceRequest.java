package com.buildings.dto.request.maintenance;

import com.buildings.entity.enums.ResourceType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceResourceRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String url;

    private ResourceType resourceType;
}
