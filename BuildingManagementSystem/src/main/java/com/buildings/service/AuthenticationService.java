package com.buildings.service;

import com.buildings.dto.request.Auth.AuthenticationRequest;
import com.buildings.dto.request.Auth.LogoutRequest;
import com.buildings.dto.request.Auth.RefreshRequest;
import com.buildings.dto.response.Auth.AuthenticationResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
    boolean verifyToken(String token);
    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
    void logout(LogoutRequest request) throws ParseException, JOSEException;
}