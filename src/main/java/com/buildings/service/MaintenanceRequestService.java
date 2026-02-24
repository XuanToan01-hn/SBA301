package com.buildings.service;

import com.buildings.dto.PageResponse;
import com.buildings.dto.request.maintenance.MaintenanceAssignRequest;
import com.buildings.dto.request.maintenance.MaintenanceCancelRequest;
import com.buildings.dto.request.maintenance.MaintenanceRequestCreateRequest;
import com.buildings.dto.request.maintenance.MaintenanceRequestUpdateRequest;
import com.buildings.dto.response.maintenance.MaintenanceRequestResponse;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRequestService {
    MaintenanceRequestResponse createRequest(MaintenanceRequestCreateRequest request);
    MaintenanceRequestResponse updateRequest(UUID id, MaintenanceRequestUpdateRequest request);
    PageResponse<MaintenanceRequestResponse> getRequests(String keyword, int page, int size);
    List<MaintenanceRequestResponse> getAllRequests(String keyword);
    MaintenanceRequestResponse getRequestById(UUID id);
    MaintenanceRequestResponse cancelRequest(UUID id, MaintenanceCancelRequest request);
    MaintenanceRequestResponse assignRequest(UUID id, MaintenanceAssignRequest request);
}
