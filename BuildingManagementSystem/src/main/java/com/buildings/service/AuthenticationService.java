package com.buildings.service;

import com.buildings.dto.request.Auth.AuthenticationRequest;
import com.buildings.dto.request.user.UserCreateRequest;
import com.buildings.dto.response.Auth.AuthenticationResponse;
import com.buildings.dto.response.user.UserResponse;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
    UserResponse signup(UserCreateRequest request);
    boolean verifyToken(String token);

}
