package com.ironcore.application.logging.audit.event;

import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;

import java.time.LocalDateTime;

public record AuditLogEvent(
        AuditActionType action,
        Long actorUserId,
        String actorEmail,
        AuditTargetType targetType,
        Long targetId,
        String beforeStateJson,
        String afterStateJson,
        LocalDateTime createdAt
) {
}
