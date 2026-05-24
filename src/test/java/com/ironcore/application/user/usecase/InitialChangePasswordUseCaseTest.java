package com.ironcore.application.user.usecase;

import com.ironcore.application.auth.port.AccessTokenGenerator;
import com.ironcore.application.auth.port.AccessTokenSubject;
import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.user.service.UserPasswordChangeService;
import com.ironcore.domain.user.model.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static com.ironcore.application.user.ChangePasswordTestFactory.command;
import static com.ironcore.domain.user.UserTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
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
    private AccessTokenGenerator accessTokenGenerator;

    @InjectMocks
    private InitialChangePasswordUseCase initialChangePasswordUseCase;

    @Nested
    class InitialPasswordChange {

        @Test
        void shouldChangeInitialPasswordAndReturnNewAccessToken() {
            ChangePasswordCommand command = command();
            User user = activeUser();
            GeneratedAccessToken generatedAccessToken = new GeneratedAccessToken(
                    "new-access-token",
                    "Bearer",
                    LocalDateTime.of(2026, 5, 24, 10, 0)
            );

            when(userPasswordChangeService.changePassword(eq(command), any()))
                    .thenReturn(user);

            when(accessTokenGenerator.generate(new AccessTokenSubject(
                    user.getId(),
                    user.getEmail(),
                    false
            ))).thenReturn(generatedAccessToken);

            InitialChangePasswordResult result = initialChangePasswordUseCase.execute(command);

            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.expiresAt()).isEqualTo(LocalDateTime.of(2026, 5, 24, 10, 0));

            verify(userPasswordChangeService).changePassword(eq(command), any());
            verify(accessTokenGenerator).generate(new AccessTokenSubject(
                    user.getId(),
                    user.getEmail(),
                    false
            ));
        }
    }

    @Nested
    class InitialPasswordChangeRequirement {

        @Test
        @SuppressWarnings("unchecked")
        void shouldRequirePendingInitialPasswordChange() {
            ChangePasswordCommand command = command();
            User user = activeUser();
            GeneratedAccessToken generatedAccessToken = new GeneratedAccessToken(
                    "new-access-token",
                    "Bearer",
                    LocalDateTime.of(2026, 5, 24, 10, 0)
            );

            when(userPasswordChangeService.changePassword(eq(command), any()))
                    .thenReturn(user);

            when(accessTokenGenerator.generate(any()))
                    .thenReturn(generatedAccessToken);

            initialChangePasswordUseCase.execute(command);

            ArgumentCaptor<Consumer<User>> captor = ArgumentCaptor.forClass(Consumer.class);
            verify(userPasswordChangeService).changePassword(eq(command), captor.capture());

            Consumer<User> beforePasswordChange = captor.getValue();

            assertThatCode(() -> beforePasswordChange.accept(activeUserWithMustChangePasswordTrue()))
                    .doesNotThrowAnyException();
            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> beforePasswordChange.accept(activeUser()))
                    .withMessage("A troca inicial de senha não é mais obrigatória.");
        }
    }
}
