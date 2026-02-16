package com.buildings.mapper;

import com.buildings.dto.request.maintenance.*;
import com.buildings.dto.response.maintenance.*;
import com.buildings.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MaintenanceMapper {

    // Request Mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "requestStatus", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "requester", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "quotations", ignore = true)
    @Mapping(target = "resources", ignore = true)
    @Mapping(target = "apartment", ignore = true)
    @Mapping(target = "building", ignore = true)
    MaintenanceRequest toMaintenanceRequest(MaintenanceRequestCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "requester", ignore = true)
    @Mapping(target = "quotations", ignore = true)
    @Mapping(target = "resources", ignore = true)
    @Mapping(target = "apartment", ignore = true)
    @Mapping(target = "building", ignore = true)
    @Mapping(target = "requestStatus", source = "status")
    @Mapping(target = "staff", ignore = true)
    void updateMaintenanceRequest(@MappingTarget MaintenanceRequest entity, MaintenanceRequestUpdateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "maintenanceRequest", ignore = true)
    @Mapping(target = "items", ignore = true)
    MaintenanceQuotation toMaintenanceQuotation(MaintenanceQuotationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quotation", ignore = true)
    MaintenanceItem toMaintenanceItem(MaintenanceItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "maintenanceRequest", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "item", ignore = true)
    MaintenanceResource toMaintenanceResource(MaintenanceResourceRequest request);

    // Response Mappings
    @Mapping(target = "requesterId", source = "requester.id")
    @Mapping(target = "requesterName", source = "requester.fullName")
    @Mapping(target = "staffId", source = "staff.id")
    @Mapping(target = "staffName", source = "staff.fullName")
    @Mapping(target = "apartmentId", source = "apartment.id")
    @Mapping(target = "apartmentCode", source = "apartment.code")
    @Mapping(target = "buildingId", source = "building.id")
    @Mapping(target = "buildingName", source = "building.name")
    MaintenanceRequestResponse toMaintenanceRequestResponse(MaintenanceRequest entity);

    MaintenanceQuotationResponse toMaintenanceQuotationResponse(MaintenanceQuotation entity);

    MaintenanceItemResponse toMaintenanceItemResponse(MaintenanceItem entity);

    @Mapping(target = "actorId", source = "actorId")
    MaintenanceLogResponse toMaintenanceLogResponse(MaintenanceLog entity);

    MaintenanceResourceResponse toMaintenanceResourceResponse(MaintenanceResource entity);
}
