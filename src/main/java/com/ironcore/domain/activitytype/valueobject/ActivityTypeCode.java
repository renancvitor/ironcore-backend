package com.ironcore.domain.activitytype.valueobject;

import com.ironcore.domain.activitytype.exception.InvalidActivityTypeException;

public record ActivityTypeCode(String value) {

    public ActivityTypeCode {
        if (value == null || value.isBlank()) {
            throw new InvalidActivityTypeException("Código do tipo de atividade é obrigatório.");
        }

        value = value.trim().toUpperCase();

        if (!value.matches("[A-Z0-9_]+")) {
            throw new InvalidActivityTypeException("Código do tipo de atividade deve conter apenas letras maiúsculas" +
                    " números e underscores.");
        }

        if (value.length() > 50) {
            throw new InvalidActivityTypeException("Código do tipo de atividade não pode exceder 50 caracteres.");
        }
    }
}
