package com.ironcore.interfaces.rest.exception.factory;

import com.ironcore.interfaces.rest.exception.model.FieldErrorResponse;
import org.springframework.validation.FieldError;

import java.util.List;

public final class FieldErrorResponseFactory {

    private FieldErrorResponseFactory() {}

    public static List<FieldErrorResponse> from(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(error -> new FieldErrorResponse(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();
    }
}
