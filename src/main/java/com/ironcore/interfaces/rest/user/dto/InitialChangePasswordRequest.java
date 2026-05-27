package com.ironcore.interfaces.rest.user.dto;

import jakarta.validation.constraints.NotBlank;

public record InitialChangePasswordRequest(
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
