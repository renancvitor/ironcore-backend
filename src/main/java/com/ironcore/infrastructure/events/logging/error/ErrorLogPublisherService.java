package com.ironcore.infrastructure.events.logging.error;

import com.ironcore.application.logging.error.event.ErrorLogEvent;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.domain.logging.error.enums.ErrorCodeType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ErrorLogPublisherService implements ErrorLogPublisher {

    private static final Logger log = LoggerFactory.getLogger(ErrorLogPublisherService.class);

    private final ApplicationEventPublisher publisher;
    private final Clock clock;

    @Override
    public void publish(
            ErrorCodeType errorCode,
            String message,
            String exceptionClass,
            String path,
            String httpMethod,
            Long userId,
            String correlationId
    ) {
        ErrorLogEvent event = new ErrorLogEvent(
                errorCode,
                message,
                exceptionClass,
                path,
                httpMethod,
                userId,
                correlationId,
                LocalDateTime.now(clock)
        );

        publisher.publishEvent(event);

        log.info(
                "Error event published: errorCode={}, message={}, httpMethod={}",
                event.errorCode(),
                event.message(),
                event.httpMethod()
        );
    }
}
