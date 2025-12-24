package com.adjt.medconnect.autenticacao.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordDTO (
        @NotBlank(message = "Senha atual é obrigatória.")
        String senhaAtual,
        @NotBlank(message = "Nova senha é obrigatória")
        String novaSenha
){

}
