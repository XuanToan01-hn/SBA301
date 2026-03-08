package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.request.apartment_resident.ApartmentResidentRequest;
import com.buildings.dto.response.apartment.ApartmentResponse;
import com.buildings.dto.response.apartment_resident.ApartmentResidentResponse;
import com.buildings.entity.enums.ApartmentStatus;
import com.buildings.service.AparmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApartmentController {

    private final AparmentService apartmentService;

    @PostMapping("/assign-resident")
    public ApiResponse<ApartmentResidentResponse> assign(@RequestBody ApartmentResidentRequest request) {
        return ApiResponse.<ApartmentResidentResponse>builder()
                .result(apartmentService.assignResident(request))
                .build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApartmentResponse>>> getAllApartments() {
        return ResponseEntity.ok(
                ApiResponse.<List<ApartmentResponse>>builder()
                        .result(apartmentService.getAllApartments())
                        .message("Get all apartments successfully")
                        .build()
        );
    }

    @GetMapping("/{apartmentId}")
    public ResponseEntity<ApiResponse<ApartmentResponse>> getById(
            @PathVariable UUID apartmentId) {
        return ResponseEntity.ok(
                ApiResponse.<ApartmentResponse>builder()
                        .result(apartmentService.getById(apartmentId))
                        .message("Get apartment successfully")
                        .code(200)
                        .build()
        );
    }

    @GetMapping("/building/{buildingId}")
    public ResponseEntity<ApiResponse<List<ApartmentResponse>>> getByBuilding(@PathVariable UUID buildingId) {
        return ResponseEntity.ok(
                ApiResponse.<List<ApartmentResponse>>builder()
                        .code(200)
                        .result(apartmentService.getByBuildingId(buildingId))
                        .message("Get apartments by building successfully")
                        .build()
        );
    }

    @GetMapping("/building/{buildingId}/paged")
    public ResponseEntity<ApiResponse<Page<ApartmentResponse>>> getByBuildingPaged(
            @PathVariable UUID buildingId,
            Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.<Page<ApartmentResponse>>builder()
                        .result(apartmentService.getByBuildingIdPaged(buildingId, pageable))
                        .message("Get paged apartments successfully")
                        .build()
        );
    }

    @GetMapping("/search/filter")
    public ResponseEntity<ApiResponse<Page<ApartmentResponse>>> filterApartments(
            @RequestParam UUID buildingId,
            @RequestParam(required = false) Integer floorNumber,
            @RequestParam(required = false) ApartmentStatus status,
            @RequestParam(required = false) Integer bedroomCount,
            Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.<Page<ApartmentResponse>>builder()
                        .result(apartmentService.searchWithFilters(
                                buildingId, floorNumber, status, bedroomCount, pageable))
                        .message("Filter apartments successfully")
                        .build()
        );
    }

    @GetMapping("/building/{buildingId}/with-owner")
    public ResponseEntity<ApiResponse<Page<ApartmentResponse>>> getWithOwner(
            @PathVariable UUID buildingId,
            Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.<Page<ApartmentResponse>>builder()
                        .result(apartmentService.getApartmentsWithOwner(buildingId, pageable))
                        .message("Get apartments with owner successfully")
                        .build()
        );
    }

    @GetMapping("/building/{buildingId}/count")
    public ResponseEntity<ApiResponse<Long>> getCount(
            @PathVariable UUID buildingId,
            @RequestParam(required = false) ApartmentStatus status) {

        Long result = (status != null)
                ? apartmentService.countByStatusInBuilding(buildingId, status)
                : apartmentService.countTotalInBuilding(buildingId);

        return ResponseEntity.ok(
                ApiResponse.<Long>builder()
                        .result(result)
                        .message("Count apartments successfully")
                        .build()
        );
    }

    @DeleteMapping("/building/{buildingId}")
    public ResponseEntity<ApiResponse<Void>> deleteByBuilding(
            @PathVariable UUID buildingId) {

        apartmentService.deleteByBuilding(buildingId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Delete apartments by building successfully")
                        .build()
        );
    }
}