package com.ironcore.interfaces.rest.workoutplanning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.IroncoreBackendApplication;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.infrastructure.persistence.logging.audit.repository.AuditLogJpaRepository;
import com.ironcore.infrastructure.persistence.logging.error.repository.ErrorLogJpaRepository;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.create.CreateWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create.CreateWorkoutCycleRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.update.UpdateWorkoutCycleRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.create.CreateWorkoutDayRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = IroncoreBackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "ironcore.bootstrap.single-user.enabled=false"
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class WorkoutPlanningIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String TRAINING_GOALS_ENDPOINT = "/api/training-goals";
    private static final String WORKOUT_CYCLES_ENDPOINT = "/api/users/me/workout-cycles";
    private static final String WORKOUT_DAYS_ENDPOINT = "/api/users/me/workout-days";
    private static final String WORKOUT_ACTIVITIES_ENDPOINT = "/api/users/me/workout-activities";
    private static final String RAW_PASSWORD = "StrongPass123@";

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ironcore_test")
            .withUsername("ironcore")
            .withPassword("ironcore");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PersonJpaRepository personJpaRepository;

    @Autowired
    private AuditLogJpaRepository auditLogJpaRepository;

    @Autowired
    private ErrorLogJpaRepository errorLogJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Cookie firstUserCookie;
    private Cookie secondUserCookie;
    private Long hypertrophyGoalId;
    private Long activeExerciseId;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        POSTGRES.start();

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @BeforeEach
    void setUp() throws Exception {
        clearTestData();
        createUser("Renan", "renan@example.com");
        createUser("Outra Pessoa", "outra@example.com");
        firstUserCookie = loginAndGetAccessTokenCookie("renan@example.com");
        secondUserCookie = loginAndGetAccessTokenCookie("outra@example.com");
        hypertrophyGoalId = idOf("SELECT id FROM training_goals WHERE code = 'HYPERTROPHY'");
        activeExerciseId = idOf("SELECT id FROM exercises WHERE active = true ORDER BY id LIMIT 1");
    }

    @AfterEach
    void tearDown() {
        clearTestData();
    }

    @Nested
    class MigrationsAndTrainingGoals {

        @Test
        void shouldApplyWorkoutPlanningMigrationsAndExposeSeededTrainingGoals() throws Exception {
            assertThat(tableExists("training_goals")).isTrue();
            assertThat(tableExists("workout_cycles")).isTrue();
            assertThat(tableExists("workout_days")).isTrue();
            assertThat(tableExists("workout_activities")).isTrue();
            assertThat(countRows("training_goals")).isEqualTo(5);
            assertThat(hypertrophyGoalId).isPositive();
            assertThat(activeExerciseId).isPositive();

            mockMvc.perform(get(TRAINING_GOALS_ENDPOINT).cookie(firstUserCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.code == 'HYPERTROPHY')].name").value("Hipertrofia"));
        }
    }

    @Nested
    class AuthenticationAndAuthorization {

        @Test
        void shouldRequireAuthenticationAndIsolateWorkoutCyclesByPerson() throws Exception {
            mockMvc.perform(get(WORKOUT_CYCLES_ENDPOINT))
                    .andExpect(status().isForbidden());

            Long firstUserCycleId = createWorkoutCycle(firstUserCookie, "Ciclo privado", hypertrophyGoalId);

            mockMvc.perform(get(WORKOUT_CYCLES_ENDPOINT + "/{id}", firstUserCycleId)
                            .cookie(secondUserCookie))
                    .andExpect(status().isNotFound());

            mockMvc.perform(put(WORKOUT_CYCLES_ENDPOINT + "/{id}", firstUserCycleId)
                            .cookie(secondUserCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody(new UpdateWorkoutCycleRequest(
                                    "Tentativa indevida", hypertrophyGoalId, 4, "Sem acesso"
                            ))))
                    .andExpect(status().isNotFound());

            mockMvc.perform(delete(WORKOUT_CYCLES_ENDPOINT + "/{id}", firstUserCycleId)
                            .cookie(secondUserCookie))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get(WORKOUT_CYCLES_ENDPOINT).cookie(secondUserCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cycles.totalElements").value(0));
        }
    }

    @Nested
    class PlanningFlow {

        @Test
        void shouldCreateComposeUpdateStartAndGetWorkoutDetail() throws Exception {
            Long cycleId = createWorkoutCycle(firstUserCookie, "Hipertrofia integrada", hypertrophyGoalId);

            mockMvc.perform(put(WORKOUT_CYCLES_ENDPOINT + "/{id}", cycleId)
                            .cookie(firstUserCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody(new UpdateWorkoutCycleRequest(
                                    "Hipertrofia atualizada", hypertrophyGoalId, 4, "Foco em progressão"
                            ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Hipertrofia atualizada"))
                    .andExpect(jsonPath("$.workoutStatus").value("NOT_STARTED"));

            Long dayId = createWorkoutDay(firstUserCookie, cycleId, WeekDay.MONDAY, "Treino A");
            Long activityId = createWorkoutActivity(firstUserCookie, dayId, activeExerciseId);

            mockMvc.perform(patch(WORKOUT_CYCLES_ENDPOINT + "/{id}/start", cycleId)
                            .cookie(firstUserCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(cycleId))
                    .andExpect(jsonPath("$.workoutStatus").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.startDate").value(LocalDate.now().toString()));

            mockMvc.perform(put(WORKOUT_CYCLES_ENDPOINT + "/{id}", cycleId)
                            .cookie(firstUserCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody(new UpdateWorkoutCycleRequest(
                                    "Hipertrofia em andamento", hypertrophyGoalId, 5, "Atualizado em andamento"
                            ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.workoutStatus").value("IN_PROGRESS"));

            mockMvc.perform(get(WORKOUT_CYCLES_ENDPOINT + "/{id}", cycleId)
                            .cookie(firstUserCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(cycleId))
                    .andExpect(jsonPath("$.name").value("Hipertrofia em andamento"))
                    .andExpect(jsonPath("$.trainingGoal.id").value(hypertrophyGoalId))
                    .andExpect(jsonPath("$.days[0].id").value(dayId))
                    .andExpect(jsonPath("$.days[0].weekDay").value("MONDAY"))
                    .andExpect(jsonPath("$.days[0].activities[0].id").value(activityId))
                    .andExpect(jsonPath("$.days[0].activities[0].exercise.id").value(activeExerciseId));
        }

        @Test
        void shouldRejectStartingWorkoutCycleWithoutCompleteComposition() throws Exception {
            Long cycleId = createWorkoutCycle(firstUserCookie, "Ciclo sem dias", hypertrophyGoalId);

            mockMvc.perform(patch(WORKOUT_CYCLES_ENDPOINT + "/{id}/start", cycleId)
                            .cookie(firstUserCookie))
                    .andExpect(status().isUnprocessableEntity());

            Long dayId = createWorkoutDay(firstUserCookie, cycleId, WeekDay.TUESDAY, "Treino incompleto");

            mockMvc.perform(patch(WORKOUT_CYCLES_ENDPOINT + "/{id}/start", cycleId)
                            .cookie(firstUserCookie))
                    .andExpect(status().isUnprocessableEntity());

            assertThat(dayId).isPositive();
        }
    }

    @Nested
    class LifecycleAndDeletion {

        @Test
        void shouldCompleteAndBlockFurtherUpdateAndDeletion() throws Exception {
            Long cycleId = createComposedWorkoutCycle(firstUserCookie, "Ciclo para concluir");
            startWorkoutCycle(firstUserCookie, cycleId);

            mockMvc.perform(patch(WORKOUT_CYCLES_ENDPOINT + "/{id}/complete", cycleId)
                            .cookie(firstUserCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.workoutStatus").value("COMPLETED"))
                    .andExpect(jsonPath("$.endDate").value(LocalDate.now().toString()));

            mockMvc.perform(put(WORKOUT_CYCLES_ENDPOINT + "/{id}", cycleId)
                            .cookie(firstUserCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody(new UpdateWorkoutCycleRequest(
                                    "Não deve atualizar", hypertrophyGoalId, 3, null
                            ))))
                    .andExpect(status().isUnprocessableEntity());

            mockMvc.perform(delete(WORKOUT_CYCLES_ENDPOINT + "/{id}", cycleId)
                            .cookie(firstUserCookie))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void shouldCancelAndBlockFurtherUpdateAndDeleteOnlyNotStartedCycle() throws Exception {
            Long cancellableCycleId = createWorkoutCycle(firstUserCookie, "Ciclo para cancelar", hypertrophyGoalId);

            mockMvc.perform(patch(WORKOUT_CYCLES_ENDPOINT + "/{id}/cancel", cancellableCycleId)
                            .cookie(firstUserCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.workoutStatus").value("CANCELLED"));

            mockMvc.perform(put(WORKOUT_CYCLES_ENDPOINT + "/{id}", cancellableCycleId)
                            .cookie(firstUserCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody(new UpdateWorkoutCycleRequest(
                                    "Não deve atualizar", hypertrophyGoalId, 3, null
                            ))))
                    .andExpect(status().isUnprocessableEntity());

            Long deletableCycleId = createWorkoutCycle(firstUserCookie, "Ciclo para excluir", hypertrophyGoalId);

            mockMvc.perform(delete(WORKOUT_CYCLES_ENDPOINT + "/{id}", deletableCycleId)
                            .cookie(firstUserCookie))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get(WORKOUT_CYCLES_ENDPOINT + "/{id}", deletableCycleId)
                            .cookie(firstUserCookie))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Listing {

        @Test
        void shouldListOnlyAuthenticatedPersonCyclesAndApplyFilters() throws Exception {
            Long matchingCycleId = createComposedWorkoutCycle(firstUserCookie, "Hipertrofia listável");
            startWorkoutCycle(firstUserCookie, matchingCycleId);
            Long otherGoalId = idOf("SELECT id FROM training_goals WHERE code = 'STRENGTH'");
            createWorkoutCycle(firstUserCookie, "Força listável", otherGoalId);
            createWorkoutCycle(secondUserCookie, "Hipertrofia de outra pessoa", hypertrophyGoalId);

            mockMvc.perform(get(WORKOUT_CYCLES_ENDPOINT)
                            .cookie(firstUserCookie)
                            .param("workoutStatus", "IN_PROGRESS")
                            .param("trainingGoalId", hypertrophyGoalId.toString())
                            .param("name", "hipertrofia")
                            .param("startDate", LocalDate.now().minusDays(1).toString())
                            .param("endDate", LocalDate.now().plusDays(1).toString())
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cycles.totalElements").value(1))
                    .andExpect(jsonPath("$.cycles.content[0].id").value(matchingCycleId))
                    .andExpect(jsonPath("$.cycles.content[0].name").value("Hipertrofia listável"));
        }
    }

    private Long createComposedWorkoutCycle(Cookie cookie, String name) throws Exception {
        Long cycleId = createWorkoutCycle(cookie, name, hypertrophyGoalId);
        Long dayId = createWorkoutDay(cookie, cycleId, WeekDay.MONDAY, "Treino completo");
        createWorkoutActivity(cookie, dayId, activeExerciseId);
        return cycleId;
    }

    private void startWorkoutCycle(Cookie cookie, Long cycleId) throws Exception {
        mockMvc.perform(patch(WORKOUT_CYCLES_ENDPOINT + "/{id}/start", cycleId).cookie(cookie))
                .andExpect(status().isOk());
    }

    private Long createWorkoutCycle(Cookie cookie, String name, Long trainingGoalId) throws Exception {
        MvcResult result = mockMvc.perform(post(WORKOUT_CYCLES_ENDPOINT)
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(new CreateWorkoutCycleRequest(name, trainingGoalId, 3, "Plano integrado"))))
                .andExpect(status().isCreated())
                .andReturn();

        return responseBody(result).get("id").asLong();
    }

    private Long createWorkoutDay(Cookie cookie, Long cycleId, WeekDay weekDay, String title) throws Exception {
        MvcResult result = mockMvc.perform(post(WORKOUT_DAYS_ENDPOINT)
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(new CreateWorkoutDayRequest(cycleId, weekDay, title))))
                .andExpect(status().isCreated())
                .andReturn();

        return responseBody(result).get("id").asLong();
    }

    private Long createWorkoutActivity(Cookie cookie, Long dayId, Long exerciseId) throws Exception {
        CreateWorkoutActivityRequest request = new CreateWorkoutActivityRequest(
                dayId,
                exerciseId,
                3,
                8,
                12,
                new BigDecimal("20.00"),
                null,
                null,
                null,
                null,
                90,
                "Controle integrado"
        );

        MvcResult result = mockMvc.perform(post(WORKOUT_ACTIVITIES_ENDPOINT)
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return responseBody(result).get("id").asLong();
    }

    private Cookie loginAndGetAccessTokenCookie(String email) throws Exception {
        MvcResult result = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, RAW_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("access_token");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isNotBlank();
        return cookie;
    }

    private void createUser(String name, String email) {
        LocalDateTime now = LocalDateTime.now();
        PersonEntity person = personJpaRepository.save(new PersonEntity(
                null,
                name,
                SexType.MALE,
                LocalDate.of(1994, 4, 9),
                now,
                null
        ));

        userJpaRepository.save(new UserEntity(
                null,
                name,
                person,
                email,
                passwordEncoder.encode(RAW_PASSWORD),
                false,
                true,
                now,
                now
        ));
    }

    private void clearTestData() {
        errorLogJpaRepository.deleteAll();
        auditLogJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        personJpaRepository.deleteAll();
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name = ?
                        """,
                Integer.class,
                tableName
        );
        return count != null && count == 1;
    }

    private long countRows(String tableName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
        assertThat(count).isNotNull();
        return count;
    }

    private Long idOf(String query) {
        Long id = jdbcTemplate.queryForObject(query, Long.class);
        assertThat(id).isNotNull();
        return id;
    }

    private String requestBody(Object request) throws Exception {
        return objectMapper.writeValueAsString(request);
    }

    private JsonNode responseBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
