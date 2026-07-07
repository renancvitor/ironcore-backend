package com.ironcore.interfaces.rest.exception.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Resposta padronizada de erro da API")
public record ApiErrorResponse(

        @Schema(description = "Data e hora em que o erro ocorreu")
        Instant timestamp,

        @Schema(description = "Código HTTP numérico")
        int status,

        @Schema(description = "Descrição do status HTTP")
        String error,

        @Schema(description = "Mensagem explicativa do erro")
        String message,

        @Schema(description = "Caminho da requisição que causou o erro")
        String path,

        @Schema(description = "Erros específicos de campos, quando aplicável")
        List<FieldErrorResponse> fields
) {
}
