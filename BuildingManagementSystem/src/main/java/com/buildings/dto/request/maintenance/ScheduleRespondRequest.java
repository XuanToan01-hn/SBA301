package com.buildings.dto.request.maintenance;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRespondRequest {

    // ACCEPT | REJECT | COUNTER_PROPOSE
    @NotBlank
    private String action;

    private LocalDateTime counterProposedTime; // chỉ dùng khi action=COUNTER_PROPOSE

    private Integer counterEstimatedDuration;

    private String note;
}
