package com.ironcore.interfaces.rest.userbodymetrics;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsUseCase;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import com.ironcore.domain.userbodymetrics.valueobject.*;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserBodyMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GetLatestUserBodyMetricsControllerTest {

    private static final String BODY_METRICS_ENDPOINT = "/api/users/me/body-metrics/latest";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetLatestUserBodyMetricsUseCase getLatestUserBodyMetricsUseCase;

    @MockitoBean
    private CreateUserBodyMetricsUseCase createUserBodyMetricsUseCase;

    @MockitoBean
    private UpdateUserBodyMetricsUseCase updateUserBodyMetricsUseCase;

    @MockitoBean
    private DeleteUserBodyMetricsUseCase deleteUserBodyMetricsUseCase;

    @MockitoBean
    private ListUserBodyMetricsUseCase listUserBodyMetricsUseCase;

    @MockitoBean
    private GetUserBodyMetricsUseCase getUserBodyMetricsUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private UserBodyMetricsRepository userBodyMetricsRepository;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldReturnLatestUserBodyMetrics() throws Exception {
        GetLatestUserBodyMetricsCommand command = new GetLatestUserBodyMetricsCommand(
                new UserId(1L)
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
        GetLatestUserBodyMetricsResult result = new GetLatestUserBodyMetricsResult(
                new UserBodyMetricsId(1L),
                new UserId(1L),
                LocalDateTime.of(2026, 6, 20, 10, 0),
                new BodyWeightKg(65.0),
                new BodyHeightCm(167.0),
                circumferences,
                new BMI(23.31),
                new BodyFatPercentage(12.5),
                new FatMassKg(8.1),
                new LeanMassKg(56.9),
                "Medição de acompanhamento.",
                LocalDateTime.of(2026, 6, 20, 11, 0)
        );

        when(getLatestUserBodyMetricsUseCase.execute(command))
                .thenReturn(result);

        mockMvc.perform(get(BODY_METRICS_ENDPOINT)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.measuredAt").value("2026-06-20T10:00:00"))
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
                .andExpect(jsonPath("$.fatMassKg").value(8.1))
                .andExpect(jsonPath("$.leanMassKg").value(56.9))
                .andExpect(jsonPath("$.notes").value("Medição de acompanhamento."))
                .andExpect(jsonPath("$.updatedAt").value("2026-06-20T11:00:00"));

        verify(getLatestUserBodyMetricsUseCase).execute(command);
    }

    @Test
    void shouldReturnNotFoundWhenLatestUserBodyMetricsDoesNotExist() throws Exception {
        GetLatestUserBodyMetricsCommand command = new GetLatestUserBodyMetricsCommand(
                new UserId(1L)
        );

        when(getLatestUserBodyMetricsUseCase.execute(command))
                .thenThrow(new ResourceNotFoundException("Métricas corporais não encontradas."));

        mockMvc.perform(get(BODY_METRICS_ENDPOINT)
                        .with(authenticatedUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Métricas corporais não encontradas."))
                .andExpect(jsonPath("$.path")
                        .value(BODY_METRICS_ENDPOINT))
                .andExpect(jsonPath("$.fields").isArray());

        verify(getLatestUserBodyMetricsUseCase).execute(command);
    }
}
