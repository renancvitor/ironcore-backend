package com.ironcore.application.bodymetrics.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.bodymetrics.BodyMetricsAuditData;
import com.ironcore.application.bodymetrics.delete.DeleteBodyMetricsCommand;
import com.ironcore.application.bodymetrics.delete.DeleteBodyMetricsUseCase;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import com.ironcore.domain.bodymetrics.valueobject.BMI;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteBodyMetricsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BodyMetricsRepository bodyMetricsRepository;

    @Mock
    private AuditLogPublisher auditLogPublisher;

    private DeleteBodyMetricsUseCase deleteBodyMetricsUseCase;

    @BeforeEach
    void setUp() {
        deleteBodyMetricsUseCase = new DeleteBodyMetricsUseCase(
                userRepository,
                bodyMetricsRepository,
                auditLogPublisher
        );
    }

    @Nested
    class SuccessfulDelete {

        @Test
        void shouldDeleteUserBodyMetrics() {
            User user = activeUser();
            BodyMetricsId bodyMetricsId = new BodyMetricsId(1L);
            DeleteBodyMetricsCommand command = new DeleteBodyMetricsCommand(bodyMetricsId, user.getId());
            BodyMetrics bodyMetrics = new BodyMetrics(
                    bodyMetricsId,
                    new UserId(1L),
                    LocalDateTime.of(2026, 5, 14, 10, 0),
                    new BodyWeightKg(65.0),
                    new BodyHeightCm(167.0),
                    null,
                    new BMI(23.0),
                    null,
                    null,
                    null,
                    null,
                    "TEXT"
            );

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(bodyMetricsRepository.findByIdAndUserId(bodyMetricsId, user.getId()))
                    .thenReturn(Optional.of(bodyMetrics));

            ArgumentCaptor<LoggableData> auditBeforeCaptor = ArgumentCaptor.forClass(LoggableData.class);

            deleteBodyMetricsUseCase.execute(command);

            verify(userRepository).findById(user.getId());
            verify(bodyMetricsRepository).findByIdAndUserId(bodyMetricsId, user.getId());
            verify(bodyMetricsRepository).deleteById(bodyMetricsId);
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.DELETE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(bodyMetricsId.value()),
                    auditBeforeCaptor.capture(),
                    isNull()
            );

            BodyMetricsAuditData auditBeforeState = (BodyMetricsAuditData) auditBeforeCaptor.getValue();

            assertThat(auditBeforeState.id()).isEqualTo(bodyMetrics.getId().value());
            assertThat(auditBeforeState.userId()).isEqualTo(bodyMetrics.getUserId().value());
            assertThat(auditBeforeState.measuredAt()).isEqualTo(bodyMetrics.getMeasuredAt());
            assertThat(auditBeforeState.weightKg()).isEqualTo(65.0);
            assertThat(auditBeforeState.heightCm()).isEqualTo(167.0);
            assertThat(auditBeforeState.bmi()).isEqualTo(23.0);
            assertThat(auditBeforeState.notes()).isEqualTo("TEXT");
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            User user = activeUser();
            BodyMetricsId bodyMetricsId = new BodyMetricsId(1L);
            DeleteBodyMetricsCommand command = new DeleteBodyMetricsCommand(bodyMetricsId, user.getId());

            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> deleteBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(user.getId());
            verify(bodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(bodyMetricsRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            BodyMetricsId bodyMetricsId = new BodyMetricsId(1L);
            DeleteBodyMetricsCommand command = new DeleteBodyMetricsCommand(bodyMetricsId, user.getId());

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> deleteBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(user.getId());
            verify(bodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(bodyMetricsRepository, never()).deleteById(any());
        }
    }

    @Nested
    class BodyMetricsValidation {

        @Test
        void shouldFailWhenBodyMetricsDoesNotExistForUser() {
            User user = activeUser();
            BodyMetricsId bodyMetricsId = new BodyMetricsId(1L);
            DeleteBodyMetricsCommand command = new DeleteBodyMetricsCommand(bodyMetricsId, user.getId());

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(bodyMetricsRepository.findByIdAndUserId(bodyMetricsId, user.getId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> deleteBodyMetricsUseCase.execute(command))
                    .withMessage("Métricas corporais não encontradas.");

            verify(userRepository).findById(user.getId());
            verify(bodyMetricsRepository).findByIdAndUserId(bodyMetricsId, user.getId());
            verify(bodyMetricsRepository, never()).deleteById(any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }
    }
}
