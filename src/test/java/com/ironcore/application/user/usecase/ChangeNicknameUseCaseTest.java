package com.ironcore.application.user.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.user.UserAuditData;
import com.ironcore.application.user.usecase.update.ChangeNicknameCommand;
import com.ironcore.application.user.usecase.update.ChangeNicknameResult;
import com.ironcore.application.user.usecase.update.ChangeNicknameUseCase;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.user.exception.InvalidUserException;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeNicknameUseCaseTest {

    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 6, 20, 10, 0);

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogPublisher auditLogPublisher;

    @Mock
    private Clock clock;

    private ChangeNicknameUseCase changeNicknameUseCase;

    @BeforeEach
    void setUp() {
        changeNicknameUseCase = new ChangeNicknameUseCase(
                userRepository,
                auditLogPublisher,
                clock
        );
    }

    @Nested
    class SuccessfulChange {

        @Test
        void shouldChangeNickname() {
            User user = activeUser();
            ChangeNicknameCommand command = new ChangeNicknameCommand(
                    new UserId(1L),
                    " Novo Apelido "
            );

            givenFixedClock();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            givenUpdatedUserIsPersisted();

            ChangeNicknameResult result = changeNicknameUseCase.execute(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<LoggableData> auditBeforeStateCaptor = ArgumentCaptor.forClass(LoggableData.class);
            ArgumentCaptor<LoggableData> auditAfterStateCaptor = ArgumentCaptor.forClass(LoggableData.class);

            verify(userRepository).findById(command.actorUserId());
            verify(userRepository).save(userCaptor.capture());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER),
                    eq(user.getId().value()),
                    auditBeforeStateCaptor.capture(),
                    auditAfterStateCaptor.capture()
            );

            User savedUser = userCaptor.getValue();
            UserAuditData auditBeforeState = (UserAuditData) auditBeforeStateCaptor.getValue();
            UserAuditData auditAfterState = (UserAuditData) auditAfterStateCaptor.getValue();

            assertThat(savedUser.getId()).isEqualTo(user.getId());
            assertThat(savedUser.getNickname()).isEqualTo("Novo Apelido");
            assertThat(savedUser.getUpdatedAt()).isEqualTo(UPDATED_AT);

            assertThat(result.nickname()).isEqualTo("Novo Apelido");

            assertThat(auditBeforeState.id()).isEqualTo(user.getId().value());
            assertThat(auditBeforeState.nickname()).isEqualTo("Renan");

            assertThat(auditAfterState.id()).isEqualTo(user.getId().value());
            assertThat(auditAfterState.nickname()).isEqualTo("Novo Apelido");
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            ChangeNicknameCommand command = commandWithNickname();

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> changeNicknameUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.actorUserId());
            verify(userRepository, never()).save(any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            ChangeNicknameCommand command = commandWithNickname();

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> changeNicknameUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.actorUserId());
            verify(userRepository, never()).save(any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    class NicknameValidation {

        @Test
        void shouldFailWhenNicknameIsNull() {
            User user = activeUser();
            ChangeNicknameCommand command = new ChangeNicknameCommand(
                    new UserId(1L),
                    null
            );

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> changeNicknameUseCase.execute(command))
                    .withMessage("Informe o apelido para atualização.");

            verify(userRepository).findById(command.actorUserId());
            verify(userRepository, never()).save(any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldFailWhenNicknameIsBlank() {
            User user = activeUser();
            ChangeNicknameCommand command = new ChangeNicknameCommand(
                    new UserId(1L),
                    " "
            );

            givenFixedClock();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> changeNicknameUseCase.execute(command))
                    .withMessage("Apelido não pode ser nulo ou vazio.");

            verify(userRepository).findById(command.actorUserId());
            verify(userRepository, never()).save(any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }
    }

    private ChangeNicknameCommand commandWithNickname() {
        return new ChangeNicknameCommand(
                new UserId(1L),
                "Novo Apelido"
        );
    }

    private void givenFixedClock() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-06-20T10:00:00Z"),
                ZoneOffset.UTC
        );

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    private void givenUpdatedUserIsPersisted() {
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
