package com.ironcore.application.logging.error.event;

import com.ironcore.domain.logging.error.enums.ErrorCodeType;

import java.time.LocalDateTime;

public record ErrorLogEvent(
        ErrorCodeType errorCode,
        String message,
        String exceptionClass,
        String path,
        String httpMethod,
        Long userId,
        String correlationId,
        LocalDateTime createdAt
) {
}
