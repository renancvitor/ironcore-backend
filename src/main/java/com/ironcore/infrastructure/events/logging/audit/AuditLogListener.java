package com.ironcore.infrastructure.events.logging.audit;

import com.ironcore.application.logging.audit.event.AuditLogEvent;
import com.ironcore.application.logging.audit.service.AuditLogApplicationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuditLogListener {

    private static final Logger log = LoggerFactory.getLogger(AuditLogListener.class);

    private final AuditLogApplicationService auditLogApplicationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AuditLogEvent event) {
        try {
            auditLogApplicationService.register(event);
        } catch (Exception exception) {
            log.error(
                    "Falha ao persistir audit log: action={}, targetType={}, targetId={}",
                    event.action(),
                    event.targetType(),
                    event.targetId(),
                    exception
            );
        }
    }
}
