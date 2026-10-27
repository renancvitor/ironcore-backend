package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.mapper.WorkoutSessionMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.LockTimeoutException;
import org.springframework.dao.CannotAcquireLockException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Optional;

import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.OWNER;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.START;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.session;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WorkoutSessionAdapterTest {

    @Mock
    private WorkoutSessionJpaRepository workoutSessionJpaRepository;
    @Mock
    private PersonJpaRepository personJpaRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private WorkoutSessionPlanningQueryAdapter workoutSessionPlanningQueryAdapter;
    @InjectMocks
    private WorkoutSessionAdapter adapter;

    private WorkoutSession workoutSession;
    private WorkoutSessionEntity entity;

    @BeforeEach
    void setUp() {
        workoutSession = session();
        PersonEntity owner = new PersonEntity();
        owner.setId(OWNER.value());
        entity = WorkoutSessionMapper.toEntity(workoutSession, owner);
        workoutSession.updateNotes(OWNER, "Atualização", START.plusSeconds(1));
    }

    @Test
    void shouldPreserveOptimisticConflictAndOriginalCause() {
        when(workoutSessionJpaRepository.findByIdAndPerson_Id(1L, OWNER.value())).thenReturn(Optional.of(entity));
        OptimisticLockException conflict = new OptimisticLockException("Version changed");
        doThrow(conflict).when(entityManager).lock(any(WorkoutSessionEntity.class),
                eq(LockModeType.PESSIMISTIC_FORCE_INCREMENT));

        assertThatThrownBy(() -> adapter.save(workoutSession))
                .isInstanceOf(WorkoutSessionConflictException.class).hasCause(conflict);
    }

    @Test
    void shouldPreservePessimisticConflictsAndOriginalCauses() {
        when(workoutSessionJpaRepository.findByIdAndPerson_Id(1L, OWNER.value())).thenReturn(Optional.of(entity));
        for (RuntimeException conflict : new RuntimeException[]{new PessimisticLockException("Locked"),
                new LockTimeoutException("Timeout"), new CannotAcquireLockException("Deadlock")}) {
            doThrow(conflict).when(entityManager).lock(any(WorkoutSessionEntity.class),
                    eq(LockModeType.PESSIMISTIC_FORCE_INCREMENT));
            assertThatThrownBy(() -> adapter.save(workoutSession))
                    .isInstanceOf(WorkoutSessionConflictException.class).hasCause(conflict);
        }
    }

    @Test
    void shouldUpdateHistoryWithoutConsultingPlanning() {
        when(workoutSessionJpaRepository.findByIdAndPerson_Id(1L, OWNER.value())).thenReturn(Optional.of(entity));
        adapter.save(workoutSession);
        verifyNoInteractions(workoutSessionPlanningQueryAdapter, personJpaRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"uk_workout_sessions_person_in_progress", "uk_workout_session_sets_number",
            "uk_workout_session_activities_order", "workout_session_activities_exercise_id_fkey"})
    void shouldTranslateOnlyExpectedIntegrityConflicts(String constraint) {
        when(workoutSessionJpaRepository.findByIdAndPerson_Id(1L, OWNER.value())).thenReturn(Optional.of(entity));
        DataIntegrityViolationException conflict = integrityViolation(constraint);
        doThrow(conflict).when(workoutSessionJpaRepository).flush();

        assertThatThrownBy(() -> adapter.save(workoutSession))
                .isInstanceOf(WorkoutSessionConflictException.class).hasCause(conflict);
    }

    @Test
    void shouldKeepUnexpectedIntegrityFailureAsInfrastructureFailure() {
        when(workoutSessionJpaRepository.findByIdAndPerson_Id(1L, OWNER.value())).thenReturn(Optional.of(entity));
        DataIntegrityViolationException failure = integrityViolation("ck_workout_sessions_lifecycle");
        doThrow(failure).when(workoutSessionJpaRepository).flush();

        assertThatThrownBy(() -> adapter.save(workoutSession))
                .isInstanceOf(PersistenceException.class).hasCause(failure);
    }

    @Test
    void shouldKeepDatabaseUnavailabilityAsInfrastructureFailure() {
        DataAccessResourceFailureException failure = new DataAccessResourceFailureException("Database unavailable");
        when(workoutSessionJpaRepository.findByIdAndPerson_Id(1L, OWNER.value())).thenThrow(failure);

        assertThatThrownBy(() -> adapter.save(workoutSession))
                .isInstanceOf(PersistenceException.class).hasCause(failure);
    }

    private DataIntegrityViolationException integrityViolation(String constraint) {
        return new DataIntegrityViolationException("Constraint violation",
                new ConstraintViolationException("Constraint violation", new SQLException("Violation"), constraint));
    }
}
