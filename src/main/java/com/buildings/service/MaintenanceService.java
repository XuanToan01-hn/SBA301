package com.buildings.service;

import com.buildings.dto.PageResponse;
import com.buildings.dto.request.maintenance.*;
import com.buildings.dto.response.maintenance.*;
import com.buildings.entity.enums.QuotationStatus;

import java.util.List;
import java.util.UUID;

public interface MaintenanceService {

    // ===================== Maintenance Request =====================

    MaintenanceRequestResponse createRequest(MaintenanceRequestCreateRequest request);

    MaintenanceRequestResponse updateRequest(UUID id, MaintenanceRequestUpdateRequest request);

    PageResponse<MaintenanceRequestResponse> getRequests(String keyword, int page, int size);

    List<MaintenanceRequestResponse> getAllRequests(String keyword);

    MaintenanceRequestResponse getRequestById(UUID id);

    MaintenanceRequestResponse cancelRequest(UUID id, MaintenanceCancelRequest request);

    MaintenanceRequestResponse assignRequest(UUID id, MaintenanceAssignRequest request);

    // ===================== Quotation =====================

    MaintenanceQuotationResponse createQuotation(UUID requestId, MaintenanceQuotationRequest quotation);

    List<MaintenanceQuotationResponse> getQuotationsByRequestId(UUID requestId);

    MaintenanceQuotationResponse getQuotationById(UUID quotationId);

    MaintenanceQuotationResponse updateQuotation(UUID quotationId, MaintenanceQuotationUpdateRequest request);

    MaintenanceQuotationResponse updateQuotationStatus(UUID quotationId, QuotationStatus status);

    // ===================== Resource =====================

    MaintenanceResourceResponse addResource(UUID requestId, MaintenanceResourceRequest resource);

    List<MaintenanceResourceResponse> getResourcesByRequestId(UUID requestId);

    // ===================== Log =====================

    List<MaintenanceLogResponse> getLogs(UUID requestId);

    // ===================== Schedule =====================

    MaintenanceScheduleResponse proposeSchedule(UUID requestId, MaintenanceScheduleRequest request);

    List<MaintenanceScheduleResponse> getSchedulesByRequestId(UUID requestId);

    MaintenanceScheduleResponse respondToSchedule(UUID requestId, UUID scheduleId, ScheduleRespondRequest request);

    // ===================== Progress =====================

    MaintenanceProgressResponse addProgress(UUID requestId, MaintenanceProgressRequest request);

    List<MaintenanceProgressResponse> getProgressByRequestId(UUID requestId);

    // ===================== Review =====================

    MaintenanceReviewResponse submitReview(UUID requestId, MaintenanceReviewRequest request);

    MaintenanceReviewResponse getReviewByRequestId(UUID requestId);

    // ===================== Statistics =====================

    MaintenanceStatisticsResponse getStatistics(String from, String to, UUID buildingId);

    List<StaffWorkloadResponse> getStaffWorkload();

    List<MaintenanceRequestResponse> getOverdueRequests();
}
