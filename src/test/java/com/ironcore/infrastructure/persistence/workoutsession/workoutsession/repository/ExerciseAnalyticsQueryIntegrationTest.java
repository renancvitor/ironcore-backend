package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.application.workoutsession.workoutsession.analytics.*;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutsession.workoutsession.enums.*;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;
import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.entity.WorkoutSessionActivityEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionset.entity.WorkoutSessionSetEntity;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest(showSql = false, properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@ActiveProfiles("test")
@Import(ExerciseAnalyticsQueryAdapter.class)
class ExerciseAnalyticsQueryIntegrationTest {
    @Autowired EntityManager em;
    @Autowired ExerciseAnalyticsQueryAdapter adapter;
    PersonEntity owner;
    ExerciseEntity exercise;
    Long early, tiedFirst, tiedLast;
    static final LocalDate DATE = LocalDate.of(2026, 10, 26);

    @BeforeEach
    void fixtures() {
        owner = person();
        var type = new ActivityTypeEntity(null, "ANALYTICS_" + UUID.randomUUID(), "Type", true, 1);
        var equipment = new EquipmentTypeEntity(null, "ANALYTICS_" + UUID.randomUUID(), "Equipment", true, 1);
        em.persist(type);
        em.persist(equipment);
        // Inactive current catalog entry must not suppress historical execution.
        exercise = new ExerciseEntity(null, "Current name", equipment, type, false, false, null, false,
                LocalDateTime.of(2026, 1, 1, 0, 0), null);
        em.persist(exercise);
        var first = session(owner, DATE, 10, WorkoutSessionCloseReason.MANUAL);
        early = first.getId();
        var a = activity(first, 1, 30, "5.00");
        set(a, 1, "20.10", ActualLoadMode.PER_SIDE, 3);
        set(a, 2, "50", ActualLoadMode.PER_SIDE, null);
        set(a, 3, null, null, 10);
        set(a, 4, null, null, null);
        activity(first, 2, 20, null);
        var second = session(owner, DATE, 11, WorkoutSessionCloseReason.MANUAL);
        tiedFirst = second.getId();
        set(activity(second, 1, 10, "1"), 1, "0", ActualLoadMode.TOTAL, 10);
        var third = session(owner, DATE, 11, WorkoutSessionCloseReason.AUTO_EXPIRED);
        tiedLast = third.getId();
        activity(third, 1, null, null);
        activity(session(owner, DATE.minusDays(1), 10, WorkoutSessionCloseReason.MANUAL), 1, 999, "999");
        activity(session(owner, DATE.plusDays(1), 10, WorkoutSessionCloseReason.MANUAL), 1, 999, "999");
        activity(session(owner, DATE, 12, null), 1, 999, "999");
        activity(session(person(), DATE, 12, WorkoutSessionCloseReason.MANUAL), 1, 999, "999");
        var otherExercise = new ExerciseEntity(null, "Other", equipment, type, false, false, null, true,
                LocalDateTime.of(2026, 1, 1, 0, 0), null);
        em.persist(otherExercise);
        activity(first, 3, 999, "999", otherExercise.getId());
        em.flush();
        em.clear();
    }

    @Test
    void shouldFilterOwnerExerciseInclusivePeriodAndOrderWithoutLoadingEntitiesOrWriting() {
        var statistics = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var strength = strength();
        var cardio = cardio();
        assertThat(strength).extracting(ExerciseAnalyticsQueryPort.StrengthRow::sessionId)
                .containsExactly(early, early, early, early, early, tiedFirst, tiedLast);
        assertThat(cardio).extracting(ExerciseAnalyticsQueryPort.CardioRow::sessionId)
                .containsExactly(early, early, tiedFirst, tiedLast);
        assertThat(cardio).extracting(ExerciseAnalyticsQueryPort.CardioRow::exerciseNameSnapshot)
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
        assertThat(em.createQuery("select count(s) from WorkoutSessionEntity s where s.status = :status", Long.class)
                .setParameter("status", WorkoutSessionStatus.IN_PROGRESS).getSingleResult()).isOne();
        assertThat(adapter.cardio(new PersonId(owner.getId()), new ExerciseId(Long.MAX_VALUE), DATE, DATE)).isEmpty();
        assertThat(adapter.strength(new PersonId(Long.MAX_VALUE), new ExerciseId(exercise.getId()), DATE, DATE)).isEmpty();
    }

    @Test
    void shouldCalculateWholePeriodFromRealProjectionsAndKeepDistinctSessionsAndPartialCoverage() {
        var users = mock(UserRepository.class);
        var persons = mock(PersonRepository.class);
        var user = mock(User.class);
        var person = mock(Person.class);
        var personId = new PersonId(owner.getId());
        when(users.findById(new UserId(77L))).thenReturn(Optional.of(user));
        when(user.isActive()).thenReturn(true);
        when(user.getPersonId()).thenReturn(personId);
        when(persons.findById(personId)).thenReturn(Optional.of(person));
        when(person.getId()).thenReturn(personId);
        var useCase = new GetExerciseAnalyticsUseCase(users, persons, adapter,
                Clock.fixed(Instant.parse("2026-10-27T12:00:00Z"), ZoneOffset.UTC));
        var command = new ExerciseAnalyticsCommand(new UserId(77L), new ExerciseId(exercise.getId()), DATE, DATE);
        var strength = useCase.strength(command);
        assertThat(strength.points()).extracting(ExerciseAnalyticsResult.StrengthPoint::sessionId)
                .containsExactly(early, tiedFirst, tiedLast);
        var first = strength.points().getFirst().metrics();
        assertThat(first.maxNormalizedLoadKg()).isEqualByComparingTo("40.20");
        assertThat(first.volumeKgReps()).isEqualByComparingTo("120.60");
        assertThat(first.registeredSetCount()).isEqualTo(4);
        assertThat(first.loadedSetCount()).isEqualTo(2);
        assertThat(first.volumeSetCount()).isOne();
        assertThat(strength.points().get(1).metrics().maxNormalizedLoadKg()).isEqualByComparingTo("0");
        assertThat(strength.points().getLast().metrics().maxNormalizedLoadKg()).isNull();
        var cardio = useCase.cardio(command);
        assertThat(cardio.points()).hasSize(3);
        assertThat(cardio.points().getFirst().metrics().durationMinutes()).isEqualTo(50);
        assertThat(cardio.summary().durationMinutes()).isEqualTo(60);
        assertThat(cardio.summary().distanceKm()).isEqualByComparingTo("6");
        assertThat(cardio.summary().averagePaceMinPerKm()).isEqualByComparingTo("6.67");
        assertThat(cardio.summary().averageSpeedKmPerHour()).isEqualByComparingTo("9");
        assertThat(cardio.points().getLast().metrics().distanceKm()).isNull();
        em.createQuery("update WorkoutSessionSetEntity x set x.actualReps=2 where x.activity.session.id=:id and x.setNumber=2")
                .setParameter("id", early).executeUpdate();
        em.clear();
        var corrected = useCase.strength(command).points().getFirst().metrics();
        assertThat(corrected.maxNormalizedLoadKg()).isEqualByComparingTo("100");
        assertThat(corrected.volumeKgReps()).isEqualByComparingTo("320.60");
    }

    @Test
    void shouldReflectCorrectionsAndPreserveMissingPlanningOriginsAndSnapshots() {
        var before = cardio();
        em.createQuery("update WorkoutSessionActivityEntity a set a.actualDistanceKm = 6.25 where a.session.id = :id and a.orderIndexSnapshot = 1")
                .setParameter("id", early).executeUpdate();
        em.createQuery("update ExerciseEntity e set e.name = 'Changed' where e.id = :id")
                .setParameter("id", exercise.getId()).executeUpdate();
        em.clear();
        assertThat(cardio().getFirst().distanceKm()).isEqualByComparingTo("6.25");
        assertThat(cardio().getFirst().exerciseNameSnapshot()).isEqualTo(before.getFirst().exerciseNameSnapshot());
        assertThat(em.find(WorkoutSessionEntity.class, early).getWorkoutCycleId()).isNull();
        assertThat(em.find(WorkoutSessionEntity.class, early).getVersion()).isZero();
        assertThat(em.find(WorkoutSessionEntity.class, tiedLast).getCloseReason()).isEqualTo(WorkoutSessionCloseReason.AUTO_EXPIRED);
    }

    List<ExerciseAnalyticsQueryPort.StrengthRow> strength() {
        return adapter.strength(new PersonId(owner.getId()), new ExerciseId(exercise.getId()), DATE, DATE);
    }
    List<ExerciseAnalyticsQueryPort.CardioRow> cardio() {
        return adapter.cardio(new PersonId(owner.getId()), new ExerciseId(exercise.getId()), DATE, DATE);
    }
    PersonEntity person() {
        var p = new PersonEntity(null, "Owner", SexType.MALE, LocalDate.of(1990, 1, 1),
                LocalDateTime.of(2026, 1, 1, 0, 0), null);
        em.persist(p);
        return p;
    }
    WorkoutSessionEntity session(PersonEntity person, LocalDate date, int hour, WorkoutSessionCloseReason reason) {
        var s = new WorkoutSessionEntity();
        s.setPerson(person);
        s.setWorkoutCycleNameSnapshot("Deleted cycle");
        s.setWorkoutDayTitleSnapshot("Deleted day");
        s.setWeekDaySnapshot(WeekDay.MONDAY);
        s.setPerformedOn(date);
        s.setStartedAt(date.atTime(hour, 0).atZone(ZoneId.of("America/Sao_Paulo")).toInstant());
        s.setCreatedAt(s.getStartedAt());
        s.setStatus(reason == null ? WorkoutSessionStatus.IN_PROGRESS : WorkoutSessionStatus.CLOSED);
        s.setCloseReason(reason);
        s.setFinishedAt(reason == null ? null : reason == WorkoutSessionCloseReason.MANUAL
                ? s.getStartedAt().plusSeconds(60)
                : date.plusDays(1).atTime(3, 0).atZone(ZoneId.of("America/Sao_Paulo")).toInstant());
        em.persist(s);
        return s;
    }
    WorkoutSessionActivityEntity activity(WorkoutSessionEntity session, int order, Integer duration, String distance) {
        return activity(session, order, duration, distance, exercise.getId());
    }
    WorkoutSessionActivityEntity activity(WorkoutSessionEntity session, int order, Integer duration, String distance, Long exerciseId) {
        var a = new WorkoutSessionActivityEntity();
        a.setSession(session);
        a.setExerciseId(exerciseId);
        a.setExerciseNameSnapshot("Historical name");
        a.setActivityTypeSnapshot("CONDITIONING");
        a.setOrderIndexSnapshot(order);
        a.setActualDurationMinutes(duration);
        a.setActualDistanceKm(distance == null ? null : new BigDecimal(distance));
        a.setCreatedAt(session.getStartedAt());
        em.persist(a);
        return a;
    }
    void set(WorkoutSessionActivityEntity activity, int number, String load, ActualLoadMode mode, Integer reps) {
        var s = new WorkoutSessionSetEntity();
        s.setActivity(activity);
        s.setSetNumber(number);
        s.setActualLoadKg(load == null ? null : new BigDecimal(load));
        s.setActualLoadMode(mode);
        s.setActualReps(reps);
        s.setNotes("Recorded");
        s.setCreatedAt(activity.getCreatedAt());
        em.persist(s);
    }
}
