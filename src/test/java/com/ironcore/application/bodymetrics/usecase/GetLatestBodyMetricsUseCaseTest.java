package com.ironcore.application.bodymetrics.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsCommand;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsResult;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsUseCase;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
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
class GetLatestBodyMetricsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BodyMetricsRepository bodyMetricsRepository;

    @InjectMocks
    private GetLatestBodyMetricsUseCase getLatestBodyMetricsUseCase;

    @Nested
    class SuccessfulGetLatestBodyMetrics {

        @Test
        void shouldReturnLatestPersonBodyMetrics() {
            User user = activeUser();
            BodyMetrics metrics = restoreBodyMetrics();
            GetLatestBodyMetricsCommand command = new GetLatestBodyMetricsCommand(
                    user.getId()
            );

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(bodyMetricsRepository.findLatestByPersonId(user.getPersonId())).thenReturn(Optional.of(metrics));

            GetLatestBodyMetricsResult result = getLatestBodyMetricsUseCase.execute(command);

            verify(userRepository).findById(command.actorUserId());
            verify(bodyMetricsRepository).findLatestByPersonId(user.getPersonId());

            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(metrics);
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            GetLatestBodyMetricsCommand command = new GetLatestBodyMetricsCommand(
                    new UserId(1L)
            );

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> getLatestBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.actorUserId());
            verifyNoInteractions(bodyMetricsRepository);
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            GetLatestBodyMetricsCommand command = new GetLatestBodyMetricsCommand(
                    user.getId()
            );

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> getLatestBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.actorUserId());
            verifyNoInteractions(bodyMetricsRepository);
        }
    }

    @Nested
    class BodyMetricsValidation {

        @Test
        void shouldFailWhenUserHasNoBodyMetrics() {
            User user = activeUser();
            GetLatestBodyMetricsCommand command = new GetLatestBodyMetricsCommand(
                    user.getId()
            );

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(bodyMetricsRepository.findLatestByPersonId(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> getLatestBodyMetricsUseCase.execute(command))
                    .withMessage("Métricas corporais não encontradas.");

            verify(userRepository).findById(command.actorUserId());
            verify(bodyMetricsRepository).findLatestByPersonId(user.getPersonId());
        }
    }
}
