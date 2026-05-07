package com.ironcore.interfaces.rest.exception.factory;

import com.ironcore.interfaces.rest.exception.model.ApiErrorResponse;
import com.ironcore.interfaces.rest.exception.model.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public final class ApiErrorResponseFactory {

    private ApiErrorResponseFactory() {}

    public static ApiErrorResponse create(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                List.of()
        );
    }

    public static ApiErrorResponse create(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<FieldErrorResponse> fields
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fields
        );
    }
}
