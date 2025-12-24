package com.adjt.medconnect.autenticacao.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank String usuario;
    @NotBlank String senha;
}
