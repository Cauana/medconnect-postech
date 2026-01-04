package com.adjt.medconnect.autenticacao.exception;

public class CredenciaisInvalidasException extends RuntimeException{
    public CredenciaisInvalidasException(){
        super("Usuário ou senha inválidos.");
    }
}
