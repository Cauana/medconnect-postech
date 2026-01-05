package com.adjt.medconnect.servicoagendamento.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            log.info("Authorization Header: {}", authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                log.info("JWT encontrado no header Authorization");

                try {
                    Authentication authentication = jwtValidator.validateTokenFromHeader(authHeader);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("✅ Autenticação estabelecida: {}", authentication.getName());
                    log.info("Authorities: {}", authentication.getAuthorities());
                } catch (Exception e) {
                    log.warn("❌ Erro ao validar token: {}", e.getMessage());
                }
            } else {
                log.debug("Nenhum JWT encontrado no header Authorization");
            }

        } catch (Exception e) {
            log.error("❌ Erro ao processar JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
