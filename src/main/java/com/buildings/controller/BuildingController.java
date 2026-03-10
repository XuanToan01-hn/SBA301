package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.BuildingDTO;
import com.buildings.service.BuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Buildings", description = "APIs quản lý tòa nhà và tạo căn hộ")
public class BuildingController {

    private final BuildingService buildingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo tòa nhà", description = "Admin hoặc Manager tạo một tòa nhà mới trong hệ thống")
    public ApiResponse<BuildingDTO> createBuilding(@Valid @RequestBody BuildingDTO buildingDTO) {
        return ApiResponse.<BuildingDTO>builder()
                .code(201)
                .message("Tòa nhà đã được tạo thành công.")
                .result(buildingService.createBuilding(buildingDTO))
                .build();
    }


    @GetMapping
    @Operation(summary = "Tìm kiếm tòa nhà", description = "Search theo name/code và filter trạng thái generate căn hộ")
    public ApiResponse<Page<BuildingDTO>> searchBuildings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean apartmentsGenerated,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<BuildingDTO> result = buildingService.searchBuildings(search, apartmentsGenerated, pageable);
        return ApiResponse.<Page<BuildingDTO>>builder()
                .code(200)
                .message("Lấy danh sách tòa nhà thành công")
                .result(result)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật tòa nhà", description = "Cập nhật thông tin của tòa nhà theo ID")
    public ApiResponse<BuildingDTO> updateBuilding(
            @PathVariable UUID id,
            @Valid @RequestBody BuildingDTO buildingDTO) {
        return ApiResponse.<BuildingDTO>builder()
                .message("Cập nhật thông tin tòa nhà thành công.")
                .result(buildingService.updateBuilding(id, buildingDTO))
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết tòa nhà", description = "Lấy thông tin chi tiết của một tòa nhà theo ID")
    public ApiResponse<BuildingDTO> getBuildingById(@PathVariable UUID id) {
        return ApiResponse.<BuildingDTO>builder()
                .result(buildingService.getBuildingById(id))
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa tòa nhà", description = "Xóa một tòa nhà khỏi hệ thống theo ID")
    public ApiResponse<Void> deleteBuilding(@PathVariable UUID id) {
        buildingService.deleteBuilding(id);
        return ApiResponse.<Void>builder()
                .message("Xóa tòa nhà thành công.")
                .build();
    }

    @PostMapping("/{id}/generate-apartments")
    @Operation(summary = "Tạo danh sách căn hộ", description = "Tự động tạo các căn hộ dựa trên cấu hình của tòa nhà")
    public ApiResponse<BuildingDTO> generateApartments(@PathVariable UUID id) {
        buildingService.generateApartments(id);
        return ApiResponse.<BuildingDTO>builder()
                .message("Đã tạo danh sách căn hộ thành công.")
                .result(buildingService.getBuildingById(id))
                .build();
    }

    @GetMapping("/all")
    @Operation(summary = "Danh sách tất cả tòa nhà", description = "Lấy toàn bộ danh sách tòa nhà không phân trang")
    public ApiResponse<List<BuildingDTO>> getAllBuildingsNoPagination() {
        return ApiResponse.<List<BuildingDTO>>builder()
                .result(buildingService.getAllBuildings())
                .build();
    }

    @GetMapping("/without-apartments")
    @Operation(summary = "Danh sách tòa nhà chưa tạo căn hộ", description = "Lấy các tòa nhà chưa được generate apartments")
    public ApiResponse<Map<String, Object>> getBuildingsWithoutApartments(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Map<String, Object>>builder()
                .result(wrapPagination(buildingService.getBuildingsWithoutGeneratedApartments(pageable)))
                .build();
    }

    @GetMapping("/with-apartments")
    @Operation(summary = "Danh sách tòa nhà đã tạo căn hộ", description = "Lấy các tòa nhà đã generate apartments")
    public ApiResponse<Map<String, Object>> getBuildingsWithApartments(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Map<String, Object>>builder()
                .result(wrapPagination(buildingService.getBuildingsWithGeneratedApartments(pageable)))
                .build();
    }

    @GetMapping("/check-code/{code}")
    @Operation(summary = "Kiểm tra mã tòa nhà", description = "Kiểm tra mã tòa nhà đã tồn tại hay chưa")
    public ApiResponse<Boolean> checkBuildingCode(
            @PathVariable String code,
            @RequestParam(required = false) UUID excludeId) {
        boolean exists = (excludeId != null)
                ? buildingService.buildingCodeExistsExcluding(code, excludeId)
                : buildingService.buildingCodeExists(code);
        return ApiResponse.<Boolean>builder().result(exists).build();
    }

    @GetMapping("/check-name/{name}")
    @Operation(summary = "Kiểm tra tên tòa nhà", description = "Kiểm tra tên tòa nhà đã tồn tại hay chưa")
    public ApiResponse<Boolean> checkBuildingName(
            @PathVariable String name,
            @RequestParam(required = false) UUID excludeId) {
        boolean exists = (excludeId != null)
                ? buildingService.buildingNameExistsExcluding(name, excludeId)
                : buildingService.buildingNameExists(name);
        return ApiResponse.<Boolean>builder().result(exists).build();
    }

    private Map<String, Object> wrapPagination(Page<BuildingDTO> page) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", page.getContent());
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        return response;
    }
}