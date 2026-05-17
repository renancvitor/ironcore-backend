package com.ironcore.application.auth.usecase;

import com.ironcore.application.auth.port.AccessTokenGenerator;
import com.ironcore.application.auth.port.AccessTokenSubject;
import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.user.service.PasswordHashingService;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.RawPassword;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ironcore.application.auth.LoginTestFactory.activeUser;
import static com.ironcore.application.auth.LoginTestFactory.command;
import static com.ironcore.application.auth.LoginTestFactory.generatedAccessToken;
import static com.ironcore.application.auth.LoginTestFactory.inactiveUser;
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
    private AccessTokenGenerator accessTokenGenerator;

    @InjectMocks
    private LoginUseCase useCase;

    @Nested
    class SuccessfulLogin {

        @Test
        void shouldLoginAndReturnAuthenticationData() {
            LoginCommand command = command();
            User user = activeUser();
            GeneratedAccessToken accessToken = generatedAccessToken();

            when(userRepository.findByEmail(command.email()))
                    .thenReturn(Optional.of(user));
            when(passwordHashingService.matches(new RawPassword(command.rawPassword()), user.getPasswordHash()))
                    .thenReturn(true);
            when(accessTokenGenerator.generate(new AccessTokenSubject(user.getId(), user.getEmail())))
                    .thenReturn(accessToken);

            LoginResult result = useCase.execute(command);

            assertThat(result.accessToken()).isEqualTo(accessToken.value());
            assertThat(result.tokenType()).isEqualTo(accessToken.tokenType());
            assertThat(result.expiresAt()).isEqualTo(accessToken.expiresAt());
            assertThat(result.userId()).isEqualTo(user.getId());
            assertThat(result.email()).isEqualTo(user.getEmail());
            assertThat(result.name()).isEqualTo(user.getName());
            assertThat(result.mustChangePassword()).isEqualTo(user.mustChangePassword());

            ArgumentCaptor<AccessTokenSubject> subjectCaptor = ArgumentCaptor.forClass(AccessTokenSubject.class);

            verify(userRepository).findByEmail(command.email());
            verify(passwordHashingService).matches(new RawPassword(command.rawPassword()), user.getPasswordHash());
            verify(accessTokenGenerator).generate(subjectCaptor.capture());

            AccessTokenSubject subject = subjectCaptor.getValue();

            assertThat(subject.userId()).isEqualTo(user.getId());
            assertThat(subject.email()).isEqualTo(user.getEmail());
        }
    }

    @Nested
    class CredentialFailures {

        @Test
        void shouldFailWhenEmailDoesNotExist() {
            LoginCommand command = command();

            when(userRepository.findByEmail(command.email()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Credenciais inválidas.");

            verify(userRepository).findByEmail(command.email());
            verify(passwordHashingService, never()).matches(any(), any());
            verify(accessTokenGenerator, never()).generate(any());
        }

        @Test
        void shouldFailWhenPasswordDoesNotMatch() {
            LoginCommand command = command();
            User user = activeUser();

            when(userRepository.findByEmail(command.email()))
                    .thenReturn(Optional.of(user));
            when(passwordHashingService.matches(new RawPassword(command.rawPassword()), user.getPasswordHash()))
                    .thenReturn(false);

            assertThatExceptionOfType(InvalidCredentialsException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Credenciais inválidas.");

            verify(userRepository).findByEmail(command.email());
            verify(passwordHashingService).matches(new RawPassword(command.rawPassword()), user.getPasswordHash());
            verify(accessTokenGenerator, never()).generate(any());
        }
    }

    @Nested
    class UserStatusValidation {

        @Test
        void shouldFailWhenUserIsInactive() {
            LoginCommand command = command();
            User user = inactiveUser();

            when(userRepository.findByEmail(command.email()))
                    .thenReturn(Optional.of(user));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findByEmail(command.email());
            verify(passwordHashingService, never()).matches(any(), any());
            verify(accessTokenGenerator, never()).generate(any());
        }
    }
}
