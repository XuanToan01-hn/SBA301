package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.request.apartment_resident.ApartmentResidentRequest;
import com.buildings.dto.response.apartment.ApartmentResponse;
import com.buildings.dto.response.apartment_resident.ApartmentResidentResponse;
import com.buildings.entity.enums.ApartmentStatus;
import com.buildings.service.AparmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Apartments", description = "APIs quản lý căn hộ trong tòa nhà")
public class ApartmentController {

    private final AparmentService apartmentService;

    @GetMapping
    @Operation(
            summary = "Danh sách tất cả căn hộ",
            description = "Lấy toàn bộ danh sách căn hộ trong hệ thống"
    )
    public ResponseEntity<ApiResponse<List<ApartmentResponse>>> getAllApartments() {
        return ResponseEntity.ok(
                ApiResponse.<List<ApartmentResponse>>builder()
                        .result(apartmentService.getAllApartments())
                        .message("Get all apartments successfully")
                        .build()
        );
    }

    @GetMapping("/{apartmentId}")
    @Operation(
            summary = "Chi tiết căn hộ",
            description = "Lấy thông tin chi tiết của một căn hộ theo ID"
    )
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
    @Operation(
            summary = "Danh sách căn hộ theo tòa nhà",
            description = "Lấy tất cả căn hộ thuộc một tòa nhà"
    )
    public ResponseEntity<ApiResponse<List<ApartmentResponse>>> getByBuilding(
            @PathVariable UUID buildingId) {

        return ResponseEntity.ok(
                ApiResponse.<List<ApartmentResponse>>builder()
                        .result(apartmentService.getByBuildingId(buildingId))
                        .message("Get apartments by building successfully")
                        .build()
        );
    }

    @GetMapping("/building/{buildingId}/paged")
    @Operation(
            summary = "Danh sách căn hộ có phân trang",
            description = "Lấy danh sách căn hộ của tòa nhà theo phân trang"
    )
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
    @Operation(
            summary = "Lọc căn hộ nâng cao",
            description = "Tìm kiếm căn hộ theo building (bắt buộc), mã căn hộ, tầng, trạng thái, số phòng ngủ"
    )
    public ResponseEntity<ApiResponse<Page<ApartmentResponse>>> filterApartments(
            @RequestParam UUID buildingId,
            @RequestParam(required = false) String code, // Nhận từ Frontend
            @RequestParam(required = false) Integer floorNumber,
            @RequestParam(required = false) ApartmentStatus status,
            @RequestParam(required = false) Integer bedroomCount,
            Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.<Page<ApartmentResponse>>builder()
                        .result(apartmentService.searchWithFilters(
                                buildingId, code, floorNumber, status, bedroomCount, pageable))
                        .message("Filter apartments successfully")
                        .build()
        );
    }

    @GetMapping("/building/{buildingId}/with-owner")
    @Operation(
            summary = "Danh sách căn hộ có chủ sở hữu",
            description = "Lấy danh sách căn hộ kèm thông tin chủ sở hữu"
    )
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
    @Operation(
            summary = "Đếm số căn hộ",
            description = "Đếm tổng số căn hộ hoặc theo trạng thái trong một tòa nhà"
    )
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
    @PostMapping("/assign-resident")
    public ApiResponse<ApartmentResidentResponse> assign(@RequestBody ApartmentResidentRequest request) {
        return ApiResponse.<ApartmentResidentResponse>builder()
                .result(apartmentService.assignResident(request))
                .build();
    }

    @GetMapping("/resident/{email}")
    public ResponseEntity<ApiResponse<List<ApartmentResponse>>> getByResidentEmail(
            @PathVariable String email) {
        return ResponseEntity.ok(
                ApiResponse.<List<ApartmentResponse>>builder()
                        .result(apartmentService.getApartmentsByResidentEmail(email))
                        .message("Get apartments by resident email successfully")
                        .build()
        );
    }
}
