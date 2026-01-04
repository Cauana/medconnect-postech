package com.adjt.medconnect.autenticacao.exception;

public class UsuarioNaoEncontradoException extends RuntimeException{

    public UsuarioNaoEncontradoException(){
        super("Usuário não encontrado");
    }
}
