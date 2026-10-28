package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseCardioExecutionResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseStrengthExecutionResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseCardioEvolutionCommand;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseCardioEvolutionUseCase;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseStrengthEvolutionCommand;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseStrengthEvolutionUseCase;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;
import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.entity.WorkoutSessionActivityEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionset.entity.WorkoutSessionSetEntity;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;

@DataJpaTest(showSql = false, properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@ActiveProfiles("test")
@Import(ExerciseAnalyticsQueryAdapter.class)
class ExerciseAnalyticsQueryIntegrationTest {

    static final LocalDate DATE = LocalDate.of(2026, 10, 26);

    @Autowired
    EntityManager entityManager;

    @Autowired
    ExerciseAnalyticsQueryAdapter adapter;

    PersonEntity owner;
    ExerciseEntity exercise;
    Long earlySessionId;
    Long firstTiedSessionId;
    Long lastTiedSessionId;

    @BeforeEach
    void setUp() {
        owner = createPerson();
        var type = new ActivityTypeEntity(null, "ANALYTICS_" + UUID.randomUUID(), "Type", true, 1);
        var equipment = new EquipmentTypeEntity(null, "ANALYTICS_" + UUID.randomUUID(), "Equipment", true, 1);
        entityManager.persist(type);
        entityManager.persist(equipment);
        exercise = new ExerciseEntity(null, "Current name", equipment, type, false, false, null, false,
                LocalDateTime.of(2026, 1, 1, 0, 0), null);
        entityManager.persist(exercise);
        var first = createSession(owner, DATE, 10, WorkoutSessionCloseReason.MANUAL);
        earlySessionId = first.getId();
        var firstActivity = createActivity(first, 1, 30, "5.00");
        createSet(firstActivity, 1, "20.10", ActualLoadMode.PER_SIDE, 3);
        createSet(firstActivity, 2, "50", ActualLoadMode.PER_SIDE, null);
        createSet(firstActivity, 3, null, null, 10);
        createSet(firstActivity, 4, null, null, null);
        createActivity(first, 2, 20, null);
        var second = createSession(owner, DATE, 11, WorkoutSessionCloseReason.MANUAL);
        firstTiedSessionId = second.getId();
        createSet(createActivity(second, 1, 10, "1"), 1, "0", ActualLoadMode.TOTAL, 10);
        var third = createSession(owner, DATE, 11, WorkoutSessionCloseReason.AUTO_EXPIRED);
        lastTiedSessionId = third.getId();
        createActivity(third, 1, null, null);
        createActivity(createSession(owner, DATE.minusDays(1), 10, WorkoutSessionCloseReason.MANUAL), 1, 999, "999");
        createActivity(createSession(owner, DATE.plusDays(1), 10, WorkoutSessionCloseReason.MANUAL), 1, 999, "999");
        createActivity(createSession(owner, DATE, 12, null), 1, 999, "999");
        createActivity(createSession(createPerson(), DATE, 12, WorkoutSessionCloseReason.MANUAL), 1, 999, "999");
        var otherExercise = new ExerciseEntity(null, "Other", equipment, type, false, false, null, true,
                LocalDateTime.of(2026, 1, 1, 0, 0), null);
        entityManager.persist(otherExercise);
        createActivity(first, 3, 999, "999", otherExercise.getId());
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFilterOwnerExerciseInclusivePeriodAndOrderWithoutLoadingEntitiesOrWriting() {
        var statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var strength = strength();
        var cardio = cardio();
        assertThat(strength).extracting(execution -> execution.sessionId().value())
                .containsExactly(earlySessionId, earlySessionId, earlySessionId, earlySessionId, earlySessionId, firstTiedSessionId, lastTiedSessionId);
        assertThat(cardio).extracting(execution -> execution.sessionId().value())
                .containsExactly(earlySessionId, earlySessionId, firstTiedSessionId, lastTiedSessionId);
        assertThat(cardio).extracting(ExerciseCardioExecutionResult::exerciseNameSnapshot)
                .containsOnly("Historical name");
        assertThat(strength.getFirst().loadKg()).isEqualByComparingTo("20.10");
        assertThat(strength.getFirst().mode()).isEqualTo(ActualLoadMode.PER_SIDE);
        assertThat(strength.getLast().setId()).isNull();
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
        assertThat(statistics.getEntityLoadCount()).isZero();
        assertThat(statistics.getCollectionFetchCount()).isZero();
        assertThat(statistics.getEntityUpdateCount()).isZero();
        assertThat(statistics.getEntityInsertCount()).isZero();
        assertThat(statistics.getEntityDeleteCount()).isZero();
        assertThat(entityManager.createQuery("select count(s) from WorkoutSessionEntity s where s.status = :status", Long.class)
                .setParameter("status", WorkoutSessionStatus.IN_PROGRESS).getSingleResult()).isOne();
        assertThat(adapter.findCardioExecution(new PersonId(owner.getId()), new ExerciseId(Long.MAX_VALUE), DATE, DATE)).isEmpty();
        assertThat(adapter.findStrengthExecution(new PersonId(Long.MAX_VALUE), new ExerciseId(exercise.getId()), DATE, DATE)).isEmpty();
    }

    @Test
    void shouldCalculateWholePeriodFromRealProjectionsAndKeepDistinctSessionsAndPartialCoverage() {
        var userRepository = mock(UserRepository.class);
        var personRepository = mock(PersonRepository.class);
        var personId = new PersonId(owner.getId());
        var userTemplate = activeUser();
        var user = User.restore(
                new UserId(77L), userTemplate.getNickname(), personId, userTemplate.getEmail(),
                userTemplate.getPasswordHash(), false, true, userTemplate.getCreatedAt(), userTemplate.getUpdatedAt()
        );
        var personTemplate = restoredPerson();
        var person = Person.restore(
                personId, personTemplate.getName(), personTemplate.getSex(), personTemplate.getBirthDate(),
                personTemplate.getCreatedAt(), personTemplate.getUpdatedAt()
        );
        when(userRepository.findById(new UserId(77L))).thenReturn(Optional.of(user));
        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        var strengthUseCase = new GetExerciseStrengthEvolutionUseCase(userRepository, personRepository, adapter,
                Clock.fixed(Instant.parse("2026-10-27T12:00:00Z"), ZoneOffset.UTC));
        var command = new GetExerciseStrengthEvolutionCommand(new UserId(77L), new ExerciseId(exercise.getId()), DATE, DATE);
        var strength = strengthUseCase.execute(command);
        assertThat(strength.points()).extracting(point -> point.sessionId().value())
                .containsExactly(earlySessionId, firstTiedSessionId, lastTiedSessionId);
        var first = strength.points().getFirst().metrics();
        assertThat(first.maxNormalizedLoadKg()).isEqualByComparingTo("40.20");
        assertThat(first.volumeKgReps()).isEqualByComparingTo("120.60");
        assertThat(first.registeredSetCount()).isEqualTo(4);
        assertThat(first.loadedSetCount()).isEqualTo(2);
        assertThat(first.volumeSetCount()).isOne();
        assertThat(strength.points().get(1).metrics().maxNormalizedLoadKg()).isEqualByComparingTo("0");
        assertThat(strength.points().getLast().metrics().maxNormalizedLoadKg()).isNull();
        var cardioUseCase = new GetExerciseCardioEvolutionUseCase(
                userRepository, personRepository, adapter, Clock.fixed(Instant.parse("2026-10-27T12:00:00Z"), ZoneOffset.UTC)
        );
        var cardio = cardioUseCase.execute(new GetExerciseCardioEvolutionCommand(
                command.actorUserId(), command.exerciseId(), command.startDate(), command.endDate()
        ));
        assertThat(cardio.points()).hasSize(3);
        assertThat(cardio.points().getFirst().metrics().durationMinutes()).isEqualTo(50);
        assertThat(cardio.summary().durationMinutes()).isEqualTo(60);
        assertThat(cardio.summary().distanceKm()).isEqualByComparingTo("6");
        assertThat(cardio.summary().averagePaceMinPerKm()).isEqualByComparingTo("6.67");
        assertThat(cardio.summary().averageSpeedKmPerHour()).isEqualByComparingTo("9");
        assertThat(cardio.points().getLast().metrics().distanceKm()).isNull();
        entityManager.createQuery("update WorkoutSessionSetEntity x set x.actualReps=2 where x.activity.session.id=:id and x.setNumber=2")
                .setParameter("id", earlySessionId).executeUpdate();
        entityManager.clear();
        var corrected = strengthUseCase.execute(command).points().getFirst().metrics();
        assertThat(corrected.maxNormalizedLoadKg()).isEqualByComparingTo("100");
        assertThat(corrected.volumeKgReps()).isEqualByComparingTo("320.60");
    }

    @Test
    void shouldReflectCorrectionsAndPreserveMissingPlanningOriginsAndSnapshots() {
        var before = cardio();
        entityManager.createQuery("update WorkoutSessionActivityEntity a set a.actualDistanceKm = 6.25 where a.session.id = :id and a.orderIndexSnapshot = 1")
                .setParameter("id", earlySessionId).executeUpdate();
        entityManager.createQuery("update ExerciseEntity e set e.name = 'Changed' where e.id = :id")
                .setParameter("id", exercise.getId()).executeUpdate();
        entityManager.clear();
        assertThat(cardio().getFirst().distanceKm()).isEqualByComparingTo("6.25");
        assertThat(cardio().getFirst().exerciseNameSnapshot()).isEqualTo(before.getFirst().exerciseNameSnapshot());
        assertThat(entityManager.find(WorkoutSessionEntity.class, earlySessionId).getWorkoutCycleId()).isNull();
        assertThat(entityManager.find(WorkoutSessionEntity.class, earlySessionId).getVersion()).isZero();
        assertThat(entityManager.find(WorkoutSessionEntity.class, lastTiedSessionId).getCloseReason()).isEqualTo(WorkoutSessionCloseReason.AUTO_EXPIRED);
    }

    List<ExerciseStrengthExecutionResult> strength() {
        return adapter.findStrengthExecution(new PersonId(owner.getId()), new ExerciseId(exercise.getId()), DATE, DATE);
    }

    List<ExerciseCardioExecutionResult> cardio() {
        return adapter.findCardioExecution(new PersonId(owner.getId()), new ExerciseId(exercise.getId()), DATE, DATE);
    }

    PersonEntity createPerson() {
        var person = new PersonEntity(null, "Owner", SexType.MALE, LocalDate.of(1990, 1, 1),
                LocalDateTime.of(2026, 1, 1, 0, 0), null);
        entityManager.persist(person);
        return person;
    }

    WorkoutSessionEntity createSession(PersonEntity person, LocalDate date, int hour, WorkoutSessionCloseReason reason) {
        var session = new WorkoutSessionEntity();
        session.setPerson(person);
        session.setWorkoutCycleNameSnapshot("Deleted cycle");
        session.setWorkoutDayTitleSnapshot("Deleted day");
        session.setWeekDaySnapshot(WeekDay.MONDAY);
        session.setPerformedOn(date);
        session.setStartedAt(date.atTime(hour, 0).atZone(ZoneId.of("America/Sao_Paulo")).toInstant());
        session.setCreatedAt(session.getStartedAt());
        session.setStatus(reason == null ? WorkoutSessionStatus.IN_PROGRESS : WorkoutSessionStatus.CLOSED);
        session.setCloseReason(reason);
        session.setFinishedAt(reason == null ? null : reason == WorkoutSessionCloseReason.MANUAL
                ? session.getStartedAt().plusSeconds(60)
                : date.plusDays(1).atTime(3, 0).atZone(ZoneId.of("America/Sao_Paulo")).toInstant());
        entityManager.persist(session);
        return session;
    }

    WorkoutSessionActivityEntity createActivity(WorkoutSessionEntity session, int order, Integer duration, String distance) {
        return createActivity(session, order, duration, distance, exercise.getId());
    }

    WorkoutSessionActivityEntity createActivity(WorkoutSessionEntity session, int order, Integer duration, String distance, Long exerciseId) {
        var activity = new WorkoutSessionActivityEntity();
        activity.setSession(session);
        activity.setExerciseId(exerciseId);
        activity.setExerciseNameSnapshot("Historical name");
        activity.setActivityTypeSnapshot("CONDITIONING");
        activity.setOrderIndexSnapshot(order);
        activity.setActualDurationMinutes(duration);
        activity.setActualDistanceKm(distance == null ? null : new BigDecimal(distance));
        activity.setCreatedAt(session.getStartedAt());
        entityManager.persist(activity);
        return activity;
    }

    void createSet(WorkoutSessionActivityEntity activity, int number, String load, ActualLoadMode mode, Integer reps) {
        var set = new WorkoutSessionSetEntity();
        set.setActivity(activity);
        set.setSetNumber(number);
        set.setActualLoadKg(load == null ? null : new BigDecimal(load));
        set.setActualLoadMode(mode);
        set.setActualReps(reps);
        set.setNotes("Recorded");
        set.setCreatedAt(activity.getCreatedAt());
        entityManager.persist(set);
    }
}
