package com.ironcore.interfaces.rest.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InitialChangePasswordRequest(
        @Email(message = "O e-mail informado possui um formato inválido.")
        @NotBlank(message = "O e-mail não pode estar em branco.")
        String email,

        @NotBlank(message = "A senha atual não pode estar em branco.")
        String currentPassword,

        @NotBlank(message = "A nova senha não pode estar em branco.")
        String newPassword,

        @NotBlank(message = "A confirmação da nova senha não pode estar em branco.")
        String confirmPassword
) {
}
