package com.ironcore.interfaces.rest.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "O e-mail informado possui um formato inválido.")
        @NotBlank(message = "O e-mail não pode estar em branco.")
        String email,

        @NotBlank(message = "A senha não pode estar em branco.")
        String password
) {
}
