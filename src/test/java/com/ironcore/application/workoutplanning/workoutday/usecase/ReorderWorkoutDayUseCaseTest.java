package com.ironcore.application.workoutplanning.workoutday.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutplanning.workoutday.WorkoutDayAuditData;
import com.ironcore.application.workoutplanning.workoutday.reorder.ReorderWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.reorder.ReorderWorkoutDayUseCase;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.ironcore.application.workoutplanning.workoutday.WorkoutDayUseCaseTestFactory.validReorderCommand;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.completedWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.cancelledWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReorderWorkoutDayUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private WorkoutCycleRepository workoutCycleRepository;

    @Mock
    private Clock clock;

    @Mock
    private AuditLogPublisher publisher;

    private ReorderWorkoutDayUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReorderWorkoutDayUseCase(
                userRepository,
                personRepository,
                workoutDayRepository,
                workoutCycleRepository,
                clock,
                publisher
        );
    }

    @Nested
    class SuccessfulReorder {

        @Test
        void shouldReorderWorkoutDaysWithinWeekDay() {
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle cycle = inProgressWorkoutCycle();
            WorkoutDay movedDay = workoutDay(new WorkoutDayId(1L), cycle, WeekDay.MONDAY, "Treino A", 1);
            WorkoutDay secondDay = workoutDay(new WorkoutDayId(2L), cycle, WeekDay.MONDAY, "Treino B", 2);
            ReorderWorkoutDayCommand command = validReorderCommand();
            LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 22, 12, 0);
            givenFixedClock(updatedAt);
            givenEditableWorkoutDay(user, person, movedDay, cycle, command);
            when(workoutDayRepository.findByWorkoutCycleId(cycle.getId())).thenReturn(List.of(movedDay, secondDay));
            when(workoutDayRepository.save(any(WorkoutDay.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            useCase.execute(command);

            ArgumentCaptor<WorkoutDay> captor = ArgumentCaptor.forClass(WorkoutDay.class);
            verify(workoutDayRepository, times(2)).save(captor.capture());
            assertThat(captor.getAllValues())
                    .extracting(WorkoutDay::getId)
                    .containsExactly(secondDay.getId(), movedDay.getId());
            assertThat(captor.getAllValues())
                    .extracting(WorkoutDay::getSortOrder)
                    .containsExactly(1, 2);
            assertThat(captor.getAllValues()).allSatisfy(day -> assertThat(day.getUpdatedAt()).isEqualTo(updatedAt));
            verify(publisher).publish(
                    eq(AuditActionType.UPDATE), eq(user.getId().value()), eq(user.getEmail().value()),
                    eq(AuditTargetType.WORKOUT_DAY), eq(movedDay.getId().value()),
                    any(WorkoutDayAuditData.class), any(WorkoutDayAuditData.class)
            );
        }

        @Test
        void shouldNormalizeBothWeekDaysWhenMovingWorkoutDayToAnotherWeekDay() {
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle cycle = inProgressWorkoutCycle();
            WorkoutDay movedDay = workoutDay(new WorkoutDayId(1L), cycle, WeekDay.MONDAY, "Treino A", 1);
            WorkoutDay remainingSourceDay = workoutDay(new WorkoutDayId(2L), cycle, WeekDay.MONDAY, "Treino B", 2);
            WorkoutDay targetDay = workoutDay(new WorkoutDayId(3L), cycle, WeekDay.WEDNESDAY, "Treino C", 1);
            ReorderWorkoutDayCommand command = new ReorderWorkoutDayCommand(
                    commandActorId(),
                    movedDay.getId(),
                    WeekDay.WEDNESDAY,
                    2
            );
            givenFixedClock(LocalDateTime.of(2026, 8, 22, 12, 0));
            givenEditableWorkoutDay(user, person, movedDay, cycle, command);
            when(workoutDayRepository.findByWorkoutCycleId(cycle.getId()))
                    .thenReturn(List.of(movedDay, remainingSourceDay, targetDay));
            when(workoutDayRepository.save(any(WorkoutDay.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            useCase.execute(command);

            assertThat(remainingSourceDay.getWeekDay()).isEqualTo(WeekDay.MONDAY);
            assertThat(remainingSourceDay.getSortOrder()).isEqualTo(1);
            assertThat(targetDay.getWeekDay()).isEqualTo(WeekDay.WEDNESDAY);
            assertThat(targetDay.getSortOrder()).isEqualTo(1);
            assertThat(movedDay.getWeekDay()).isEqualTo(WeekDay.WEDNESDAY);
            assertThat(movedDay.getSortOrder()).isEqualTo(2);
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            ReorderWorkoutDayCommand command = validReorderCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");
            verify(workoutDayRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            ReorderWorkoutDayCommand command = validReorderCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");
            verify(workoutDayRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            User user = activeUser();
            ReorderWorkoutDayCommand command = validReorderCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");
            verify(workoutDayRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutDayDoesNotBelongToPerson() {
            User user = activeUser();
            ReorderWorkoutDayCommand command = validReorderCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(restoredPerson()));
            when(workoutDayRepository.findByIdAndPersonId(command.id(), user.getPersonId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Dia de treino não encontrado.");
            verify(workoutDayRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutCycleDoesNotBelongToPerson() {
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutDay workoutDay = restoredWorkoutDay();
            ReorderWorkoutDayCommand command = validReorderCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(workoutDayRepository.findByIdAndPersonId(command.id(), person.getId()))
                    .thenReturn(Optional.of(workoutDay));
            when(workoutCycleRepository.findByIdAndPersonId(workoutDay.getWorkoutCycleId(), person.getId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Ciclo de treino não encontrado.");
            verify(workoutDayRepository, never()).save(any());
        }
    }

    @Nested
    class ReorderValidation {

        @Test
        void shouldFailWhenRequestedPositionIsOutsideWeekDayRange() {
            ReorderWorkoutDayCommand command = new ReorderWorkoutDayCommand(
                    commandActorId(),
                    new WorkoutDayId(1L),
                    WeekDay.MONDAY,
                    3
            );
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle cycle = inProgressWorkoutCycle();
            WorkoutDay workoutDay = restoredWorkoutDay();
            givenEditableWorkoutDay(user, person, workoutDay, cycle, command);
            when(workoutDayRepository.findByWorkoutCycleId(cycle.getId())).thenReturn(List.of(workoutDay));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Posição de ordenação inválida.");
            verify(workoutDayRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutCycleIsCompleted() {
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutDay workoutDay = restoredWorkoutDay();
            ReorderWorkoutDayCommand command = validReorderCommand();
            givenEditableWorkoutDay(user, person, workoutDay, completedWorkoutCycle(), command);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Não é permitido editar dias de treino de ciclos concluídos ou cancelados.");
            verify(workoutDayRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutCycleIsCancelled() {
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutDay workoutDay = restoredWorkoutDay();
            ReorderWorkoutDayCommand command = validReorderCommand();
            givenEditableWorkoutDay(user, person, workoutDay, cancelledWorkoutCycle(), command);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Não é permitido editar dias de treino de ciclos concluídos ou cancelados.");
            verify(workoutDayRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenRequestedPositionIsNotPositive() {
            ReorderWorkoutDayCommand command = new ReorderWorkoutDayCommand(
                    commandActorId(),
                    new WorkoutDayId(1L),
                    WeekDay.MONDAY,
                    0
            );
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle cycle = inProgressWorkoutCycle();
            WorkoutDay workoutDay = restoredWorkoutDay();
            givenEditableWorkoutDay(user, person, workoutDay, cycle, command);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Posição de ordenação inválida.");
            verify(workoutDayRepository, never()).save(any());
        }
    }

    private void givenEditableWorkoutDay(
            User user,
            Person person,
            WorkoutDay workoutDay,
            WorkoutCycle cycle,
            ReorderWorkoutDayCommand command
    ) {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
        when(workoutDayRepository.findByIdAndPersonId(command.id(), person.getId()))
                .thenReturn(Optional.of(workoutDay));
        when(workoutCycleRepository.findByIdAndPersonId(workoutDay.getWorkoutCycleId(), person.getId()))
                .thenReturn(Optional.of(cycle));
    }

    private com.ironcore.domain.user.valueobject.UserId commandActorId() {
        return validReorderCommand().actorUserId();
    }

    private WorkoutDay workoutDay(
            WorkoutDayId id,
            WorkoutCycle cycle,
            WeekDay weekDay,
            String title,
            int sortOrder
    ) {
        return WorkoutDay.restore(
                id,
                cycle.getId(),
                weekDay,
                title,
                sortOrder,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                null
        );
    }

    private void givenFixedClock(LocalDateTime dateTime) {
        Clock fixedClock = Clock.fixed(
                Instant.from(dateTime.toInstant(ZoneOffset.UTC)),
                ZoneOffset.UTC
        );
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }
}
