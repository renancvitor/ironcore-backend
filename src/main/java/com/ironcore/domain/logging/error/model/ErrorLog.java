package com.ironcore.domain.logging.error.model;

import com.ironcore.domain.logging.error.enums.ErrorCode;
import com.ironcore.domain.logging.error.valueobject.ErrorRequestContext;

import java.time.LocalDateTime;
import java.util.Objects;

public class ErrorLog {

    private Long id;

    private ErrorCode errorCode;
    private String message;
    private String exceptionClass;

    private ErrorRequestContext requestContext;

    private Long userId;
    private String correlationId;

    private LocalDateTime createdAt;

    public ErrorLog() {}

    public ErrorLog(Long id, ErrorCode errorCode, String message, String exceptionClass,
                    ErrorRequestContext requestContext, Long userId, String correlationId, LocalDateTime createdAt) {
        this.id = id;
        this.errorCode = Objects.requireNonNull(errorCode, "Código de erro não pode ser nulo");
        this.message = requireNonBlank(message, "Mensagem do erro não pode ser nulo ou vazio");
        this.exceptionClass = requireNonBlank(exceptionClass, "Classe da exceção não pode ser nulo ou vazio");
        this.requestContext = Objects.requireNonNull(requestContext, "Contexto da requisição não pode ser nulo");
        this.userId = requirePositiveIfPresent(userId);
        this.correlationId = requireNonBlank(correlationId, "Correlation id não pode ser nulo ou vazio");
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação do erro não pode ser nulo");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = Objects.requireNonNull(errorCode, "Código de erro não pode ser nulo");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = requireNonBlank(message, "Mensagem do erro não pode ser nula ou vazia");
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = requireNonBlank(exceptionClass, "Classe da exceção não pode ser nula ou vazia");
    }

    public ErrorRequestContext getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(ErrorRequestContext requestContext) {
        this.requestContext = Objects.requireNonNull(requestContext, "Contexto da requisição não pode ser nulo");
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = requirePositiveIfPresent(userId);
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = requireNonBlank(correlationId, "Correlation id não pode ser nulo ou vazio");
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação do erro não pode ser nula");
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    private Long requirePositiveIfPresent(Long value) {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Id do usuário deve ser positivo");
        }

        return value;
    }
}
