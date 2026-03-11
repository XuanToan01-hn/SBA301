package com.buildings.controller;

import com.buildings.dto.request.Auth.AuthenticationRequest;
import com.buildings.dto.request.user.UserCreateRequest;
import com.buildings.dto.response.Auth.AuthenticationResponse;
import com.buildings.dto.response.user.UserResponse;
import com.buildings.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API xác thực người dùng: đăng nhập và đăng ký")
public class AuthController {

    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    @Operation(
            summary = "Đăng nhập",
            description = "Người dùng đăng nhập vào hệ thống bằng email/username và mật khẩu. Trả về JWT token nếu thành công."
    )
    public ResponseEntity<AuthenticationResponse> signin(
            @RequestBody AuthenticationRequest request) {

        AuthenticationResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    @Operation(
            summary = "Đăng ký tài khoản",
            description = "Tạo tài khoản mới cho người dùng (Resident, Staff hoặc Admin tùy cấu hình hệ thống)."
    )
    public ResponseEntity<UserResponse> signup(
            @RequestBody UserCreateRequest request) {

        UserResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }
}