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

    // ===================== MaintenanceRequest =====================

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

    @Mapping(target = "requesterId", source = "requester.id")
    @Mapping(target = "requesterName", source = "requester.fullName")
    @Mapping(target = "staffId", source = "staff.id")
    @Mapping(target = "staffName", source = "staff.fullName")
    @Mapping(target = "apartmentId", source = "apartment.id")
    @Mapping(target = "apartmentCode", source = "apartment.code")
    @Mapping(target = "buildingId", source = "building.id")
    @Mapping(target = "buildingName", source = "building.name")
    MaintenanceRequestResponse toMaintenanceRequestResponse(MaintenanceRequest entity);

    // ===================== MaintenanceQuotation =====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "maintenanceRequest", ignore = true)
    @Mapping(target = "items", ignore = true)
    MaintenanceQuotation toMaintenanceQuotation(MaintenanceQuotationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "maintenanceRequest", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateMaintenanceQuotation(@MappingTarget MaintenanceQuotation entity, MaintenanceQuotationUpdateRequest request);

    MaintenanceQuotationResponse toMaintenanceQuotationResponse(MaintenanceQuotation entity);

    // ===================== MaintenanceItem =====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quotation", ignore = true)
    MaintenanceItem toMaintenanceItem(MaintenanceItemRequest request);

    MaintenanceItemResponse toMaintenanceItemResponse(MaintenanceItem entity);

    // ===================== MaintenanceResource =====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "maintenanceRequest", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "item", ignore = true)
    MaintenanceResource toMaintenanceResource(MaintenanceResourceRequest request);

    MaintenanceResourceResponse toMaintenanceResourceResponse(MaintenanceResource entity);

    // ===================== MaintenanceLog =====================

    @Mapping(target = "actorId", source = "actorId")
    MaintenanceLogResponse toMaintenanceLogResponse(MaintenanceLog entity);

    // ===================== MaintenanceSchedule =====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "maintenanceRequest", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "proposedByRole", ignore = true)
    @Mapping(target = "proposedBy", ignore = true)
    @Mapping(target = "parentSchedule", ignore = true)
    MaintenanceSchedule toMaintenanceSchedule(MaintenanceScheduleRequest request);

    @Mapping(target = "maintenanceRequestId", source = "maintenanceRequest.id")
    @Mapping(target = "proposedById", source = "proposedBy.id")
    @Mapping(target = "proposedByName", source = "proposedBy.fullName")
    @Mapping(target = "parentScheduleId", source = "parentSchedule.id")
    MaintenanceScheduleResponse toMaintenanceScheduleResponse(MaintenanceSchedule entity);

    // ===================== MaintenanceReview =====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "maintenanceRequest", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    MaintenanceReview toMaintenanceReview(MaintenanceReviewRequest request);

    @Mapping(target = "maintenanceRequestId", source = "maintenanceRequest.id")
    @Mapping(target = "reviewedById", source = "reviewedBy.id")
    @Mapping(target = "reviewedByName", source = "reviewedBy.fullName")
    MaintenanceReviewResponse toMaintenanceReviewResponse(MaintenanceReview entity);

    // ===================== MaintenanceProgress =====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "maintenanceRequest", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    MaintenanceProgress toMaintenanceProgress(MaintenanceProgressRequest request);

    @Mapping(target = "maintenanceRequestId", source = "maintenanceRequest.id")
    @Mapping(target = "updatedById", source = "updatedBy.id")
    @Mapping(target = "updatedByName", source = "updatedBy.fullName")
    MaintenanceProgressResponse toMaintenanceProgressResponse(MaintenanceProgress entity);
}
