package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.response.role.RoleResponse;
import com.buildings.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs quản lý vai trò người dùng")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả vai trò")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(
                ApiResponse.<List<RoleResponse>>builder()
                        .result(roleService.getAllRoles())
                        .code(200)
                        .message("Fetch all roles successfully")
                        .build()
        );
    }
}