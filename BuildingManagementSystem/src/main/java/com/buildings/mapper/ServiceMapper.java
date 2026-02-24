package com.buildings.mapper;

import java.time.LocalDate;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.buildings.dto.request.service.ServiceCreateRequest;
import com.buildings.dto.request.service.ServiceUpdateRequest;
import com.buildings.dto.request.service.TariffCreateRequest;
import com.buildings.dto.response.service.ServiceResponse;
import com.buildings.dto.response.service.ServiceTariffResponse;
import com.buildings.entity.Service;
import com.buildings.entity.ServiceTariff;
import com.buildings.entity.ServiceTariffTier;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServiceMapper {

    // Service mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "tariffs", ignore = true)
    @Mapping(target = "meterReadings", ignore = true)
    Service toEntity(ServiceCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // Không cho update code
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "tariffs", ignore = true)
    @Mapping(target = "meterReadings", ignore = true)
    void updateEntity(@MappingTarget Service service, ServiceUpdateRequest request);

    @Mapping(target = "currentTariff", ignore = true)
    @Mapping(target = "tariffs", ignore = true)
    ServiceResponse toResponse(Service service);

    List<ServiceResponse> toResponseList(List<Service> services);

    // Tariff mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "tiers", ignore = true)
    ServiceTariff toTariffEntity(TariffCreateRequest request);

    @Mapping(target = "isActive", expression = "java(isActiveTariff(tariff))")
    @Mapping(target = "tiers", source = "tiers")
    ServiceTariffResponse toTariffResponse(ServiceTariff tariff);

    List<ServiceTariffResponse> toTariffResponseList(List<ServiceTariff> tariffs);

    // Tier mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "tariff", ignore = true)
    @Mapping(target = "tariffId", ignore = true)
    ServiceTariffTier toTierEntity(TariffCreateRequest.TierRequest request);

    ServiceTariffResponse.TierResponse toTierResponse(ServiceTariffTier tier);

    List<ServiceTariffTier> toTierEntities(List<TariffCreateRequest.TierRequest> requests);

    // Helper method to check if tariff is active
    default boolean isActiveTariff(ServiceTariff tariff) {
        LocalDate now = LocalDate.now();
        boolean afterStart = !tariff.getEffectiveFrom().isAfter(now);
        boolean beforeEnd = tariff.getEffectiveTo() == null || !tariff.getEffectiveTo().isBefore(now);
        return afterStart && beforeEnd;
    }
}
