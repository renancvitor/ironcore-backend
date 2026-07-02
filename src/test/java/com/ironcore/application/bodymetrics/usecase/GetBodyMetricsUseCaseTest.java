package com.ironcore.application.bodymetrics.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsCommand;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsResult;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsUseCase;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.restoreBodyMetrics;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBodyMetricsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BodyMetricsRepository bodyMetricsRepository;

    @InjectMocks
    private GetBodyMetricsUseCase getBodyMetricsUseCase;

    @Nested
    class SuccessfulGetBodyMetrics {

        @Test
        void shouldReturnUserBodyMetricsById() {
            User user = activeUser();
            BodyMetrics metrics = restoreBodyMetrics();
            GetBodyMetricsCommand command = new GetBodyMetricsCommand(
                    metrics.getId(),
                    user.getId()
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(bodyMetricsRepository.findByIdAndUserId(
                    command.bodyMetricsId(),
                    command.userId()
            )).thenReturn(Optional.of(metrics));

            GetBodyMetricsResult result = getBodyMetricsUseCase.execute(command);

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository).findByIdAndUserId(command.bodyMetricsId(), user.getId());

            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(metrics);
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            GetBodyMetricsCommand command = new GetBodyMetricsCommand(
                    new BodyMetricsId(1L),
                    new UserId(1L)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> getBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(bodyMetricsRepository);
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            BodyMetrics metrics = restoreBodyMetrics();
            GetBodyMetricsCommand command = new GetBodyMetricsCommand(
                    metrics.getId(),
                    user.getId()
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> getBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(bodyMetricsRepository);
        }
    }

    @Nested
    class BodyMetricsValidation {

        @Test
        void shouldFailWhenBodyMetricsDoesNotExist() {
            User user = activeUser();
            GetBodyMetricsCommand command = new GetBodyMetricsCommand(
                    new BodyMetricsId(1L),
                    user.getId()
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(bodyMetricsRepository.findByIdAndUserId(
                    command.bodyMetricsId(),
                    command.userId()
            )).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> getBodyMetricsUseCase.execute(command))
                    .withMessage("Métricas corporais não encontradas.");

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository).findByIdAndUserId(command.bodyMetricsId(), user.getId());
        }
    }
}
