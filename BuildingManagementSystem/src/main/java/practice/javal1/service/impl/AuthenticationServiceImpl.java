package practice.javal1.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import practice.javal1.dto.request.Auth.AuthenticationRequest;
import practice.javal1.dto.request.Auth.LogoutRequest;
import practice.javal1.dto.request.Auth.RefreshRequest;
import practice.javal1.dto.response.Auth.AuthenticationResponse;
import practice.javal1.entity.InvalidTokenEntity;
import practice.javal1.entity.User;
import practice.javal1.exception.auth.InvalidTokenException;
import practice.javal1.exception.auth.UnAuthorException;
import practice.javal1.exception.user.UserNotFound;
import practice.javal1.repository.InvalidTokenRepository;
import practice.javal1.service.AuthenticationService;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvalidTokenRepository invalidTokenRepository;

    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;


    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        User user = userRepository.findByUsername(authenticationRequest.getUsername());
        if(user == null) {
            throw new UserNotFound("User not exists");
        }

        boolean matches = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if(!matches) {
            throw new UnAuthorException("Wrong username or password");
        }

        String accessToken = generateToken(user, VALID_DURATION);

        log.info("User {} logged in successfully with role {}", user.getUsername(), user.getRole().getCode());

        return AuthenticationResponse.builder()
                .token(accessToken)
//                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyRefreshToken(request.getToken());

        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidTokenEntity invalidToken = InvalidTokenEntity.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();
        invalidTokenRepository.save(invalidToken);

        String username = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUsername(username);
        if(user == null) {
            throw new UserNotFound("User not found");
        }

        String newAccessToken = generateToken(user, VALID_DURATION);
        String newRefreshToken = generateToken(user, REFRESHABLE_DURATION);

        log.info("Tokens refreshed for user {}", username);

        return AuthenticationResponse.builder()
                .token(newRefreshToken)
//                .refreshToken(newRefreshToken)
                .authenticated(true)
                .build();
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            SignedJWT signedToken = SignedJWT.parse(request.getToken());
            String jit = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

            InvalidTokenEntity invalidToken = InvalidTokenEntity.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();
            invalidTokenRepository.save(invalidToken);

            log.info("User logged out, token invalidated: {}", jit);
        } catch (ParseException e) {
            log.error("Error parsing token during logout", e);
            throw new InvalidTokenException("Invalid token format");
        }
    }


    private String generateToken(User user, long durationInSeconds) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("practice.app")
                .claim("userId", user.getId())
                .claim("scope", buildScope(user))
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(durationInSeconds, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verifyToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean isValid = signedJWT.verify(verifier);

            if (!(isValid && expiryTime.after(new Date()) &&
                    !invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))) {
                throw new InvalidTokenException("Token is not valid");
            }
            return true;
        } catch (JOSEException | ParseException e) {
            throw new InvalidTokenException("Token is not valid");
        }
    }

    private SignedJWT verifyRefreshToken(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean verified = signedJWT.verify(verifier);

        if(invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new InvalidTokenException("Token has been logged out");
        }

        if(!(verified && expiryTime.after(new Date()))) {
            throw new InvalidTokenException("Token expired or invalid");
        }
        return signedJWT;
    }

    private String buildScope(User user) {
        if(user.getRole() != null) {
            return "ROLE_" + user.getRole().getCode();
        }
        return "ROLE_USER"; // Default fallback
    }
}