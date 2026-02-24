package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.BuildingDTO;
import com.buildings.service.BuildingService;
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
public class BuildingController {

    private final BuildingService buildingService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BuildingDTO> createBuilding(@Valid @RequestBody BuildingDTO buildingDTO) {
        return ApiResponse.<BuildingDTO>builder()
                .code(201)
                .message("Tòa nhà đã được tạo thành công.")
                .result(buildingService.createBuilding(buildingDTO))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<BuildingDTO> updateBuilding(
            @PathVariable UUID id,
            @Valid @RequestBody BuildingDTO buildingDTO) {
        return ApiResponse.<BuildingDTO>builder()
                .message("Cập nhật thông tin tòa nhà thành công.")
                .result(buildingService.updateBuilding(id, buildingDTO))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BuildingDTO> getBuildingById(@PathVariable UUID id) {
        return ApiResponse.<BuildingDTO>builder()
                .result(buildingService.getBuildingById(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBuilding(@PathVariable UUID id) {
        buildingService.deleteBuilding(id);
        return ApiResponse.<Void>builder()
                .message("Xóa tòa nhà thành công.")
                .build();
    }


    @PostMapping("/{id}/generate-apartments")
    public ApiResponse<BuildingDTO> generateApartments(@PathVariable UUID id) {
        buildingService.generateApartments(id);
        return ApiResponse.<BuildingDTO>builder()
                .message("Đã tạo danh sách căn hộ thành công.")
                .result(buildingService.getBuildingById(id))
                .build();
    }


    @GetMapping
    public ApiResponse<Map<String, Object>> getAllBuildings(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {

        Page<BuildingDTO> page = (search != null && !search.isEmpty())
                ? buildingService.searchBuildings(search, pageable)
                : buildingService.getAllBuildings(pageable);

        return ApiResponse.<Map<String, Object>>builder()
                .result(wrapPagination(page))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<BuildingDTO>> getAllBuildingsNoPagination() {
        return ApiResponse.<List<BuildingDTO>>builder()
                .result(buildingService.getAllBuildings())
                .build();
    }

    @GetMapping("/without-apartments")
    public ApiResponse<Map<String, Object>> getBuildingsWithoutApartments(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Map<String, Object>>builder()
                .result(wrapPagination(buildingService.getBuildingsWithoutGeneratedApartments(pageable)))
                .build();
    }

    @GetMapping("/with-apartments") // Bổ sung API này cho đủ bộ với Service
    public ApiResponse<Map<String, Object>> getBuildingsWithApartments(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Map<String, Object>>builder()
                .result(wrapPagination(buildingService.getBuildingsWithGeneratedApartments(pageable)))
                .build();
    }


    @GetMapping("/check-code/{code}")
    public ApiResponse<Boolean> checkBuildingCode(
            @PathVariable String code,
            @RequestParam(required = false) UUID excludeId) {
        boolean exists = (excludeId != null)
                ? buildingService.buildingCodeExistsExcluding(code, excludeId)
                : buildingService.buildingCodeExists(code);
        return ApiResponse.<Boolean>builder().result(exists).build();
    }

    @GetMapping("/check-name/{name}")
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