package com.ironcore.application.user.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.user.service.PasswordHashingService;
import com.ironcore.application.user.usecase.bootstrap.BootstrapSingleUserCommand;
import com.ironcore.application.user.usecase.bootstrap.BootstrapSingleUserUseCase;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.RawPassword;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ironcore.application.user.BootstrapSingleUserTestFactory.CREATED_AT;
import static com.ironcore.application.user.BootstrapSingleUserTestFactory.command;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootstrapSingleUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHashingService passwordHashingService;

    @InjectMocks
    private BootstrapSingleUserUseCase useCase;

    @Nested
    class Idempotency {

        @Test
        void shouldDoNothingWhenConfiguredUserAlreadyExists() {
            BootstrapSingleUserCommand command = command();

            when(userRepository.existsByEmail(command.email())).thenReturn(true);

            useCase.execute(command);

            verify(userRepository).existsByEmail(command.email());
            verify(userRepository, never()).existsAny();
            verify(passwordHashingService, never()).hash(any());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class SingleUserViolation {

        @Test
        void shouldFailWhenAnotherUserAlreadyExists() {
            BootstrapSingleUserCommand command = command();

            when(userRepository.existsByEmail(command.email())).thenReturn(false);
            when(userRepository.existsAny()).thenReturn(true);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Bootstrap de usuário único não pode criar outro usuário.");

            verify(userRepository).existsAny();
            verify(passwordHashingService, never()).hash(any());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class BootstrapCreation {

        @Test
        void shouldCreateSingleUserWhenNoUserExists() {
            BootstrapSingleUserCommand command = command();
            PasswordHash passwordHash = new PasswordHash("hashed-password");

            when(userRepository.existsByEmail(command.email())).thenReturn(false);
            when(userRepository.existsAny()).thenReturn(false);
            when(passwordHashingService.hash(new RawPassword(command.rawPassword())))
                    .thenReturn(passwordHash);

            useCase.execute(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).existsAny();
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getNickname()).isEqualTo("Renan");
            assertThat(savedUser.getEmail()).isEqualTo(new Email("renan@example.com"));
            assertThat(savedUser.getPasswordHash()).isEqualTo(passwordHash);
            assertThat(savedUser.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(savedUser.isActive()).isTrue();
            assertThat(savedUser.mustChangePassword()).isTrue();
        }
    }
}
