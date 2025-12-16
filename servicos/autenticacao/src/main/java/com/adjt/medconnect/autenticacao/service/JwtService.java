package com.adjt.medconnect.autenticacao.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET = "wZ3fK8s9vTqL1xR6mYjP0hVbC4uN8qFw5dG2sK0pQrE=";

    @Value("${JWT_EXPIRATION}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /* =========================
       GERAÇÃO DO TOKEN
       ========================= */
    public String gerarToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /* =========================
       EXTRAÇÃO DO USERNAME
       ========================= */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /* =========================
       VALIDAÇÃO DO TOKEN
       ========================= */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /* =========================
       PARSE CENTRALIZADO
       ========================= */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
