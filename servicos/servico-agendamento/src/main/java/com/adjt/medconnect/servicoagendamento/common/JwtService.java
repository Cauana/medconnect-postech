package com.adjt.medconnect.servicoagendamento.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtService {

    @Value("${jwt.secret:${JWT_SECRET}}")
    private String jwtSecret;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
        System.out.println("--- JWT SECRET carregado no serviço de agendamento");
    }

    // Extrai username do token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extrai role do token
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // Extrai userId (se presente) do token
    public Long extractUserId(String token) {
        Object val = extractAllClaims(token).get("userId");
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            try { return Long.parseLong((String) val); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    // Valida token
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Parse centralizado
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
