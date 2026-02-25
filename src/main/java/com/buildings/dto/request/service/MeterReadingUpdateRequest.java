package com.buildings.dto.request.service;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterReadingUpdateRequest {

    @DecimalMin(value = "0.0", message = "Old index must be non-negative")
    private BigDecimal oldIndex;

    @DecimalMin(value = "0.0", message = "New index must be non-negative")
    private BigDecimal newIndex;

    private Boolean isMeterReset;

    private String note;
}
