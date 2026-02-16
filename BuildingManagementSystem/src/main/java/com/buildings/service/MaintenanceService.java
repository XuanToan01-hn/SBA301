package com.buildings.service;

import com.buildings.dto.PageResponse;
import com.buildings.dto.request.maintenance.*;
import com.buildings.dto.response.maintenance.*;
import com.buildings.entity.enums.QuotationStatus;

import java.util.List;
import java.util.UUID;

public interface MaintenanceService {
    MaintenanceRequestResponse createRequest(MaintenanceRequestCreateRequest request);
    MaintenanceRequestResponse updateRequest(UUID id, MaintenanceRequestUpdateRequest request);
    PageResponse<MaintenanceRequestResponse> getRequests(String keyword, int page, int size);
    List<MaintenanceRequestResponse> getAllRequests(String keyword);
    MaintenanceRequestResponse getRequestById(UUID id);

    MaintenanceQuotationResponse createQuotation(UUID requestId, MaintenanceQuotationRequest quotation);
    MaintenanceQuotationResponse updateQuotationStatus(UUID quotationId, QuotationStatus status);

    MaintenanceResourceResponse addResource(UUID requestId, MaintenanceResourceRequest resource);

    List<MaintenanceLogResponse> getLogs(UUID requestId);
}
