//package com.buildings.controller;
//
//import com.buildings.dto.ApiResponse;
//import com.buildings.dto.request.Auth.AuthenticationRequest;
//import com.buildings.dto.request.Auth.LogoutRequest;
//import com.buildings.dto.request.Auth.RefreshRequest;
//import com.buildings.dto.response.Auth.AuthenticationResponse;
//import com.buildings.service.AuthenticationService;
//import com.nimbusds.jose.JOSEException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.text.ParseException;
//
//@RestController
//@RequestMapping("/auth")
//@RequiredArgsConstructor
//public class AuthenticationController {
//
//    @Autowired
//    private AuthenticationService authenticationService;
//
//    @PostMapping("/token")
//    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
//        var result = authenticationService.authenticate(request);
//        return ApiResponse.<AuthenticationResponse>builder()
//                .result(result)
//                .build();
//    }
//
//    @PostMapping("/refresh")
//    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest request)
//            throws ParseException, JOSEException {
//        var result = authenticationService.refreshToken(request);
//        return ApiResponse.<AuthenticationResponse>builder()
//                .result(result)
//                .build();
//    }
//
//    @PostMapping("/logout")
//    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
//            throws ParseException, JOSEException {
//        authenticationService.logout(request);
//        return ApiResponse.<Void>builder()
//                .message("Logout successfully")
//                .build();
//    }
//}