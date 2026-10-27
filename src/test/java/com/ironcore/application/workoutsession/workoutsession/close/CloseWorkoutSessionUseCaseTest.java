package com.ironcore.application.workoutsession.workoutsession.close;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutsession.workoutsession.WorkoutSessionAuditData;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
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
class CloseWorkoutSessionUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    @Mock
    private Clock clock;

    @Mock
    private AuditLogPublisher publisher;

    private CloseWorkoutSessionUseCase useCase;
    private final User user = User.restore(new UserId(77L), "Renan", OWNER,
            activeUser().getEmail(), activeUser().getPasswordHash(), false, true, activeUser().getCreatedAt(), null);
    private final CloseWorkoutSessionCommand command = new CloseWorkoutSessionCommand(user.getId(), session().getId());

    @BeforeEach
    void setUp() {
        useCase = new CloseWorkoutSessionUseCase(
                userRepository, personRepository, workoutSessionRepository, clock, publisher);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-10-27T03:00:00Z", "2026-10-27T06:00:00Z", "2026-10-29T13:00:00Z"})
    void shouldCloseUsingDomainReasonAndPreserveRecordedExecution(String instant) {
        WorkoutSession session = session();
        session.addSet(OWNER, ACTIVITY_ID, 1, null, null, null, "Relato da série", START.plusSeconds(1));
        session.updateActivityExecution(OWNER, ACTIVITY_ID, 20, null, "Relato da atividade", START.plusSeconds(2));
        session.updateNotes(OWNER, "Relato geral", START.plusSeconds(3));
        session = restoredSession(session);
        WorkoutSession before = restoredSession(session);
        givenSession(session);
        Instant now = Instant.parse(instant);
        when(clock.instant()).thenReturn(now);
        when(workoutSessionRepository.save(session)).thenAnswer(invocation -> restoredSession(invocation.getArgument(0)));

        CloseWorkoutSessionResult result = useCase.execute(command);

        boolean expired = !now.isBefore(EXPIRY);
        assertThat(result.status()).isEqualTo(WorkoutSessionStatus.CLOSED);
        assertThat(result.closeReason()).isEqualTo(expired
                ? WorkoutSessionCloseReason.AUTO_EXPIRED : WorkoutSessionCloseReason.MANUAL);
        assertThat(result.finishedAt()).isEqualTo(expired ? EXPIRY : now);
        assertThat(result.updatedAt()).isEqualTo(now);
        assertThat(result.id()).isEqualTo(session.getId());
        assertThat(result.startedAt()).isEqualTo(START);
        assertThat(result.performedOn()).isEqualTo(before.getPerformedOn());
        assertThat(session).usingRecursiveComparison()
                .ignoringFields("status", "closeReason", "finishedAt", "updatedAt").isEqualTo(before);
        verify(workoutSessionRepository).save(session);
        verify(publisher).publish(AuditActionType.UPDATE, user.getId().value(), user.getEmail().value(),
                AuditTargetType.WORKOUT_SESSION, session.getId().value(),
                WorkoutSessionAuditData.from(before), WorkoutSessionAuditData.from(session));
    }

    @Test
    void shouldCloseWithoutFillingExecution() {
        WorkoutSession session = session();
        givenSession(session);
        when(clock.instant()).thenReturn(START.plusSeconds(1));
        when(workoutSessionRepository.save(session)).thenReturn(session);

        useCase.execute(command);

        assertThat(session.getActivities()).allSatisfy(activity -> {
            assertThat(activity.getSets()).isEmpty();
            assertThat(activity.getActualDurationMinutes()).isNull();
            assertThat(activity.getActualDistanceKm()).isNull();
        });
    }

    @Test
    void shouldRejectMissingUser() {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(personRepository, workoutSessionRepository, clock, publisher);
    }

    @Test
    void shouldRejectInactiveUser() {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));
        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(UserInactiveException.class);
        verifyNoInteractions(personRepository, workoutSessionRepository, clock, publisher);
    }

    @Test
    void shouldRejectMissingPerson() {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(workoutSessionRepository, clock, publisher);
    }

    @Test
    void shouldRejectMissingOrForeignSessionThroughOwnershipQuery() {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(restoredPerson()));
        when(workoutSessionRepository.findByIdAndPersonId(command.id(), restoredPerson().getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sessão de treino não encontrada.");
        verify(workoutSessionRepository, never()).save(any());
        verifyNoInteractions(clock, publisher);
    }

    @Test
    void shouldRejectAlreadyClosedSessionWithoutSavingOrAuditing() {
        WorkoutSession session = session();
        session.closeManually(OWNER, START);
        givenSession(session);
        when(clock.instant()).thenReturn(START.plusSeconds(1));

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(InvalidWorkoutSessionException.class);
        verify(workoutSessionRepository, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldPropagateConflictWithoutPublishingAudit() {
        WorkoutSession session = session();
        givenSession(session);
        when(clock.instant()).thenReturn(START.plusSeconds(1));
        when(workoutSessionRepository.save(session)).thenThrow(new WorkoutSessionConflictException("Conflito"));

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(WorkoutSessionConflictException.class);
        verifyNoInteractions(publisher);
    }

    private void givenSession(WorkoutSession session) {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(restoredPerson()));
        when(workoutSessionRepository.findByIdAndPersonId(command.id(), restoredPerson().getId()))
                .thenReturn(Optional.of(session));
    }
}
