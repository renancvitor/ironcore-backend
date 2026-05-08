package com.ironcore.infrastructure.events.logging.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.logging.audit.event.AuditLogEvent;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.infrastructure.exception.JsonSerializationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuditLogPublisherService implements AuditLogPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuditLogPublisherService.class);

    private final ApplicationEventPublisher publisher;
    private final ObjectMapper mapper;

    @Override
    public void publish(
            AuditActionType action,
            Long actorUserId,
            String actorEmail,
            AuditTargetType targetType,
            Long targetId,
            LoggableData beforeState,
            LoggableData afterState
    ) {
        String beforeStateJson = toJsonOrNull(beforeState);
        String afterStateJson = toJsonOrNull(afterState);

        AuditLogEvent event = new AuditLogEvent(
                action,
                actorUserId,
                actorEmail,
                targetType,
                targetId,
                beforeStateJson,
                afterStateJson,
                LocalDateTime.now()
        );

        publisher.publishEvent(event);

        log.info(
                "Audit event published: action={}, actionUserId={}, targetType={}, targetId={}",
                action,
                actorUserId,
                targetType,
                targetId
        );
    }

    private String toJsonOrNull(LoggableData data) {
        if (data == null) {
            return null;
        }

        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException exception) {
            throw new JsonSerializationException("Falha ao serializar audit log.", exception);
        }
    }
}
