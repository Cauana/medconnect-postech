package com.adjt.medconnect.autenticacao.exception;

import com.adjt.medconnect.autenticacao.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UsuarioJaExisteException.class)
    public ResponseEntity<Map<String,String>>handleUsuarioJaExiste(UsuarioJaExisteException ex){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message",ex.getMessage()));
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ApiResponse>handleUsuarioNaoEncontrado(
            UsuarioNaoEncontradoException ex
    ){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(ex.getMessage()));
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ApiResponse> handleAcessoNegado(AcessoNegadoException ex){
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleSpringSecurityAccessDenied(){
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse("Você não tem permissão para acessar este recurso"));
    }

    public ResponseEntity<ApiResponse> handleGeneric(Exception ex){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("Erro interno no servidor."));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Map<String, String>> handleCredenciaisInvalidas(
            CredenciaisInvalidasException ex
    ){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro",ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handlerUnauthorized(UnauthorizedException ex, HttpServletRequest request){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status",401,
                        "error", "Unauthorized",
                        "message",ex.getMessage(),
                        "path",request.getRequestURI()
                ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatusCode().value());
        body.put("error", "Erro de Validação");
        body.put("message", ex.getReason()); // Aqui pegamos sua mensagem personalizada

        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(401).body("Erro de autenticação: " + e.getMessage());
    }


    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(io.jsonwebtoken.ExpiredJwtException e) {
        return ResponseEntity.status(401).body("Token JWT expirado");
    }

    @ExceptionHandler(io.jsonwebtoken.security.SignatureException.class)
    public ResponseEntity<String> handleSignatureException(io.jsonwebtoken.security.SignatureException e) {
        return ResponseEntity.status(401).body("Token JWT inválido");
    }

}
