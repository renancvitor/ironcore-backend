package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest(showSql = false, properties = {
        "spring.jpa.hibernate.ddl-auto=validate", "spring.flyway.enabled=true",
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
        // Attach real planning references to exercise ON DELETE SET NULL through the actual migrations.
        var goal = ((Number) em.createNativeQuery("select id from training_goals limit 1").getSingleResult()).longValue();
        var cycle = ((Number) em.createNativeQuery("""
                insert into workout_cycles(person_id,name,training_goal_id,start_date,desired_duration_months,workout_status,workout_origin,created_at)
                values (:person,'Original',:goal,date '2026-10-26',3,'IN_PROGRESS','MANUAL',timestamp '2026-10-26 10:00:00')
                returning id
                """).setParameter("person", owner.getId()).setParameter("goal", goal).getSingleResult()).longValue();
        var day = ((Number) em.createNativeQuery("""
                insert into workout_days(workout_cycle_id,week_day,title,sort_order,created_at)
                values (:cycle,1,'Original',1,timestamp '2026-10-26 10:00:00') returning id
                """).setParameter("cycle", cycle).getSingleResult()).longValue();
        em.createNativeQuery("update workout_sessions set workout_cycle_id=:cycle,workout_day_id=:day where id=:id")
                .setParameter("cycle", cycle).setParameter("day", day).setParameter("id", early).executeUpdate();
        var before = strength();
        em.createNativeQuery("delete from workout_cycles where id=:id").setParameter("id", cycle).executeUpdate();
        em.clear();
        assertThat(strength()).isEqualTo(before);
        em.createNativeQuery("delete from exercises where id=:id").setParameter("id", exercise.getId()).executeUpdate();
        em.clear();
        assertThat(strength()).isEmpty();
        assertThat(cardio()).isEmpty();
        assertThat(((Number) em.createNativeQuery("""
                select count(*) from workout_session_activities where workout_session_id=:id
                and exercise_id is null and exercise_name_snapshot='Historical name'
                """).setParameter("id", early).getSingleResult()).longValue()).isEqualTo(2);
        assertThat(((Number) em.createNativeQuery("""
                select count(*) from workout_session_sets x join workout_session_activities a
                on a.id=x.workout_session_activity_id where a.workout_session_id=:id
                """).setParameter("id", early).getSingleResult()).longValue()).isEqualTo(4);
    }
}
