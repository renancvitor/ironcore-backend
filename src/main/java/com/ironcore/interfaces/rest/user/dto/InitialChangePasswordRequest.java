package com.ironcore.interfaces.rest.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InitialChangePasswordRequest(
        @Email(message = "O e-mail informado possui um formato inválido.")
        @NotBlank(message = "O e-mail não pode estar em branco.")
        String email,

        @NotBlank(message = "A senha atual não pode estar em branco.")
        @Schema(format = "password", description = "Mínimo 8 caracteres; credenciais antigas acima de 72 bytes continuam aceitas para verificação.", accessMode = Schema.AccessMode.WRITE_ONLY)
        String currentPassword,

        @NotBlank(message = "A nova senha não pode estar em branco.")
        @Schema(format = "password", description = "Mínimo 8 caracteres; máximo 72 bytes em UTF-8.", accessMode = Schema.AccessMode.WRITE_ONLY)
        String newPassword,

        @NotBlank(message = "A confirmação da nova senha não pode estar em branco.")
        @Schema(format = "password", description = "Deve coincidir com a nova senha; máximo 72 bytes em UTF-8.", accessMode = Schema.AccessMode.WRITE_ONLY)
        String confirmNewPassword
) {
}
