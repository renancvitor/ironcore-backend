package com.ironcore.interfaces.rest.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "O e-mail informado possui um formato inválido.")
        @NotBlank(message = "O e-mail não pode estar em branco.")
        String email,

        @NotBlank(message = "A senha não pode estar em branco.")
        @Schema(format = "password", description = "Mínimo 8 caracteres; credenciais antigas acima de 72 bytes continuam aceitas para verificação.", accessMode = Schema.AccessMode.WRITE_ONLY)
        String password
) {
}
