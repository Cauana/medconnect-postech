package com.adjt.medconnect.autenticacao.service;

import com.adjt.medconnect.autenticacao.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_EXPIRATION}")
    private long jwtExpiration;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
        System.out.println("--- JWT SECRET carregado: " + jwtSecret);
    }

    // Gera token com username e role
    public String gerarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", usuario.getRole().name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getUsuario())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extrai username do token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extrai role do token
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
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
