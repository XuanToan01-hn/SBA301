package com.buildings.service;

import com.buildings.dto.request.maintenance.MaintenanceQuotationRequest;
import com.buildings.dto.request.maintenance.MaintenanceQuotationUpdateRequest;
import com.buildings.dto.response.maintenance.MaintenanceQuotationResponse;
import com.buildings.entity.enums.QuotationStatus;

import java.util.List;
import java.util.UUID;

public interface MaintenanceQuotationService {
    MaintenanceQuotationResponse createQuotation(UUID requestId, MaintenanceQuotationRequest quotation);
    List<MaintenanceQuotationResponse> getQuotationsByRequestId(UUID requestId);
    MaintenanceQuotationResponse getQuotationById(UUID quotationId);
    MaintenanceQuotationResponse updateQuotation(UUID quotationId, MaintenanceQuotationUpdateRequest request);
    MaintenanceQuotationResponse updateQuotationStatus(UUID quotationId, QuotationStatus status);
}
