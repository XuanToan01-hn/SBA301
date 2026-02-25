package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.request.service.ServiceCreateRequest;
import com.buildings.dto.request.service.ServiceUpdateRequest;
import com.buildings.dto.request.service.TariffCreateRequest;
import com.buildings.dto.request.service.TariffUpdateRequest;
import com.buildings.dto.response.service.ServiceResponse;
import com.buildings.dto.response.service.ServiceTariffResponse;
import com.buildings.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

        private final ServiceService serviceService;

        // ==================== SERVICE CRUD ====================

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<ServiceResponse> create(@Valid @RequestBody ServiceCreateRequest request) {
                ServiceResponse result = serviceService.create(request);
                return ApiResponse.<ServiceResponse>builder()
                                .result(result)
                                .message("Service created successfully")
                                .build();
        }

        @GetMapping
        public ApiResponse<List<ServiceResponse>> getAll(
                        @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
                List<ServiceResponse> result = activeOnly
                                ? serviceService.getAllActive()
                                : serviceService.getAll();
                return ApiResponse.<List<ServiceResponse>>builder()
                                .result(result)
                                .build();
        }

        @GetMapping("/{id}")
        public ApiResponse<ServiceResponse> getById(
                        @PathVariable UUID id,
                        @RequestParam(required = false, defaultValue = "false") Boolean includeTariffs) {
                ServiceResponse result = includeTariffs
                                ? serviceService.getByIdWithTariffs(id)
                                : serviceService.getById(id);
                return ApiResponse.<ServiceResponse>builder()
                                .result(result)
                                .build();
        }

        @PutMapping("/{id}")
        public ApiResponse<ServiceResponse> update(
                        @PathVariable UUID id,
                        @Valid @RequestBody ServiceUpdateRequest request) {
                ServiceResponse result = serviceService.update(id, request);
                return ApiResponse.<ServiceResponse>builder()
                                .result(result)
                                .message("Service updated successfully")
                                .build();
        }

        @DeleteMapping("/{id}")
        public ApiResponse<Void> delete(@PathVariable UUID id) {
                serviceService.delete(id);
                return ApiResponse.<Void>builder()
                                .message("Service deleted successfully")
                                .build();
        }

        @PatchMapping("/{id}/deactivate")
        public ApiResponse<ServiceResponse> deactivate(@PathVariable UUID id) {
                ServiceResponse result = serviceService.deactivate(id);
                return ApiResponse.<ServiceResponse>builder()
                                .result(result)
                                .message("Service deactivated successfully")
                                .build();
        }

        @PatchMapping("/{id}/activate")
        public ApiResponse<ServiceResponse> activate(@PathVariable UUID id) {
                ServiceResponse result = serviceService.activate(id);
                return ApiResponse.<ServiceResponse>builder()
                                .result(result)
                                .message("Service activated successfully")
                                .build();
        }
        // ==================== TARIFF MANAGEMENT ====================

        @PostMapping("/{serviceId}/tariffs")
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<ServiceTariffResponse> addTariff(
                        @PathVariable UUID serviceId,
                        @Valid @RequestBody TariffCreateRequest request) {
                ServiceTariffResponse result = serviceService.addTariff(serviceId, request);
                return ApiResponse.<ServiceTariffResponse>builder()
                                .result(result)
                                .message("Tariff added successfully")
                                .build();
        }

        @GetMapping("/{serviceId}/tariffs")
        public ApiResponse<List<ServiceTariffResponse>> getTariffs(@PathVariable UUID serviceId) {
                List<ServiceTariffResponse> result = serviceService.getTariffsByServiceId(serviceId);
                return ApiResponse.<List<ServiceTariffResponse>>builder()
                                .result(result)
                                .build();
        }

        @GetMapping("/{serviceId}/tariffs/current")
        public ApiResponse<ServiceTariffResponse> getCurrentTariff(@PathVariable UUID serviceId) {
                ServiceTariffResponse result = serviceService.getCurrentTariff(serviceId);
                return ApiResponse.<ServiceTariffResponse>builder()
                                .result(result)
                                .build();
        }

        @GetMapping("/{serviceId}/tariffs/by-date")
        public ApiResponse<ServiceTariffResponse> getTariffByDate(
                        @PathVariable UUID serviceId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                ServiceTariffResponse result = serviceService.getTariffByDate(serviceId, date);
                return ApiResponse.<ServiceTariffResponse>builder()
                                .result(result)
                                .build();
        }

        @DeleteMapping("/tariffs/{tariffId}")
        public ApiResponse<Void> deleteTariff(@PathVariable UUID tariffId) {
                serviceService.deleteTariff(tariffId);
                return ApiResponse.<Void>builder()
                                .message("Tariff deleted successfully")
                                .build();
        }

        // UC-02: Update existing tariff (price, vatRate, currency, tiers)
        @PutMapping("/{serviceId}/tariffs/{tariffId}")
        public ApiResponse<ServiceTariffResponse> updateTariff(
                        @PathVariable UUID serviceId,
                        @PathVariable UUID tariffId,
                        @Valid @RequestBody TariffUpdateRequest request) {
                ServiceTariffResponse result = serviceService.updateTariff(tariffId, request);
                return ApiResponse.<ServiceTariffResponse>builder()
                                .result(result)
                                .message("Tariff updated successfully")
                                .build();
        }
}
