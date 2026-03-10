package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.response.user.UserProfileResponse;
import com.buildings.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "APIs lấy thông tin cá nhân")
public class UserController {

    private final UserService userService;

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
