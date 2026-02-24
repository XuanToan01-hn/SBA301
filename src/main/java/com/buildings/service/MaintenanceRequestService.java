package com.buildings.service;

import com.buildings.dto.PageResponse;
import com.buildings.dto.request.maintenance.MaintenanceAssignRequest;
import com.buildings.dto.request.maintenance.MaintenanceCancelRequest;
import com.buildings.dto.request.maintenance.MaintenanceRequestCreateRequest;
import com.buildings.dto.request.maintenance.MaintenanceRequestUpdateRequest;
import com.buildings.dto.response.maintenance.MaintenanceRequestResponse;

import java.util.List;

public interface MaintenanceRequestService {
    MaintenanceRequestResponse createRequest(MaintenanceRequestCreateRequest request);
    MaintenanceRequestResponse updateRequest(String id, MaintenanceRequestUpdateRequest request);
    PageResponse<MaintenanceRequestResponse> getRequests(String keyword, int page, int size);
    List<MaintenanceRequestResponse> getAllRequests(String keyword);
    MaintenanceRequestResponse getRequestById(String id);
    MaintenanceRequestResponse cancelRequest(String id, MaintenanceCancelRequest request);
    MaintenanceRequestResponse assignRequest(String id, MaintenanceAssignRequest request);
}
