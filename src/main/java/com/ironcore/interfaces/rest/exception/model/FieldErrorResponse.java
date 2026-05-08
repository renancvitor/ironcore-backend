package com.ironcore.interfaces.rest.exception.model;

public record FieldErrorResponse(
        String field,
        String message
) {
}
