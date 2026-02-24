package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.PageResponse;
import com.buildings.dto.request.maintenance.MaintenanceCancelRequest;
import com.buildings.dto.request.maintenance.MaintenanceAssignRequest;
import com.buildings.dto.request.maintenance.MaintenanceRequestCreateRequest;
import com.buildings.dto.request.maintenance.MaintenanceRequestUpdateRequest;
import com.buildings.dto.response.maintenance.MaintenanceRequestResponse;
import com.buildings.service.MaintenanceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance-requests")
@RequiredArgsConstructor
@Tag(name = "Maintenance - Requests", description = "Core request lifecycle: create, list, update, assign, cancel")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;

    @PostMapping
    @Operation(summary = "Tạo yêu cầu bảo trì", description = "Cư dân tạo yêu cầu bảo trì mới")
    public ApiResponse<MaintenanceRequestResponse> createRequest(
            @Valid @RequestBody MaintenanceRequestCreateRequest request) {
        return ApiResponse.<MaintenanceRequestResponse>builder()
                .result(maintenanceRequestService.createRequest(request))
                .build();
    }

    @GetMapping
    @Operation(summary = "Danh sách yêu cầu bảo trì", description = "Lấy danh sách yêu cầu bảo trì, hỗ trợ tìm kiếm và phân trang")
    public ApiResponse<?> getRequests(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "true") boolean pagination) {
        if (pagination) {
            return ApiResponse.<PageResponse<MaintenanceRequestResponse>>builder()
                    .result(maintenanceRequestService.getRequests(keyword, page, size))
                    .build();
        } else {
            return ApiResponse.<List<MaintenanceRequestResponse>>builder()
                    .result(maintenanceRequestService.getAllRequests(keyword))
                    .build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết yêu cầu bảo trì", description = "Lấy thông tin chi tiết của một yêu cầu bảo trì theo ID")
    public ApiResponse<MaintenanceRequestResponse> getRequestById(@PathVariable UUID id) {
        return ApiResponse.<MaintenanceRequestResponse>builder()
                .result(maintenanceRequestService.getRequestById(id))
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật yêu cầu bảo trì", description = "Cư dân cập nhật thông tin yêu cầu bảo trì")
    public ApiResponse<MaintenanceRequestResponse> updateRequest(
            @PathVariable UUID id,
            @RequestBody MaintenanceRequestUpdateRequest request) {
        return ApiResponse.<MaintenanceRequestResponse>builder()
                .result(maintenanceRequestService.updateRequest(id, request))
                .build();
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Huỷ yêu cầu bảo trì", description = "Cư dân hoặc quản lý huỷ yêu cầu bảo trì, có thể kèm lý do")
    public ApiResponse<MaintenanceRequestResponse> cancelRequest(
            @PathVariable UUID id,
            @RequestBody(required = false) MaintenanceCancelRequest request) {
        return ApiResponse.<MaintenanceRequestResponse>builder()
                .result(maintenanceRequestService.cancelRequest(id,
                        request != null ? request : new MaintenanceCancelRequest()))
                .build();
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Giao yêu cầu cho nhân viên", description = "Quản lý giao yêu cầu bảo trì cho một nhân viên kỹ thuật")
    public ApiResponse<MaintenanceRequestResponse> assignRequest(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceAssignRequest request) {
        return ApiResponse.<MaintenanceRequestResponse>builder()
                .result(maintenanceRequestService.assignRequest(id, request))
                .build();
    }
}
