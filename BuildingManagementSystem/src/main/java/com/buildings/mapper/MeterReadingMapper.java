package com.buildings.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.buildings.dto.request.service.MeterReadingCreateRequest;
import com.buildings.dto.request.service.MeterReadingUpdateRequest;
import com.buildings.dto.response.service.MeterReadingResponse;
import com.buildings.entity.MeterReading;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MeterReadingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "apartment", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "consumption", ignore = true) // Sẽ được tính trong service
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "takenAt", ignore = true)
    @Mapping(target = "takenBy", ignore = true)
    @Mapping(target = "takenById", ignore = true)
    @Mapping(target = "status", ignore = true)
    MeterReading toEntity(MeterReadingCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "apartment", ignore = true)
    @Mapping(target = "apartmentId", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "period", ignore = true)
    @Mapping(target = "consumption", ignore = true) // Sẽ được tính lại trong service
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "takenAt", ignore = true)
    @Mapping(target = "takenBy", ignore = true)
    @Mapping(target = "takenById", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(@MappingTarget MeterReading meterReading, MeterReadingUpdateRequest request);

    @Mapping(source = "apartment.code", target = "apartmentCode")
    @Mapping(source = "service.code", target = "serviceCode")
    @Mapping(source = "service.name", target = "serviceName")
    @Mapping(source = "service.unit", target = "serviceUnit")
    @Mapping(source = "takenBy.fullName", target = "takenByName")
    MeterReadingResponse toResponse(MeterReading meterReading);

    List<MeterReadingResponse> toResponseList(List<MeterReading> meterReadings);
}
