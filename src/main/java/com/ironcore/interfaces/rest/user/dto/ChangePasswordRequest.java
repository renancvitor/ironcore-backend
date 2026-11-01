package com.ironcore.interfaces.rest.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "A senha atual não pode estar em branco.")
        @Schema(format = "password")
        String currentPassword,

        @NotBlank(message = "A nova senha não pode estar em branco.")
        @Schema(format = "password")
        String newPassword,

        @NotBlank(message = "A confirmação da nova senha não pode estar em branco.")
        @Schema(format = "password")
        String confirmNewPassword
) {
}
