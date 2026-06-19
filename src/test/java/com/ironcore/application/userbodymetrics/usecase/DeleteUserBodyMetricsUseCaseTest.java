package com.ironcore.application.userbodymetrics.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.userbodymetrics.UserBodyMetricsAuditData;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsUseCase;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import com.ironcore.domain.userbodymetrics.valueobject.BMI;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;
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
public class DeleteUserBodyMetricsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBodyMetricsRepository userBodyMetricsRepository;

    @Mock
    private AuditLogPublisher auditLogPublisher;

    private DeleteUserBodyMetricsUseCase deleteUserBodyMetricsUseCase;

    @BeforeEach
    void setUp() {
        deleteUserBodyMetricsUseCase = new DeleteUserBodyMetricsUseCase(
                userRepository,
                userBodyMetricsRepository,
                auditLogPublisher
        );
    }

    @Nested
    class SuccessfulDelete {

        @Test
        void shouldDeleteUserBodyMetrics() {
            User user = activeUser();
            UserBodyMetricsId userBodyMetricsId = new UserBodyMetricsId(1L);
            UserBodyMetrics bodyMetrics = new UserBodyMetrics(
                    userBodyMetricsId,
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
            when(userBodyMetricsRepository.findByIdAndUserId(userBodyMetricsId, user.getId()))
                    .thenReturn(Optional.of(bodyMetrics));

            ArgumentCaptor<LoggableData> auditBeforeCaptor = ArgumentCaptor.forClass(LoggableData.class);

            deleteUserBodyMetricsUseCase.execute(userBodyMetricsId, user.getId());

            verify(userRepository).findById(user.getId());
            verify(userBodyMetricsRepository).findByIdAndUserId(userBodyMetricsId, user.getId());
            verify(userBodyMetricsRepository).deleteById(userBodyMetricsId);
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.DELETE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(userBodyMetricsId.value()),
                    auditBeforeCaptor.capture(),
                    isNull()
            );

            UserBodyMetricsAuditData auditBeforeState = (UserBodyMetricsAuditData) auditBeforeCaptor.getValue();

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
            UserBodyMetricsId userBodyMetricsId = new UserBodyMetricsId(1L);

            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> deleteUserBodyMetricsUseCase.execute(userBodyMetricsId, user.getId()))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(user.getId());
            verify(userBodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(userBodyMetricsRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            UserBodyMetricsId userBodyMetricsId = new UserBodyMetricsId(1L);

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> deleteUserBodyMetricsUseCase.execute(userBodyMetricsId, user.getId()))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(user.getId());
            verify(userBodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(userBodyMetricsRepository, never()).deleteById(any());
        }
    }

    @Nested
    class BodyMetricsValidation {

        @Test
        void shouldFailWhenBodyMetricsDoesNotExistForUser() {
            User user = activeUser();
            UserBodyMetricsId userBodyMetricsId = new UserBodyMetricsId(1L);

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(userBodyMetricsRepository.findByIdAndUserId(userBodyMetricsId, user.getId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> deleteUserBodyMetricsUseCase.execute(userBodyMetricsId, user.getId()))
                    .withMessage("Métricas corporais não encontradas.");

            verify(userRepository).findById(user.getId());
            verify(userBodyMetricsRepository).findByIdAndUserId(userBodyMetricsId, user.getId());
            verify(userBodyMetricsRepository, never()).deleteById(any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }
    }
}
