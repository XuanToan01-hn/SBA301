package com.buildings.dto.response.maintenance;

import com.buildings.entity.enums.ReviewOutcome;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaintenanceReviewResponse {
    private String id;
    private String maintenanceRequestId;
    private Integer rating;
    private String comment;
    private ReviewOutcome outcome;
    private String reviewedById;
    private String reviewedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
