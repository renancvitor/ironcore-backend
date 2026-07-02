package com.ironcore.interfaces.rest.bodymetrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsCommand;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsResult;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.delete.DeleteBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.progress.GetBodyMetricsProgressChangesUseCase;
import com.ironcore.application.bodymetrics.progress.GetBodyMetricsProgressChartUseCase;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsUseCase;
import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.bodymetrics.dto.BodyCircumferencesRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.create.CreateBodyMetricsRequest;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsInAnyOrder;

@WebMvcTest(BodyMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CreateBodyMetricsControllerTest {

    private static final String BODY_METRICS_ENDPOINT = "/api/users/me/body-metrics";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateBodyMetricsUseCase createBodyMetricsUseCase;

    @MockitoBean
    private UpdateBodyMetricsUseCase updateBodyMetricsUseCase;

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
    class SuccessfulCreation {

        @Test
        void shouldCreatePersonBodyMetricsWithCircumferences() throws Exception {
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
            CreateBodyMetricsRequest request = new CreateBodyMetricsRequest(
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
            CreateBodyMetricsCommand command = new CreateBodyMetricsCommand(
                    new UserId(1L),
                    new BodyWeightKg(65.0),
                    new BodyHeightCm(167.0),
                    circumferences,
                    "TEXT"
            );
            CreateBodyMetricsResult result = new CreateBodyMetricsResult(
                    new BodyMetricsId(1L),
                    new PersonId(1L),
                    LocalDateTime.of(2026, 6, 14, 10, 0),
                    new BodyWeightKg(65.0),
                    new BodyHeightCm(167.0),
                    circumferences,
                    new BMI(23.31),
                    new BodyFatPercentage(12.5),
                    new FatMassKg(56.9),
                    new LeanMassKg(8.1),
                    "TEXT"
            );

            when(createBodyMetricsUseCase.execute(command)).thenReturn(result);

            mockMvc.perform(post(BODY_METRICS_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.personId").value(1L))
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
                    .andExpect(jsonPath("$.fatMassKg").value(56.9))
                    .andExpect(jsonPath("$.leanMassKg").value(8.1))
                    .andExpect(jsonPath("$.notes").value("TEXT"));

            verify(createBodyMetricsUseCase).execute(command);
        }

        @Test
        void shouldCreatePersonBodyMetricsWithoutCircumferences() throws Exception {
            BodyCircumferencesRequest circumferencesRequest = null;
            CreateBodyMetricsRequest request = new CreateBodyMetricsRequest(
                    65.0,
                    167.0,
                    circumferencesRequest,
                    "TEXT"
            );

            BodyCircumferences circumferences = null;
            CreateBodyMetricsCommand command = new CreateBodyMetricsCommand(
                    new UserId(1L),
                    new BodyWeightKg(65.0),
                    new BodyHeightCm(167.0),
                    circumferences,
                    "TEXT"
            );
            CreateBodyMetricsResult result = new CreateBodyMetricsResult(
                    new BodyMetricsId(1L),
                    new PersonId(1L),
                    LocalDateTime.of(2026, 6, 14, 10, 0),
                    new BodyWeightKg(65.0),
                    new BodyHeightCm(167.0),
                    circumferences,
                    new BMI(23.31),
                    null,
                    null,
                    null,
                    "TEXT"
            );

            when(createBodyMetricsUseCase.execute(command)).thenReturn(result);

            mockMvc.perform(post(BODY_METRICS_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.personId").value(1L))
                    .andExpect(jsonPath("$.weightKg").value(65.0))
                    .andExpect(jsonPath("$.heightCm").value(167.0))
                    .andExpect(jsonPath("$.bmi").value(23.31))
                    .andExpect(jsonPath("$.notes").value("TEXT"));

            verify(createBodyMetricsUseCase).execute(command);
        }
    }

    @Nested
    class RequestValidation {

        @Test
        void shouldFailWhenWeightIsEmpty() throws Exception {
            BodyCircumferencesRequest circumferencesRequest = null;
            CreateBodyMetricsRequest request = new CreateBodyMetricsRequest(
                    null,
                    167.0,
                    circumferencesRequest,
                    "TEXT"
            );

            mockMvc.perform(post(BODY_METRICS_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.path").value(BODY_METRICS_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isArray())
                    .andExpect(jsonPath("$.fields[*].field", containsInAnyOrder(
                            "weightKg"
                    )));

            verify(createBodyMetricsUseCase, never()).execute(any());

        }

        @Test
        void shouldFailWhenHeightIsEmpty() throws Exception {
            BodyCircumferencesRequest circumferencesRequest = null;
            CreateBodyMetricsRequest request = new CreateBodyMetricsRequest(
                    67.0,
                    null,
                    circumferencesRequest,
                    "TEXT"
            );

            mockMvc.perform(post(BODY_METRICS_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.path").value(BODY_METRICS_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isArray())
                    .andExpect(jsonPath("$.fields[*].field", containsInAnyOrder(
                            "heightCm"
                    )));

            verify(createBodyMetricsUseCase, never()).execute(any());
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
            CreateBodyMetricsRequest request = new CreateBodyMetricsRequest(
                    0.0,
                    -167.0,
                    circumferencesRequest,
                    "TEXT"
            );

            mockMvc.perform(post(BODY_METRICS_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.path").value(BODY_METRICS_ENDPOINT))
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

            verify(createBodyMetricsUseCase, never()).execute(any());
        }
    }
}
