package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.application.shared.pagination.*;
import com.ironcore.application.workoutsession.workoutsession.list.ListWorkoutSessionsItemResult;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.*;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.enums.*;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.entity.TrainingGoalEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity.WorkoutDayEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.entity.WorkoutSessionActivityEntity;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest(showSql = false, properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@ActiveProfiles("test")
@Import(ListWorkoutSessionsQueryAdapter.class)
class WorkoutSessionHistoryQueryIntegrationTest {
    @Autowired EntityManager em;
    @Autowired ListWorkoutSessionsQueryAdapter adapter;
    PersonEntity owner;
    WorkoutCycleEntity cycle;
    WorkoutDayEntity day;
    WorkoutCycleEntity otherCycle;
    WorkoutDayEntity otherDay;
    Long oldest;
    Long early;
    Long tieFirst;
    Long tieLast;
    Long withoutOrigin;
    static final LocalDate DATE = LocalDate.of(2026, 10, 26);

    @BeforeEach
    void fixtures() {
        owner = person("Owner");
        var stranger = person("Stranger");
        var goal = new TrainingGoalEntity(null, "HISTORY_" + UUID.randomUUID(), "Histórico", true, 1);
        em.persist(goal);
        cycle = cycle(owner, goal, "Original cycle");
        day = day(cycle, "Original day");
        otherCycle = cycle(owner, goal, "Other cycle");
        otherDay = day(otherCycle, "Other day");
        oldest = session(owner, day, DATE.minusDays(1), 12, WorkoutSessionCloseReason.MANUAL).getId();
        early = session(owner, day, DATE, 11, WorkoutSessionCloseReason.MANUAL).getId();
        tieFirst = session(owner, day, DATE, 12, WorkoutSessionCloseReason.MANUAL).getId();
        tieLast = session(owner, day, DATE, 12, WorkoutSessionCloseReason.AUTO_EXPIRED).getId();
        withoutOrigin = session(owner, null, DATE.plusDays(1), 12, WorkoutSessionCloseReason.MANUAL).getId();
        session(owner, otherDay, DATE.minusDays(2), 12, WorkoutSessionCloseReason.MANUAL);
        session(stranger, null, DATE.plusDays(3), 12, WorkoutSessionCloseReason.MANUAL);
        session(owner, day, DATE.minusDays(4), 12, null);
        em.flush();
        em.clear();
    }

    @Test
    void shouldPageOnlyClosedOwnedSessionsInStableOrderWithoutLoadingAggregates() {
        var statistics = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var first = query(null, null, null, null, 0, 2);
        var second = query(null, null, null, null, 1, 2);
        var last = query(null, null, null, null, 2, 2);
        assertIds(first, withoutOrigin, tieLast);
        assertIds(second, tieFirst, early);
        assertThat(first.totalElements()).isEqualTo(6);
        assertThat(first.totalPages()).isEqualTo(3);
        assertThat(first.page()).isZero();
        assertThat(first.size()).isEqualTo(2);
        assertThat(first.last()).isFalse();
        assertThat(last.last()).isTrue();
        assertThat(first.content()).allSatisfy(row -> assertThat(row.status()).isEqualTo(WorkoutSessionStatus.CLOSED));
        assertThat(first.content()).extracting(ListWorkoutSessionsItemResult::closeReason)
                .containsExactly(WorkoutSessionCloseReason.MANUAL, WorkoutSessionCloseReason.AUTO_EXPIRED);
        assertThat(second.content()).allSatisfy(row -> assertThat(row.status()).isEqualTo(WorkoutSessionStatus.CLOSED));
        assertThat(statistics.getEntityLoadCount()).isZero();
        assertThat(statistics.getCollectionFetchCount()).isZero();
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(6);
        var beyond = query(null, null, null, null, 4, 2);
        assertThat(beyond.content()).isEmpty();
        assertThat(beyond.totalElements()).isEqualTo(6);
        assertThat(beyond.last()).isTrue();
    }

    @Test
    void shouldFilterEachOriginAndCombineFiltersWithMatchingTotals() {
        assertIds(query(cycle.getId(), null, null, null, 0, 20), tieLast, tieFirst, early, oldest);
        assertIds(query(null, day.getId(), null, null, 0, 20), tieLast, tieFirst, early, oldest);
        var filtered = query(cycle.getId(), day.getId(), DATE, DATE, 0, 2);
        assertIds(filtered, tieLast, tieFirst);
        assertThat(filtered.totalElements()).isEqualTo(3);
        assertThat(filtered.totalPages()).isEqualTo(2);
        assertIds(query(cycle.getId(), day.getId(), DATE, DATE, 1, 2), early);
        assertThat(query(cycle.getId(), otherDay.getId(), null, null, 0, 20).totalElements()).isZero();
        assertThat(query(Long.MAX_VALUE, null, null, null, 0, 20).content()).isEmpty();
        assertThat(query(null, Long.MAX_VALUE, null, null, 0, 20).content()).isEmpty();
    }

    @Test
    void shouldApplyInclusiveAndSingleSidedDateBounds() {
        assertIds(query(null, null, DATE, DATE, 0, 20), tieLast, tieFirst, early);
        assertIds(query(null, null, DATE, null, 0, 20), withoutOrigin, tieLast, tieFirst, early);
        assertIds(query(null, null, null, DATE.minusDays(1), 0, 20), oldest,
                query(otherCycle.getId(), null, null, null, 0, 20).content().getFirst().id().value());
        var empty = query(null, null, DATE.plusDays(10), null, 0, 20);
        assertThat(empty.content()).isEmpty();
        assertThat(empty.totalElements()).isZero();
        assertThat(empty.totalPages()).isZero();
        assertThat(empty.last()).isTrue();
    }

    @Test
    void shouldKeepSnapshotsAfterPlanningChangesAndIncludeMissingOrigins() {
        em.createQuery("update WorkoutCycleEntity c set c.name = 'Changed' where c.id = :id")
                .setParameter("id", cycle.getId()).executeUpdate();
        em.createQuery("update WorkoutDayEntity d set d.title = 'Changed' where d.id = :id")
                .setParameter("id", day.getId()).executeUpdate();
        var rows = query(cycle.getId(), day.getId(), DATE, DATE, 0, 20);
        assertThat(rows.content()).hasSize(3).allSatisfy(row -> {
            assertThat(row.workoutCycleNameSnapshot()).isEqualTo("Original cycle");
            assertThat(row.workoutDayTitleSnapshot()).isEqualTo("Original day");
        });
        var missing = query(null, null, DATE.plusDays(1), DATE.plusDays(1), 0, 20);
        assertIds(missing, withoutOrigin);
        assertThat(missing.content().getFirst().workoutCycleId()).isNull();
        assertThat(missing.content().getFirst().workoutDayId()).isNull();
        assertThat(query(cycle.getId(), null, DATE.plusDays(1), null, 0, 20).content()).isEmpty();
    }

    @Test
    void shouldNotRevealAnotherPersonsOriginsOrExpireOverdueSessions() {
        var stranger = person("Another owner");
        var goal = em.find(TrainingGoalEntity.class, cycle.getTrainingGoal().getId());
        var foreignCycle = cycle(stranger, goal, "Private");
        var foreignDay = day(foreignCycle, "Private");
        session(stranger, foreignDay, DATE, 12, WorkoutSessionCloseReason.MANUAL);
        em.flush();
        assertThat(query(foreignCycle.getId(), null, null, null, 0, 20).totalElements()).isZero();
        assertThat(query(null, foreignDay.getId(), null, null, 0, 20).content()).isEmpty();
        var before = em.createQuery("select count(s) from WorkoutSessionEntity s where s.status = :status", Long.class)
                .setParameter("status", WorkoutSessionStatus.IN_PROGRESS).getSingleResult();
        query(null, null, null, null, 0, 20);
        assertThat(em.createQuery("select count(s) from WorkoutSessionEntity s where s.status = :status", Long.class)
                .setParameter("status", WorkoutSessionStatus.IN_PROGRESS).getSingleResult()).isEqualTo(before).isEqualTo(1);
    }

    PageResult<ListWorkoutSessionsItemResult> query(Long cycleId, Long dayId, LocalDate start, LocalDate end,
                                                   int page, int size) {
        return adapter.findClosedSessions(new PersonId(owner.getId()),
                cycleId == null ? null : new WorkoutCycleId(cycleId),
                dayId == null ? null : new WorkoutDayId(dayId), start, end, new PageQuery(page, size), null);
    }

    @Test
    void shouldSearchBothSnapshotsWithPaginationAndDates() {
        session(person("Another person"), day, DATE, 15, WorkoutSessionCloseReason.MANUAL);
        em.flush();
        var byCycle = adapter.findClosedSessions(new PersonId(owner.getId()), null, null,
                null, null, new PageQuery(0, 1), "  ORIGINAL CYCLE  ");
        assertThat(byCycle.totalElements()).isEqualTo(4);
        assertThat(byCycle.content()).hasSize(1);
        var removed = adapter.findClosedSessions(new PersonId(owner.getId()), null, null,
                null, null, new PageQuery(0, 20), "removed day");
        assertIds(removed, withoutOrigin);
        var dated = adapter.findClosedSessions(new PersonId(owner.getId()), null, null,
                DATE.plusDays(1), null, new PageQuery(0, 20), "original");
        assertThat(dated.content()).isEmpty();
        for (String name : new String[] {"%", "_", "!", "private", "nonexistent"}) {
            assertThat(adapter.findClosedSessions(new PersonId(owner.getId()), null, null,
                    null, null, new PageQuery(0, 20), name).totalElements()).isZero();
        }
    }

    private void assertIds(PageResult<ListWorkoutSessionsItemResult> page, Long... ids) {
        assertThat(page.content()).extracting(row -> row.id().value()).containsExactly(ids);
        if (page.size() == 20) assertThat(page.totalElements()).isEqualTo(ids.length);
    }

    PersonEntity person(String name) {
        var person = new PersonEntity(null, name, SexType.MALE, LocalDate.of(1990, 1, 1),
                LocalDateTime.of(2026, 1, 1, 0, 0), null);
        em.persist(person);
        return person;
    }

    WorkoutCycleEntity cycle(PersonEntity person, TrainingGoalEntity goal, String name) {
        var cycle = new WorkoutCycleEntity(null, person, name, goal, DATE, null, 3,
                WeekDay.SUNDAY, WorkoutStatus.IN_PROGRESS, WorkoutOrigin.MANUAL, null,
                LocalDateTime.of(2026, 1, 1, 0, 0), null);
        em.persist(cycle);
        return cycle;
    }

    WorkoutDayEntity day(WorkoutCycleEntity cycle, String title) {
        var day = new WorkoutDayEntity(null, cycle, WeekDay.MONDAY, title, 1,
                LocalDateTime.of(2026, 1, 1, 0, 0), null);
        em.persist(day);
        return day;
    }

    WorkoutSessionEntity session(PersonEntity person, WorkoutDayEntity origin, LocalDate date, int hour,
                                  WorkoutSessionCloseReason reason) {
        var session = new WorkoutSessionEntity();
        session.setPerson(person);
        session.setWorkoutCycleId(origin == null ? null : origin.getWorkoutCycle().getId());
        session.setWorkoutDayId(origin == null ? null : origin.getId());
        session.setWorkoutCycleNameSnapshot(origin == null ? "Removed cycle" : origin.getWorkoutCycle().getName());
        session.setWorkoutDayTitleSnapshot(origin == null ? "Removed day" : origin.getTitle());
        session.setWeekDaySnapshot(WeekDay.MONDAY);
        session.setPerformedOn(date);
        session.setStartedAt(date.atTime(hour, 0).atZone(ZoneId.of("America/Sao_Paulo")).toInstant());
        session.setCreatedAt(session.getStartedAt());
        session.setStatus(reason == null ? WorkoutSessionStatus.IN_PROGRESS : WorkoutSessionStatus.CLOSED);
        session.setCloseReason(reason);
        session.setFinishedAt(reason == null ? null : reason == WorkoutSessionCloseReason.MANUAL
                ? session.getStartedAt().plusSeconds(60)
                : date.plusDays(1).atTime(3, 0).atZone(ZoneId.of("America/Sao_Paulo")).toInstant());
        var activity = new WorkoutSessionActivityEntity();
        activity.setSession(session);
        activity.setExerciseNameSnapshot("Original exercise");
        activity.setActivityTypeSnapshot("STRENGTH");
        activity.setOrderIndexSnapshot(1);
        activity.setCreatedAt(session.getStartedAt());
        session.getActivities().add(activity);
        em.persist(session);
        return session;
    }
}
