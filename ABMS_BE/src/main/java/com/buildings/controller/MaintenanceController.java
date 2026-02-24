package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.PageResponse;
import com.buildings.dto.request.maintenance.*;
import com.buildings.dto.response.maintenance.*;
import com.buildings.entity.enums.QuotationStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/maintenance-requests")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping
    public ApiResponse<MaintenanceRequestResponse> createRequest(@Valid @RequestBody MaintenanceRequestCreateRequest request) {
        return ApiResponse.<MaintenanceRequestResponse>builder()
                .result(maintenanceService.createRequest(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<MaintenanceRequestResponse> updateRequest(
            @PathVariable UUID id,
            @RequestBody MaintenanceRequestUpdateRequest request) {
        return ApiResponse.<MaintenanceRequestResponse>builder()
                .result(maintenanceService.updateRequest(id, request))
                .build();
    }

    @GetMapping
    public ApiResponse<?> getRequests(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "true") boolean pagination) {

        if (pagination) {
             return ApiResponse.<PageResponse<MaintenanceRequestResponse>>builder()
                .result(maintenanceService.getRequests(keyword, page, size))
                .build();
        } else {
             return ApiResponse.<List<MaintenanceRequestResponse>>builder()
                .result(maintenanceService.getAllRequests(keyword))
                .build();
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<MaintenanceRequestResponse> getRequestById(@PathVariable UUID id) {
        return ApiResponse.<MaintenanceRequestResponse>builder()
                .result(maintenanceService.getRequestById(id))
                .build();
    }

    @PostMapping("/{id}/quotations")
    public ApiResponse<MaintenanceQuotationResponse> createQuotation(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceQuotationRequest request) {
        return ApiResponse.<MaintenanceQuotationResponse>builder()
                .result(maintenanceService.createQuotation(id, request))
                .build();
    }

    @PatchMapping("/quotations/{quotationId}/status")
    public ApiResponse<MaintenanceQuotationResponse> updateQuotationStatus(
            @PathVariable UUID quotationId,
            @RequestParam QuotationStatus status) {
        return ApiResponse.<MaintenanceQuotationResponse>builder()
                .result(maintenanceService.updateQuotationStatus(quotationId, status))
                .build();
    }

    @PostMapping("/{id}/resources")
    public ApiResponse<MaintenanceResourceResponse> addResource(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceResourceRequest request) {
        return ApiResponse.<MaintenanceResourceResponse>builder()
                .result(maintenanceService.addResource(id, request))
                .build();
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<List<MaintenanceLogResponse>> getLogs(@PathVariable UUID id) {
        return ApiResponse.<List<MaintenanceLogResponse>>builder()
                .result(maintenanceService.getLogs(id))
                .build();
    }
}
