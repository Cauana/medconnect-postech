package com.adjt.medconnect.autenticacao.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import com.adjt.medconnect.autenticacao.common.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login","/auth/register","/auth/roles",
                                "/auth/test-role", "/auth/debug").permitAll()
                        .requestMatchers("/auth/me/**").permitAll()
                        .requestMatchers("/medico/**").hasRole("MEDICO")
                        .requestMatchers("/enfermeiro/**").hasRole("ENFERMEIRO")
                        .requestMatchers("/paciente/**").hasRole("PACIENTE")
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/auth/users/**").hasRole("ADMIN")
                        .requestMatchers("/admin/teste").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }


}

