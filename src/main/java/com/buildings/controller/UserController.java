package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.response.user.UserProfileResponse;
import com.buildings.dto.response.user.UserResponse;
import com.buildings.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "APIs lấy thông tin cá nhân")
public class UserController {

    private final UserService userService;

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
}
