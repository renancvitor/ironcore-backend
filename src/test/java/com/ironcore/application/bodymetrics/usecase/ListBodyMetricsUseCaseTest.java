package com.ironcore.application.bodymetrics.usecase;

import com.ironcore.application.bodymetrics.list.ListBodyMetricsCommand;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsItemResult;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsResult;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsUseCase;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.bodymetrics.port.ListBodyMetricsQueryPort;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.restoreBodyMetrics;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListBodyMetricsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListBodyMetricsQueryPort queryPort;

    @InjectMocks
    private ListBodyMetricsUseCase listBodyMetricsUseCase;

    @Nested
    class SuccessfulList {

        @Test
        void shouldListUserBodyMetricsWithPagination() {
            User user = activeUser();
            BodyMetrics metrics = restoreBodyMetrics();
            ListBodyMetricsCommand command = new ListBodyMetricsCommand(
                    user.getId(),
                    1,
                    2
            );

            PageQuery expectedQuery = new PageQuery(1, 2);
            ListBodyMetricsItemResult expectedItem = new ListBodyMetricsItemResult(
                    metrics.getId(),
                    metrics.getMeasuredAt(),
                    metrics.getWeight(),
                    metrics.getHeight(),
                    metrics.getNotes()
            );
            PageResult<ListBodyMetricsItemResult> expectedPage =
                    new PageResult<>(
                            List.of(expectedItem),
                            1,
                            2,
                            5,
                            3,
                            false
                    );

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(queryPort.findByUserIdOrderByMeasuredAtDesc(
                    command.userId(),
                    expectedQuery
            )).thenReturn(expectedPage);

            ListBodyMetricsResult result = listBodyMetricsUseCase.execute(command);

            verify(userRepository).findById(user.getId());
            verify(queryPort).findByUserIdOrderByMeasuredAtDesc(
                    command.userId(),
                    expectedQuery
            );

            assertThat(result.metrics()).isEqualTo(expectedPage);
        }

        @Test
        void shouldReturnEmptyPageWhenUserHasNoBodyMetrics() {
            User user = activeUser();
            ListBodyMetricsCommand command = new ListBodyMetricsCommand(
                    user.getId(),
                    0,
                    1
            );

            PageQuery expectedQuery = new PageQuery(0, 1);
            PageResult<ListBodyMetricsItemResult> expectedPage =
                    new PageResult<>(
                            List.of(),
                            0,
                            1,
                            0,
                            0,
                            true
                    );

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(queryPort.findByUserIdOrderByMeasuredAtDesc(
                    command.userId(),
                    expectedQuery
            )).thenReturn(expectedPage);

            ListBodyMetricsResult result = listBodyMetricsUseCase.execute(command);

            verify(userRepository).findById(user.getId());
            verify(queryPort).findByUserIdOrderByMeasuredAtDesc(
                    command.userId(),
                    expectedQuery
            );

            assertThat(result.metrics()).isEqualTo(expectedPage);
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            ListBodyMetricsCommand command = new ListBodyMetricsCommand(
                    new UserId(1L),
                    0,
                    1
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> listBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.userId());
            verify(queryPort, never()).findByUserIdOrderByMeasuredAtDesc(
                    any(),
                    any()
            );
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            ListBodyMetricsCommand command = new ListBodyMetricsCommand(
                    user.getId(),
                    0,
                    1
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> listBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
            verify(queryPort, never()).findByUserIdOrderByMeasuredAtDesc(
                    any(),
                    any()
            );
        }
    }
}
