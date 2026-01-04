package com.adjt.medconnect.autenticacao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException(){
        super("Usuário não autenticado. Gere um token para continuar.");

    }

}
