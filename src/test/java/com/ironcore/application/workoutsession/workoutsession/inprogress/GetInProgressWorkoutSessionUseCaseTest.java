package com.ironcore.application.workoutsession.workoutsession.inprogress;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionDetailResult;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetInProgressWorkoutSessionUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    private GetInProgressWorkoutSessionUseCase useCase;
    private final User user = User.restore(new UserId(77L), "Renan", restoredPerson().getId(),
            activeUser().getEmail(), activeUser().getPasswordHash(), false, true, activeUser().getCreatedAt(), null);
    private final Person person = restoredPerson();
    private final GetInProgressWorkoutSessionCommand command =
            new GetInProgressWorkoutSessionCommand(user.getId());

    @BeforeEach
    void setUp() {
        useCase = new GetInProgressWorkoutSessionUseCase(
                userRepository, personRepository, workoutSessionRepository);
    }

    @Test
    void shouldReturnOpenSessionWithSnapshotsAndExecutionToResume() {
        givenPerson();
        WorkoutSession open = session();
        open.addSet(person.getId(), ACTIVITY_ID, 1, new BigDecimal("35.00"), ActualLoadMode.TOTAL,
                12, "Registrada", START.plusSeconds(1));
        open.updateActivityExecution(person.getId(), ACTIVITY_ID, 15, new BigDecimal("1.25"),
                "Parcial", START.plusSeconds(2));
        open.updateNotes(person.getId(), "Retomar treino", START.plusSeconds(3));
        open = restoredSession(open);
        when(workoutSessionRepository.findInProgressByPersonId(person.getId())).thenReturn(Optional.of(open));

        WorkoutSessionDetailResult result = useCase.execute(command);

        assertThat(result.id()).isEqualTo(open.getId());
        assertThat(result.personId()).isEqualTo(person.getId());
        assertThat(result.status()).isEqualTo(WorkoutSessionStatus.IN_PROGRESS);
        assertThat(result.finishedAt()).isNull();
        assertThat(result.closeReason()).isNull();
        assertThat(result.notes()).isEqualTo("Retomar treino");
        assertThat(result.workoutCycleNameSnapshot()).isEqualTo(open.getSnapshot().workoutCycleName());
        var activity = result.activities().getFirst();
        assertThat(activity.id()).isEqualTo(ACTIVITY_ID);
        assertThat(activity.plannedSetsSnapshot())
                .isEqualTo(open.getActivities().getFirst().getSnapshot().plannedSets());
        assertThat(activity.actualDurationMinutes()).isEqualTo(15);
        assertThat(activity.actualDistanceKm()).isEqualByComparingTo("1.25");
        assertThat(activity.notes()).isEqualTo("Parcial");
        assertThat(activity.sets()).hasSize(1);
        var set = activity.sets().getFirst();
        assertThat(set.id()).isNotNull();
        assertThat(set.actualLoadKg()).isEqualByComparingTo("35.00");
        assertThat(set.actualLoadMode()).isEqualTo(ActualLoadMode.TOTAL);
        assertThat(set.actualReps()).isEqualTo(12);
        verify(workoutSessionRepository).findInProgressByPersonId(person.getId());
        verify(workoutSessionRepository, never()).save(any());
        verify(workoutSessionRepository, never()).findByIdAndPersonId(any(), any());
    }

    @Test
    void shouldReturnPersistedInProgressSessionWithoutExpiringIt() {
        givenPerson();
        WorkoutSession open = session();
        when(workoutSessionRepository.findInProgressByPersonId(person.getId())).thenReturn(Optional.of(open));

        WorkoutSessionDetailResult result = useCase.execute(command);

        assertThat(result.status()).isEqualTo(WorkoutSessionStatus.IN_PROGRESS);
        assertThat(result.startedAt()).isEqualTo(START);
        assertThat(result.updatedAt()).isNull();
        assertThat(result.activities().getFirst().sets()).isEmpty();
        assertThat(result.activities().getFirst().actualDurationMinutes()).isNull();
        assertThat(result.activities().getFirst().actualDistanceKm()).isNull();
        assertThat(open.getStatus()).isEqualTo(WorkoutSessionStatus.IN_PROGRESS);
        verify(workoutSessionRepository, never()).save(any());
    }

    @Test
    void shouldRejectMissingUser() {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário não encontrado.");
        verifyNoInteractions(personRepository, workoutSessionRepository);
    }

    @Test
    void shouldRejectInactiveUser() {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));
        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(UserInactiveException.class);
        verifyNoInteractions(personRepository, workoutSessionRepository);
    }

    @Test
    void shouldRejectMissingPerson() {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pessoa não encontrada.");
        verifyNoInteractions(workoutSessionRepository);
    }

    @Test
    void shouldReturnNotFoundWhenPersonHasNoOpenSession() {
        givenPerson();
        when(workoutSessionRepository.findInProgressByPersonId(person.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sessão de treino em andamento não encontrada.");
        verify(workoutSessionRepository).findInProgressByPersonId(person.getId());
        verify(workoutSessionRepository, never()).save(any());
    }

    private void givenPerson() {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
    }
}
