package com.buildings.dto.request.maintenance;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceProgressRequest {

    @NotBlank
    private String note;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer progressPercent;
}
