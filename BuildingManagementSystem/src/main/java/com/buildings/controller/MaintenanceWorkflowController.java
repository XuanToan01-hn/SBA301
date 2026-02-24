package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.request.maintenance.*;
import com.buildings.dto.response.maintenance.*;
import com.buildings.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/maintenance-requests")
@RequiredArgsConstructor
@Tag(name = "Maintenance - Workflow", description = "Sub-resources per request: quotations, resources, logs, schedules, progress, review")
public class MaintenanceWorkflowController {

    private final MaintenanceService maintenanceService;

    // ======================== Quotation (nested) ========================

    @PostMapping("/{id}/quotations")
    @Operation(summary = "Tạo báo giá", description = "Nhân viên tạo báo giá cho yêu cầu bảo trì")
    public ApiResponse<MaintenanceQuotationResponse> createQuotation(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceQuotationRequest request) {
        return ApiResponse.<MaintenanceQuotationResponse>builder()
                .result(maintenanceService.createQuotation(id, request))
                .build();
    }

    @GetMapping("/{id}/quotations")
    @Operation(summary = "Danh sách báo giá", description = "Lấy tất cả báo giá của một yêu cầu bảo trì")
    public ApiResponse<List<MaintenanceQuotationResponse>> getQuotationsByRequestId(
            @PathVariable UUID id) {
        return ApiResponse.<List<MaintenanceQuotationResponse>>builder()
                .result(maintenanceService.getQuotationsByRequestId(id))
                .build();
    }

    // ======================== Resource ========================

    @PostMapping("/{id}/resources")
    @Operation(summary = "Đính kèm tài nguyên", description = "Thêm file đính kèm (ảnh, video, tài liệu) cho yêu cầu bảo trì")
    public ApiResponse<MaintenanceResourceResponse> addResource(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceResourceRequest request) {
        return ApiResponse.<MaintenanceResourceResponse>builder()
                .result(maintenanceService.addResource(id, request))
                .build();
    }

    @GetMapping("/{id}/resources")
    @Operation(summary = "Danh sách tài nguyên", description = "Lấy tất cả file đính kèm của một yêu cầu bảo trì")
    public ApiResponse<List<MaintenanceResourceResponse>> getResourcesByRequestId(
            @PathVariable UUID id) {
        return ApiResponse.<List<MaintenanceResourceResponse>>builder()
                .result(maintenanceService.getResourcesByRequestId(id))
                .build();
    }

    // ======================== Log ========================

    @GetMapping("/{id}/logs")
    @Operation(summary = "Lịch sử hoạt động", description = "Xem toàn bộ lịch sử thay đổi và hành động trên yêu cầu bảo trì")
    public ApiResponse<List<MaintenanceLogResponse>> getLogs(@PathVariable UUID id) {
        return ApiResponse.<List<MaintenanceLogResponse>>builder()
                .result(maintenanceService.getLogs(id))
                .build();
    }

    // ======================== Schedule ========================

    @PostMapping("/{id}/schedules")
    @Operation(summary = "Đề xuất lịch sửa chữa", description = "Cư dân hoặc nhân viên đề xuất lịch sửa chữa")
    public ApiResponse<MaintenanceScheduleResponse> proposeSchedule(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceScheduleRequest request) {
        return ApiResponse.<MaintenanceScheduleResponse>builder()
                .result(maintenanceService.proposeSchedule(id, request))
                .build();
    }

    @GetMapping("/{id}/schedules")
    @Operation(summary = "Danh sách lịch sửa chữa", description = "Xem toàn bộ lịch sử đề xuất lịch (bao gồm counter-proposal)")
    public ApiResponse<List<MaintenanceScheduleResponse>> getSchedulesByRequestId(
            @PathVariable UUID id) {
        return ApiResponse.<List<MaintenanceScheduleResponse>>builder()
                .result(maintenanceService.getSchedulesByRequestId(id))
                .build();
    }

    @PatchMapping("/{id}/schedules/{scheduleId}/respond")
    @Operation(summary = "Phản hồi lịch sửa chữa",
            description = "Phản hồi đề xuất lịch: ACCEPT (xác nhận), REJECT (từ chối), COUNTER_PROPOSE (đề xuất lại)")
    public ApiResponse<MaintenanceScheduleResponse> respondToSchedule(
            @PathVariable UUID id,
            @PathVariable UUID scheduleId,
            @Valid @RequestBody ScheduleRespondRequest request) {
        return ApiResponse.<MaintenanceScheduleResponse>builder()
                .result(maintenanceService.respondToSchedule(id, scheduleId, request))
                .build();
    }

    // ======================== Progress ========================

    @PostMapping("/{id}/progress")
    @Operation(summary = "Cập nhật tiến độ", description = "Nhân viên cập nhật tiến độ sửa chữa. Khi đạt 100% sẽ tự động chuyển sang COMPLETED")
    public ApiResponse<MaintenanceProgressResponse> addProgress(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceProgressRequest request) {
        return ApiResponse.<MaintenanceProgressResponse>builder()
                .result(maintenanceService.addProgress(id, request))
                .build();
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Lịch sử tiến độ", description = "Xem toàn bộ lịch sử cập nhật tiến độ của yêu cầu bảo trì")
    public ApiResponse<List<MaintenanceProgressResponse>> getProgressByRequestId(
            @PathVariable UUID id) {
        return ApiResponse.<List<MaintenanceProgressResponse>>builder()
                .result(maintenanceService.getProgressByRequestId(id))
                .build();
    }

    // ======================== Review ========================

    @PostMapping("/{id}/review")
    @Operation(summary = "Đánh giá kết quả sửa chữa",
            description = "Cư dân đánh giá kết quả sau khi hoàn thành: ACCEPTED, PARTIAL_ACCEPT hoặc REDO (yêu cầu làm lại)")
    public ApiResponse<MaintenanceReviewResponse> submitReview(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceReviewRequest request) {
        return ApiResponse.<MaintenanceReviewResponse>builder()
                .result(maintenanceService.submitReview(id, request))
                .build();
    }

    @GetMapping("/{id}/review")
    @Operation(summary = "Xem đánh giá", description = "Lấy đánh giá của cư dân cho yêu cầu bảo trì")
    public ApiResponse<MaintenanceReviewResponse> getReviewByRequestId(
            @PathVariable UUID id) {
        return ApiResponse.<MaintenanceReviewResponse>builder()
                .result(maintenanceService.getReviewByRequestId(id))
                .build();
    }
}
