package com.ironcore.application.logging.audit.service;

import com.ironcore.application.logging.audit.event.AuditLogEvent;
import com.ironcore.domain.logging.audit.model.AuditLog;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.logging.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogApplicationServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogApplicationService auditLogApplicationService;

    @Test
    void shouldRegisterAuditLogFromEvent() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 1, 16, 10);
        AuditLogEvent event = new AuditLogEvent(
                AuditActionType.UPDATE,
                1L,
                "actor@ironcore.com",
                AuditTargetType.USER_BODY_METRICS,
                10L,
                "{\"weightKg\":80.0}",
                "{\"weightKg\":82.0}",
                createdAt
        );

        auditLogApplicationService.register(event);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog auditLog = captor.getValue();
        assertThat(auditLog.getId()).isNull();
        assertThat(auditLog.getActor().userId().value()).isEqualTo(1L);
        assertThat(auditLog.getActor().email().value()).isEqualTo("actor@ironcore.com");
        assertThat(auditLog.getAction().type()).isEqualTo(AuditActionType.UPDATE);
        assertThat(auditLog.getTarget().type()).isEqualTo(AuditTargetType.USER_BODY_METRICS);
        assertThat(auditLog.getTarget().id()).isEqualTo(10L);
        assertThat(auditLog.getBeforeState().value()).isEqualTo("{\"weightKg\":80.0}");
        assertThat(auditLog.getAfterState().value()).isEqualTo("{\"weightKg\":82.0}");
        assertThat(auditLog.getCreatedAt()).isEqualTo(createdAt);
    }
}
