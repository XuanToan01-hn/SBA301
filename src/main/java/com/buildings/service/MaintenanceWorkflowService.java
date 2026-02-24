package com.buildings.service;

import com.buildings.dto.request.maintenance.*;
import com.buildings.dto.response.maintenance.*;

import java.util.List;
import java.util.UUID;

public interface MaintenanceWorkflowService {
    // Resource
    MaintenanceResourceResponse addResource(UUID requestId, MaintenanceResourceRequest resource);
    List<MaintenanceResourceResponse> getResourcesByRequestId(UUID requestId);

    // Log
    List<MaintenanceLogResponse> getLogs(UUID requestId);

    // Schedule
    MaintenanceScheduleResponse proposeSchedule(UUID requestId, MaintenanceScheduleRequest request);
    List<MaintenanceScheduleResponse> getSchedulesByRequestId(UUID requestId);
    MaintenanceScheduleResponse respondToSchedule(UUID requestId, UUID scheduleId, ScheduleRespondRequest request);

    // Progress
    MaintenanceProgressResponse addProgress(UUID requestId, MaintenanceProgressRequest request);
    List<MaintenanceProgressResponse> getProgressByRequestId(UUID requestId);

    // Review
    MaintenanceReviewResponse submitReview(UUID requestId, MaintenanceReviewRequest request);
    MaintenanceReviewResponse getReviewByRequestId(UUID requestId);
}
