package com.ironcore.interfaces.rest.userbodymetrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.IroncoreBackendApplication;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import com.ironcore.infrastructure.persistence.userbodymetrics.entity.UserBodyMetricsEntity;
import com.ironcore.infrastructure.persistence.userbodymetrics.repository.UserBodyMetricsJpaRepository;
import com.ironcore.interfaces.rest.userbodymetrics.dto.BodyCircumferencesRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.create.CreateUserBodyMetricsRequest;
import com.ironcore.interfaces.rest.userbodymetrics.dto.update.UpdateUserBodyMetricsRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@ActiveProfiles("test")
class UserBodyMetricsIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String BODY_METRICS_ENDPOINT = "/api/users/me/body-metrics";
    private static final String EMAIL = "renan@example.com";
    private static final String RAW_PASSWORD = "StrongPass123@";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PersonJpaRepository personJpaRepository;

    @Autowired
    private UserBodyMetricsJpaRepository userBodyMetricsJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userBodyMetricsJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        personJpaRepository.deleteAll();
        userJpaRepository.save(activeUser());
    }

    @AfterEach
    void tearDown() {
        userBodyMetricsJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        personJpaRepository.deleteAll();
    }

    @Nested
    class Authentication {

        @Test
        void shouldBlockUserBodyMetricsAccessWithoutAuthentication() throws Exception {
            mockMvc.perform(get(BODY_METRICS_ENDPOINT))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class IntegratedFlow {

        @Test
        void shouldManageUserBodyMetricsThroughAuthenticatedHttpFlow() throws Exception {
            Cookie accessTokenCookie = loginAndGetAccessTokenCookie();

            Long firstMetricId = createUserBodyMetrics(
                    accessTokenCookie,
                    createRequest(80.0, "Medição inicial.")
            );
            updateMeasuredAt(firstMetricId, LocalDateTime.of(2026, 6, 1, 10, 0));

            Long secondMetricId = createUserBodyMetrics(
                    accessTokenCookie,
                    createRequest(78.0, "Medição de acompanhamento.")
            );
            updateMeasuredAt(secondMetricId, LocalDateTime.of(2026, 6, 20, 10, 0));

            mockMvc.perform(get(BODY_METRICS_ENDPOINT + "/{id}", firstMetricId)
                            .cookie(accessTokenCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(firstMetricId))
                    .andExpect(jsonPath("$.weightKg").value(80.0))
                    .andExpect(jsonPath("$.heightCm").value(180.0))
                    .andExpect(jsonPath("$.bmi").value(24.691358024691358))
                    .andExpect(jsonPath("$.bodyFatPercentage").isNumber())
                    .andExpect(jsonPath("$.fatMassKg").isNumber())
                    .andExpect(jsonPath("$.leanMassKg").isNumber())
                    .andExpect(jsonPath("$.notes").value("Medição inicial."));

            mockMvc.perform(get(BODY_METRICS_ENDPOINT + "/latest")
                            .cookie(accessTokenCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(secondMetricId))
                    .andExpect(jsonPath("$.measuredAt").value("2026-06-20T10:00:00"))
                    .andExpect(jsonPath("$.weightKg").value(78.0));

            mockMvc.perform(get(BODY_METRICS_ENDPOINT)
                            .cookie(accessTokenCookie)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.metrics.totalElements").value(2))
                    .andExpect(jsonPath("$.metrics.content.length()").value(2))
                    .andExpect(jsonPath("$.metrics.content[0].id").value(secondMetricId))
                    .andExpect(jsonPath("$.metrics.content[0].measuredAt").value("2026-06-20T10:00:00"))
                    .andExpect(jsonPath("$.metrics.content[1].id").value(firstMetricId))
                    .andExpect(jsonPath("$.metrics.content[1].measuredAt").value("2026-06-01T10:00:00"));

            mockMvc.perform(get(BODY_METRICS_ENDPOINT + "/progress/changes")
                            .cookie(accessTokenCookie)
                            .param("startDate", "2026-06-01")
                            .param("endDate", "2026-06-28"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.changes[0].metric").value("WEIGHT_KG"))
                    .andExpect(jsonPath("$.changes[0].firstDate").value("2026-06-01"))
                    .andExpect(jsonPath("$.changes[0].firstValue").value(80.0))
                    .andExpect(jsonPath("$.changes[0].lastDate").value("2026-06-20"))
                    .andExpect(jsonPath("$.changes[0].lastValue").value(78.0))
                    .andExpect(jsonPath("$.changes[0].absoluteChange").value(-2.0))
                    .andExpect(jsonPath("$.changes[0].percentageChange").value(-2.5));

            mockMvc.perform(get(BODY_METRICS_ENDPOINT + "/progress/body-composition")
                            .cookie(accessTokenCookie)
                            .param("startDate", "2026-06-01")
                            .param("endDate", "2026-06-28"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.chartType").value("BODY_COMPOSITION"))
                    .andExpect(jsonPath("$.series[0].metric").value("WEIGHT_KG"))
                    .andExpect(jsonPath("$.series[0].points[0].period").value("2026-06"))
                    .andExpect(jsonPath("$.series[0].points[0].value").value(78.0));

            UpdateUserBodyMetricsRequest updateRequest = new UpdateUserBodyMetricsRequest(
                    77.5,
                    180.0,
                    circumferences(),
                    "Medição atualizada."
            );

            mockMvc.perform(put(BODY_METRICS_ENDPOINT + "/{id}", secondMetricId)
                            .cookie(accessTokenCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(secondMetricId))
                    .andExpect(jsonPath("$.weightKg").value(77.5))
                    .andExpect(jsonPath("$.bmi").value(23.919753086419753))
                    .andExpect(jsonPath("$.notes").value("Medição atualizada."))
                    .andExpect(jsonPath("$.updatedAt").isString());

            UserBodyMetricsEntity updatedMetric = userBodyMetricsJpaRepository.findById(secondMetricId).orElseThrow();
            assertThat(updatedMetric.getWeightKg()).isEqualTo(77.5);
            assertThat(updatedMetric.getBmi()).isEqualTo(23.919753086419753);
            assertThat(updatedMetric.getBodyFatPercentage()).isNotNull();
            assertThat(updatedMetric.getFatMassKg()).isNotNull();
            assertThat(updatedMetric.getLeanMassKg()).isNotNull();

            mockMvc.perform(delete(BODY_METRICS_ENDPOINT + "/{id}", firstMetricId)
                            .cookie(accessTokenCookie))
                    .andExpect(status().isNoContent());

            assertThat(userBodyMetricsJpaRepository.findById(firstMetricId)).isEmpty();
            assertThat(userBodyMetricsJpaRepository.findById(secondMetricId)).isPresent();
        }
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

        Cookie accessTokenCookie = result.getResponse().getCookie("access_token");

        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getValue()).isNotBlank();

        return accessTokenCookie;
    }

    private Long createUserBodyMetrics(Cookie accessTokenCookie, CreateUserBodyMetricsRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post(BODY_METRICS_ENDPOINT)
                        .cookie(accessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.weightKg").value(request.weightKg()))
                .andExpect(jsonPath("$.heightCm").value(request.heightCm()))
                .andExpect(jsonPath("$.bmi").isNumber())
                .andExpect(jsonPath("$.bodyFatPercentage").isNumber())
                .andExpect(jsonPath("$.fatMass").isNumber())
                .andExpect(jsonPath("$.leanMass").isNumber())
                .andExpect(jsonPath("$.notes").value(request.notes()))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        Long id = response.get("id").asLong();

        assertThat(userBodyMetricsJpaRepository.findById(id)).isPresent();

        return id;
    }

    private void updateMeasuredAt(Long metricId, LocalDateTime measuredAt) {
        UserBodyMetricsEntity entity = userBodyMetricsJpaRepository.findById(metricId).orElseThrow();
        entity.setMeasuredAt(measuredAt);
        userBodyMetricsJpaRepository.saveAndFlush(entity);
    }

    private CreateUserBodyMetricsRequest createRequest(Double weightKg, String notes) {
        return new CreateUserBodyMetricsRequest(
                weightKg,
                180.0,
                circumferences(),
                notes
        );
    }

    private BodyCircumferencesRequest circumferences() {
        return new BodyCircumferencesRequest(
                39.0,
                104.0,
                118.0,
                33.0,
                27.0,
                82.0,
                94.0,
                55.0,
                36.0
        );
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
