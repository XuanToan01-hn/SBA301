package com.buildings.dto.request.maintenance;

import com.buildings.entity.enums.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceItemRequest {
    @NotBlank
    private String name;

    private String description;

    private ItemType itemType;

    @NotNull
    private Integer quantity;

    @NotNull
    private BigDecimal unitPrice;
}
