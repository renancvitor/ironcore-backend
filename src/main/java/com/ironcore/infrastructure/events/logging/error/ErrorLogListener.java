package com.ironcore.infrastructure.events.logging.error;

import com.ironcore.application.logging.error.event.ErrorLogEvent;
import com.ironcore.application.logging.error.service.ErrorLogApplicationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ErrorLogListener {

    private static final Logger log = LoggerFactory.getLogger(ErrorLogListener.class);

    private final ErrorLogApplicationService errorLogApplicationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION, fallbackExecution = true)
    public void handle(ErrorLogEvent event) {
        try {
            errorLogApplicationService.register(event);
        } catch (Exception exception) {
            log.error(
                    "Falha ao persistir error log: errorCode={}, message={}, httpMethod={}",
                    event.errorCode(),
                    event.message(),
                    event.httpMethod(),
                    exception
            );
        }
    }
}
