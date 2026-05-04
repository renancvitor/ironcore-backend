package com.ironcore.application.logging.error.port;

import com.ironcore.domain.logging.error.enums.ErrorCodeType;

public interface ErrorLogPublisher {

    void publish(
            ErrorCodeType errorCode,
            String message,
            String exceptionClass,
            String path,
            String httpMethod,
            Long userId,
            String correlationId
    );
}
