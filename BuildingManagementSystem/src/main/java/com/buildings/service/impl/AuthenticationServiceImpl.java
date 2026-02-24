package com.buildings.service.impl;

 import com.buildings.configuration.JwtTokenProvider;
 import com.buildings.dto.request.Auth.AuthenticationRequest;
 import com.buildings.dto.request.user.UserCreateRequest;
 import com.buildings.dto.response.Auth.AuthenticationResponse;
 import com.buildings.dto.response.user.UserResponse;
 import com.buildings.entity.User;
 import com.buildings.entity.UserRole;
 import com.buildings.entity.enums.UserStatus;
 import com.buildings.exception.AppException;
 import com.buildings.exception.ErrorCode;
 import com.buildings.repository.UserRepository;
 import com.buildings.service.AuthenticationService;
 import com.nimbusds.jose.*;

 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 import org.springframework.security.crypto.password.PasswordEncoder;
 import org.springframework.stereotype.Service;


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
     private PasswordEncoder passwordEncoder;

     @Autowired
     private AuthenticationManager authenticationManager;

     @Autowired
     private JwtTokenProvider jwtTokenProvider;

     @Override
     public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
         PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

         User user = userRepository.findByEmail(authenticationRequest.getEmail())
                 .orElseThrow(() ->
                         new AppException(ErrorCode.USER_NOT_EXISTED));

         boolean matches = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
         if(!matches) {
             throw new AppException(ErrorCode.INVALID_EMAIL_PASSWORD);
         }

         String accessToken = jwtTokenProvider.generateToken(user.getEmail());

         log.info("User {} logged in successfully with role {}", user.getFullName());

         return AuthenticationResponse.builder()
                 .token(accessToken)
                 .authenticated(true)
                 .build();
     }

     @Override
     public UserResponse signup(UserCreateRequest request) {

         if (userRepository.findByEmail(request.getEmail()).isPresent()) {
             throw new AppException(ErrorCode.USER_EXISTED);
         }
         User user = User.builder()
                 .fullName(request.getFullName())
                 .email(request.getEmail())
                 .password(passwordEncoder.encode(request.getPassword()))
                 .phone(request.getPhone())
                 .status(UserStatus.ACTIVE)
                 .build();

         userRepository.save(user);

         return UserResponse.builder()
                 .fullName(user.getFullName())
                 .email(user.getEmail())
                 .phone(user.getPhone())
                 .status(user.getStatus())
                 .build();
     }

//     @Override
//     public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
//         SignedJWT signedJWT = verifyRefreshToken(request.getToken());
//
//         String jit = signedJWT.getJWTClaimsSet().getJWTID();
//         Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
//
//         InvalidTokenEntity invalidToken = InvalidTokenEntity.builder()
//                 .id(jit)
//                 .expiryTime(expiryTime)
//                 .build();
//         invalidTokenRepository.save(invalidToken);
//
//         String username = signedJWT.getJWTClaimsSet().getSubject();
//         User user = userRepository.findByUsername(username);
//         if(user == null) {
//             throw new UserNotFound("User not found");
//         }
//
//         String newAccessToken = generateToken(user, VALID_DURATION);
//         String newRefreshToken = generateToken(user, REFRESHABLE_DURATION);
//
//         log.info("Tokens refreshed for user {}", username);
//
//         return AuthenticationResponse.builder()
//                 .token(newRefreshToken)
// //                .refreshToken(newRefreshToken)
//                 .authenticated(true)
//                 .build();
//     }
//
//     @Override
//     public void logout(LogoutRequest request) throws ParseException, JOSEException {
//         try {
//             SignedJWT signedToken = SignedJWT.parse(request.getToken());
//             String jit = signedToken.getJWTClaimsSet().getJWTID();
//             Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
//
//             InvalidTokenEntity invalidToken = InvalidTokenEntity.builder()
//                     .id(jit)
//                     .expiryTime(expiryTime)
//                     .build();
//             invalidTokenRepository.save(invalidToken);
//
//             log.info("User logged out, token invalidated: {}", jit);
//         } catch (ParseException e) {
//             log.error("Error parsing token during logout", e);
//             throw new InvalidTokenException("Invalid token format");
//         }
//     }
//
//
//     private String generateToken(User user, long durationInSeconds) {
//         JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
//
//         JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
//                 .subject(user.getUsername())
//                 .issuer("practice.app")
//                 .claim("userId", user.getId())
//                 .claim("scope", buildScope(user))
//                 .issueTime(new Date())
//                 .expirationTime(new Date(
//                         Instant.now().plus(durationInSeconds, ChronoUnit.SECONDS).toEpochMilli()))
//                 .jwtID(UUID.randomUUID().toString())
//                 .build();
//
//         Payload payload = new Payload(jwtClaimsSet.toJSONObject());
//         JWSObject jwsObject = new JWSObject(header, payload);
//
//         try {
//             jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
//             return jwsObject.serialize();
//         } catch (JOSEException e) {
//             log.error("Cannot create token", e);
//             throw new RuntimeException(e);
//         }
//     }
//
//     @Override
//     public boolean verifyToken(String token) {
//         try {
//             SignedJWT signedJWT = SignedJWT.parse(token);
//             JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
//             Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
//             boolean isValid = signedJWT.verify(verifier);
//
//             if (!(isValid && expiryTime.after(new Date()) &&
//                     !invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))) {
//                 throw new InvalidTokenException("Token is not valid");
//             }
//             return true;
//         } catch (JOSEException | ParseException e) {
//             throw new InvalidTokenException("Token is not valid");
//         }
//     }
//
//     private SignedJWT verifyRefreshToken(String token) throws JOSEException, ParseException {
//         SignedJWT signedJWT = SignedJWT.parse(token);
//         JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
//
//         Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
//         boolean verified = signedJWT.verify(verifier);
//
//         if(invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
//             throw new InvalidTokenException("Token has been logged out");
//         }
//
//         if(!(verified && expiryTime.after(new Date()))) {
//             throw new InvalidTokenException("Token expired or invalid");
//         }
//         return signedJWT;
//     }
//
//     private String buildScope(User user) {
//         if(user.getRole() != null) {
//             return "ROLE_" + user.getRole().getCode();
//         }
//         return "ROLE_USER"; // Default fallback
//     }
 }