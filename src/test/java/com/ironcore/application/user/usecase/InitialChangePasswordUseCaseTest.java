package com.ironcore.application.user.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.user.service.UserPasswordChangeService;
import com.ironcore.application.user.usecase.initialchangepassword.InitialChangePasswordCommand;
import com.ironcore.application.user.usecase.initialchangepassword.InitialChangePasswordUseCase;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.RawPassword;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Consumer;

import static com.ironcore.domain.user.UserTestFactory.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitialChangePasswordUseCaseTest {

    @Mock
    private UserPasswordChangeService userPasswordChangeService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InitialChangePasswordUseCase initialChangePasswordUseCase;

    @Nested
    class InitialPasswordChange {

        @Test
        void shouldChangeInitialPassword() {
            InitialChangePasswordCommand command = command();
            User user = activeUserWithMustChangePasswordTrue();

            when(userRepository.findByEmail(command.email()))
                    .thenReturn(Optional.of(user));
            when(userPasswordChangeService.changePassword(
                    eq(user),
                    eq(command.currentPassword()),
                    eq(command.newPassword()),
                    eq(command.confirmPassword()),
                    any()))
                    .thenReturn(user);

            initialChangePasswordUseCase.execute(command);

            verify(userRepository).findByEmail(command.email());
            verify(userPasswordChangeService).changePassword(
                    eq(user),
                    eq(command.currentPassword()),
                    eq(command.newPassword()),
                    eq(command.confirmPassword()),
                    any());
        }
    }

    @Nested
    class InitialPasswordChangeRequirement {

        @Test
        @SuppressWarnings("unchecked")
        void shouldRequirePendingInitialPasswordChange() {
            InitialChangePasswordCommand command = command();
            User user = activeUserWithMustChangePasswordTrue();

            when(userRepository.findByEmail(command.email()))
                    .thenReturn(Optional.of(user));
            when(userPasswordChangeService.changePassword(
                    eq(user),
                    eq(command.currentPassword()),
                    eq(command.newPassword()),
                    eq(command.confirmPassword()),
                    any()))
                    .thenReturn(user);

            initialChangePasswordUseCase.execute(command);

            ArgumentCaptor<Consumer<User>> captor = ArgumentCaptor.forClass(Consumer.class);
            verify(userPasswordChangeService).changePassword(
                    eq(user),
                    eq(command.currentPassword()),
                    eq(command.newPassword()),
                    eq(command.confirmPassword()),
                    captor.capture());

            Consumer<User> beforePasswordChange = captor.getValue();

            assertThatCode(() -> beforePasswordChange.accept(activeUserWithMustChangePasswordTrue()))
                    .doesNotThrowAnyException();
            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> beforePasswordChange.accept(activeUser()))
                    .withMessage("A troca inicial de senha não é mais obrigatória.");
        }
    }

    private InitialChangePasswordCommand command() {
        return new InitialChangePasswordCommand(
                new Email("renan@example.com"),
                new RawPassword("StrongOldPassword"),
                new RawPassword("StrongNewPassword"),
                new RawPassword("StrongNewPassword")
        );
    }
}
