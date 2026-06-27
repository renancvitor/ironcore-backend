package com.ironcore.application.userbodymetrics.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.userbodymetrics.list.*;
import com.ironcore.application.userbodymetrics.port.ListUserBodyMetricsQueryPort;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
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
import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.restoreBodyMetrics;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUserBodyMetricsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListUserBodyMetricsQueryPort queryPort;

    @InjectMocks
    private ListUserBodyMetricsUseCase listUserBodyMetricsUseCase;

    @Nested
    class SuccessfulList {

        @Test
        void shouldListUserBodyMetricsWithPagination() {
            User user = activeUser();
            UserBodyMetrics metrics = restoreBodyMetrics();
            ListUserBodyMetricsCommand command = new ListUserBodyMetricsCommand(
                    user.getId(),
                    1,
                    2
            );

            PageQuery expectedQuery = new PageQuery(1, 2);
            ListUserBodyMetricsItemResult expectedItem = new ListUserBodyMetricsItemResult(
                    metrics.getId(),
                    metrics.getMeasuredAt(),
                    metrics.getWeight(),
                    metrics.getHeight(),
                    metrics.getNotes()
            );
            PageResult<ListUserBodyMetricsItemResult> expectedPage =
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

            ListUserBodyMetricsResult result = listUserBodyMetricsUseCase.execute(command);

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
            ListUserBodyMetricsCommand command = new ListUserBodyMetricsCommand(
                    user.getId(),
                    0,
                    1
            );

            PageQuery expectedQuery = new PageQuery(0, 1);
            PageResult<ListUserBodyMetricsItemResult> expectedPage =
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

            ListUserBodyMetricsResult result = listUserBodyMetricsUseCase.execute(command);

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
            ListUserBodyMetricsCommand command = new ListUserBodyMetricsCommand(
                    new UserId(1L),
                    0,
                    1
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> listUserBodyMetricsUseCase.execute(command))
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
            ListUserBodyMetricsCommand command = new ListUserBodyMetricsCommand(
                    user.getId(),
                    0,
                    1
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> listUserBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
            verify(queryPort, never()).findByUserIdOrderByMeasuredAtDesc(
                    any(),
                    any()
            );
        }
    }
}
