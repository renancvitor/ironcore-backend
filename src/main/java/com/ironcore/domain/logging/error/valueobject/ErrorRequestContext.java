package com.ironcore.domain.logging.error.valueobject;

public record ErrorRequestContext(String path, String httpMethod) {

    public ErrorRequestContext {
        path = requireNonBlank(path, "Caminho da requisição não pode ser nulo ou vazio");
        httpMethod = requireNonBlank(httpMethod, "Método HTTP não pode ser nulo ou vazio");
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}
