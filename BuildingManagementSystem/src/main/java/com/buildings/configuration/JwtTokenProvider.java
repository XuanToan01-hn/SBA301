package com.buildings.configuration;//package com.buildings.configuration;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//
//@Service
//public class JwtTokenProvider {
//
//    @Value("${jwt.signerKey}")
//    private String jwtSecret;
//
//    @Value("${jwt.valid-duration}")
//    private long jwtExpiration;
//
//    private SecretKey key;
//
//    @PostConstruct
//    public void init() {
//        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
//    }
//
//    public String generateToken(String email) {
//        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + jwtExpiration);
//
//        return Jwts.builder()
//                .subject(email) // email làm subject
//                .issuedAt(now)
//                .expiration(expiryDate)
//                .signWith(key)
//                .compact();
//    }
//
//    public String getUsernameFromToken(String token) {
//        return Jwts.parser()
//                .verifyWith(key)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .getSubject();
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parser()
//                    .verifyWith(key)
//                    .build()
//                    .parseSignedClaims(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public long getJwtExpiration() {
//        return jwtExpiration;
//    }
//}