package com.buildings.dto.response.maintenance;

import com.buildings.entity.enums.ResourceType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MaintenanceResourceResponse {
    private UUID id;
    private String name;
    private String url;
    private ResourceType resourceType;
}
