package com.adjt.medconnect.autenticacao.common;

import com.adjt.medconnect.autenticacao.exception.JwtValidationException;
import com.adjt.medconnect.autenticacao.model.Role;
import com.adjt.medconnect.autenticacao.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final JwtService jwtService;

    public Authentication validateToken(String token) {
        log.debug("Validando token JWT...");

        if (token == null || token.trim().isEmpty()) {
            throw new JwtValidationException("Token não pode ser nulo ou vazio");
        }

        if (!jwtService.isTokenValid(token)) {
            throw new JwtValidationException("Token inválido ou expirado");
        }

        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);

        // Valida se a role extraída é uma Role válida
        validateRole(role);

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        log.debug("Token válido para usuário: {}, role: {}", username, role);

        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );
    }

    public boolean hasRequiredRole(String token, Role requiredRole) {
        String role = jwtService.extractRole(token);
        return requiredRole.name().equals(role);
    }

    public boolean hasAnyRole(String token, Role... roles) {
        String userRole = jwtService.extractRole(token);
        return Arrays.stream(roles)
                .anyMatch(role -> role.name().equals(userRole));
    }

    public String extractUsername(String token) {
        return jwtService.extractUsername(token);
    }

    public Role extractRoleEnum(String token) {
        String roleStr = jwtService.extractRole(token);
        return Role.valueOf(roleStr);
    }

    private void validateRole(String role) {
        try {
            Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new JwtValidationException(
                    "Role inválida no token: " + role +
                            ". Roles válidas: " + Arrays.toString(Role.values())
            );
        }
    }

    // Métodos auxiliares para header Authorization
    public Authentication validateTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtValidationException("Formato de Authorization header inválido. Esperado: Bearer <token>");
        }

        String token = authHeader.substring(7); // Remove "Bearer "
        return validateToken(token);
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtValidationException("Formato de Authorization header inválido");
        }
        return authHeader.substring(7);
    }
}