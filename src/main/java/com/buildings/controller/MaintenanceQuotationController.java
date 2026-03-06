package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.request.maintenance.MaintenanceQuotationUpdateRequest;
import com.buildings.dto.response.maintenance.MaintenanceQuotationResponse;
import com.buildings.entity.enums.QuotationStatus;
import com.buildings.service.MaintenanceQuotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/maintenance-requests/quotations")
@RequiredArgsConstructor
@Tag(name = "Maintenance - Quotations", description = "Direct quotation operations by quotation ID")
public class MaintenanceQuotationController {

    private final MaintenanceQuotationService maintenanceQuotationService;

    @GetMapping("/{quotationId}")
    @Operation(summary = "Chi tiết báo giá", description = "Lấy thông tin chi tiết của một báo giá theo ID")
    public ApiResponse<MaintenanceQuotationResponse> getQuotationById(
            @PathVariable UUID quotationId) {
        return ApiResponse.<MaintenanceQuotationResponse>builder()
                .result(maintenanceQuotationService.getQuotationById(quotationId))
                .build();
    }

    @PutMapping("/{quotationId}")
    @Operation(summary = "Cập nhật báo giá", description = "Nhân viên cập nhật báo giá (chỉ khi ở trạng thái DRAFT)")
    public ApiResponse<MaintenanceQuotationResponse> updateQuotation(
            @PathVariable UUID quotationId,
            @Valid @RequestBody MaintenanceQuotationUpdateRequest request) {
        return ApiResponse.<MaintenanceQuotationResponse>builder()
                .result(maintenanceQuotationService.updateQuotation(quotationId, request))
                .build();
    }

    @PatchMapping("/{quotationId}/status")
    @Operation(summary = "Cập nhật trạng thái báo giá",
            description = "Cập nhật trạng thái báo giá: SENT (gửi cho cư dân), APPROVED/REJECTED (cư dân phản hồi)")
    public ApiResponse<MaintenanceQuotationResponse> updateQuotationStatus(
            @PathVariable UUID quotationId,
            @RequestParam QuotationStatus status) {
        return ApiResponse.<MaintenanceQuotationResponse>builder()
                .result(maintenanceQuotationService.updateQuotationStatus(quotationId, status))
                .build();
    }
}
