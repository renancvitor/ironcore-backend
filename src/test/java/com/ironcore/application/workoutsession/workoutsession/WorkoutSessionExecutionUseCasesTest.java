package com.ironcore.application.workoutsession.workoutsession;

import com.ironcore.application.exception.ResourceAlreadyExistsException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutsession.workoutsession.notes.UpdateWorkoutSessionNotesCommand;
import com.ironcore.application.workoutsession.workoutsession.notes.UpdateWorkoutSessionNotesUseCase;
import com.ironcore.application.workoutsession.workoutsessionactivity.execution.UpdateWorkoutSessionActivityExecutionCommand;
import com.ironcore.application.workoutsession.workoutsessionactivity.execution.UpdateWorkoutSessionActivityExecutionUseCase;
import com.ironcore.application.workoutsession.workoutsessionactivity.loadmode.ChangeWorkoutSessionActivityLoadModeCommand;
import com.ironcore.application.workoutsession.workoutsessionactivity.loadmode.ChangeWorkoutSessionActivityLoadModeUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.create.CreateWorkoutSessionSetCommand;
import com.ironcore.application.workoutsession.workoutsessionset.create.CreateWorkoutSessionSetUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.delete.DeleteWorkoutSessionSetCommand;
import com.ironcore.application.workoutsession.workoutsessionset.delete.DeleteWorkoutSessionSetUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.update.UpdateWorkoutSessionSetCommand;
import com.ironcore.application.workoutsession.workoutsessionset.update.UpdateWorkoutSessionSetUseCase;
import com.ironcore.domain.exception.DomainException;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutSessionExecutionUseCasesTest {
    enum Operation { NOTES, ACTIVITY, CREATE, UPDATE, DELETE, MODE }
    @Mock UserRepository users;
    @Mock PersonRepository persons;
    @Mock WorkoutSessionRepository sessions;
    @Mock Clock clock;
    @Mock AuditLogPublisher publisher;
    private UpdateWorkoutSessionNotesUseCase updateWorkoutSessionNotes;
    private UpdateWorkoutSessionActivityExecutionUseCase updateWorkoutSessionActivityExecution;
    private CreateWorkoutSessionSetUseCase createWorkoutSessionSet;
    private UpdateWorkoutSessionSetUseCase updateWorkoutSessionSet;
    private DeleteWorkoutSessionSetUseCase deleteWorkoutSessionSet;
    private ChangeWorkoutSessionActivityLoadModeUseCase changeWorkoutSessionActivityLoadMode;
    private WorkoutSession session;

    @BeforeEach
    void setUp() {
        updateWorkoutSessionNotes = new UpdateWorkoutSessionNotesUseCase(users, persons, sessions, clock, publisher);
        updateWorkoutSessionActivityExecution = new UpdateWorkoutSessionActivityExecutionUseCase(users, persons, sessions, clock, publisher);
        createWorkoutSessionSet = new CreateWorkoutSessionSetUseCase(users, persons, sessions, clock, publisher);
        updateWorkoutSessionSet = new UpdateWorkoutSessionSetUseCase(users, persons, sessions, clock, publisher);
        deleteWorkoutSessionSet = new DeleteWorkoutSessionSetUseCase(users, persons, sessions, clock, publisher);
        changeWorkoutSessionActivityLoadMode = new ChangeWorkoutSessionActivityLoadModeUseCase(users, persons, sessions, clock, publisher);
        session = session();
        session.addSet(OWNER, ACTIVITY_ID, 1, new BigDecimal("20.25"), ActualLoadMode.TOTAL, 8, "Série original", START);
        session.updateActivityExecution(OWNER, ACTIVITY_ID, 20, new BigDecimal("2.50"), "Atividade original", START);
        session.updateNotes(OWNER, "Sessão original", START);
        session = restoredSession(session);
    }

    static Stream<Arguments> operationsAndLifecycle() {
        return Stream.of(Operation.values()).flatMap(operation ->
                Stream.of("OPEN", "MANUAL", "AUTO_EXPIRED", "OVERDUE").map(state -> Arguments.of(operation, state)));
    }

    @ParameterizedTest
    @MethodSource("operationsAndLifecycle")
    void shouldWriteAndAuditEveryOperationInEachSupportedLifecycle(Operation operation, String state) {
        if (state.equals("MANUAL")) session.closeManually(OWNER, START.plusSeconds(1));
        if (state.equals("AUTO_EXPIRED")) session.expireIfDue(EXPIRY);
        var before = WorkoutSessionAuditData.from(session);
        var snapshot = session.getActivities().getFirst().getSnapshot();
        givenSession();
        Instant now = state.equals("OPEN") ? START.plusSeconds(60) : EXPIRY.plusSeconds(60);
        when(clock.instant()).thenReturn(now);

        execute(operation, session.getId(), ACTIVITY_ID, setId());

        assertThat(session.getActivities().getFirst().getSnapshot()).isEqualTo(snapshot);
        assertThat(session.getActivities().getFirst().getSnapshot().plannedTargetLoadKg()).isEqualByComparingTo(snapshot.plannedTargetLoadKg());
        if (state.equals("MANUAL")) {
            assertThat(session.getStatus()).isEqualTo(WorkoutSessionStatus.CLOSED);
            assertThat(session.getFinishedAt()).isEqualTo(START.plusSeconds(1));
            assertThat(session.getCloseReason()).isEqualTo(WorkoutSessionCloseReason.MANUAL);
        } else if (!state.equals("OPEN")) {
            assertThat(session.getStatus()).isEqualTo(WorkoutSessionStatus.CLOSED);
            assertThat(session.getFinishedAt()).isEqualTo(EXPIRY);
            assertThat(session.getCloseReason()).isEqualTo(WorkoutSessionCloseReason.AUTO_EXPIRED);
        } else {
            assertThat(session.getStatus()).isEqualTo(WorkoutSessionStatus.IN_PROGRESS);
        }
        verify(sessions).save(session);
        var previous = ArgumentCaptor.forClass(WorkoutSessionAuditData.class);
        var current = ArgumentCaptor.forClass(WorkoutSessionAuditData.class);
        verify(publisher).publish(eq(AuditActionType.UPDATE), eq(activeUser().getId().value()),
                eq(activeUser().getEmail().value()), eq(AuditTargetType.WORKOUT_SESSION),
                eq(session.getId().value()), previous.capture(), current.capture());
        assertThat(previous.getValue()).isEqualTo(before);
        assertThat(current.getValue()).isEqualTo(WorkoutSessionAuditData.from(restoredSession(session)));
        assertThat(before.activities().getFirst().sets().getFirst().actualReps()).isEqualTo(8);
        assertThat(before.activities().getFirst().sets().getFirst().actualLoadMode()).isEqualTo(ActualLoadMode.TOTAL);
        assertThatThrownBy(() -> before.activities().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> before.activities().getFirst().sets().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void shouldHideMissingOrForeignSessions(Operation operation) {
        when(users.findById(activeUser().getId())).thenReturn(Optional.of(activeUser()));
        when(persons.findById(activeUser().getPersonId())).thenReturn(Optional.of(restoredPerson()));
        when(sessions.findByIdAndPersonId(session.getId(), OWNER)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> execute(operation, session.getId(), ACTIVITY_ID, setId()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(sessions, never()).save(any());
        verifyNoInteractions(clock, publisher);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void shouldRejectMissingUsers(Operation operation) {
        assertThatThrownBy(() -> execute(operation, session.getId(), ACTIVITY_ID, setId()))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(persons, sessions, clock, publisher);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void shouldRejectInactiveUsers(Operation operation) {
        when(users.findById(activeUser().getId())).thenReturn(Optional.of(inactiveUser()));
        assertThatThrownBy(() -> execute(operation, session.getId(), ACTIVITY_ID, setId()))
                .isInstanceOf(UserInactiveException.class);
        verifyNoInteractions(persons, sessions, clock, publisher);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void shouldRejectMissingPerson(Operation operation) {
        when(users.findById(activeUser().getId())).thenReturn(Optional.of(activeUser()));
        assertThatThrownBy(() -> execute(operation, session.getId(), ACTIVITY_ID, setId()))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(sessions, clock, publisher);
    }

    @ParameterizedTest
    @EnumSource(value = Operation.class, names = {"ACTIVITY", "CREATE", "UPDATE", "DELETE", "MODE"})
    void shouldRejectActivityOutsideOwnedSession(Operation operation) {
        givenSession();
        assertThatThrownBy(() -> execute(operation, session.getId(), new WorkoutSessionActivityId(999L), setId()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(sessions, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @ParameterizedTest
    @EnumSource(value = Operation.class, names = {"UPDATE", "DELETE"})
    void shouldRejectMissingOrForeignSet(Operation operation) {
        givenSession();
        assertThatThrownBy(() -> execute(operation, session.getId(), ACTIVITY_ID, new WorkoutSessionSetId(999L)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(sessions, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void shouldPropagateConcurrencyWithoutPublishingSuccess(Operation operation) {
        givenSession();
        when(sessions.save(session)).thenThrow(new WorkoutSessionConflictException("Conflito"));
        assertThatThrownBy(() -> execute(operation, session.getId(), ACTIVITY_ID, setId()))
                .isInstanceOf(WorkoutSessionConflictException.class);
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldPreserveOmittedFieldsAndClearExplicitNullInActivityAndSession() {
        givenSession();
        updateWorkoutSessionActivityExecution.execute(new UpdateWorkoutSessionActivityExecutionCommand(
                activeUser().getId(), session.getId(), ACTIVITY_ID, null, true, null, false, null, true));
        var activity = session.getActivities().getFirst();
        assertThat(activity.getActualDurationMinutes()).isNull();
        assertThat(activity.getActualDistanceKm()).isEqualByComparingTo("2.50");
        assertThat(activity.getNotes()).isNull();
        updateWorkoutSessionNotes.execute(new UpdateWorkoutSessionNotesCommand(activeUser().getId(), session.getId(), null, true));
        assertThat(session.getNotes()).isNull();
        updateWorkoutSessionActivityExecution.execute(new UpdateWorkoutSessionActivityExecutionCommand(
                activeUser().getId(), session.getId(), ACTIVITY_ID, 30, true, null, true, null, false));
        assertThat(activity.getActualDurationMinutes()).isEqualTo(30);
        assertThat(activity.getActualDistanceKm()).isNull();
    }

    @Test
    void shouldClearLoadAndModeWhilePreservingRepsAndIdentity() {
        givenSession();
        var id = setId();
        var result = updateWorkoutSessionSet.execute(new UpdateWorkoutSessionSetCommand(
                activeUser().getId(), session.getId(), ACTIVITY_ID, id,
                null, true, null, true, null, false, null, true));
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.setNumber()).isEqualTo(1);
        assertThat(result.createdAt()).isEqualTo(START);
        assertThat(result.actualLoadKg()).isNull();
        assertThat(result.actualLoadMode()).isNull();
        assertThat(result.normalizedLoadKg()).isNull();
        assertThat(result.actualReps()).isEqualTo(8);
        assertThat(result.notes()).isNull();
    }

    @Test
    void shouldRejectInvalidFinalStateWithoutSavingOrAuditing() {
        givenSession();
        var before = WorkoutSessionAuditData.from(session);
        assertThatThrownBy(() -> updateWorkoutSessionSet.execute(new UpdateWorkoutSessionSetCommand(
                activeUser().getId(), session.getId(), ACTIVITY_ID, setId(),
                null, true, null, false, null, false, null, false))).isInstanceOf(DomainException.class);
        assertThat(WorkoutSessionAuditData.from(session)).isEqualTo(before);
        verify(sessions, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldRejectDuplicateSetNumber() {
        givenSession();
        assertThatThrownBy(() -> createWorkoutSessionSet.execute(new CreateWorkoutSessionSetCommand(
                activeUser().getId(), session.getId(), ACTIVITY_ID, 1, null, null, 12, null)))
                .isInstanceOf(ResourceAlreadyExistsException.class);
        verify(sessions, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldReturnPersistedIdentityWithoutPresumingExecution() {
        givenSession();
        var created = createWorkoutSessionSet.execute(new CreateWorkoutSessionSetCommand(
                activeUser().getId(), session.getId(), ACTIVITY_ID, 4, null, null, 12, null));
        assertThat(created.id()).isNotNull();
        assertThat(created.actualLoadKg()).isNull();
        assertThat(created.actualLoadMode()).isNull();
        assertThat(created.normalizedLoadKg()).isNull();
        assertThat(created.actualReps()).isEqualTo(12);
        assertThat(session.getActivities().getFirst().getSets()).hasSize(2);
    }

    @Test
    void shouldNotPersistASelectorWithoutLoadedSets() {
        session = session();
        givenSession();
        var result = changeWorkoutSessionActivityLoadMode.execute(new ChangeWorkoutSessionActivityLoadModeCommand(
                activeUser().getId(), session.getId(), ACTIVITY_ID, ActualLoadMode.PER_SIDE));
        assertThat(result.actualLoadMode()).isNull();
        assertThat(result.sets()).isEmpty();
        verify(sessions, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldNormalizeReinterpretedModeWithoutConvertingRawLoad() {
        givenSession();
        var result = changeWorkoutSessionActivityLoadMode.execute(new ChangeWorkoutSessionActivityLoadModeCommand(
                activeUser().getId(), session.getId(), ACTIVITY_ID, ActualLoadMode.PER_SIDE));
        assertThat(result.actualLoadMode()).isEqualTo(ActualLoadMode.PER_SIDE);
        assertThat(result.sets().getFirst().actualLoadKg()).isEqualByComparingTo("20.25");
        assertThat(result.sets().getFirst().normalizedLoadKg()).isEqualByComparingTo("40.50");
    }

    @Test
    void shouldClearRepsWhileKeepingOriginalLoadAndMode() {
        givenSession();
        var result = updateWorkoutSessionSet.execute(new UpdateWorkoutSessionSetCommand(
                activeUser().getId(), session.getId(), ACTIVITY_ID, setId(),
                null, false, null, false, null, true, null, false));
        assertThat(result.actualReps()).isNull();
        assertThat(result.actualLoadKg()).isEqualByComparingTo("20.25");
        assertThat(result.actualLoadMode()).isEqualTo(ActualLoadMode.TOTAL);
        assertThat(result.normalizedLoadKg()).isEqualByComparingTo("20.25");
        assertThat(result.notes()).isEqualTo("Série original");
    }

    @Test
    void shouldWriteOnlyReportsWithoutFillingPrescriptions() {
        session = session();
        givenSession();
        updateWorkoutSessionNotes.execute(new UpdateWorkoutSessionNotesCommand(
                activeUser().getId(), session.getId(), "Somente relato geral", true));
        var result = updateWorkoutSessionActivityExecution.execute(new UpdateWorkoutSessionActivityExecutionCommand(
                activeUser().getId(), session.getId(), ACTIVITY_ID, null, false, null, false, "Somente relato da atividade", true));
        assertThat(result.actualDurationMinutes()).isNull();
        assertThat(result.actualDistanceKm()).isNull();
        assertThat(result.actualLoadMode()).isNull();
        assertThat(result.sets()).isEmpty();
    }

    @Test
    void shouldRejectEmptyCommandsWithoutSaving() {
        givenSession();
        var actor = activeUser().getId();
        assertThatThrownBy(() -> updateWorkoutSessionNotes.execute(new UpdateWorkoutSessionNotesCommand(
                actor, session.getId(), null, false))).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> updateWorkoutSessionActivityExecution.execute(new UpdateWorkoutSessionActivityExecutionCommand(
                actor, session.getId(), ACTIVITY_ID, null, false, null, false, null, false)))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> updateWorkoutSessionSet.execute(new UpdateWorkoutSessionSetCommand(
                actor, session.getId(), ACTIVITY_ID, setId(), null, false, null, false, null, false, null, false)))
                .isInstanceOf(DomainException.class);
        verify(sessions, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldRegularizeExpiredSessionEvenWhenNoLoadedSetDefinesAMode() {
        session = session();
        givenSession();
        when(clock.instant()).thenReturn(EXPIRY.plusSeconds(60));
        var result = changeWorkoutSessionActivityLoadMode.execute(new ChangeWorkoutSessionActivityLoadModeCommand(
                activeUser().getId(), session.getId(), ACTIVITY_ID, ActualLoadMode.PER_SIDE));
        assertThat(result.actualLoadMode()).isNull();
        assertThat(result.sets()).isEmpty();
        assertThat(session.getCloseReason()).isEqualTo(WorkoutSessionCloseReason.AUTO_EXPIRED);
        assertThat(session.getFinishedAt()).isEqualTo(EXPIRY);
        verify(sessions).save(session);
    }

    private void givenSession() {
        lenient().when(users.findById(activeUser().getId())).thenReturn(Optional.of(activeUser()));
        lenient().when(persons.findById(activeUser().getPersonId())).thenReturn(Optional.of(restoredPerson()));
        lenient().when(sessions.findByIdAndPersonId(session.getId(), OWNER)).thenReturn(Optional.of(session));
        lenient().when(clock.instant()).thenReturn(START.plusSeconds(60));
        lenient().when(sessions.save(any())).thenAnswer(invocation -> restoredSession(invocation.getArgument(0)));
    }

    private WorkoutSessionSetId setId() {
        return session.getActivities().getFirst().getSets().getFirst().getId();
    }

    private Object execute(Operation operation, WorkoutSessionId id, WorkoutSessionActivityId activityId, WorkoutSessionSetId setId) {
        var actor = activeUser().getId();
        return switch (operation) {
            case NOTES -> updateWorkoutSessionNotes.execute(new UpdateWorkoutSessionNotesCommand(actor, id, "Novo relato", true));
            case ACTIVITY -> updateWorkoutSessionActivityExecution.execute(new UpdateWorkoutSessionActivityExecutionCommand(
                    actor, id, activityId, null, false, null, false, "Novo relato", true));
            case CREATE -> createWorkoutSessionSet.execute(new CreateWorkoutSessionSetCommand(actor, id, activityId, 2, null, null, 12, null));
            case UPDATE -> updateWorkoutSessionSet.execute(new UpdateWorkoutSessionSetCommand(actor, id, activityId, setId,
                    null, false, null, false, 10, true, null, false));
            case DELETE -> {
                deleteWorkoutSessionSet.execute(new DeleteWorkoutSessionSetCommand(actor, id, activityId, setId));
                yield null;
            }
            case MODE -> changeWorkoutSessionActivityLoadMode.execute(new ChangeWorkoutSessionActivityLoadModeCommand(
                    actor, id, activityId, ActualLoadMode.PER_SIDE));
        };
    }
}
