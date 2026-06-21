package com.ironcore.application.userbodymetrics.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsUseCase;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.restoreBodyMetrics;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserBodyMetricsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBodyMetricsRepository userBodyMetricsRepository;

    @InjectMocks
    private GetUserBodyMetricsUseCase getUserBodyMetricsUseCase;

    @Nested
    class SuccessfulGetUserBodyMetrics {

        @Test
        void shouldReturnUserBodyMetricsById() {
            User user = activeUser();
            UserBodyMetrics metrics = restoreBodyMetrics();
            GetUserBodyMetricsCommand command = new GetUserBodyMetricsCommand(
                    metrics.getId(),
                    user.getId()
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(userBodyMetricsRepository.findByIdAndUserId(
                    command.userBodyMetricsId(),
                    command.userId()
            )).thenReturn(Optional.of(metrics));

            GetUserBodyMetricsResult result = getUserBodyMetricsUseCase.execute(command);

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository).findByIdAndUserId(command.userBodyMetricsId(), user.getId());

            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(metrics);
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            GetUserBodyMetricsCommand command = new GetUserBodyMetricsCommand(
                    new UserBodyMetricsId(1L),
                    new UserId(1L)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> getUserBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(userBodyMetricsRepository);
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            UserBodyMetrics metrics = restoreBodyMetrics();
            GetUserBodyMetricsCommand command = new GetUserBodyMetricsCommand(
                    metrics.getId(),
                    user.getId()
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> getUserBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(userBodyMetricsRepository);
        }
    }

    @Nested
    class BodyMetricsValidation {

        @Test
        void shouldFailWhenBodyMetricsDoesNotExist() {
            User user = activeUser();
            GetUserBodyMetricsCommand command = new GetUserBodyMetricsCommand(
                    new UserBodyMetricsId(1L),
                    user.getId()
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(userBodyMetricsRepository.findByIdAndUserId(
                    command.userBodyMetricsId(),
                    command.userId()
            )).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> getUserBodyMetricsUseCase.execute(command))
                    .withMessage("Métricas corporais não encontradas.");

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository).findByIdAndUserId(command.userBodyMetricsId(), user.getId());
        }
    }
}
