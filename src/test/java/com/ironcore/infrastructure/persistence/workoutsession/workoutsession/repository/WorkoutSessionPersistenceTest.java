package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySource;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doAnswer;

@DataJpaTest(showSql = false, properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({WorkoutSessionAdapter.class, WorkoutSessionPlanningQueryAdapter.class})
@ActiveProfiles("test")
@Testcontainers
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class WorkoutSessionPersistenceTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("workout_session_test").withUsername("ironcore").withPassword("ironcore");

    private static final Instant START = Instant.parse("2026-10-27T02:30:00Z");
    private static final Instant EXPIRY = Instant.parse("2026-10-27T06:00:00Z");
    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 10, 26, 10, 0);

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private WorkoutSessionRepository repository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @MockitoSpyBean
    private WorkoutSessionPlanningQueryAdapter planningQueryAdapter;
    @Autowired
    private JdbcTemplate jdbc;
    private PersonId owner;
    private Long cycleId;
    private Long dayId;
    private Long goalId;
    private Long strengthExerciseId;
    private Long cardioExerciseId;
    private Long strengthTypeId;
    private Long cardioTypeId;
    private Long equipmentId;
    private List<WorkoutSessionActivitySource> sources;

    @BeforeEach
    void fixtures() {
        owner = new PersonId(id("INSERT INTO persons (name, sex, birth_date, created_at) VALUES ('Session owner', 'MALE', DATE '1990-01-01', now()) RETURNING id"));
        goalId = id("SELECT id FROM training_goals ORDER BY id LIMIT 1");
        equipmentId = id("SELECT id FROM equipment_types ORDER BY id LIMIT 1");
        strengthTypeId = id("SELECT id FROM activity_types WHERE code = 'STRENGTH'");
        cardioTypeId = id("SELECT id FROM activity_types WHERE code = 'CARDIO'");
        cycleId = id("""
                INSERT INTO workout_cycles (person_id, name, training_goal_id, workout_status, workout_origin, created_at)
                VALUES (?, 'Ciclo histórico', ?, 'IN_PROGRESS', 'MANUAL', now()) RETURNING id
                """, owner.value(), goalId);
        dayId = id("""
                INSERT INTO workout_days (workout_cycle_id, week_day, title, sort_order, created_at)
                VALUES (?, 2, 'Treino misto', 1, now()) RETURNING id
                """, cycleId);
        strengthExerciseId = exercise("Agachamento", strengthTypeId);
        cardioExerciseId = exercise("Caminhada", cardioTypeId);
        sources = List.of(activity(strengthExerciseId, strengthTypeId, "STRENGTH", 1, 4),
                activity(cardioExerciseId, cardioTypeId, "CARDIO", 2, null));
    }

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM persons WHERE id = ?", owner.value());
        jdbc.update("DELETE FROM exercises WHERE id IN (?, ?)", strengthExerciseId, cardioExerciseId);
    }

    @Test
    void shouldPersistMixedSnapshotsWithoutCreatingExecutedSets() {
        WorkoutSession saved = repository.save(newSession());
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isNotNull();
        assertThat(saved.getPerformedOn()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(saved.getStartedAt()).isEqualTo(START);
        assertThat(saved.getActivities()).hasSize(2).allSatisfy(a -> {
            assertThat(a.getId()).isNotNull();
            assertThat(a.getSets()).isEmpty();
            assertThat(a.getActualDurationMinutes()).isNull();
            assertThat(a.getActualDistanceKm()).isNull();
        });
        var loaded = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
        assertThat(loaded).usingRecursiveComparison().isEqualTo(saved);
        assertThat(loaded.getActivities().getLast().getSnapshot().plannedSets()).isNull();
        assertThat(loaded.getActivities().getFirst().getSnapshot().plannedTargetLoadKg()).isEqualByComparingTo("45.50");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM workout_session_sets", Long.class)).isZero();
        assertThat(repository.findByIdAndPersonId(saved.getId(), new PersonId(owner.value() + 1000))).isEmpty();
        assertThat(repository.findInProgressByPersonId(owner)).isPresent();
    }

    @Test
    void shouldPreserveExecutionAndSnapshotsAfterAllOriginsAreDeleted() {
        WorkoutSession saved = repository.save(newSession());
        saved.addSet(owner, saved.getActivities().getFirst().getId(), 5, new BigDecimal("50.00"), ActualLoadMode.PER_SIDE, 8, "Extra", START.plusSeconds(1));
        saved.addSet(owner, saved.getActivities().getLast().getId(), 1, null, null, null, "Pausa", START.plusSeconds(2));
        saved.updateActivityExecution(owner, saved.getActivities().getLast().getId(), 20, new BigDecimal("2.00"), "Executado", START.plusSeconds(3));
        saved = repository.save(saved);
        var snapshots = saved.getActivities().stream().map(a -> a.getSnapshot()).toList();
        jdbc.update("UPDATE workout_cycles SET name = 'Alterado' WHERE id = ?", cycleId);
        jdbc.update("UPDATE workout_days SET title = 'Alterado' WHERE id = ?", dayId);
        jdbc.update("UPDATE workout_activities SET sets = 9 WHERE workout_day_id = ?", dayId);
        jdbc.update("UPDATE exercises SET name = 'Alterado' WHERE id IN (?, ?)", strengthExerciseId, cardioExerciseId);
        assertThat(repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow().getActivities())
                .extracting(a -> a.getSnapshot()).containsExactlyElementsOf(snapshots);
        jdbc.update("DELETE FROM workout_activities WHERE id = ?", sources.getFirst().activity().getId().value());
        var afterActivityDeletion = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
        assertThat(afterActivityDeletion.getActivities().getFirst().getWorkoutActivityId()).isNull();
        assertThat(afterActivityDeletion.getActivities().getFirst().getSets()).hasSize(1);
        jdbc.update("DELETE FROM workout_days WHERE id = ?", dayId);
        assertThat(repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow().getWorkoutDayId()).isNull();
        jdbc.update("DELETE FROM workout_cycles WHERE id = ?", cycleId);
        jdbc.update("DELETE FROM exercises WHERE id IN (?, ?)", strengthExerciseId, cardioExerciseId);
        saved.updateNotes(owner, "Relato durante remoção da origem", START.plusSeconds(4));
        saved = repository.save(saved);
        assertThat(saved.getWorkoutCycleId()).isNull();
        assertThat(saved.getWorkoutDayId()).isNull();
        WorkoutSession historical = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
        assertThat(historical.getWorkoutCycleId()).isNull();
        assertThat(historical.getWorkoutDayId()).isNull();
        assertThat(historical.getActivities()).extracting(a -> a.getSnapshot()).containsExactlyElementsOf(snapshots);
        assertThat(historical.getActivities()).allSatisfy(a -> {
            assertThat(a.getWorkoutActivityId()).isNull();
            assertThat(a.getExerciseId()).isNull();
            assertThat(a.getSets()).hasSize(1);
        });
        historical.expireIfDue(EXPIRY.plusSeconds(3600));
        historical.updateNotes(owner, "Histórico corrigido", EXPIRY.plusSeconds(3601));
        historical = repository.save(historical);
        assertThat(historical.getFinishedAt()).isEqualTo(EXPIRY);
        assertThat(historical.getStatus()).isEqualTo(WorkoutSessionStatus.CLOSED);
    }

    @Test
    void shouldAllowSequentialSessionsOnSameDayAndEditClosedExecution() {
        WorkoutSession first = repository.save(newSession());
        assertThatThrownBy(() -> repository.save(newSession())).isInstanceOf(WorkoutSessionConflictException.class);
        first.closeManually(owner, START.plusSeconds(30));
        first = repository.save(first);
        WorkoutSession second = repository.save(newSession());
        assertThat(second.getId()).isNotEqualTo(first.getId());
        assertThat(repository.findInProgressByPersonId(owner).orElseThrow().getId()).isEqualTo(second.getId());
        first.addSet(owner, first.getActivities().getFirst().getId(), 1, new BigDecimal("0.00"), ActualLoadMode.TOTAL, 10, null, EXPIRY.plusSeconds(1));
        first.updateNotes(owner, "Texto posterior", EXPIRY.plusSeconds(2));
        first = repository.save(first);
        Long setId = first.getActivities().getFirst().getSets().getFirst().getId().value();
        first.updateSet(owner, first.getActivities().getFirst().getId(), new WorkoutSessionSetId(setId), new BigDecimal("10.00"), ActualLoadMode.TOTAL, 12, "Correção", EXPIRY.plusSeconds(3));
        first = repository.save(first);
        assertThat(first.getActivities().getFirst().getSets().getFirst().getId().value()).isEqualTo(setId);
        assertThat(first.getStatus()).isEqualTo(WorkoutSessionStatus.CLOSED);
        assertThat(first.getFinishedAt()).isEqualTo(START.plusSeconds(30));
        first.removeSet(owner, first.getActivities().getFirst().getId(), new WorkoutSessionSetId(setId), EXPIRY.plusSeconds(4));
        first = repository.save(first);
        assertThat(first.getActivities().getFirst().getSets()).isEmpty();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM workout_session_sets WHERE id = ?", Long.class, setId)).isZero();
    }

    @Test
    void shouldRejectStaleAggregateEvenWhenOnlyChildExecutionChanges() {
        WorkoutSession saved = repository.save(newSession());
        WorkoutSession stale = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
        saved.addSet(owner, saved.getActivities().getFirst().getId(), 1, null, null, null, "Primeiro registro", START);
        WorkoutSession updated = repository.save(saved);
        assertThat(updated.getVersion()).isGreaterThan(stale.getVersion());
        stale.updateNotes(owner, "Versão antiga", START.plusSeconds(1));
        assertThatThrownBy(() -> repository.save(stale)).isInstanceOf(WorkoutSessionConflictException.class);
        assertThat(repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow()
                .getActivities().getFirst().getSets()).hasSize(1);
    }

    @Test
    void shouldFlushDeletedSetsBeforeReusingTheirNumbers() {
        WorkoutSession saved = repository.save(newSession());
        var activityId = saved.getActivities().getFirst().getId();
        saved.addSet(owner, activityId, 1, null, null, null, "Original", START.plusSeconds(1));
        saved.addSet(owner, activityId, 2, null, null, null, "Mantida", START.plusSeconds(2));
        saved = repository.save(saved);
        var original = saved.getActivities().getFirst().getSets().getFirst();
        var retained = saved.getActivities().getFirst().getSets().getLast();
        saved.removeSet(owner, activityId, original.getId(), START.plusSeconds(3));
        saved.addSet(owner, activityId, 1, new BigDecimal("20.00"), ActualLoadMode.TOTAL,
                8, "Substituta", START.plusSeconds(4));

        WorkoutSession replaced = repository.save(saved);
        var sets = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow()
                .getActivities().getFirst().getSets();
        assertThat(sets).hasSize(2);
        assertThat(sets.getFirst().getSetNumber()).isEqualTo(1);
        assertThat(sets.getFirst().getId()).isNotEqualTo(original.getId());
        assertThat(sets.getFirst().getNotes()).isEqualTo("Substituta");
        assertThat(sets.getFirst().getActualLoadKg()).isEqualByComparingTo("20.00");
        assertThat(sets.getLast().getId()).isEqualTo(retained.getId());
        assertThat(replaced.getVersion()).isGreaterThan(saved.getVersion());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM workout_session_sets WHERE id = ?",
                Long.class, original.getId().value())).isZero();
    }

    @Test
    void shouldRollBackFlushedReplacementWhenChildPersistenceFails() {
        WorkoutSession saved = repository.save(newSession());
        var activityId = saved.getActivities().getFirst().getId();
        saved.addSet(owner, activityId, 1, null, null, null, "Original", START.plusSeconds(1));
        saved = repository.save(saved);
        var original = saved.getActivities().getFirst().getSets().getFirst();
        saved.removeSet(owner, activityId, original.getId(), START.plusSeconds(2));
        saved.addSet(owner, activityId, 1, null, null, null, "Substituta", START.plusSeconds(3));
        WorkoutSession candidate = saved;
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> transaction.execute(status -> {
            repository.save(candidate);
            return jdbc.update("""
                    INSERT INTO workout_session_sets (workout_session_activity_id, set_number, notes, created_at)
                    VALUES (?, 1, 'Duplicada', now())
                    """, activityId.value());
        })).isInstanceOf(DataIntegrityViolationException.class);

        WorkoutSession history = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
        assertThat(history.getVersion()).isEqualTo(saved.getVersion());
        assertThat(history.getActivities().getFirst().getSets()).hasSize(1);
        assertThat(history.getActivities().getFirst().getSets().getFirst().getId()).isEqualTo(original.getId());
        assertThat(history.getActivities().getFirst().getSets().getFirst().getNotes()).isEqualTo("Original");
    }

    @Test
    void shouldTranslatePostgresLockTimeoutOnHistoryUpdate() throws Exception {
        WorkoutSession saved = repository.save(newSession());
        saved.updateNotes(owner, "Atualização bloqueada", START.plusSeconds(1));
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> holder = executor.submit(() -> transaction.execute(status -> {
                jdbc.queryForObject("SELECT id FROM workout_sessions WHERE id = ? FOR UPDATE",
                        Long.class, saved.getId().value());
                locked.countDown();
                try {
                    if (!release.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Session lock was not released");
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                return null;
            }));
            try {
                assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();
                assertThatThrownBy(() -> transaction.execute(status -> {
                    jdbc.execute("SET LOCAL lock_timeout = '200ms'");
                    return repository.save(saved);
                })).isInstanceOf(WorkoutSessionConflictException.class);
            } finally {
                release.countDown();
            }
            holder.get(10, TimeUnit.SECONDS);
        }
        assertThat(repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow().getNotes()).isNull();
    }

    @Test
    void shouldPreventConcurrentOpenSessionsAcrossDaysAndCyclesForSamePerson() throws Exception {
        Long otherCycleId = id("""
                INSERT INTO workout_cycles (person_id, name, training_goal_id, workout_status, workout_origin, created_at)
                VALUES (?, 'Outro ciclo', ?, 'IN_PROGRESS', 'MANUAL', now()) RETURNING id
                """, owner.value(), goalId);
        Long otherDayId = id("""
                INSERT INTO workout_days (workout_cycle_id, week_day, title, sort_order, created_at)
                VALUES (?, 3, 'Outro dia', 1, now()) RETURNING id
                """, otherCycleId);
        WorkoutSession firstSession = newSession();
        jdbc.update("INSERT INTO workout_activities (workout_day_id, exercise_id, order_index, created_at) VALUES (?, ?, 1, now())",
                otherDayId, strengthExerciseId);
        Long otherActivityId = id("SELECT id FROM workout_activities WHERE workout_day_id = ?", otherDayId);
        var source = sources.getFirst();
        WorkoutActivity otherActivity = WorkoutActivity.restore(new WorkoutActivityId(otherActivityId),
                new WorkoutDayId(otherDayId), source.exercise().getId(), 1,
                null, null, null, null, null, null, null, null, null, null, CREATED, null);
        WorkoutSession secondSession = newSession(otherCycleId, otherDayId,
                List.of(new WorkoutSessionActivitySource(otherActivity, source.exercise(), source.activityType())));
        CyclicBarrier barrier = new CyclicBarrier(2);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Function<WorkoutSession, Callable<Boolean>> insert = session -> () -> {
                barrier.await(10, TimeUnit.SECONDS);
                try {
                    repository.save(session);
                    return true;
                } catch (WorkoutSessionConflictException ex) {
                    assertThat(rootCause(ex)).isInstanceOf(java.sql.SQLException.class);
                    assertThat(((java.sql.SQLException) rootCause(ex)).getSQLState()).isEqualTo("23505");
                    return false;
                }
            };
            Future<Boolean> first = executor.submit(insert.apply(firstSession));
            Future<Boolean> second = executor.submit(insert.apply(secondSession));
            assertThat(List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE person_id = ? AND status = 'IN_PROGRESS'",
                Long.class, owner.value())).isEqualTo(1);
    }

    @Test
    void shouldRejectSavingSessionUnderAnotherOwner() {
        WorkoutSession saved = repository.save(newSession());
        WorkoutSession foreign = WorkoutSession.restore(saved.getId(), new PersonId(owner.value() + 1000),
                saved.getWorkoutCycleId(), saved.getWorkoutDayId(), saved.getSnapshot(), saved.getPerformedOn(),
                saved.getStartedAt(), saved.getFinishedAt(), saved.getStatus(), saved.getCloseReason(),
                "Tentativa de alteração", saved.getCreatedAt(), saved.getUpdatedAt(), saved.getVersion(), saved.getActivities());
        assertThatThrownBy(() -> repository.save(foreign)).isInstanceOf(WorkoutSessionConflictException.class);
        assertThat(repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow().getNotes()).isNull();
    }

    @Test
    void shouldRejectIncompleteOrStalePlanningCaptures() {
        WorkoutSession incomplete = newSession(cycleId, dayId, List.of(sources.getFirst()));
        assertThatThrownBy(() -> repository.save(incomplete)).isInstanceOf(WorkoutSessionConflictException.class);
        WorkoutSession stale = newSession();
        jdbc.update("UPDATE workout_activities SET sets = 8 WHERE workout_day_id = ?", dayId);
        assertThatThrownBy(() -> repository.save(stale)).isInstanceOf(WorkoutSessionConflictException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE person_id = ?",
                Long.class, owner.value())).isZero();
    }

    @Test
    void shouldPreventConcurrentEditsFromOverwritingExecution() throws Exception {
        WorkoutSession saved = repository.save(newSession());
        WorkoutSession first = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
        WorkoutSession second = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
        first.addSet(owner, first.getActivities().getFirst().getId(), 1,
                null, null, null, "Primeiro", START.plusSeconds(1));
        second.addSet(owner, second.getActivities().getFirst().getId(), 2,
                null, null, null, "Segundo", START.plusSeconds(1));
        CyclicBarrier barrier = new CyclicBarrier(2);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Function<WorkoutSession, Callable<Boolean>> update = session -> () -> {
                barrier.await(10, TimeUnit.SECONDS);
                try {
                    repository.save(session);
                    return true;
                } catch (WorkoutSessionConflictException exception) {
                    return false;
                }
            };
            Future<Boolean> firstResult = executor.submit(update.apply(first));
            Future<Boolean> secondResult = executor.submit(update.apply(second));
            assertThat(List.of(firstResult.get(30, TimeUnit.SECONDS), secondResult.get(30, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        }
        assertThat(repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow()
                .getActivities().getFirst().getSets()).hasSize(1);
    }

    @Test
    void shouldPreventConcurrentClosingAndChildEditsFromOverwritingEachOther() throws Exception {
        WorkoutSession saved = repository.save(newSession());
        WorkoutSession closing = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
        WorkoutSession editing = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
        closing.closeManually(owner, START.plusSeconds(1));
        editing.addSet(owner, editing.getActivities().getFirst().getId(), 1,
                null, null, null, "Registro concorrente", START.plusSeconds(1));
        CyclicBarrier barrier = new CyclicBarrier(2);
        boolean closed;
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Function<WorkoutSession, Callable<Boolean>> update = session -> () -> {
                barrier.await(10, TimeUnit.SECONDS);
                try {
                    repository.save(session);
                    return true;
                } catch (WorkoutSessionConflictException exception) {
                    return false;
                }
            };
            Future<Boolean> closeResult = executor.submit(update.apply(closing));
            Future<Boolean> editResult = executor.submit(update.apply(editing));
            closed = closeResult.get(30, TimeUnit.SECONDS);
            assertThat(List.of(closed, editResult.get(30, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        }
        WorkoutSession history = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
        assertThat(history.getStatus()).isEqualTo(closed ? WorkoutSessionStatus.CLOSED : WorkoutSessionStatus.IN_PROGRESS);
        assertThat(history.getActivities().getFirst().getSets()).hasSize(closed ? 0 : 1);
    }

    @Test
    void shouldAllowDistinctCreationTimestampWithFreshMigrations() {
        WorkoutSession saved = repository.save(newSession());
        jdbc.update("""
                UPDATE workout_sessions SET created_at = started_at + INTERVAL '1 second' WHERE id = ?
                """, saved.getId().value());

        WorkoutSession restored = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
        assertThat(restored.getCreatedAt()).isEqualTo(START.plusSeconds(1));
        assertThat(restored.getStartedAt()).isEqualTo(START);
        assertThat(restored.getSnapshot()).isEqualTo(saved.getSnapshot());
        assertThat(jdbc.queryForObject("""
                SELECT version FROM flyway_schema_history
                WHERE success AND type = 'SQL' ORDER BY installed_rank DESC LIMIT 1
                """, String.class)).isEqualTo("26");
    }

    @Test
    void shouldCreateThroughRepositorySave() {
        WorkoutSession saved = repository.save(newSession());
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findByIdAndPersonId(saved.getId(), owner)).isPresent();
    }

    @Test
    void shouldRejectDayWithoutActivitiesAndPlanningEmptiedAfterCapture() {
        Long emptyDayId = id("""
                INSERT INTO workout_days (workout_cycle_id, week_day, title, sort_order, created_at)
                VALUES (?, 3, 'Dia novo sem atividades', 1, now()) RETURNING id
                """, cycleId);
        assertThatThrownBy(() -> newSession(cycleId, emptyDayId, List.of()))
                .isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> newSession(cycleId, dayId, List.of()))
                .isInstanceOf(InvalidWorkoutSessionException.class);
        WorkoutSession candidate = newSession();
        jdbc.update("DELETE FROM workout_activities WHERE workout_day_id = ?", dayId);
        assertThatThrownBy(() -> repository.save(candidate)).isInstanceOf(WorkoutSessionConflictException.class);
        assertThatThrownBy(() -> newSession(cycleId, dayId, List.of()))
                .isInstanceOf(InvalidWorkoutSessionException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE person_id = ?",
                Long.class, owner.value())).isZero();
    }

    @Test
    void shouldAcceptEquivalentDecimalScalesFromPlanningAndOnHistoryEdits() {
        List<WorkoutSessionActivitySource> scaled = sources.stream().map(source -> {
            WorkoutActivity a = source.activity();
            WorkoutActivity copy = WorkoutActivity.restore(a.getId(), a.getWorkoutDayId(), a.getExerciseId(),
                    a.getOrderIndex(), a.getSets(), a.getRepRangeMin(), a.getRepRangeMax(),
                    a.getTargetLoadKg() == null ? null : a.getTargetLoadKg().stripTrailingZeros(),
                    a.getTargetLoadText(), a.getDurationMinutes(),
                    a.getDistanceKm() == null ? null : a.getDistanceKm().stripTrailingZeros(),
                    a.getIntensityText(), a.getRestSeconds(), a.getNotes(), a.getCreatedAt(), a.getUpdatedAt());
            return new WorkoutSessionActivitySource(copy, source.exercise(), source.activityType());
        }).toList();
        WorkoutSession saved = repository.save(newSession(cycleId, dayId, scaled));
        saved.updateNotes(owner, "Escalas equivalentes", START.plusSeconds(1));
        saved = repository.save(saved);
        assertThat(saved.getNotes()).isEqualTo("Escalas equivalentes");
        assertThat(saved.getActivities().getFirst().getSnapshot().plannedTargetLoadKg()).isEqualByComparingTo("45.50");
        assertThat(saved.getActivities().getLast().getSnapshot().plannedDistanceKm()).isEqualByComparingTo("3.00");
    }

    @ParameterizedTest
    @ValueSource(ints = {TransactionDefinition.ISOLATION_REPEATABLE_READ, TransactionDefinition.ISOLATION_SERIALIZABLE})
    void shouldRejectJoinedTransactionThatCouldHidePlanningInserts(int isolationLevel) {
        WorkoutSession candidate = newSession();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setIsolationLevel(isolationLevel);
        assertThatThrownBy(() -> transaction.execute(status -> repository.save(candidate)))
                .isInstanceOf(WorkoutSessionConflictException.class)
                .hasMessage("Não foi possível validar a prescrição nesta transação. Tente novamente.");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE person_id = ?",
                Long.class, owner.value())).isZero();
    }

    @Test
    void shouldReadPlanningWithTheExistingDomainTextConventions() {
        WorkoutSession candidate = newSession();
        jdbc.update("UPDATE exercises SET name = ' Agachamento ' WHERE id = ?", strengthExerciseId);
        jdbc.update("UPDATE workout_cycles SET name = ' Ciclo histórico ' WHERE id = ?", cycleId);
        jdbc.update("UPDATE workout_days SET title = ' Treino misto ' WHERE id = ?", dayId);
        jdbc.update("UPDATE workout_activities SET notes = ' Prescrição original ' WHERE workout_day_id = ?", dayId);
        jdbc.update("UPDATE activity_types SET code = ' strength ' WHERE id = ?", strengthTypeId);
        try {
            assertThat(repository.save(candidate).getSnapshot()).isEqualTo(candidate.getSnapshot());
        } finally {
            jdbc.update("UPDATE activity_types SET code = 'STRENGTH' WHERE id = ?", strengthTypeId);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOAD", "DISTANCE"})
    void shouldRejectActualNumericChangesInPlanning(String field) {
        WorkoutSession stale = newSession();
        if ("LOAD".equals(field)) {
            jdbc.update("UPDATE workout_activities SET target_load_kg = 45.51 WHERE id = ?",
                    sources.getFirst().activity().getId().value());
        } else {
            jdbc.update("UPDATE workout_activities SET distance_km = 3.01 WHERE id = ?",
                    sources.getLast().activity().getId().value());
        }
        assertThatThrownBy(() -> repository.save(stale)).isInstanceOf(WorkoutSessionConflictException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE person_id = ?",
                Long.class, owner.value())).isZero();
    }

    @Test
    void shouldRejectInitialSnapshotOwnedByAnotherPerson() {
        PersonId other = new PersonId(owner.value() + 1000);
        WorkoutCycle cycle = WorkoutCycle.restore(new WorkoutCycleId(cycleId), other, "Ciclo histórico",
                new TrainingGoalId(goalId), LocalDate.of(2026, 10, 1), null, null, WeekDay.SUNDAY,
                WorkoutStatus.IN_PROGRESS, WorkoutOrigin.MANUAL, null, CREATED, null);
        WorkoutDay day = WorkoutDay.restore(new WorkoutDayId(dayId), cycle.getId(), WeekDay.MONDAY,
                "Treino misto", 1, CREATED, null);
        WorkoutSession foreign = WorkoutSession.register(other, cycle, day, sources, START);
        assertThatThrownBy(() -> repository.save(foreign)).isInstanceOf(WorkoutSessionConflictException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM workout_sessions WHERE person_id = ?",
                Long.class, owner.value())).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"UPDATE", "INSERT", "DELETE", "CATALOG", "CYCLE", "DAY", "TYPE", "EXERCISE"})
    void shouldKeepPlanningStableUntilSessionIsPersisted(String operation) throws Exception {
        CountDownLatch captured = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        WorkoutSession candidate = newSession();
        Long extraExerciseId = "INSERT".equals(operation) || "EXERCISE".equals(operation)
                ? exercise("Exercício adicional", strengthTypeId) : null;
        WorkoutSessionPlanningQueryAdapter queryTarget = AopTestUtils.getUltimateTargetObject(planningQueryAdapter);
        doAnswer(invocation -> {
            Object projection = invocation.callRealMethod();
            captured.countDown();
            if (!release.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Session creation was not released");
            }
            return projection;
        }).when(queryTarget).lockByDayIdAndPersonId(new WorkoutDayId(dayId), owner);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<WorkoutSession> creation = executor.submit(() -> repository.save(candidate));
            try {
                assertThat(captured.await(10, TimeUnit.SECONDS)).isTrue();
                Future<Integer> edit = executor.submit(() -> switch (operation) {
                    case "UPDATE" -> jdbc.update("""
                            /* session-planning-edit */ UPDATE workout_activities SET target_load_kg = 46.50 WHERE id = ?
                            """, sources.getFirst().activity().getId().value());
                    case "INSERT" -> jdbc.update("""
                            /* session-planning-edit */ INSERT INTO workout_activities
                                (workout_day_id, exercise_id, order_index, created_at) VALUES (?, ?, 3, now())
                            """, dayId, extraExerciseId);
                    case "DELETE" -> jdbc.update("""
                            /* session-planning-edit */ DELETE FROM workout_activities WHERE id = ?
                            """, sources.getLast().activity().getId().value());
                    case "CATALOG" -> jdbc.update("""
                            /* session-planning-edit */ UPDATE exercises SET name = 'Nome alterado' WHERE id = ?
                            """, strengthExerciseId);
                    case "CYCLE" -> jdbc.update("""
                            /* session-planning-edit */ UPDATE workout_cycles SET workout_status = 'COMPLETED' WHERE id = ?
                            """, cycleId);
                    case "DAY" -> jdbc.update("""
                            /* session-planning-edit */ UPDATE workout_days SET title = 'Título alterado' WHERE id = ?
                            """, dayId);
                    case "TYPE" -> jdbc.update("""
                            /* session-planning-edit */ UPDATE activity_types SET code = 'STRENGTH_UPDATED' WHERE id = ?
                            """, strengthTypeId);
                    case "EXERCISE" -> jdbc.update("""
                            /* session-planning-edit */ UPDATE workout_activities SET exercise_id = ? WHERE id = ?
                            """, extraExerciseId, sources.getFirst().activity().getId().value());
                    default -> throw new IllegalArgumentException(operation);
                });
                await().atMost(5, TimeUnit.SECONDS).until(() -> Boolean.TRUE.equals(jdbc.queryForObject("""
                        SELECT EXISTS (SELECT 1 FROM pg_stat_activity
                        WHERE datname = current_database() AND query LIKE '/* session-planning-edit */%'
                          AND wait_event_type = 'Lock')
                        """, Boolean.class)));
                assertThat(edit.isDone()).isFalse();
                release.countDown();
                WorkoutSession saved = creation.get(10, TimeUnit.SECONDS);
                assertThat(edit.get(10, TimeUnit.SECONDS)).isEqualTo(1);
                WorkoutSession history = repository.findByIdAndPersonId(saved.getId(), owner).orElseThrow();
                assertThat(history.getSnapshot()).isEqualTo(candidate.getSnapshot());
                assertThat(history.getActivities()).extracting(activity -> activity.getSnapshot())
                        .containsExactlyElementsOf(candidate.getActivities().stream()
                                .map(activity -> activity.getSnapshot()).toList());
            } finally {
                release.countDown();
            }
        } finally {
            if ("TYPE".equals(operation)) {
                jdbc.update("UPDATE activity_types SET code = 'STRENGTH' WHERE id = ?", strengthTypeId);
            }
            if (extraExerciseId != null) {
                jdbc.update("DELETE FROM workout_activities WHERE exercise_id = ?", extraExerciseId);
                jdbc.update("DELETE FROM exercises WHERE id = ?", extraExerciseId);
            }
        }
    }

    @Test
    void shouldEnforceSetNumbersForeignKeysAndExecutionPairsInDatabase() {
        WorkoutSession saved = repository.save(newSession());
        Long activityId = saved.getActivities().getLast().getId().value();
        jdbc.update("""
                INSERT INTO workout_session_sets (workout_session_activity_id, set_number, notes, created_at)
                VALUES (?, 1, 'Relato sem prescrição', now())
                """, activityId);
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO workout_session_sets (workout_session_activity_id, set_number, notes, created_at)
                VALUES (?, 1, 'Duplicada', now())
                """, activityId)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO workout_session_sets (workout_session_activity_id, set_number, notes, created_at)
                VALUES (?, 0, 'Inválida', now())
                """, activityId)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO workout_session_sets (workout_session_activity_id, set_number, actual_load_kg, created_at)
                VALUES (?, 2, 10.00, now())
                """, activityId)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO workout_session_sets (workout_session_activity_id, set_number, notes, created_at)
                VALUES (-1, 1, 'Sem pai', now())
                """)).isInstanceOf(DataIntegrityViolationException.class);
    }

    private WorkoutSession newSession() {
        return newSession(cycleId, dayId, sources);
    }

    private WorkoutSession newSession(Long originCycleId, Long originDayId, List<WorkoutSessionActivitySource> activities) {
        WorkoutCycle cycle = WorkoutCycle.restore(new WorkoutCycleId(originCycleId), owner,
                jdbc.queryForObject("SELECT name FROM workout_cycles WHERE id = ?", String.class, originCycleId),
                new TrainingGoalId(goalId), LocalDate.of(2026, 10, 1), null, null, WeekDay.SUNDAY,
                WorkoutStatus.IN_PROGRESS, WorkoutOrigin.MANUAL, null, CREATED, null);
        WorkoutDay day = WorkoutDay.restore(new WorkoutDayId(originDayId), cycle.getId(),
                WeekDay.fromValue(jdbc.queryForObject("SELECT week_day FROM workout_days WHERE id = ?", Integer.class, originDayId)),
                jdbc.queryForObject("SELECT title FROM workout_days WHERE id = ?", String.class, originDayId), 1, CREATED, null);
        return WorkoutSession.register(owner, cycle, day, activities, START);
    }

    private Long exercise(String name, Long typeId) {
        return id("""
                INSERT INTO exercises (name, equipment_type_id, activity_type_id, unilateral, compound, active, created_at)
                VALUES (?, ?, ?, false, false, true, now()) RETURNING id
                """, name, equipmentId, typeId);
    }

    private WorkoutSessionActivitySource activity(Long exerciseId, Long typeId, String code, int order, Integer sets) {
        Long activityId = id("""
                INSERT INTO workout_activities (workout_day_id, exercise_id, order_index, sets, created_at)
                VALUES (?, ?, ?, ?, now()) RETURNING id
                """, dayId, exerciseId, order, sets);
        ActivityType type = ActivityType.restore(new ActivityTypeId(typeId), new ActivityTypeCode(code), code, true, order);
        Exercise exercise = Exercise.restore(new ExerciseId(exerciseId), sets == null ? "Caminhada" : "Agachamento",
                new EquipmentTypeId(equipmentId), type.getId(), false, false, null, true, CREATED, null);
        WorkoutActivity activity = WorkoutActivity.restore(new WorkoutActivityId(activityId), new WorkoutDayId(dayId),
                exercise.getId(), order, sets, sets == null ? null : 8, sets == null ? null : 12,
                sets == null ? null : new BigDecimal("45.50"), sets == null ? null : "Cada lado",
                sets == null ? 30 : null, sets == null ? new BigDecimal("3.00") : null,
                "Moderada", 60, "Prescrição original", CREATED, null);
        jdbc.update("""
                UPDATE workout_activities SET rep_range_min = ?, rep_range_max = ?, target_load_kg = ?,
                    target_load_text = ?, duration_minutes = ?, distance_km = ?, intensity_text = ?,
                    rest_seconds = ?, notes = ? WHERE id = ?
                """, activity.getRepRangeMin(), activity.getRepRangeMax(), activity.getTargetLoadKg(),
                activity.getTargetLoadText(), activity.getDurationMinutes(), activity.getDistanceKm(),
                activity.getIntensityText(), activity.getRestSeconds(), activity.getNotes(), activityId);
        return new WorkoutSessionActivitySource(activity, exercise, type);
    }

    private Long id(String sql, Object... args) { return jdbc.queryForObject(sql, Long.class, args); }

    private Throwable rootCause(Throwable ex) {
        while (ex.getCause() != null) {
            ex = ex.getCause();
        }
        return ex;
    }
}
