package com.buildings.dto.response.maintenance;

import com.buildings.entity.enums.ReviewOutcome;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MaintenanceReviewResponse {
    private UUID id;
    private UUID maintenanceRequestId;
    private Integer rating;
    private String comment;
    private ReviewOutcome outcome;
    private UUID reviewedById;
    private String reviewedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
