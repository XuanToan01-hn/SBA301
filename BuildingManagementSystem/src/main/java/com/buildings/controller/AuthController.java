package com.buildings.controller;//package com.buildings.controller;
//
//
//import com.buildings.dto.request.Auth.AuthenticationRequest;
//import com.buildings.dto.request.user.UserCreateRequest;
//import com.buildings.dto.response.Auth.AuthenticationResponse;
//import com.buildings.dto.response.user.UserResponse;
//import com.buildings.service.AuthenticationService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    private final AuthenticationService authService;
//
//    public AuthController(AuthenticationService authService) {
//        this.authService = authService;
//    }
//    @PostMapping("/signin")
//    public ResponseEntity<AuthenticationResponse> signin(
//            @RequestBody AuthenticationRequest request) {
//
//        AuthenticationResponse response = authService.authenticate(request);
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/signup")
//    public ResponseEntity<UserResponse> signup(
//            @RequestBody UserCreateRequest request) {
//
//        UserResponse response = authService.signup(request);
//        return ResponseEntity.ok(response);
//    }
//}