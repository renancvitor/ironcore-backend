package com.ironcore.interfaces.rest.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeNicknameRequest(
        @Size(max = 30, message = "Apelido deve ter no máximo 30 caracteres.")
        @NotBlank(message = "Campo não pode ser vazio.")
        String nickname
) {
}
