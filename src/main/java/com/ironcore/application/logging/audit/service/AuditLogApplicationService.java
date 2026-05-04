package com.ironcore.application.logging.audit.service;

import com.ironcore.application.logging.audit.event.AuditLogEvent;
import com.ironcore.domain.logging.audit.model.AuditLog;
import com.ironcore.domain.logging.audit.repository.AuditLogRepository;
import com.ironcore.domain.logging.audit.valueobject.AuditAction;
import com.ironcore.domain.logging.audit.valueobject.AuditActor;
import com.ironcore.domain.logging.audit.valueobject.AuditSnapshot;
import com.ironcore.domain.logging.audit.valueobject.AuditTarget;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogApplicationService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void register(AuditLogEvent event) {
        AuditLog auditLog = new AuditLog(
                null,
                new AuditActor(new UserId(event.actorUserId()), new Email(event.actorEmail())),
                new AuditAction(event.action()),
                new AuditTarget(event.targetType(), event.targetId()),
                snapshot(event.beforeStateJson()),
                snapshot(event.afterStateJson()),
                event.createdAt()
        );

        auditLogRepository.save(auditLog);
    }

    private AuditSnapshot snapshot(String value) {
        return value == null ? null : new AuditSnapshot(value);
    }
}
