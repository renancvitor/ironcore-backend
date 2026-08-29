package com.ironcore.application.workoutplanning.workoutcycle.list;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.workoutplanning.workoutcycle.port.ListWorkoutCyclesQueryPort;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.person.valueobject.Sex;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListWorkoutCyclesUseCaseTest {

    @Mock
    private ListWorkoutCyclesQueryPort queryPort;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private ListWorkoutCyclesUseCase useCase;

    @Nested
    class SuccessfulList {

        @Test
        void shouldListAuthenticatedPersonWorkoutCyclesWithFiltersAndPagination() {
            User user = activeUser();
            Person person = person();
            ListWorkoutCyclesCommand command = new ListWorkoutCyclesCommand(
                    user.getId(),
                    WorkoutStatus.IN_PROGRESS,
                    new TrainingGoalId(2L),
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 3, 31),
                    "hipertrofia",
                    1,
                    2
            );
            PageQuery expectedPageQuery = new PageQuery(1, 2);
            PageResult<ListWorkoutCyclesItemResult> expectedPage = new PageResult<>(
                    List.of(item()),
                    1,
                    2,
                    5,
                    3,
                    false
            );

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(queryPort.findWorkoutCycles(
                    person.getId(),
                    command.workoutStatus(),
                    command.trainingGoalId(),
                    command.startDate(),
                    command.endDate(),
                    command.name(),
                    expectedPageQuery
            )).thenReturn(expectedPage);

            ListWorkoutCyclesResult result = useCase.execute(command);

            verify(userRepository).findById(user.getId());
            verify(personRepository).findById(user.getPersonId());
            verify(queryPort).findWorkoutCycles(
                    person.getId(),
                    command.workoutStatus(),
                    command.trainingGoalId(),
                    command.startDate(),
                    command.endDate(),
                    command.name(),
                    expectedPageQuery
            );
            assertThat(result.cycles()).isEqualTo(expectedPage);
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            ListWorkoutCyclesCommand command = command(new UserId(1L), null, null);
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(queryPort, never()).findWorkoutCycles(
                    any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            ListWorkoutCyclesCommand command = command(user.getId(), null, null);
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(personRepository, never()).findById(any());
            verify(queryPort, never()).findWorkoutCycles(
                    any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            User user = activeUser();
            ListWorkoutCyclesCommand command = command(user.getId(), null, null);
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");

            verify(queryPort, never()).findWorkoutCycles(
                    any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        void shouldFailWhenStartDateIsAfterEndDate() {
            User user = activeUser();
            Person person = person();
            ListWorkoutCyclesCommand command = command(
                    user.getId(),
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 3, 31)
            );
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Data inicial não pode ser posterior à data final.");

            verify(queryPort, never()).findWorkoutCycles(
                    any(), any(), any(), any(), any(), any(), any()
            );
        }
    }

    private static ListWorkoutCyclesCommand command(
            UserId userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new ListWorkoutCyclesCommand(
                userId,
                null,
                null,
                startDate,
                endDate,
                null,
                0,
                10
        );
    }

    private static Person person() {
        return Person.restore(
                new PersonId(1L),
                "Renan",
                new Sex(SexType.MALE),
                new BirthDate(LocalDate.of(1994, 4, 9)),
                LocalDateTime.of(2026, 5, 10, 10, 0),
                null
        );
    }

    private static ListWorkoutCyclesItemResult item() {
        return new ListWorkoutCyclesItemResult(
                new WorkoutCycleId(1L),
                "Ciclo de hipertrofia",
                WorkoutStatus.IN_PROGRESS,
                new TrainingGoalItemResult(new TrainingGoalId(2L), "Hipertrofia"),
                LocalDate.of(2026, 1, 1),
                null,
                3
        );
    }
}
