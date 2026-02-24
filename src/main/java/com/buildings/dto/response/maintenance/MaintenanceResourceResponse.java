package com.buildings.dto.response.maintenance;

import com.buildings.entity.enums.ResourceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaintenanceResourceResponse {
    private String id;
    private String name;
    private String url;
    private ResourceType resourceType;
}
