package com.ironcore.application.userbodymetrics.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsUseCase;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
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
class GetLatestUserBodyMetricsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBodyMetricsRepository userBodyMetricsRepository;

    @InjectMocks
    private GetLatestUserBodyMetricsUseCase getLatestUserBodyMetricsUseCase;

    @Nested
    class SuccessfulGetLatestUserBodyMetrics {

        @Test
        void shouldReturnLatestUserBodyMetrics() {
            User user = activeUser();
            UserBodyMetrics metrics = restoreBodyMetrics();
            GetLatestUserBodyMetricsCommand command = new GetLatestUserBodyMetricsCommand(
                    user.getId()
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(userBodyMetricsRepository.findLatestByUserId(command.userId())).thenReturn(Optional.of(metrics));

            GetLatestUserBodyMetricsResult result = getLatestUserBodyMetricsUseCase.execute(command);

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository).findLatestByUserId(command.userId());

            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(metrics);
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            GetLatestUserBodyMetricsCommand command = new GetLatestUserBodyMetricsCommand(
                    new UserId(1L)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> getLatestUserBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(userBodyMetricsRepository);
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            GetLatestUserBodyMetricsCommand command = new GetLatestUserBodyMetricsCommand(
                    user.getId()
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> getLatestUserBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(userBodyMetricsRepository);
        }
    }

    @Nested
    class BodyMetricsValidation {

        @Test
        void shouldFailWhenUserHasNoBodyMetrics() {
            User user = activeUser();
            GetLatestUserBodyMetricsCommand command = new GetLatestUserBodyMetricsCommand(
                    user.getId()
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(userBodyMetricsRepository.findLatestByUserId(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> getLatestUserBodyMetricsUseCase.execute(command))
                    .withMessage("Métricas corporais não encontradas.");

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository).findLatestByUserId(command.userId());
        }
    }
}
