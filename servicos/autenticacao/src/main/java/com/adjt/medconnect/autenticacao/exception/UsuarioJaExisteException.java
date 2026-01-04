package com.adjt.medconnect.autenticacao.exception;

public class UsuarioJaExisteException extends RuntimeException{

    public UsuarioJaExisteException(String message){
        super(message);
    }
}


