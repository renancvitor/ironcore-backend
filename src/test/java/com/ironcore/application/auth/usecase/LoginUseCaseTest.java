package com.ironcore.application.auth.usecase;

import com.ironcore.application.auth.LoginTestFactory;
import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.auth.service.AuthSessionIssuer;
import com.ironcore.application.exception.InitialPasswordChangeRequiredException;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.user.service.PasswordHashingService;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.RawPassword;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ironcore.application.auth.LoginTestFactory.command;
import static com.ironcore.application.auth.LoginTestFactory.generatedAccessToken;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.activeUserWithMustChangePasswordTrue;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHashingService passwordHashingService;

    @Mock
    private AuthSessionIssuer authSessionIssuer;

    @InjectMocks
    private LoginUseCase useCase;

    @Nested
    class SuccessfulLogin {

        @Test
        void shouldLoginAndReturnAuthenticationData() {
            LoginCommand command = command();
            User user = activeUser();
            GeneratedAccessToken accessToken = generatedAccessToken();

            when(userRepository.findByEmailForUpdate(command.email()))
                    .thenReturn(Optional.of(user));
            when(passwordHashingService.matches(new RawPassword(command.rawPassword()), user.getPasswordHash()))
                    .thenReturn(true);
            var credentials = LoginTestFactory.sessionCredentials();
            when(authSessionIssuer.issue(user)).thenReturn(credentials);

            LoginResult result = useCase.execute(command);

            assertThat(result.credentials().accessToken().value()).isEqualTo(accessToken.value());
            assertThat(result.credentials().accessToken().tokenType()).isEqualTo(accessToken.tokenType());
            assertThat(result.credentials().accessToken().expiresAt()).isEqualTo(accessToken.expiresAt());
            assertThat(result.userId()).isEqualTo(user.getId());
            assertThat(result.email()).isEqualTo(user.getEmail());
            assertThat(result.nickname()).isEqualTo(user.getNickname());
            assertThat(result.mustChangePassword()).isEqualTo(user.mustChangePassword());

            verify(authSessionIssuer).issue(user);
        }
    }

    @Nested
    class CredentialFailures {

        @Test
        void shouldFailWhenEmailDoesNotExist() {
            LoginCommand command = command();

            when(userRepository.findByEmailForUpdate(command.email()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Credenciais inválidas.");

            verify(userRepository).findByEmailForUpdate(command.email());
            verify(passwordHashingService, never()).matches(any(), any());
            verify(authSessionIssuer, never()).issue(any());
        }

        @Test
        void shouldFailWhenPasswordDoesNotMatch() {
            LoginCommand command = command();
            User user = activeUser();

            when(userRepository.findByEmailForUpdate(command.email()))
                    .thenReturn(Optional.of(user));
            when(passwordHashingService.matches(new RawPassword(command.rawPassword()), user.getPasswordHash()))
                    .thenReturn(false);

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Credenciais inválidas.");

            verify(userRepository).findByEmailForUpdate(command.email());
            verify(passwordHashingService).matches(new RawPassword(command.rawPassword()), user.getPasswordHash());
            verify(authSessionIssuer, never()).issue(any());
        }
    }

    @Nested
    class UserStatusValidation {

        @Test
        void shouldFailWhenUserIsInactive() {
            LoginCommand command = command();
            User user = inactiveUser();

            when(userRepository.findByEmailForUpdate(command.email()))
                    .thenReturn(Optional.of(user));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findByEmailForUpdate(command.email());
            verify(passwordHashingService, never()).matches(any(), any());
            verify(authSessionIssuer, never()).issue(any());
        }

        @Test
        void shouldRequireInitialPasswordChangeWhenCredentialsAreValid() {
            LoginCommand command = command();
            User user = activeUserWithMustChangePasswordTrue();

            when(userRepository.findByEmailForUpdate(command.email()))
                    .thenReturn(Optional.of(user));
            when(passwordHashingService.matches(new RawPassword(command.rawPassword()), user.getPasswordHash()))
                    .thenReturn(true);

            assertThatExceptionOfType(InitialPasswordChangeRequiredException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Troca de senha inicial obrigatória.");

            verify(userRepository).findByEmailForUpdate(command.email());
            verify(passwordHashingService).matches(new RawPassword(command.rawPassword()), user.getPasswordHash());
            verify(authSessionIssuer, never()).issue(any());
        }
    }
}
