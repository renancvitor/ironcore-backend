package com.ironcore.interfaces.rest.workoutsession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.IroncoreBackendApplication;
import com.ironcore.application.workoutsession.workoutsession.expire.ExpireWorkoutSessionsUseCase;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import com.ironcore.infrastructure.scheduling.workoutsession.WorkoutSessionExpirationScheduler;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = IroncoreBackendApplication.class,
        properties = "ironcore.bootstrap.single-user.enabled=false")
@AutoConfigureMockMvc
@Import(WorkoutSessionIntegrationTest.TimeConfiguration.class)
@ActiveProfiles("test")
@Testcontainers
class WorkoutSessionIntegrationTest {

    private static final String SESSIONS = "/api/users/me/workout-sessions";
    private static final String CYCLES = "/api/users/me/workout-cycles";
    private static final String DAYS = "/api/users/me/workout-days";
    private static final String ACTIVITIES = "/api/users/me/workout-activities";
    private static final String PASSWORD = "StrongPass123@";
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    // Future relative to wall time so real JWT verification remains valid with a controlled domain clock.
    private static final Instant START = LocalDate.now(ZONE).plusDays(1).atTime(12, 0).atZone(ZONE).toInstant();
    private static final LocalDate DATE = START.atZone(ZONE).toLocalDate();

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired PersonJpaRepository persons;
    @Autowired UserJpaRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ExpireWorkoutSessionsUseCase expireSessions;
    @Autowired TestClock clock;
    // Invoke the real expiration use case explicitly, without a background race during fixtures/cleanup.
    @MockitoBean WorkoutSessionExpirationScheduler scheduler;

    private Cookie owner;
    private Cookie other;
    private long ownerPersonId;
    private long strengthExercise;
    private long cardioExercise;
    private long goal;

    @BeforeEach
    void setUp() throws Exception {
        clearData();
        at(START);
        ownerPersonId = createUser("Owner", "owner@example.com");
        createUser("Other", "other@example.com");
        owner = login("owner@example.com");
        other = login("other@example.com");
        goal = jdbc.queryForObject("select id from training_goals where code = 'HYPERTROPHY'", Long.class);
        strengthExercise = exercise("STRENGTH");
        cardioExercise = exercise("CARDIO");
    }

    @AfterEach
    void clearData() {
        jdbc.update("delete from error_logs");
        jdbc.update("delete from audit_logs");
        users.deleteAll();
        persons.deleteAll();
    }

    @Test
    void shouldApplyAllSessionMigrationsOnAnEmptyPostgresDatabase() {
        assertThat(jdbc.queryForList("""
                select version from flyway_schema_history
                where version in ('24', '25', '26', '27') and success = true order by installed_rank
                """, String.class)).containsExactly("24", "25", "26", "27");
        assertThat(jdbc.queryForList("""
                select table_name from information_schema.tables where table_schema = 'public'
                and table_name like 'workout_session%' order by table_name
                """, String.class)).containsExactly("workout_session_activities", "workout_session_sets", "workout_sessions");
        assertThat(jdbc.queryForObject("""
                select indexdef from pg_indexes where indexname = 'uk_workout_sessions_person_in_progress'
                """, String.class)).contains("UNIQUE", "person_id", "IN_PROGRESS");
    }

