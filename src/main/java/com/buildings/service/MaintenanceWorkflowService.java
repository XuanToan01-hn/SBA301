package com.buildings.service;

import com.buildings.dto.request.maintenance.*;
import com.buildings.dto.response.maintenance.*;

import java.util.List;

public interface MaintenanceWorkflowService {
    // Resource
    MaintenanceResourceResponse addResource(String requestId, MaintenanceResourceRequest resource);
    List<MaintenanceResourceResponse> getResourcesByRequestId(String requestId);

    // Log
    List<MaintenanceLogResponse> getLogs(String requestId);

    // Schedule
    MaintenanceScheduleResponse proposeSchedule(String requestId, MaintenanceScheduleRequest request);
    List<MaintenanceScheduleResponse> getSchedulesByRequestId(String requestId);
    MaintenanceScheduleResponse respondToSchedule(String requestId, String scheduleId, ScheduleRespondRequest request);

    // Progress
    MaintenanceProgressResponse addProgress(String requestId, MaintenanceProgressRequest request);
    List<MaintenanceProgressResponse> getProgressByRequestId(String requestId);

    // Review
    MaintenanceReviewResponse submitReview(String requestId, MaintenanceReviewRequest request);
    MaintenanceReviewResponse getReviewByRequestId(String requestId);
}
