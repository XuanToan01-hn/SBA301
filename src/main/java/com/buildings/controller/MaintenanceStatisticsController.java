package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.response.maintenance.MaintenanceRequestResponse;
import com.buildings.dto.response.maintenance.MaintenanceStatisticsResponse;
import com.buildings.dto.response.maintenance.StaffWorkloadResponse;
import com.buildings.service.MaintenanceStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance/statistics")
@RequiredArgsConstructor
@Tag(name = "Maintenance - Statistics", description = "Operations: get statistics for maintenance")
public class MaintenanceStatisticsController {

    private final MaintenanceStatisticsService maintenanceStatisticsService;

    @GetMapping("/statistics")
    @Operation(summary = "Thống kê bảo trì",
            description = "Thống kê tổng quan: theo trạng thái, danh mục, độ ưu tiên, thời gian xử lý trung bình, rating. Hỗ trợ lọc theo ngày (yyyy-MM-dd) và toà nhà")
    public ApiResponse<MaintenanceStatisticsResponse> getStatistics(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) UUID buildingId) {
        return ApiResponse.<MaintenanceStatisticsResponse>builder()
                .result(maintenanceStatisticsService.getStatistics(from, to, buildingId))
                .build();
    }

    @GetMapping("/staff-workload")
    @Operation(summary = "Khối lượng công việc nhân viên",
            description = "Thống kê số lượng yêu cầu được giao và rating trung bình của từng nhân viên")
    public ApiResponse<List<StaffWorkloadResponse>> getStaffWorkload() {
        return ApiResponse.<List<StaffWorkloadResponse>>builder()
                .result(maintenanceStatisticsService.getStaffWorkload())
                .build();
    }

    @GetMapping("/overdue")
    @Operation(summary = "Yêu cầu quá hạn",
            description = "Lấy danh sách các yêu cầu đang IN_PROGRESS nhưng đã bắt đầu hơn 7 ngày mà chưa hoàn thành")
    public ApiResponse<List<MaintenanceRequestResponse>> getOverdueRequests() {
        return ApiResponse.<List<MaintenanceRequestResponse>>builder()
                .result(maintenanceStatisticsService.getOverdueRequests())
                .build();
    }
}