    @Test
    void shouldStartFromAValidDayCaptureSnapshotsAndRecoverWithoutAutofillingExecution() throws Exception {
        Plan plan = plan(owner, true);
        long session = start(owner, plan);
        JsonNode detail = detail(owner, session);
        assertThat(detail.path("personId").asLong()).isEqualTo(ownerPersonId);
        assertThat(detail.path("workoutCycleId").asLong()).isEqualTo(plan.cycle());
        assertThat(detail.path("workoutDayId").asLong()).isEqualTo(plan.day());
        assertThat(detail.path("workoutCycleNameSnapshot").asText()).isEqualTo("Original cycle");
        assertThat(detail.path("workoutDayTitleSnapshot").asText()).isEqualTo("Original day");
        assertThat(detail.path("performedOn").asText()).isEqualTo(DATE.toString());
        assertThat(detail.path("startedAt").asText()).isEqualTo(START.toString());
        assertThat(detail.path("activities")).hasSize(2);
        JsonNode strength = detail.path("activities").get(0);
        assertThat(strength.path("plannedSetsSnapshot").asInt()).isEqualTo(3);
        assertThat(strength.path("plannedTargetLoadKgSnapshot").decimalValue()).isEqualByComparingTo("20");
        assertThat(strength.path("plannedNotesSnapshot").asText()).isEqualTo("Planned notes");
        assertThat(detail.path("activities").get(1).path("plannedDurationMinutesSnapshot").asInt()).isEqualTo(30);
        for (JsonNode activity : detail.path("activities")) {
            assertThat(activity.path("sets")).isEmpty();
            assertNull(activity, "actualDurationMinutes", "actualDistanceKm", "actualLoadMode", "notes");
        }
        assertNull(detail, "finishedAt", "closeReason", "notes");
        assertThat(body(call(get(SESSIONS + "/in-progress"), owner).andExpect(status().isOk())))
                .isEqualTo(detail);
        call(post(SESSIONS).content(startBody(plan)), owner).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("select count(*) from workout_sessions", Long.class)).isEqualTo(1);
    }

    @Test
    void shouldRejectInvalidPlanningWithoutPersistingASession() throws Exception {
        Plan notStarted = plan(owner, false);
        call(post(SESSIONS).content(startBody(notStarted)), owner).andExpect(status().isBadRequest());
        call(patch(CYCLES + "/" + notStarted.cycle() + "/start"), owner).andExpect(status().isOk());
        long emptyDay = create(post(DAYS), owner, Map.of("workoutCycleId", notStarted.cycle(),
                "weekDay", "TUESDAY", "title", "Empty day"));
        call(post(SESSIONS).content(startBody(new Plan(notStarted.cycle(), emptyDay))), owner)
                .andExpect(status().isBadRequest());
        Plan another = plan(owner, false);
        call(post(SESSIONS).content(startBody(new Plan(notStarted.cycle(), another.day()))), owner)
                .andExpect(status().isBadRequest());
        call(patch(CYCLES + "/" + notStarted.cycle() + "/complete"), owner).andExpect(status().isOk());
        call(post(SESSIONS).content(startBody(notStarted)), owner).andExpect(status().isBadRequest());
        call(patch(CYCLES + "/" + another.cycle() + "/cancel"), owner).andExpect(status().isOk());
        call(post(SESSIONS).content(startBody(another)), owner).andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("select count(*) from workout_sessions", Long.class)).isZero();
    }

    @Test
    void shouldAuthenticateAllEndpointsAndRequireCsrfForMutations() throws Exception {
        Plan plan = plan(owner, true);
        long session = start(owner, plan);
        long activity = activity(detail(owner, session), 0);
        long set = set(owner, session, activity, "{\"setNumber\":1,\"actualReps\":8}");
        for (MockHttpServletRequestBuilder request : List.of(
                get(SESSIONS), get(SESSIONS + "/in-progress"), get(SESSIONS + "/" + session),
                analytics(strengthExercise, "strength"), analytics(cardioExercise, "cardio"))) {
            mvc.perform(request).andExpect(status().isUnauthorized());
        }
        for (MockHttpServletRequestBuilder request : mutations(plan, session, activity, set)) {
            mvc.perform(request.with(csrf()).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
        for (MockHttpServletRequestBuilder request : mutations(plan, session, activity, set)) {
            mvc.perform(request.cookie(owner).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(get(SESSIONS).cookie(new Cookie("access_token", "invalid-token")))
                .andExpect(status().isUnauthorized());
        assertThat(detail(owner, session).path("status").asText()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void shouldIsolatePlanningSessionsExecutionHistoryAndAnalyticsByPerson() throws Exception {
        Plan plan = plan(owner, true);
        long session = start(owner, plan);
        long activity = activity(detail(owner, session), 0);
        long set = set(owner, session, activity,
                "{\"setNumber\":1,\"actualLoadKg\":20,\"actualLoadMode\":\"TOTAL\",\"actualReps\":8}");
        JsonNode before = detail(owner, session);
        call(get(SESSIONS + "/" + session), other).andExpect(status().isNotFound());
        call(get(SESSIONS + "/in-progress"), other).andExpect(status().isNotFound());
        for (MockHttpServletRequestBuilder request : mutations(plan, session, activity, set)) {
            call(request, other).andExpect(status().isNotFound());
        }
        call(get(SESSIONS).param("workoutCycleId", Long.toString(plan.cycle())), other)
                .andExpect(status().isOk()).andExpect(jsonPath("$.sessions.totalElements").value(0));
        assertThat(detail(owner, session)).isEqualTo(before);
        long otherSession = start(other, plan(other, true));
        call(get(SESSIONS + "/in-progress"), owner).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(session));
        call(get(SESSIONS + "/in-progress"), other).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(otherSession));
        long otherActivity = activity(detail(other, otherSession), 0);
        call(patch(activityPath(session, otherActivity) + "/execution").content("{\"notes\":\"Wrong session\"}"), owner)
                .andExpect(status().isNotFound());
        close(session);
        call(analytics(strengthExercise, "strength"), owner).andExpect(status().isOk())
                .andExpect(jsonPath("$.points[0].sessionId").value(session));
        call(analytics(strengthExercise, "strength"), other).andExpect(status().isOk())
                .andExpect(jsonPath("$.points").isEmpty());
        call(analytics(cardioExercise, "cardio"), owner).andExpect(status().isOk())
                .andExpect(jsonPath("$.points[0].sessionId").value(session));
        call(analytics(cardioExercise, "cardio"), other).andExpect(status().isOk())
                .andExpect(jsonPath("$.points").isEmpty());
    }

    @Test
    void shouldPreservePartialExecutionAndOptionalReportsAfterManualClosure() throws Exception {
        long session = start(owner, plan(owner, true));
        long strength = activity(detail(owner, session), 0);
        long cardio = activity(detail(owner, session), 1);
        set(owner, session, strength, "{\"setNumber\":1,\"actualReps\":8}");
        set(owner, session, strength, "{\"setNumber\":2,\"actualLoadKg\":12.5,\"actualLoadMode\":\"PER_SIDE\"}");
        set(owner, session, strength, "{\"setNumber\":3,\"notes\":\"Stopped due to fatigue\"}");
        call(patch(SESSIONS + "/" + session + "/notes").content("{\"notes\":\"Partial workout\"}"), owner)
                .andExpect(status().isOk());
        call(patch(activityPath(session, cardio) + "/execution").content("{\"actualDurationMinutes\":15}"), owner)
                .andExpect(status().isOk());
        call(patch(activityPath(session, cardio) + "/execution").content("{\"notes\":\"No distance recorded\"}"), owner)
                .andExpect(status().isOk()).andExpect(jsonPath("$.actualDurationMinutes").value(15));
        JsonNode before = detail(owner, session);
        at(START.plusSeconds(1800));
        close(session);
        JsonNode after = detail(owner, session);
        assertThat(after.path("activities")).isEqualTo(before.path("activities"));
        assertThat(after.path("notes").asText()).isEqualTo("Partial workout");
        assertThat(after.path("closeReason").asText()).isEqualTo("MANUAL");
        assertThat(after.path("finishedAt").asText()).isEqualTo(START.plusSeconds(1800).toString());
        assertNull(after.path("activities").get(0).path("sets").get(0), "actualLoadKg", "actualLoadMode", "normalizedLoadKg", "notes");
        assertNull(after.path("activities").get(0).path("sets").get(1), "actualReps", "notes");
        assertThat(after.path("activities").get(0).path("sets").get(1).path("normalizedLoadKg").decimalValue())
                .isEqualByComparingTo("25");
        assertNull(after.path("activities").get(1), "actualDistanceKm");
        call(get(SESSIONS + "/in-progress"), owner).andExpect(status().isNotFound());
        call(patch(SESSIONS + "/" + session + "/close"), owner).andExpect(status().isBadRequest());
    }

    @Test
    void shouldExpireAtTheLocalDeadlinePreservePartialDataAndAllowANewSession() throws Exception {
        Plan plan = plan(owner, true);
        long session = start(owner, plan);
        long activity = activity(detail(owner, session), 0);
        set(owner, session, activity, "{\"setNumber\":1,\"actualReps\":6}");
        JsonNode before = detail(owner, session).path("activities");
        Instant deadline = DATE.plusDays(1).atTime(3, 0).atZone(ZONE).toInstant();
        at(deadline.minusSeconds(1));
        expireSessions.execute();
        assertThat(detail(owner, session).path("status").asText()).isEqualTo("IN_PROGRESS");
        at(deadline);
        expireSessions.execute();
        JsonNode expired = detail(owner, session);
        assertThat(expired.path("status").asText()).isEqualTo("CLOSED");
        assertThat(expired.path("closeReason").asText()).isEqualTo("AUTO_EXPIRED");
        assertThat(expired.path("finishedAt").asText()).isEqualTo(deadline.toString());
        assertThat(expired.path("activities")).isEqualTo(before);
        expireSessions.execute();
        assertThat(detail(owner, session)).isEqualTo(expired);
        long next = start(owner, plan);
        assertThat(next).isNotEqualTo(session);
        assertThat(detail(owner, next).path("performedOn").asText()).isEqualTo(DATE.plusDays(1).toString());
    }

    @Test
    void shouldExpireAnOverdueSessionWhenStartingAgainWithoutTheScheduler() throws Exception {
        Plan plan = plan(owner, true);
        long old = start(owner, plan);
        Instant deadline = DATE.plusDays(1).atTime(3, 0).atZone(ZONE).toInstant();
        at(deadline.plusSeconds(60));
        long next = start(owner, plan);
        JsonNode expired = detail(owner, old);
        assertThat(expired.path("closeReason").asText()).isEqualTo("AUTO_EXPIRED");
        assertThat(expired.path("finishedAt").asText()).isEqualTo(deadline.toString());
        call(get(SESSIONS + "/in-progress"), owner).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(next));
    }

    @Test
    void shouldMaintainClosedExecutionAndReportsWithoutChangingLifecycleOrSnapshots() throws Exception {
        long session = start(owner, plan(owner, true));
        long activity = activity(detail(owner, session), 0);
        long cardio = activity(detail(owner, session), 1);
        long set = set(owner, session, activity, "{\"setNumber\":1,\"actualReps\":8,\"notes\":\"Initial\"}");
        close(session);
        JsonNode before = detail(owner, session);
        at(START.plusSeconds(3600));
        call(patch(activityPath(session, activity) + "/sets/" + set)
                .content("{\"actualLoadKg\":10,\"actualLoadMode\":\"TOTAL\"}"), owner)
                .andExpect(status().isOk()).andExpect(jsonPath("$.actualReps").value(8))
                .andExpect(jsonPath("$.notes").value("Initial"));
        call(patch(activityPath(session, activity) + "/load-mode").content("{\"actualLoadMode\":\"PER_SIDE\"}"), owner)
                .andExpect(status().isOk()).andExpect(jsonPath("$.sets[0].normalizedLoadKg").value(20));
        long removed = set(owner, session, activity, "{\"setNumber\":2,\"notes\":\"Temporary\"}");
        call(delete(activityPath(session, activity) + "/sets/" + removed), owner).andExpect(status().isNoContent());
        call(patch(activityPath(session, activity) + "/sets/" + set).content("{\"notes\":null}"), owner)
                .andExpect(status().isOk()).andExpect(jsonPath("$.actualReps").value(8));
        call(patch(SESSIONS + "/" + session + "/notes").content("{\"notes\":\"Later report\"}"), owner)
                .andExpect(status().isOk());
        call(patch(activityPath(session, cardio) + "/execution")
                .content("{\"actualDistanceKm\":2.5,\"notes\":\"Later activity report\"}"), owner)
                .andExpect(status().isOk());
        JsonNode after = detail(owner, session);
        for (String field : List.of("status", "closeReason", "startedAt", "finishedAt", "performedOn",
                "workoutCycleNameSnapshot", "workoutDayTitleSnapshot")) {
            assertThat(after.path(field)).isEqualTo(before.path(field));
        }
        assertThat(after.path("notes").asText()).isEqualTo("Later report");
        assertThat(after.path("activities").get(0).path("sets")).hasSize(1);
        assertNull(after.path("activities").get(0).path("sets").get(0), "notes");
        assertThat(after.path("activities").get(1).path("notes").asText()).isEqualTo("Later activity report");
        call(analytics(strengthExercise, "strength"), owner).andExpect(status().isOk())
                .andExpect(jsonPath("$.points[0].maxNormalizedLoadKg").value(20))
                .andExpect(jsonPath("$.points[0].volumeKgReps").value(160))
                .andExpect(jsonPath("$.points[0].registeredSetCount").value(1));
    }

    @Test
    void shouldPageAndFilterHistoryAndKeepSnapshotsAfterPlanningChangesAndDeletion() throws Exception {
        Plan plan = plan(owner, true);
        long first = start(owner, plan);
        long strength = activity(detail(owner, first), 0);
        set(owner, first, strength, "{\"setNumber\":1,\"actualReps\":8}");
        close(first);
        at(START.plusSeconds(60));
        long second = start(owner, plan);
        close(second);
        at(START.plusSeconds(120));
        long third = start(owner, plan);
        close(third);
        start(other, plan(other, true));
        JsonNode before = detail(owner, first);
        call(put(CYCLES + "/" + plan.cycle()).content(json.writeValueAsString(Map.of(
                "name", "Renamed cycle", "trainingGoalId", goal, "desiredDurationMonths", 4))), owner)
                .andExpect(status().isOk());
        call(put(DAYS + "/" + plan.day()).content("{\"title\":\"Renamed day\"}"), owner).andExpect(status().isOk());
        long origin = before.path("activities").get(0).path("workoutActivityId").asLong();
        call(put(ACTIVITIES + "/" + origin).content(json.writeValueAsString(Map.of(
                "exerciseId", strengthExercise, "sets", 5, "targetLoadKg", 50, "notes", "Changed plan"))), owner)
                .andExpect(status().isOk());
        assertThat(detail(owner, first)).isEqualTo(before);
        call(get(SESSIONS).param("page", "0").param("size", "2"), owner)
                .andExpect(status().isOk()).andExpect(jsonPath("$.sessions.totalElements").value(3))
                .andExpect(jsonPath("$.sessions.totalPages").value(2))
                .andExpect(jsonPath("$.sessions.content[0].id").value(third))
                .andExpect(jsonPath("$.sessions.content[1].id").value(second));
        call(get(SESSIONS).param("page", "1").param("size", "2"), owner)
                .andExpect(status().isOk()).andExpect(jsonPath("$.sessions.content.length()").value(1))
                .andExpect(jsonPath("$.sessions.content[0].id").value(first));
        call(get(SESSIONS).param("workoutCycleId", Long.toString(plan.cycle()))
                .param("workoutDayId", Long.toString(plan.day())).param("startDate", DATE.toString())
                .param("endDate", DATE.toString()).param("name", "original"), owner)
                .andExpect(status().isOk()).andExpect(jsonPath("$.sessions.totalElements").value(3));
        for (Map<String, String> filter : List.of(Map.of("name", "Renamed"),
                Map.of("startDate", DATE.plusDays(1).toString()), Map.of("endDate", DATE.minusDays(1).toString()),
                Map.of("workoutCycleId", "9223372036854775807"), Map.of("workoutDayId", "9223372036854775807"))) {
            var request = get(SESSIONS);
            filter.forEach(request::param);
            call(request, owner).andExpect(status().isOk()).andExpect(jsonPath("$.sessions.totalElements").value(0));
        }
        call(delete(DAYS + "/" + plan.day()), owner).andExpect(status().isNoContent());
        JsonNode after = detail(owner, first);
        assertNull(after, "workoutDayId");
        assertThat(after.path("workoutDayTitleSnapshot")).isEqualTo(before.path("workoutDayTitleSnapshot"));
        for (int i = 0; i < 2; i++) {
            JsonNode expected = before.path("activities").get(i).deepCopy();
            ((com.fasterxml.jackson.databind.node.ObjectNode) expected).putNull("workoutActivityId");
            assertThat(after.path("activities").get(i)).isEqualTo(expected);
        }
        call(get(SESSIONS).param("workoutCycleId", Long.toString(plan.cycle())), owner)
                .andExpect(status().isOk()).andExpect(jsonPath("$.sessions.totalElements").value(3))
                .andExpect(jsonPath("$.sessions.content[0].workoutDayTitleSnapshot").value("Original day"));
        call(get(SESSIONS).param("workoutDayId", Long.toString(plan.day())), owner)
                .andExpect(status().isOk()).andExpect(jsonPath("$.sessions.totalElements").value(0));
    }

    @Test
    void shouldCalculateStrengthFromTotalAndPerSideLoadsAndKeepMissingMetricsAbsent() throws Exception {
        Plan plan = plan(owner, true);
        long first = start(owner, plan);
        long activity = activity(detail(owner, first), 0);
        set(owner, first, activity, "{\"setNumber\":1,\"actualLoadKg\":20,\"actualLoadMode\":\"TOTAL\",\"actualReps\":8}");
        close(first);
        at(START.plusSeconds(60));
        long second = start(owner, plan);
        activity = activity(detail(owner, second), 0);
        set(owner, second, activity, "{\"setNumber\":1,\"actualLoadKg\":12.5,\"actualLoadMode\":\"PER_SIDE\",\"actualReps\":10}");
        set(owner, second, activity, "{\"setNumber\":2,\"actualReps\":5}");
        set(owner, second, activity, "{\"setNumber\":3,\"actualLoadKg\":100,\"actualLoadMode\":\"PER_SIDE\"}");
        set(owner, second, activity, "{\"setNumber\":4,\"notes\":\"Unfinished\"}");
        close(second);
        at(START.plusSeconds(120));
        long empty = start(owner, plan);
        close(empty);
        JsonNode points = body(call(analytics(strengthExercise, "strength"), owner).andExpect(status().isOk())).path("points");
        assertThat(points).hasSize(3);
        JsonNode total = point(points, first);
        assertThat(total.path("maxNormalizedLoadKg").decimalValue()).isEqualByComparingTo("20");
        assertThat(total.path("volumeKgReps").decimalValue()).isEqualByComparingTo("160");
        JsonNode perSide = point(points, second);
        assertThat(perSide.path("maxNormalizedLoadKg").decimalValue()).isEqualByComparingTo("25");
        assertThat(perSide.path("volumeKgReps").decimalValue()).isEqualByComparingTo("250");
        assertThat(perSide.path("registeredSetCount").asInt()).isEqualTo(4);
        assertThat(perSide.path("loadedSetCount").asInt()).isEqualTo(2);
        assertThat(perSide.path("volumeSetCount").asInt()).isEqualTo(1);
        JsonNode missing = point(points, empty);
        assertNull(missing, "maxNormalizedLoadKg", "volumeKgReps");
        assertThat(missing.path("registeredSetCount").asInt()).isZero();
        assertThat(detail(owner, first).path("activities").get(0).path("sets").get(0).path("normalizedLoadKg").decimalValue())
                .isEqualByComparingTo("20");
    }

    @Test
    void shouldCalculateCardioMetricsWithoutInferringMissingExecutionFromThePlan() throws Exception {
        Plan plan = plan(owner, true);
        long first = start(owner, plan);
        long cardio = activity(detail(owner, first), 1);
        call(patch(activityPath(first, cardio) + "/execution")
                .content("{\"actualDurationMinutes\":30,\"actualDistanceKm\":5}"), owner).andExpect(status().isOk());
        close(first);
        at(START.plusSeconds(60));
        long partial = start(owner, plan);
        cardio = activity(detail(owner, partial), 1);
        call(patch(activityPath(partial, cardio) + "/execution").content("{\"actualDurationMinutes\":10}"), owner)
                .andExpect(status().isOk());
        close(partial);
        at(START.plusSeconds(120));
        long empty = start(owner, plan);
        close(empty);
        JsonNode result = body(call(analytics(cardioExercise, "cardio"), owner).andExpect(status().isOk()));
        assertThat(result.path("points")).hasSize(3);
        JsonNode full = point(result.path("points"), first);
        assertThat(full.path("averagePaceMinPerKm").decimalValue()).isEqualByComparingTo("6");
        assertThat(full.path("averageSpeedKmPerHour").decimalValue()).isEqualByComparingTo("10");
        assertNull(point(result.path("points"), partial), "distanceKm", "averagePaceMinPerKm", "averageSpeedKmPerHour");
        assertNull(point(result.path("points"), empty), "distanceKm", "durationMinutes", "averagePaceMinPerKm", "averageSpeedKmPerHour");
        JsonNode summary = result.path("summary");
        assertThat(summary.path("totalDistanceKm").decimalValue()).isEqualByComparingTo("5");
        assertThat(summary.path("totalDurationMinutes").asInt()).isEqualTo(40);
        assertThat(summary.path("averagePaceMinPerKm").decimalValue()).isEqualByComparingTo("6");
        assertThat(summary.path("distanceRecordCount").asInt()).isEqualTo(1);
        assertThat(summary.path("durationRecordCount").asInt()).isEqualTo(2);
    }

    private List<MockHttpServletRequestBuilder> mutations(Plan plan, long session, long activity, long set) throws Exception {
        String path = activityPath(session, activity);
        return List.of(post(SESSIONS).content(startBody(plan)),
                patch(SESSIONS + "/" + session + "/close"),
                patch(SESSIONS + "/" + session + "/notes").content("{\"notes\":\"Report\"}"),
                patch(path + "/execution").content("{\"notes\":\"Report\"}"),
                patch(path + "/load-mode").content("{\"actualLoadMode\":\"PER_SIDE\"}"),
                post(path + "/sets").content("{\"setNumber\":2,\"actualReps\":8}"),
                patch(path + "/sets/" + set).content("{\"actualReps\":10}"),
                delete(path + "/sets/" + set));
    }

    private Plan plan(Cookie cookie, boolean start) throws Exception {
        long cycle = create(post(CYCLES), cookie, Map.of("name", "Original cycle", "trainingGoalId", goal,
                "desiredDurationMonths", 3, "weekStartsOn", "MONDAY"));
        long day = create(post(DAYS), cookie, Map.of("workoutCycleId", cycle, "weekDay", "MONDAY", "title", "Original day"));
        create(post(ACTIVITIES), cookie, Map.of("workoutDayId", day, "exerciseId", strengthExercise,
                "sets", 3, "repRangeMin", 8, "repRangeMax", 12, "targetLoadKg", 20, "restSeconds", 90, "notes", "Planned notes"));
        create(post(ACTIVITIES), cookie, Map.of("workoutDayId", day, "exerciseId", cardioExercise,
                "durationMinutes", 30, "distanceKm", 5));
        if (start) {
            call(patch(CYCLES + "/" + cycle + "/start"), cookie).andExpect(status().isOk());
        }
        return new Plan(cycle, day);
    }

    private long start(Cookie cookie, Plan plan) throws Exception {
        return body(call(post(SESSIONS).content(startBody(plan)), cookie).andExpect(status().isCreated())).path("id").asLong();
    }

    private String startBody(Plan plan) throws Exception {
        return json.writeValueAsString(Map.of("workoutCycleId", plan.cycle(), "workoutDayId", plan.day()));
    }

    private void close(long session) throws Exception {
        call(patch(SESSIONS + "/" + session + "/close"), owner).andExpect(status().isOk());
    }

    private long create(MockHttpServletRequestBuilder request, Cookie cookie, Object value) throws Exception {
        return body(call(request.content(json.writeValueAsString(value)), cookie).andExpect(status().isCreated())).path("id").asLong();
    }

    private long set(Cookie cookie, long session, long activity, String value) throws Exception {
        return body(call(post(activityPath(session, activity) + "/sets").content(value), cookie)
                .andExpect(status().isCreated())).path("id").asLong();
    }

    private JsonNode detail(Cookie cookie, long session) throws Exception {
        return body(call(get(SESSIONS + "/" + session), cookie).andExpect(status().isOk()));
    }

    private long activity(JsonNode detail, int index) {
        return detail.path("activities").get(index).path("id").asLong();
    }

    private String activityPath(long session, long activity) {
        return SESSIONS + "/" + session + "/activities/" + activity;
    }

    private MockHttpServletRequestBuilder analytics(long exercise, String type) {
        return get(SESSIONS + "/analytics/exercises/" + exercise + "/" + type)
                .param("startDate", DATE.toString()).param("endDate", DATE.toString());
    }

    private ResultActions call(MockHttpServletRequestBuilder request, Cookie cookie) throws Exception {
        return mvc.perform(request.cookie(cookie).with(csrf()).contentType(MediaType.APPLICATION_JSON));
    }

    private JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private JsonNode point(JsonNode points, long session) {
        for (JsonNode point : points) {
            if (point.path("sessionId").asLong() == session) return point;
        }
        throw new AssertionError("Missing analytics point for session " + session);
    }

    private void assertNull(JsonNode node, String... fields) {
        for (String field : fields) {
            assertThat(node.path(field).isNull() || node.path(field).isMissingNode()).as(field).isTrue();
        }
    }

    private void at(Instant instant) {
        clock.now = instant;
    }

    private long exercise(String type) {
        return jdbc.queryForObject("""
                select e.id from exercises e join activity_types t on t.id = e.activity_type_id
                where e.active = true and t.code = ? order by e.id limit 1
                """, Long.class, type);
    }

    private long createUser(String name, String email) {
        LocalDateTime now = LocalDateTime.ofInstant(START, ZONE);
        PersonEntity person = persons.save(new PersonEntity(null, name, SexType.MALE,
                LocalDate.of(1994, 4, 9), now, null));
        users.save(new UserEntity(null, name, person, email, passwordEncoder.encode(PASSWORD), false, true, now, now));
        return person.getId();
    }

    private Cookie login(String email) throws Exception {
        Cookie cookie = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk()).andReturn().getResponse().getCookie("access_token");
        assertThat(cookie).isNotNull();
        return cookie;
    }

    private record Plan(long cycle, long day) { }

    @TestConfiguration
    static class TimeConfiguration {
        @Bean
        @Primary
        TestClock sessionTestClock() {
            return new TestClock();
        }
    }

    static class TestClock extends Clock {
        private volatile Instant now = START;

        @Override
        public ZoneId getZone() {
            return ZONE;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(now, zone);
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
