package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.PageResponse;
import com.buildings.dto.request.service.MeterReadingCreateRequest;
import com.buildings.dto.request.service.MeterReadingUpdateRequest;
import com.buildings.dto.response.service.MeterReadingResponse;
import com.buildings.dto.response.service.OldIndexResponse;
import com.buildings.dto.response.service.PeriodSummaryResponse;
import com.buildings.entity.enums.MeterReadingStatus;
import com.buildings.service.FileStorageService;
import com.buildings.service.MeterReadingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meter-readings")
@RequiredArgsConstructor
public class MeterReadingController {

        private final MeterReadingService meterReadingService;
        private final FileStorageService fileStorageService;
        private final ObjectMapper objectMapper;

        private static final String METER_PHOTOS_DIR = "meter-photos";

        // ==================== CRUD ====================

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<MeterReadingResponse> create(
                        @RequestParam("data") String data,
                        @RequestParam(value = "photo", required = false) MultipartFile photo,
                        @AuthenticationPrincipal Jwt jwt) {

                MeterReadingCreateRequest request;
                try {
                        request = objectMapper.readValue(data, MeterReadingCreateRequest.class);
                } catch (Exception e) {
                        throw new RuntimeException("Invalid JSON in 'data' field: " + e.getMessage());
                }

                // Save photo if provided
                String photoUrl = null;
                if (photo != null && !photo.isEmpty()) {
                        photoUrl = fileStorageService.saveFile(photo, METER_PHOTOS_DIR);
                }

                // Get current user ID from JWT
                UUID takenById = null;
                if (jwt != null) {
                        String userId = jwt.getSubject();
                        if (userId != null) {
                                try {
                                        takenById = UUID.fromString(userId);
                                } catch (IllegalArgumentException ignored) {
                                        // Invalid UUID in token, ignore
                                }
                        }
                }

                MeterReadingResponse result = meterReadingService.create(request, photoUrl, takenById);
                return ApiResponse.<MeterReadingResponse>builder()
                                .result(result)
                                .message("Meter reading created successfully")
                                .build();
        }

        @GetMapping("/{id}")
        public ApiResponse<MeterReadingResponse> getById(@PathVariable UUID id) {
                MeterReadingResponse result = meterReadingService.getById(id);
                return ApiResponse.<MeterReadingResponse>builder()
                                .result(result)
                                .build();
        }

        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ApiResponse<MeterReadingResponse> update(
                        @PathVariable UUID id,
                        @RequestParam("data") String data,
                        @RequestParam(value = "photo", required = false) MultipartFile photo) {

                MeterReadingUpdateRequest request;
                try {
                        request = objectMapper.readValue(data, MeterReadingUpdateRequest.class);
                } catch (Exception e) {
                        throw new RuntimeException("Invalid JSON in 'data' field: " + e.getMessage());
                }

                // Save new photo if provided
                String newPhotoUrl = null;
                if (photo != null && !photo.isEmpty()) {
                        newPhotoUrl = fileStorageService.saveFile(photo, METER_PHOTOS_DIR);
                }

                MeterReadingResponse result = meterReadingService.update(id, request, newPhotoUrl);
                return ApiResponse.<MeterReadingResponse>builder()
                                .result(result)
                                .message("Meter reading updated successfully")
                                .build();
        }

        @DeleteMapping("/{id}")
        public ApiResponse<Void> delete(@PathVariable UUID id) {
                meterReadingService.delete(id);
                return ApiResponse.<Void>builder()
                                .message("Meter reading deleted successfully")
                                .build();
        }

        // ==================== QUERIES ====================

        @GetMapping
        public ApiResponse<PageResponse<MeterReadingResponse>> search(
                        @RequestParam(required = false) UUID apartmentId,
                        @RequestParam(required = false) UUID serviceId,
                        @RequestParam(required = false) String period,
                        @RequestParam(required = false) MeterReadingStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                Pageable pageable = PageRequest.of(page, size);
                Page<MeterReadingResponse> resultPage = meterReadingService.search(
                                apartmentId, serviceId, period, status, pageable);

                PageResponse<MeterReadingResponse> pageResponse = PageResponse.<MeterReadingResponse>builder()
                                .data(resultPage.getContent())
                                .currentPage(resultPage.getNumber())
                                .pageSize(resultPage.getSize())
                                .totalElements(resultPage.getTotalElements())
                                .totalPages(resultPage.getTotalPages())
                                .build();

                return ApiResponse.<PageResponse<MeterReadingResponse>>builder()
                                .result(pageResponse)
                                .build();
        }

        @GetMapping("/by-apartment/{apartmentId}")
        public ApiResponse<List<MeterReadingResponse>> getByApartment(
                        @PathVariable UUID apartmentId,
                        @RequestParam(required = false) UUID serviceId) {

                List<MeterReadingResponse> result = serviceId != null
                                ? meterReadingService.getByApartmentAndService(apartmentId, serviceId)
                                : meterReadingService.getByApartment(apartmentId);

                return ApiResponse.<List<MeterReadingResponse>>builder()
                                .result(result)
                                .build();
        }

        @GetMapping("/by-period/{period}")
        public ApiResponse<List<MeterReadingResponse>> getByPeriod(
                        @PathVariable String period,
                        @RequestParam(required = false) UUID serviceId) {

                List<MeterReadingResponse> result = serviceId != null
                                ? meterReadingService.getByPeriodAndService(period, serviceId)
                                : meterReadingService.getByPeriod(period);

                return ApiResponse.<List<MeterReadingResponse>>builder()
                                .result(result)
                                .build();
        }

        // ==================== BUSINESS LOGIC ====================

        @GetMapping("/old-index")
        public ApiResponse<OldIndexResponse> getOldIndexSuggestion(
                        @RequestParam UUID apartmentId,
                        @RequestParam UUID serviceId,
                        @RequestParam String period) {

                OldIndexResponse result = meterReadingService.getOldIndexSuggestion(apartmentId, serviceId, period);
                return ApiResponse.<OldIndexResponse>builder()
                                .result(result)
                                .build();
        }

        // ==================== STATUS MANAGEMENT ====================

        @PatchMapping("/{id}/confirm")
        public ApiResponse<MeterReadingResponse> confirm(@PathVariable UUID id) {
                MeterReadingResponse result = meterReadingService.confirm(id);
                return ApiResponse.<MeterReadingResponse>builder()
                                .result(result)
                                .message("Meter reading confirmed successfully")
                                .build();
        }

        @PatchMapping("/{id}/lock")
        public ApiResponse<MeterReadingResponse> lock(@PathVariable UUID id) {
                MeterReadingResponse result = meterReadingService.lock(id);
                return ApiResponse.<MeterReadingResponse>builder()
                                .result(result)
                                .message("Meter reading locked successfully")
                                .build();
        }

        // ==================== STATISTICS ====================

        @GetMapping("/stats/count")
        public ApiResponse<Long> countByPeriodAndStatus(
                        @RequestParam String period,
                        @RequestParam MeterReadingStatus status) {

                long count = meterReadingService.countByPeriodAndStatus(period, status);
                return ApiResponse.<Long>builder()
                                .result(count)
                                .build();
        }

        // Thống kê tổng hợp theo kỳ (completion rate, high usage, totals)
        @GetMapping("/summary/{period}")
        public ApiResponse<PeriodSummaryResponse> getPeriodSummary(@PathVariable String period) {
                PeriodSummaryResponse result = meterReadingService.getPeriodSummary(period);
                return ApiResponse.<PeriodSummaryResponse>builder()
                                .result(result)
                                .build();
        }
}
