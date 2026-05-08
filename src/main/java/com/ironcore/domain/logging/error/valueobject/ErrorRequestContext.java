package com.ironcore.domain.logging.error.valueobject;

import com.ironcore.domain.logging.error.exception.InvalidErrorLogException;

public record ErrorRequestContext(String path, String httpMethod) {

    public ErrorRequestContext {
        path = requireNonBlank(path, "Caminho da requisição não pode ser nulo ou vazio");
        httpMethod = requireNonBlank(httpMethod, "Método HTTP não pode ser nulo ou vazio");
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidErrorLogException(message);
        }

        return value.trim();
    }
}
