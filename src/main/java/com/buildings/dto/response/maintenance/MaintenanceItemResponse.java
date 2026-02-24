package com.buildings.dto.response.maintenance;

import com.buildings.entity.enums.ItemType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MaintenanceItemResponse {
    private String id;
    private String name;
    private String description;
    private ItemType itemType;
    private Integer quantity;
    private BigDecimal unitPrice;
}
