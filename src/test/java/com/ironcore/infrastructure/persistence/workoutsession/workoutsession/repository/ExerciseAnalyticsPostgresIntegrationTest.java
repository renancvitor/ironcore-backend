package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false, properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ExerciseAnalyticsPostgresIntegrationTest extends ExerciseAnalyticsQueryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Test
    void shouldPreserveExecutionAfterPlanningDeletionAndNotReidentifyDeletedExercise() {
        var trainingGoalId = ((Number) entityManager.createNativeQuery("select id from training_goals limit 1")
                .getSingleResult()).longValue();
        var workoutCycleId = ((Number) entityManager.createNativeQuery("""
                insert into workout_cycles(
                    person_id, name, training_goal_id, start_date, desired_duration_months,
                    workout_status, workout_origin, created_at)
                values (
                    :person, 'Original', :trainingGoalId, date '2026-10-26', 3,
                    'IN_PROGRESS', 'MANUAL', timestamp '2026-10-26 10:00:00')
                returning id
                """)
                .setParameter("person", owner.getId())
                .setParameter("trainingGoalId", trainingGoalId)
                .getSingleResult()).longValue();
        var workoutDayId = ((Number) entityManager.createNativeQuery("""
                insert into workout_days(workout_cycle_id,week_day,title,sort_order,created_at)
                values (:workoutCycleId,1,'Original',1,timestamp '2026-10-26 10:00:00') returning id
                """)
                .setParameter("workoutCycleId", workoutCycleId)
                .getSingleResult()).longValue();
        entityManager.createNativeQuery("""
                update workout_sessions
                set workout_cycle_id=:workoutCycleId, workout_day_id=:workoutDayId
                where id=:id
                """)
                .setParameter("workoutCycleId", workoutCycleId)
                .setParameter("workoutDayId", workoutDayId)
                .setParameter("id", earlySessionId)
                .executeUpdate();

        var strengthBeforeDeletion = strength();
        entityManager.createNativeQuery("delete from workout_cycles where id=:id")
                .setParameter("id", workoutCycleId)
                .executeUpdate();
        entityManager.clear();
        assertThat(strength()).isEqualTo(strengthBeforeDeletion);

        entityManager.createNativeQuery("delete from exercises where id=:id")
                .setParameter("id", exercise.getId())
                .executeUpdate();
        entityManager.clear();
        assertThat(strength()).isEmpty();
        assertThat(cardio()).isEmpty();
        long preservedActivities = ((Number) entityManager.createNativeQuery("""
                select count(*) from workout_session_activities where workout_session_id=:id
                and exercise_id is null and exercise_name_snapshot='Historical name'
                """)
                .setParameter("id", earlySessionId)
                .getSingleResult()).longValue();
        long preservedSets = ((Number) entityManager.createNativeQuery("""
                select count(*) from workout_session_sets x join workout_session_activities a
                on a.id=x.workout_session_activity_id where a.workout_session_id=:id
                """)
                .setParameter("id", earlySessionId)
                .getSingleResult()).longValue();

        assertThat(preservedActivities).isEqualTo(2);
        assertThat(preservedSets).isEqualTo(4);
    }
}
