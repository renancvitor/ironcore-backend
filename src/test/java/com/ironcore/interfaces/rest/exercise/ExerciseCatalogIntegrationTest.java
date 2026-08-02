package com.ironcore.interfaces.rest.exercise;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.IroncoreBackendApplication;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = IroncoreBackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "ironcore.bootstrap.single-user.enabled=false"
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ExerciseCatalogIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String CATALOG_ENDPOINT = "/api/exercise-catalog";
    private static final String EXERCISES_ENDPOINT = CATALOG_ENDPOINT + "/exercises";
    private static final String EMAIL = "renan@example.com";
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
    private PasswordEncoder passwordEncoder;

    private Cookie accessTokenCookie;

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
        userJpaRepository.deleteAll();
        personJpaRepository.deleteAll();
        userJpaRepository.save(activeUser());
        accessTokenCookie = loginAndGetAccessTokenCookie();
    }

    @AfterEach
    void tearDown() {
        userJpaRepository.deleteAll();
        personJpaRepository.deleteAll();
    }

    @Nested
    class Migrations {

        @Test
        void shouldApplyCatalogMigrationsAndSeedAllExercisesAndMuscleTargets() {
            Integer latestMigrationCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '17' AND success",
                    Integer.class
            );

            assertThat(latestMigrationCount).isEqualTo(1);
            assertThat(tableExists("activity_types")).isTrue();
            assertThat(tableExists("equipment_types")).isTrue();
            assertThat(tableExists("muscle_groups")).isTrue();
            assertThat(tableExists("muscle_subgroups")).isTrue();
            assertThat(tableExists("exercises")).isTrue();
            assertThat(tableExists("exercise_muscle_targets")).isTrue();
            assertThat(countRows("exercises")).isEqualTo(341);
            assertThat(countRows("exercise_muscle_targets")).isEqualTo(2766);
        }
    }

    @Nested
    class AuxiliaryCatalogs {

        @Test
        void shouldReturnSeededAuxiliaryCatalogsAndFilterMuscleSubgroupsByGroup() throws Exception {
            JsonNode activityTypes = authenticatedGet(CATALOG_ENDPOINT + "/activity-types");
            JsonNode equipmentTypes = authenticatedGet(CATALOG_ENDPOINT + "/equipment-types");
            JsonNode muscleGroups = authenticatedGet(CATALOG_ENDPOINT + "/muscle-groups");

            Long chestId = catalogId("muscle_groups", "CHEST");
            JsonNode chestSubgroups = authenticatedGet(
                    CATALOG_ENDPOINT + "/muscle-subgroups?muscleGroupId=" + chestId
            );

            assertThat(activityTypes).hasSize(6);
            assertThat(activityTypes.get(0).get("code").asText()).isEqualTo("STRENGTH");
            assertThat(activityTypes.get(0).get("name").asText()).isEqualTo("Força");

            assertThat(equipmentTypes).hasSize(7);
            assertThat(equipmentTypes.get(0).get("code").asText()).isEqualTo("BODYWEIGHT");

            assertThat(muscleGroups).hasSize(13);
            assertThat(muscleGroups.get(0).get("code").asText()).isEqualTo("CHEST");

            assertThat(chestSubgroups).hasSize(3);
            assertThat(chestSubgroups)
                    .allSatisfy(subgroup -> assertThat(subgroup.get("muscleGroupId").asLong()).isEqualTo(chestId));
            assertThat(codesOf(chestSubgroups)).containsExactly("UPPER_CHEST", "MID_CHEST", "LOWER_CHEST");
        }
    }

    @Nested
    class ExerciseDetail {

        @Test
        void shouldReturnExerciseByIdWithItsMuscleTargetsAndTargetRoles() throws Exception {
            Long exerciseId = exerciseId("Supino reto", "BARBELL", "STRENGTH");

            JsonNode exercise = authenticatedGet(EXERCISES_ENDPOINT + "/" + exerciseId);
            JsonNode muscleTargets = exercise.get("muscleTargets");

            assertThat(exercise.get("id").asLong()).isEqualTo(exerciseId);
            assertThat(exercise.get("name").asText()).isEqualTo("Supino reto");
            assertThat(exercise.at("/equipmentType/code").asText()).isEqualTo("BARBELL");
            assertThat(exercise.at("/activityType/code").asText()).isEqualTo("STRENGTH");
            assertThat(exercise.get("compound").asBoolean()).isTrue();
            assertThat(exercise.get("suggestedRestSeconds").asInt()).isEqualTo(120);
            assertThat(muscleTargets).hasSize(6);
            assertThat(hasTarget(muscleTargets, "MID_CHEST", "PRIMARY")).isTrue();
            assertThat(hasTarget(muscleTargets, "ANTERIOR_DELTOID", "SECONDARY")).isTrue();
            assertThat(hasTarget(muscleTargets, "SERRATUS_ANTERIOR", "STABILIZER")).isTrue();
        }
    }

    @Nested
    class ExerciseList {

        @Test
        void shouldListExercisesWithStablePagination() throws Exception {
            JsonNode firstPage = exercisePage("page=0&size=2");
            JsonNode secondPage = exercisePage("page=1&size=2");
            long totalExercises = countRows("exercises");

            assertThat(firstPage.get("page").asInt()).isZero();
            assertThat(firstPage.get("size").asInt()).isEqualTo(2);
            assertThat(firstPage.get("totalElements").asLong()).isEqualTo(totalExercises);
            assertThat(firstPage.get("totalPages").asInt()).isEqualTo((totalExercises + 1) / 2);
            assertThat(firstPage.get("last").asBoolean()).isFalse();
            assertThat(firstPage.get("content")).hasSize(2);

            assertThat(secondPage.get("page").asInt()).isEqualTo(1);
            assertThat(secondPage.get("content")).hasSize(2);
            assertThat(idsOf(firstPage.get("content")))
                    .doesNotContainAnyElementsOf(idsOf(secondPage.get("content")));
        }

        @Test
        void shouldApplyEachExerciseFilterIndependently() throws Exception {
            Long strengthId = catalogId("activity_types", "STRENGTH");
            Long barbellId = catalogId("equipment_types", "BARBELL");
            Long chestId = catalogId("muscle_groups", "CHEST");
            Long midChestId = catalogId("muscle_subgroups", "MID_CHEST");

            assertFilterTotal(
                    "name=Supino",
                    countDistinctExercises("LOWER(e.name) LIKE LOWER(?)", "%supino%")
            );
            assertFilterTotal(
                    "activityTypeId=" + strengthId,
                    countDistinctExercises("e.activity_type_id = ?", strengthId)
            );
            assertFilterTotal(
                    "equipmentTypeId=" + barbellId,
                    countDistinctExercises("e.equipment_type_id = ?", barbellId)
            );
            assertFilterTotal(
                    "muscleGroupId=" + chestId,
                    countDistinctExercisesWithTarget("ms.muscle_group_id = ?", chestId)
            );
            assertFilterTotal(
                    "muscleSubgroupId=" + midChestId,
                    countDistinctExercisesWithTarget("emt.muscle_subgroup_id = ?", midChestId)
            );
            assertFilterTotal(
                    "targetRole=STABILIZER",
                    countDistinctExercisesWithTarget("emt.target_role = ?", "STABILIZER")
            );
        }

        @Test
        void shouldApplyCombinedExerciseFilters() throws Exception {
            Long strengthId = catalogId("activity_types", "STRENGTH");
            Long barbellId = catalogId("equipment_types", "BARBELL");
            Long chestId = catalogId("muscle_groups", "CHEST");
            Long midChestId = catalogId("muscle_subgroups", "MID_CHEST");
            Long expectedExerciseId = exerciseId("Supino reto", "BARBELL", "STRENGTH");

            JsonNode page = exercisePage(
                    "name=Supino"
                            + "&activityTypeId=" + strengthId
                            + "&equipmentTypeId=" + barbellId
                            + "&muscleGroupId=" + chestId
                            + "&muscleSubgroupId=" + midChestId
                            + "&targetRole=PRIMARY"
                            + "&page=0&size=20"
            );

            assertThat(page.get("totalElements").asLong()).isEqualTo(1);
            assertThat(page.get("content")).hasSize(1);
            assertThat(page.at("/content/0/id").asLong()).isEqualTo(expectedExerciseId);
            assertThat(page.at("/content/0/name").asText()).isEqualTo("Supino reto");
            assertThat(page.at("/content/0/equipmentType/code").asText()).isEqualTo("BARBELL");
            assertThat(page.at("/content/0/activityType/code").asText()).isEqualTo("STRENGTH");
        }
    }

    @Nested
    class Authentication {

        @Test
        void shouldBlockCatalogQueriesWithoutAuthentication() throws Exception {
            Long exerciseId = exerciseId("Supino reto", "BARBELL", "STRENGTH");

            mockMvc.perform(get(CATALOG_ENDPOINT + "/activity-types"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get(EXERCISES_ENDPOINT))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get(EXERCISES_ENDPOINT + "/" + exerciseId))
                    .andExpect(status().isForbidden());
        }
    }

    private JsonNode authenticatedGet(String endpoint) throws Exception {
        MvcResult result = mockMvc.perform(get(endpoint).cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode exercisePage(String query) throws Exception {
        return authenticatedGet(EXERCISES_ENDPOINT + "?" + query).get("exercises");
    }

    private void assertFilterTotal(String query, long expectedTotal) throws Exception {
        JsonNode page = exercisePage(query + "&page=0&size=100");

        assertThat(expectedTotal).isPositive();
        assertThat(page.get("totalElements").asLong()).isEqualTo(expectedTotal);
        assertThat(page.get("content")).isNotEmpty();
    }

    private Cookie loginAndGetAccessTokenCookie() throws Exception {
        MvcResult result = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "renan@example.com",
                                  "password": "StrongPass123@"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("access_token");

        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isNotBlank();

        return cookie;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'public' AND table_name = ?
                        """,
                Integer.class,
                tableName
        );

        return count != null && count == 1;
    }

    private long countRows(String tableName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
        return count == null ? 0 : count;
    }

    private long countDistinctExercises(String predicate, Object parameter) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT e.id) FROM exercises e WHERE e.active AND " + predicate,
                Long.class,
                parameter
        );

        return count == null ? 0 : count;
    }

    private long countDistinctExercisesWithTarget(String predicate, Object parameter) {
        Long count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(DISTINCT e.id)
                        FROM exercises e
                        JOIN exercise_muscle_targets emt ON emt.exercise_id = e.id
                        JOIN muscle_subgroups ms ON ms.id = emt.muscle_subgroup_id
                        WHERE e.active AND emt.active AND
                        """ + predicate,
                Long.class,
                parameter
        );

        return count == null ? 0 : count;
    }

    private Long catalogId(String tableName, String code) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM " + tableName + " WHERE code = ?",
                Long.class,
                code
        );
    }

    private Long exerciseId(String name, String equipmentCode, String activityCode) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT e.id
                        FROM exercises e
                        JOIN equipment_types et ON et.id = e.equipment_type_id
                        JOIN activity_types at ON at.id = e.activity_type_id
                        WHERE e.name = ? AND et.code = ? AND at.code = ?
                        """,
                Long.class,
                name,
                equipmentCode,
                activityCode
        );
    }

    private static boolean hasTarget(JsonNode muscleTargets, String subgroupCode, String targetRole) {
        for (JsonNode target : muscleTargets) {
            if (subgroupCode.equals(target.at("/muscleSubgroup/code").asText())
                    && targetRole.equals(target.get("targetRole").asText())) {
                return true;
            }
        }

        return false;
    }

    private static Set<String> codesOf(JsonNode items) {
        Set<String> codes = new java.util.LinkedHashSet<>();
        items.forEach(item -> codes.add(item.get("code").asText()));
        return codes;
    }

    private static Set<Long> idsOf(JsonNode items) {
        Set<Long> ids = new HashSet<>();
        items.forEach(item -> ids.add(item.get("id").asLong()));
        return ids;
    }

    private UserEntity activeUser() {
        LocalDateTime now = LocalDateTime.now();

        return new UserEntity(
                null,
                "Renan",
                activePerson(),
                EMAIL,
                passwordEncoder.encode(RAW_PASSWORD),
                false,
                true,
                now,
                now
        );
    }

    private PersonEntity activePerson() {
        LocalDateTime now = LocalDateTime.now();

        return personJpaRepository.save(new PersonEntity(
                null,
                "Renan",
                SexType.MALE,
                LocalDate.of(1994, 4, 9),
                now,
                null
        ));
    }
}
