package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.request.user.UserCreateRequest;
import com.buildings.dto.request.user.UserUpdateRequest;
import com.buildings.dto.response.user.UserProfileResponse;
import com.buildings.dto.response.user.UserResponse;
import com.buildings.entity.enums.UserStatus;
import com.buildings.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "APIs lấy thông tin cá nhân")
public class UserController {

    private final UserService userService;
    @PostMapping("/create")

    @Operation(
            summary = "Tạo tài khoản",
            description = "Tạo tài khoản mới cho người dùng (Resident, Staff hoặc Admin tùy cấu hình hệ thống)."
    )
    public ResponseEntity<UserResponse> signup(
            @RequestBody UserCreateRequest request) {

        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Tìm kiếm người dùng",
            description = "Tìm kiếm cư dân/người dùng theo tên, email hoặc số điện thoại để gán vào căn hộ"
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(
                ApiResponse.<List<UserResponse>>builder()
                        .result(userService.searchUsers(query))
                        .message("Search users successfully")
                        .code(200)
                        .build()
        );
    }


    @GetMapping()
    @Operation(summary = "Danh sách người dùng có phân trang và lọc")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ApiResponse.<Page<UserResponse>>builder()
                        .result(userService.searchUser(keyword, status, page, size))
                        .build()
        );
    }

    // 2. Cập nhật thông tin
    @PutMapping("/{userId}")
    @Operation(summary = "Cập nhật thông tin người dùng")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable UUID userId,
            @RequestBody UserUpdateRequest request) {

        request.setId(request.getId());
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .result(userService.updateUser(request))
                        .message("User updated successfully")
                        .build()
        );
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Vô hiệu hóa tài khoản (Soft Delete)")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("User deactivated successfully")
                        .build()
        );
    }
    @GetMapping("/me")
    @Operation(summary = "Lấy profile người dùng hiện tại")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(
                ApiResponse.<UserProfileResponse>builder()
                        .result(userService.getMyProfile())
                        .message("Fetch profile successfully")
                        .build()
        );
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Lấy chi tiết người dùng theo ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .result(userService.getUserById(userId)) // Đảm bảo userService đã có hàm này
                        .build()
        );
    }
}
