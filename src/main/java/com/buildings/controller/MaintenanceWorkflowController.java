package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.request.maintenance.*;
import com.buildings.dto.response.maintenance.*;
import com.buildings.service.MaintenanceQuotationService;
import com.buildings.service.MaintenanceWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance-requests")
@RequiredArgsConstructor
@Tag(name = "Maintenance - Workflow", description = "Sub-resources per request: quotations, resources, logs, schedules, progress, review")
public class MaintenanceWorkflowController {

    private final MaintenanceWorkflowService maintenanceWorkflowService;
    private final MaintenanceQuotationService maintenanceQuotationService;

    // ======================== Quotation (nested) ========================

    @PostMapping("/{id}/quotations")
    @Operation(summary = "Tạo báo giá", description = "Nhân viên tạo báo giá cho yêu cầu bảo trì")
    public ApiResponse<MaintenanceQuotationResponse> createQuotation(
            @PathVariable String id,
            @Valid @RequestBody MaintenanceQuotationRequest request) {
        return ApiResponse.<MaintenanceQuotationResponse>builder()
                .result(maintenanceQuotationService.createQuotation(id, request))
                .build();
    }

    @GetMapping("/{id}/quotations")
    @Operation(summary = "Danh sách báo giá", description = "Lấy tất cả báo giá của một yêu cầu bảo trì")
    public ApiResponse<List<MaintenanceQuotationResponse>> getQuotationsByRequestId(
            @PathVariable String id) {
        return ApiResponse.<List<MaintenanceQuotationResponse>>builder()
                .result(maintenanceQuotationService.getQuotationsByRequestId(id))
                .build();
    }

    // ======================== Resource ========================

    @PostMapping("/{id}/resources")
    @Operation(summary = "Đính kèm tài nguyên", description = "Thêm file đính kèm (ảnh, video, tài liệu) cho yêu cầu bảo trì")
    public ApiResponse<MaintenanceResourceResponse> addResource(
            @PathVariable String id,
            @Valid @RequestBody MaintenanceResourceRequest request) {
        return ApiResponse.<MaintenanceResourceResponse>builder()
                .result(maintenanceWorkflowService.addResource(id, request))
                .build();
    }

    @GetMapping("/{id}/resources")
    @Operation(summary = "Danh sách tài nguyên", description = "Lấy tất cả file đính kèm của một yêu cầu bảo trì")
    public ApiResponse<List<MaintenanceResourceResponse>> getResourcesByRequestId(
            @PathVariable String id) {
        return ApiResponse.<List<MaintenanceResourceResponse>>builder()
                .result(maintenanceWorkflowService.getResourcesByRequestId(id))
                .build();
    }

    // ======================== Log ========================

    @GetMapping("/{id}/logs")
    @Operation(summary = "Lịch sử hoạt động", description = "Xem toàn bộ lịch sử thay đổi và hành động trên yêu cầu bảo trì")
    public ApiResponse<List<MaintenanceLogResponse>> getLogs(@PathVariable String id) {
        return ApiResponse.<List<MaintenanceLogResponse>>builder()
                .result(maintenanceWorkflowService.getLogs(id))
                .build();
    }

    // ======================== Schedule ========================

    @PostMapping("/{id}/schedules")
    @Operation(summary = "Đề xuất lịch sửa chữa", description = "Cư dân hoặc nhân viên đề xuất lịch sửa chữa")
    public ApiResponse<MaintenanceScheduleResponse> proposeSchedule(
            @PathVariable String id,
            @Valid @RequestBody MaintenanceScheduleRequest request) {
        return ApiResponse.<MaintenanceScheduleResponse>builder()
                .result(maintenanceWorkflowService.proposeSchedule(id, request))
                .build();
    }

    @GetMapping("/{id}/schedules")
    @Operation(summary = "Danh sách lịch sửa chữa", description = "Xem toàn bộ lịch sử đề xuất lịch (bao gồm counter-proposal)")
    public ApiResponse<List<MaintenanceScheduleResponse>> getSchedulesByRequestId(
            @PathVariable String id) {
        return ApiResponse.<List<MaintenanceScheduleResponse>>builder()
                .result(maintenanceWorkflowService.getSchedulesByRequestId(id))
                .build();
    }

    @PatchMapping("/{id}/schedules/{scheduleId}/respond")
    @Operation(summary = "Phản hồi lịch sửa chữa",
            description = "Phản hồi đề xuất lịch: ACCEPT (xác nhận), REJECT (từ chối), COUNTER_PROPOSE (đề xuất lại)")
    public ApiResponse<MaintenanceScheduleResponse> respondToSchedule(
            @PathVariable String id,
            @PathVariable String scheduleId,
            @Valid @RequestBody ScheduleRespondRequest request) {
        return ApiResponse.<MaintenanceScheduleResponse>builder()
                .result(maintenanceWorkflowService.respondToSchedule(id, scheduleId, request))
                .build();
    }

    // ======================== Progress ========================

    @PostMapping("/{id}/progress")
    @Operation(summary = "Cập nhật tiến độ", description = "Nhân viên cập nhật tiến độ sửa chữa. Khi đạt 100% sẽ tự động chuyển sang COMPLETED")
    public ApiResponse<MaintenanceProgressResponse> addProgress(
            @PathVariable String id,
            @Valid @RequestBody MaintenanceProgressRequest request) {
        return ApiResponse.<MaintenanceProgressResponse>builder()
                .result(maintenanceWorkflowService.addProgress(id, request))
                .build();
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Lịch sử tiến độ", description = "Xem toàn bộ lịch sử cập nhật tiến độ của yêu cầu bảo trì")
    public ApiResponse<List<MaintenanceProgressResponse>> getProgressByRequestId(
            @PathVariable String id) {
        return ApiResponse.<List<MaintenanceProgressResponse>>builder()
                .result(maintenanceWorkflowService.getProgressByRequestId(id))
                .build();
    }

    // ======================== Review ========================

    @PostMapping("/{id}/review")
    @Operation(summary = "Đánh giá kết quả sửa chữa",
            description = "Cư dân đánh giá kết quả sau khi hoàn thành: ACCEPTED, PARTIAL_ACCEPT hoặc REDO (yêu cầu làm lại)")
    public ApiResponse<MaintenanceReviewResponse> submitReview(
            @PathVariable String id,
            @Valid @RequestBody MaintenanceReviewRequest request) {
        return ApiResponse.<MaintenanceReviewResponse>builder()
                .result(maintenanceWorkflowService.submitReview(id, request))
                .build();
    }

    @GetMapping("/{id}/review")
    @Operation(summary = "Xem đánh giá", description = "Lấy đánh giá của cư dân cho yêu cầu bảo trì")
    public ApiResponse<MaintenanceReviewResponse> getReviewByRequestId(
            @PathVariable String id) {
        return ApiResponse.<MaintenanceReviewResponse>builder()
                .result(maintenanceWorkflowService.getReviewByRequestId(id))
                .build();
    }
}
