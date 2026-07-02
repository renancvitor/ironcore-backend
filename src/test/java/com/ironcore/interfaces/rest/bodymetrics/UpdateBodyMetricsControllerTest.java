package com.ironcore.interfaces.rest.bodymetrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.delete.DeleteBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.progress.GetBodyMetricsProgressChangesUseCase;
import com.ironcore.application.bodymetrics.progress.GetBodyMetricsProgressChartUseCase;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsCommand;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsResult;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsUseCase;
import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.bodymetrics.dto.BodyCircumferencesRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.update.UpdateBodyMetricsRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BodyMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UpdateBodyMetricsControllerTest {

    private static final String BODY_METRICS_BASE_ENDPOINT = "/api/users/me/body-metrics";
    private static final String BODY_METRICS_ENDPOINT = BODY_METRICS_BASE_ENDPOINT + "/{id}";
    private static final Long BODY_METRICS_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UpdateBodyMetricsUseCase updateBodyMetricsUseCase;

    @MockitoBean
    private CreateBodyMetricsUseCase createBodyMetricsUseCase;

    @MockitoBean
    private DeleteBodyMetricsUseCase deleteBodyMetricsUseCase;

    @MockitoBean
    private ListBodyMetricsUseCase listBodyMetricsUseCase;

    @MockitoBean
    private GetBodyMetricsUseCase getBodyMetricsUseCase;

    @MockitoBean
    private GetLatestBodyMetricsUseCase getLatestBodyMetricsUseCase;

    @MockitoBean
    private GetBodyMetricsProgressChartUseCase getBodyMetricsProgressChartUseCase;

    @MockitoBean
    private GetBodyMetricsProgressChangesUseCase getBodyMetricsProgressChangesUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private BodyMetricsRepository bodyMetricsRepository;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    class SuccessfulUpdate {

        @Test
        void shouldUpdateUserBodyMetricsWithCircumferences() throws Exception {
            BodyCircumferencesRequest circumferencesRequest = new BodyCircumferencesRequest(
                    39.0,
                    104.0,
                    118.0,
                    33.0,
                    27.0,
                    79.0,
                    93.0,
                    55.0,
                    36.0
            );
            UpdateBodyMetricsRequest request = new UpdateBodyMetricsRequest(
                    65.0,
                    167.0,
                    circumferencesRequest,
                    "TEXT"
            );

            BodyCircumferences circumferences = new BodyCircumferences(
                    new BodyCircumferenceCm(39.0),
                    new BodyCircumferenceCm(104.0),
                    new BodyCircumferenceCm(118.0),
                    new BodyCircumferenceCm(33.0),
                    new BodyCircumferenceCm(27.0),
                    new BodyCircumferenceCm(79.0),
                    new BodyCircumferenceCm(93.0),
                    new BodyCircumferenceCm(55.0),
                    new BodyCircumferenceCm(36.0)
            );
            UpdateBodyMetricsCommand command = new UpdateBodyMetricsCommand(
                    new BodyMetricsId(1L),
                    new UserId(1L),
                    new BodyWeightKg(65.0),
                    new BodyHeightCm(167.0),
                    circumferences,
                    "TEXT"
            );
            UpdateBodyMetricsResult result = new UpdateBodyMetricsResult(
                    new BodyMetricsId(1L),
                    new UserId(1L),
                    LocalDateTime.of(2026, 6, 14, 10, 0),
                    new BodyWeightKg(65.0),
                    new BodyHeightCm(167.0),
                    circumferences,
                    new BMI(23.31),
                    new BodyFatPercentage(12.5),
                    new FatMassKg(56.9),
                    new LeanMassKg(8.1),
                    "TEXT",
                    LocalDateTime.of(2026, 6, 15, 10, 0)
            );

            when(updateBodyMetricsUseCase.execute(command)).thenReturn(result);

            mockMvc.perform(put(BODY_METRICS_ENDPOINT, BODY_METRICS_ID)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.weightKg").value(65.0))
                    .andExpect(jsonPath("$.heightCm").value(167.0))
                    .andExpect(jsonPath("$.circumferences.neckCm").value(39.0))
                    .andExpect(jsonPath("$.circumferences.chestCm").value(104.0))
                    .andExpect(jsonPath("$.circumferences.shoulderCm").value(118.0))
                    .andExpect(jsonPath("$.circumferences.armCm").value(33.0))
                    .andExpect(jsonPath("$.circumferences.forearmCm").value(27.0))
                    .andExpect(jsonPath("$.circumferences.waistCm").value(79.0))
                    .andExpect(jsonPath("$.circumferences.hipCm").value(93.0))
                    .andExpect(jsonPath("$.circumferences.thighCm").value(55.0))
                    .andExpect(jsonPath("$.circumferences.calfCm").value(36.0))
                    .andExpect(jsonPath("$.bmi").value(23.31))
                    .andExpect(jsonPath("$.bodyFatPercentage").value(12.5))
                    .andExpect(jsonPath("$.fatMass").value(56.9))
                    .andExpect(jsonPath("$.leanMass").value(8.1))
                    .andExpect(jsonPath("$.notes").value("TEXT"))
                    .andExpect(jsonPath("$.updatedAt").value("2026-06-15T10:00:00"));

            verify(updateBodyMetricsUseCase).execute(command);
        }

        @Test
        void shouldUpdateUserBodyMetricsWithoutCircumferences() throws Exception {
            BodyCircumferencesRequest circumferencesRequest = null;
            UpdateBodyMetricsRequest request = new UpdateBodyMetricsRequest(
                    65.0,
                    167.0,
                    circumferencesRequest,
                    "TEXT"
            );

            BodyCircumferences circumferences = null;
            UpdateBodyMetricsCommand command = new UpdateBodyMetricsCommand(
                    new BodyMetricsId(1L),
                    new UserId(1L),
                    new BodyWeightKg(65.0),
                    new BodyHeightCm(167.0),
                    circumferences,
                    "TEXT"
            );
            UpdateBodyMetricsResult result = new UpdateBodyMetricsResult(
                    new BodyMetricsId(1L),
                    new UserId(1L),
                    LocalDateTime.of(2026, 6, 14, 10, 0),
                    new BodyWeightKg(65.0),
                    new BodyHeightCm(167.0),
                    circumferences,
                    new BMI(23.31),
                    null,
                    null,
                    null,
                    "TEXT",
                    LocalDateTime.of(2026, 6, 15, 10, 0)
            );

            when(updateBodyMetricsUseCase.execute(command)).thenReturn(result);

            mockMvc.perform(put(BODY_METRICS_ENDPOINT, BODY_METRICS_ID)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.weightKg").value(65.0))
                    .andExpect(jsonPath("$.heightCm").value(167.0))
                    .andExpect(jsonPath("$.bmi").value(23.31))
                    .andExpect(jsonPath("$.notes").value("TEXT"));

            verify(updateBodyMetricsUseCase).execute(command);
        }
    }

    @Nested
    class RequestValidation {

        @Test
        void shouldFailWhenWeightIsEmpty() throws Exception {
            BodyCircumferencesRequest circumferencesRequest = null;
            UpdateBodyMetricsRequest request = new UpdateBodyMetricsRequest(
                    null,
                    167.0,
                    circumferencesRequest,
                    "TEXT"
            );

            mockMvc.perform(put(BODY_METRICS_ENDPOINT, BODY_METRICS_ID)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message")
                            .value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.path")
                            .value(BODY_METRICS_BASE_ENDPOINT + "/" + BODY_METRICS_ID))
                    .andExpect(jsonPath("$.fields").isArray())
                    .andExpect(jsonPath("$.fields[*].field", containsInAnyOrder(
                            "weightKg"
                    )));

            verify(updateBodyMetricsUseCase, never()).execute(any());
        }

        @Test
        void shouldFailWhenHeightCmIsEmpty() throws Exception {
            BodyCircumferencesRequest circumferencesRequest = null;
            UpdateBodyMetricsRequest request = new UpdateBodyMetricsRequest(
                    65.0,
                    null,
                    circumferencesRequest,
                    "TEXT"
            );

            mockMvc.perform(put(BODY_METRICS_ENDPOINT, 1L)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message")
                            .value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.path")
                            .value(BODY_METRICS_BASE_ENDPOINT +  "/" + BODY_METRICS_ID))
                    .andExpect(jsonPath("$.fields").isArray())
                    .andExpect(jsonPath("$.fields[*].field", containsInAnyOrder(
                            "heightCm"
                    )));

            verify(updateBodyMetricsUseCase, never()).execute(any());
        }

        @Test
        void shouldFailWhenWeightOrHeightIsZeroOrNegative() throws Exception {
            BodyCircumferencesRequest circumferencesRequest = new BodyCircumferencesRequest(
                    -0.0,
                    -104.0,
                    -118.0,
                    0.0,
                    0.0,
                    -79.0,
                    0.0,
                    -55.0,
                    0.0
            );
            UpdateBodyMetricsRequest request = new UpdateBodyMetricsRequest(
                    0.0,
                    -167.0,
                    circumferencesRequest,
                    "TEXT"
            );

            mockMvc.perform(put(BODY_METRICS_ENDPOINT, BODY_METRICS_ID)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.path")
                            .value(BODY_METRICS_BASE_ENDPOINT + "/" + BODY_METRICS_ID))
                    .andExpect(jsonPath("$.fields").isArray())
                    .andExpect(jsonPath("$.fields[*].field", containsInAnyOrder(
                            "weightKg",
                            "heightCm",
                            "circumferences.neckCm",
                            "circumferences.chestCm",
                            "circumferences.shoulderCm",
                            "circumferences.armCm",
                            "circumferences.forearmCm",
                            "circumferences.waistCm",
                            "circumferences.hipCm",
                            "circumferences.thighCm",
                            "circumferences.calfCm"
                    )));

            verify(updateBodyMetricsUseCase, never()).execute(any());
        }
    }
}
