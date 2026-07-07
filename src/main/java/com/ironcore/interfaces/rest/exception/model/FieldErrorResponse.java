package com.ironcore.interfaces.rest.exception.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Erro de validação associado a um campo")
public record FieldErrorResponse(

        @Schema(description = "Nome do campo inválido")
        String field,

        @Schema(description = "Motivo de rejeição do valor")
        String message
) {
}
