package com.buildings.service;

import com.buildings.dto.response.maintenance.MaintenanceRequestResponse;
import com.buildings.dto.response.maintenance.MaintenanceStatisticsResponse;
import com.buildings.dto.response.maintenance.StaffWorkloadResponse;

import java.util.List;

public interface MaintenanceStatisticsService {
    MaintenanceStatisticsResponse getStatistics(String from, String to, String buildingId);
    List<StaffWorkloadResponse> getStaffWorkload();
    List<MaintenanceRequestResponse> getOverdueRequests();
}
