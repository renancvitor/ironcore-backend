package com.ironcore.application.user.service;

import com.ironcore.application.exception.BusinessRuleViolationException;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.user.usecase.changepassword.ChangePasswordCommand;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.PasswordHash;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.ironcore.application.user.ChangePasswordTestFactory.command;
import static com.ironcore.application.user.ChangePasswordTestFactory.commandWithDifferentPasswordConfirmation;
import static com.ironcore.domain.user.UserTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPasswordChangeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHashingService passwordHashingService;

    @Mock
    private Clock clock;

    @InjectMocks
    private UserPasswordChangeService userPasswordChangeService;

    @Nested
    class PasswordChange {

        @Test
        void shouldChangePasswordSuccessfully() {
            ChangePasswordCommand command = command();
            User user = activeUser();
            PasswordHash newPasswordHash = new PasswordHash("new-hashed-password");

            Clock fixedClock = Clock.fixed(
                    Instant.parse("2026-05-23T10:00:00Z"),
                    ZoneOffset.UTC
            );
            LocalDateTime updatedAt = LocalDateTime.of(2026, 5, 23, 10, 0);

            when(clock.instant()).thenReturn(fixedClock.instant());
            when(clock.getZone()).thenReturn(fixedClock.getZone());

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(passwordHashingService.matches(command.currentPassword(), user.getPasswordHash()))
                    .thenReturn(true);
            when(passwordHashingService.matches(command.newPassword(), user.getPasswordHash()))
                    .thenReturn(false);
            when(passwordHashingService.hash(command.newPassword()))
                    .thenReturn(newPasswordHash);
            when(userRepository.save(user)).thenReturn(user);

            User result = userPasswordChangeService.changePassword(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).findById(command.userId());
            verify(passwordHashingService).matches(command.currentPassword(), passwordHash("hashed-password"));
            verify(passwordHashingService).matches(command.newPassword(), passwordHash("hashed-password"));
            verify(passwordHashingService).hash(command.newPassword());
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();

            assertThat(result).isSameAs(savedUser);
            assertThat(savedUser.getPasswordHash()).isEqualTo(newPasswordHash);
            assertThat(savedUser.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(savedUser.mustChangePassword()).isFalse();
        }

        @Test
        void shouldRunBeforePasswordChangeRuleBeforePasswordValidation() {
            ChangePasswordCommand command = command();
            User user = activeUser();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> userPasswordChangeService.changePassword(
                            command,
                            currentUser -> {
                                throw new OperationNotAllowedException("Operação não permitida.");
                            }
                    ))
                    .withMessage("Operação não permitida.");

            verify(userRepository).findById(command.userId());
            verify(passwordHashingService, never()).matches(any(), any());
            verify(passwordHashingService, never()).hash(any());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserNotFound() {
            ChangePasswordCommand command = command();

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> userPasswordChangeService.changePassword(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.userId());
            verify(passwordHashingService, never()).matches(any(), any());
            verify(passwordHashingService, never()).hash(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenUserIsNotActive() {
            ChangePasswordCommand command = command();
            User user = inactiveUser();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> userPasswordChangeService.changePassword(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
            verify(passwordHashingService, never()).matches(any(), any());
            verify(passwordHashingService, never()).hash(any());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class PasswordValidation {

        @Test
        void shouldFailWhenCurrentPasswordDoesNotMatch() {
            ChangePasswordCommand command = command();
            User user = activeUser();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(passwordHashingService.matches(command.currentPassword(), user.getPasswordHash()))
                    .thenReturn(false);

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> userPasswordChangeService.changePassword(command))
                    .withMessage("Senha atual inválida.");

            verify(userRepository).findById(command.userId());
            verify(passwordHashingService).matches(command.currentPassword(), user.getPasswordHash());
            verify(passwordHashingService, never()).hash(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenNewPasswordEqualsCurrentPassword() {
            ChangePasswordCommand command = command();
            User user = activeUser();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(passwordHashingService.matches(command.currentPassword(), user.getPasswordHash()))
                    .thenReturn(true);
            when(passwordHashingService.matches(command.newPassword(), user.getPasswordHash()))
                    .thenReturn(true);

            assertThatExceptionOfType(BusinessRuleViolationException.class)
                    .isThrownBy(() -> userPasswordChangeService.changePassword(command))
                    .withMessage("A nova senha deve ser diferente da senha atual.");

            verify(userRepository).findById(command.userId());
            verify(passwordHashingService).matches(command.currentPassword(), user.getPasswordHash());
            verify(passwordHashingService).matches(command.newPassword(), user.getPasswordHash());
            verify(passwordHashingService, never()).hash(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenNewPasswordDoesNotMatchConfirmation() {
            ChangePasswordCommand command = commandWithDifferentPasswordConfirmation();
            User user = activeUser();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(BusinessRuleViolationException.class)
                    .isThrownBy(() -> userPasswordChangeService.changePassword(command))
                    .withMessage("A confirmação de senha não confere.");

            verify(userRepository).findById(command.userId());
            verify(passwordHashingService, never()).matches(any(), any());
            verify(passwordHashingService, never()).hash(any());
            verify(userRepository, never()).save(any());
        }
    }
}
