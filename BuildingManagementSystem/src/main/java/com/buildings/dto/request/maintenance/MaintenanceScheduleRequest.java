package com.buildings.dto.request.maintenance;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceScheduleRequest {

    @NotNull
    private LocalDateTime proposedTime;

    private Integer estimatedDuration; // phút

    private String note;
}
