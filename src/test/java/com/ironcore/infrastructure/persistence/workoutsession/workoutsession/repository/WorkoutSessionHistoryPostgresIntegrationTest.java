package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.repository;

import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutplanning.workoutday.delete.*;
import com.ironcore.application.workoutsession.workoutsession.detail.*;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository.WorkoutCycleAdapter;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.repository.WorkoutDayAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import java.time.*;
import java.util.Optional;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest(showSql = false, properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ListWorkoutSessionsQueryAdapter.class, WorkoutSessionAdapter.class, WorkoutSessionPlanningQueryAdapter.class,
        WorkoutDayAdapter.class, WorkoutCycleAdapter.class, DeleteWorkoutDayUseCase.class,
        GetWorkoutSessionDetailUseCase.class})
@Testcontainers
class WorkoutSessionHistoryPostgresIntegrationTest extends WorkoutSessionHistoryQueryIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @MockitoBean UserRepository users;
    @MockitoBean PersonRepository persons;
    @MockitoBean AuditLogPublisher publisher;
    @MockitoBean Clock clock;
    @Autowired DeleteWorkoutDayUseCase deleteDay;
    @Autowired GetWorkoutSessionDetailUseCase detail;

    @Test
    void shouldKeepHistoryAndDetailAfterPermittedPlanningDeletion() {
        var original = activeUser();
        var ownerId = new PersonId(owner.getId());
        var user = User.restore(original.getId(), original.getNickname(), ownerId, original.getEmail(),
                original.getPasswordHash(), false, true, original.getCreatedAt(), original.getUpdatedAt());
        var template = restoredPerson();
        var person = Person.restore(ownerId, template.getName(), template.getSex(), template.getBirthDate(),
                template.getCreatedAt(), template.getUpdatedAt());
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(persons.findById(ownerId)).thenReturn(Optional.of(person));
        when(clock.instant()).thenReturn(Instant.parse("2026-10-27T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        var before = detail.execute(new GetWorkoutSessionDetailCommand(user.getId(),
                new WorkoutSessionId(tieLast)));

        deleteDay.execute(new DeleteWorkoutDayCommand(user.getId(), new WorkoutDayId(day.getId())));
        em.flush();
        em.clear();

        var after = detail.execute(new GetWorkoutSessionDetailCommand(user.getId(), before.id()));
        assertThat(after.workoutDayId()).isNull();
        assertThat(after.workoutDayTitleSnapshot()).isEqualTo(before.workoutDayTitleSnapshot());
        assertThat(after.activities()).isEqualTo(before.activities());
        assertThat(after.finishedAt()).isEqualTo(before.finishedAt());
        assertThat(query(null, day.getId(), null, null, 0, 20).content()).isEmpty();
        var history = query(cycle.getId(), null, DATE, DATE, 0, 20);
        assertThat(history.totalElements()).isEqualTo(3);
        assertThat(history.content()).allSatisfy(row -> {
            assertThat(row.workoutDayId()).isNull();
            assertThat(row.workoutDayTitleSnapshot()).isEqualTo("Original day");
        });

        em.createNativeQuery("delete from workout_cycles where id = :id")
                .setParameter("id", cycle.getId()).executeUpdate();
        em.clear();
        assertThat(query(cycle.getId(), null, null, null, 0, 20).content()).isEmpty();
        assertThat(query(null, null, DATE, DATE, 0, 20).totalElements()).isEqualTo(3);
        assertThat(detail.execute(new GetWorkoutSessionDetailCommand(user.getId(), before.id())).workoutCycleId()).isNull();
    }
}
