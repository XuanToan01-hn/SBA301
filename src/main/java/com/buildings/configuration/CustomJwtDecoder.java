package com.buildings.configuration;

import com.buildings.repository.InvalidTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String secret;

    private final JwtProvider jwtProvider;
    private final InvalidTokenRepository invalidTokenRepository;
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // 1. Kiểm tra kỹ thuật & Blacklist
            var signedJwt = jwtProvider.verifyToken(token);
            String jit = signedJwt.getJWTClaimsSet().getJWTID();

            if (invalidTokenRepository.existsById(jit)) {
                throw new JwtException("Token has been logged out");
            }
        } catch (Exception e) {
            throw new JwtException(e.getMessage());
        }

        // 2. Khởi tạo decoder chuẩn của Spring
        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder
                    .withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}