package com.adjt.medconnect.autenticacao.exception;

public class JwtValidationException extends RuntimeException {

    // Construtor com apenas mensagem
    public JwtValidationException(String message) {
        super(message);
    }

    // Construtor com mensagem e causa
    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
