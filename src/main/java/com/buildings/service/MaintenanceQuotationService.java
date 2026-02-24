package com.buildings.service;

import com.buildings.dto.request.maintenance.MaintenanceQuotationRequest;
import com.buildings.dto.request.maintenance.MaintenanceQuotationUpdateRequest;
import com.buildings.dto.response.maintenance.MaintenanceQuotationResponse;
import com.buildings.entity.enums.QuotationStatus;

import java.util.List;

public interface MaintenanceQuotationService {
    MaintenanceQuotationResponse createQuotation(String requestId, MaintenanceQuotationRequest quotation);
    List<MaintenanceQuotationResponse> getQuotationsByRequestId(String requestId);
    MaintenanceQuotationResponse getQuotationById(String quotationId);
    MaintenanceQuotationResponse updateQuotation(String quotationId, MaintenanceQuotationUpdateRequest request);
    MaintenanceQuotationResponse updateQuotationStatus(String quotationId, QuotationStatus status);
}
