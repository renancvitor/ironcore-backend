package com.ironcore.application.logging.audit.port;

import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;

public interface AuditLogPublisher {

    void publish(
            AuditActionType action,
            Long actorUserId,
            String actorEmail,
            AuditTargetType targetType,
            Long targetId,
            LoggableData beforeState,
            LoggableData afterState
    );
}
