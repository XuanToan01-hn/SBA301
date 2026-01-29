package practice.javal1.service;

import com.nimbusds.jose.JOSEException;
import practice.javal1.dto.request.Auth.AuthenticationRequest;
import practice.javal1.dto.request.Auth.LogoutRequest;
import practice.javal1.dto.request.Auth.RefreshRequest;
import practice.javal1.dto.response.Auth.AuthenticationResponse;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
    boolean verifyToken(String token);
    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
    void logout(LogoutRequest request) throws ParseException, JOSEException;
}