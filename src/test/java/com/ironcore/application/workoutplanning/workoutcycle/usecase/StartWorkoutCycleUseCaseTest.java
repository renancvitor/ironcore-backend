package com.ironcore.application.workoutplanning.workoutcycle.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutplanning.workoutcycle.start.StartWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.start.StartWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.start.StartWorkoutCycleUseCase;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.traininggoal.exception.InvalidTrainingGoalException;
import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.repository.TrainingGoalRepository;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalCode;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.repository.WorkoutActivityRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.exception.InvalidWorkoutCycleException;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ironcore.application.workoutplanning.workoutcycle.StartWorkoutCycleUseCaseTestFactory.validCommand;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutplanning.traininggoal.TrainingGoalTestFactory.restoreTrainingGoal;
import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.restoredWorkoutActivity;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.cancelledWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.completedWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.restoredWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartWorkoutCycleUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private TrainingGoalRepository trainingGoalRepository;

    @Mock
    private WorkoutCycleRepository workoutCycleRepository;

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private WorkoutActivityRepository workoutActivityRepository;

    @Mock
    private Clock clock;

    private StartWorkoutCycleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new StartWorkoutCycleUseCase(
                userRepository,
                personRepository,
                trainingGoalRepository,
                workoutCycleRepository,
                workoutDayRepository,
                workoutActivityRepository,
                clock
        );
    }

    @Nested
    class SuccessfulStart {

        @Test
        void shouldStartWorkoutCycleWithCurrentDate() {
            StartWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle workoutCycle = restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null);
            WorkoutDay workoutDay = restoredWorkoutDay();
            LocalDate startDate = LocalDate.of(2026, 8, 24);

            givenFixedClock(startDate);
            givenStartableWorkoutCycle(user, person, workoutCycle, command);
            when(trainingGoalRepository.findById(workoutCycle.getTrainingGoalId()))
                    .thenReturn(Optional.of(restoreTrainingGoal()));
            when(workoutDayRepository.findByWorkoutCycleId(workoutCycle.getId()))
                    .thenReturn(List.of(workoutDay));
            when(workoutActivityRepository.findByPersonIdAndWorkoutDayId(person.getId(), workoutDay.getId()))
                    .thenReturn(List.of(restoredWorkoutActivity()));
            when(workoutCycleRepository.save(any(WorkoutCycle.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            StartWorkoutCycleResult result = useCase.execute(command);

            ArgumentCaptor<WorkoutCycle> captor = ArgumentCaptor.forClass(WorkoutCycle.class);
            verify(workoutCycleRepository).save(captor.capture());

            WorkoutCycle savedWorkoutCycle = captor.getValue();
            assertThat(savedWorkoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
            assertThat(savedWorkoutCycle.getStartDate()).isEqualTo(startDate);
            assertThat(result.id()).isEqualTo(workoutCycle.getId());
            assertThat(result.trainingGoalId()).isEqualTo(workoutCycle.getTrainingGoalId());
            assertThat(result.startDate()).isEqualTo(startDate);
            assertThat(result.workoutStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            StartWorkoutCycleCommand command = validCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            StartWorkoutCycleCommand command = validCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            StartWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutCycleDoesNotBelongToPerson() {
            StartWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Ciclo de treino não encontrado.");

            verify(workoutCycleRepository, never()).save(any());
        }
    }

    @Nested
    class BusinessValidation {

        @Test
        void shouldFailWhenTrainingGoalDoesNotExist() {
            StartWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle workoutCycle = restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null);
            givenStartableWorkoutCycle(user, person, workoutCycle, command);
            when(trainingGoalRepository.findById(workoutCycle.getTrainingGoalId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Objetivo de treino não encontrado.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenTrainingGoalIsInactive() {
            StartWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle workoutCycle = restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null);
            givenStartableWorkoutCycle(user, person, workoutCycle, command);
            when(trainingGoalRepository.findById(workoutCycle.getTrainingGoalId()))
                    .thenReturn(Optional.of(inactiveTrainingGoal()));

            assertThatExceptionOfType(InvalidTrainingGoalException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Objetivo de treino inativo.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutCycleDoesNotHaveWorkoutDays() {
            StartWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle workoutCycle = restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null);
            givenStartableWorkoutCycle(user, person, workoutCycle, command);
            when(trainingGoalRepository.findById(workoutCycle.getTrainingGoalId()))
                    .thenReturn(Optional.of(restoreTrainingGoal()));
            when(workoutDayRepository.findByWorkoutCycleId(workoutCycle.getId())).thenReturn(List.of());

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("O ciclo de treino deve possuir pelo menos um dia de treino.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenAnyWorkoutDayDoesNotHaveActivities() {
            StartWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle workoutCycle = restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null);
            WorkoutDay firstWorkoutDay = restoredWorkoutDay();
            WorkoutDay secondWorkoutDay = secondWorkoutDay(workoutCycle);
            givenStartableWorkoutCycle(user, person, workoutCycle, command);
            when(trainingGoalRepository.findById(workoutCycle.getTrainingGoalId()))
                    .thenReturn(Optional.of(restoreTrainingGoal()));
            when(workoutDayRepository.findByWorkoutCycleId(workoutCycle.getId()))
                    .thenReturn(List.of(firstWorkoutDay, secondWorkoutDay));
            when(workoutActivityRepository.findByPersonIdAndWorkoutDayId(
                    person.getId(), firstWorkoutDay.getId())).thenReturn(List.of(restoredWorkoutActivity()));
            when(workoutActivityRepository.findByPersonIdAndWorkoutDayId(
                    person.getId(), secondWorkoutDay.getId())).thenReturn(List.of());

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Cada dia de treino deve possuir pelo menos uma atividade.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutCycleIsAlreadyInProgress() {
            assertCannotStartWorkoutCycle(inProgressWorkoutCycle());
        }

        @Test
        void shouldFailWhenWorkoutCycleIsCompleted() {
            assertCannotStartWorkoutCycle(completedWorkoutCycle());
        }

        @Test
        void shouldFailWhenWorkoutCycleIsCancelled() {
            assertCannotStartWorkoutCycle(cancelledWorkoutCycle());
        }
    }

    private void assertCannotStartWorkoutCycle(WorkoutCycle workoutCycle) {
        StartWorkoutCycleCommand command = validCommand();
        User user = activeUser();
        Person person = restoredPerson();
        WorkoutDay workoutDay = restoredWorkoutDay();
        givenFixedClock(LocalDate.of(2026, 8, 24));
        givenStartableWorkoutCycle(user, person, workoutCycle, command);
        when(trainingGoalRepository.findById(workoutCycle.getTrainingGoalId()))
                .thenReturn(Optional.of(restoreTrainingGoal()));
        when(workoutDayRepository.findByWorkoutCycleId(workoutCycle.getId()))
                .thenReturn(List.of(workoutDay));
        when(workoutActivityRepository.findByPersonIdAndWorkoutDayId(person.getId(), workoutDay.getId()))
                .thenReturn(List.of(restoredWorkoutActivity()));

        assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage("Somente um ciclo não iniciado pode ser iniciado.");

        verify(workoutCycleRepository, never()).save(any());
    }

    private void givenStartableWorkoutCycle(
            User user,
            Person person,
            WorkoutCycle workoutCycle,
            StartWorkoutCycleCommand command
    ) {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
        when(workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId()))
                .thenReturn(Optional.of(workoutCycle));
    }

    private void givenFixedClock(LocalDate date) {
        Clock fixed = Clock.fixed(Instant.from(date.atStartOfDay(ZoneOffset.UTC)), ZoneOffset.UTC);
        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());
    }

    private WorkoutDay secondWorkoutDay(WorkoutCycle workoutCycle) {
        return WorkoutDay.restore(
                new WorkoutDayId(2L),
                workoutCycle.getId(),
                WeekDay.FRIDAY,
                "Treino complementar",
                1,
                LocalDateTime.of(2026, 8, 24, 10, 0),
                null
        );
    }

    private TrainingGoal inactiveTrainingGoal() {
        return TrainingGoal.restore(
                restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null).getTrainingGoalId(),
                new TrainingGoalCode("hypertrophy"),
                "Hipertrofia",
                false,
                10
        );
    }
}
