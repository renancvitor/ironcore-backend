package com.ironcore.infrastructure.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.domain.logging.error.enums.ErrorCodeType;
import com.ironcore.infrastructure.security.jwt.exception.JwtTokenValidationException;
import com.ironcore.interfaces.rest.exception.factory.ApiErrorResponseFactory;
import com.ironcore.interfaces.rest.exception.model.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String AUTHENTICATION_REQUIRED_MESSAGE = "Autenticação necessária.";
    private static final String INVALID_TOKEN_MESSAGE = "Token de autenticação inválido ou expirado.";

    private final ObjectMapper objectMapper;
    private final ErrorLogPublisher publisher;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        Throwable authenticationFailure = getAuthenticationFailure(exception);

        publisher.publish(
                ErrorCodeType.AUTHENTICATION_ERROR,
                getExceptionMessage(authenticationFailure),
                authenticationFailure.getClass().getName(),
                request.getRequestURI(),
                request.getMethod(),
                null,
                getOrCreateCorrelationId(request)
        );

        ApiErrorResponse apiErrorResponse = ApiErrorResponseFactory.create(
                HttpStatus.UNAUTHORIZED,
                authenticationFailure instanceof JwtTokenValidationException
                        ? INVALID_TOKEN_MESSAGE
                        : AUTHENTICATION_REQUIRED_MESSAGE,
                request
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), apiErrorResponse);
    }

    private Throwable getAuthenticationFailure(AuthenticationException exception) {
        return exception.getCause() instanceof JwtTokenValidationException
                ? exception.getCause()
                : exception;
    }

    private String getExceptionMessage(Throwable exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return message;
    }

    private String getOrCreateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-Id");

        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return correlationId;
    }
}
