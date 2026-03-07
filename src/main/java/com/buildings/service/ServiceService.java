package com.buildings.service;

import com.buildings.dto.request.service.ServiceCreateRequest;
import com.buildings.dto.request.service.ServiceUpdateRequest;
import com.buildings.dto.request.service.TariffCreateRequest;
import com.buildings.dto.request.service.TariffUpdateRequest;
import com.buildings.dto.response.service.ServiceResponse;
import com.buildings.dto.response.service.ServiceTariffResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ServiceService {

    // Service CRUD
    ServiceResponse create(ServiceCreateRequest request);

    ServiceResponse getById(UUID id);

    ServiceResponse getByIdWithTariffs(UUID id);

    List<ServiceResponse> getAll();

    List<ServiceResponse> getAllActive();

    ServiceResponse update(UUID id, ServiceUpdateRequest request);

    void delete(UUID id);

    // Status management (UC-01)
    ServiceResponse deactivate(UUID id);

    ServiceResponse activate(UUID id);

    // Tariff management
    ServiceTariffResponse addTariff(UUID serviceId, TariffCreateRequest request);

    List<ServiceTariffResponse> getTariffsByServiceId(UUID serviceId);

    ServiceTariffResponse getCurrentTariff(UUID serviceId);

    ServiceTariffResponse getTariffByDate(UUID serviceId, LocalDate date);

    // UC-02: Update tariff
    ServiceTariffResponse updateTariff(UUID tariffId, TariffUpdateRequest request);

    void deleteTariff(UUID tariffId);

    // Price calculation
    BigDecimal calculatePrice(UUID serviceId, BigDecimal consumption, LocalDate date);

    BigDecimal calculateTieredPrice(UUID tariffId, BigDecimal consumption);
}
