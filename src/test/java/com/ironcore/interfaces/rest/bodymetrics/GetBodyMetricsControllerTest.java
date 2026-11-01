package com.ironcore.interfaces.rest.bodymetrics;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.delete.DeleteBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsCommand;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsResult;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.progress.GetBodyMetricsProgressChangesUseCase;
import com.ironcore.application.bodymetrics.progress.GetBodyMetricsProgressChartUseCase;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsUseCase;
import com.ironcore.domain.bodymetrics.valueobject.*;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BodyMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GetBodyMetricsControllerTest {

    private static final String BODY_METRICS_BASE_ENDPOINT = "/api/users/me/body-metrics";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetBodyMetricsUseCase getBodyMetricsUseCase;

    @MockitoBean
    private CreateBodyMetricsUseCase createBodyMetricsUseCase;

    @MockitoBean
    private UpdateBodyMetricsUseCase updateBodyMetricsUseCase;

    @MockitoBean
    private DeleteBodyMetricsUseCase deleteBodyMetricsUseCase;

    @MockitoBean
    private ListBodyMetricsUseCase listBodyMetricsUseCase;

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

    @Test
    void shouldReturnPersonBodyMetricsById() throws Exception {
        GetBodyMetricsCommand command = new GetBodyMetricsCommand(
                new BodyMetricsId(1L),
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
        GetBodyMetricsResult result = new GetBodyMetricsResult(
                new BodyMetricsId(1L),
                new PersonId(1L),
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

        when(getBodyMetricsUseCase.execute(command))
                .thenReturn(result);

        mockMvc.perform(get(BODY_METRICS_BASE_ENDPOINT + "/1")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.personId").value(1L))
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

        verify(getBodyMetricsUseCase).execute(command);
    }

    @Test
    void shouldReturnNotFoundWhenPersonBodyMetricsDoesNotExist() throws Exception {
        GetBodyMetricsCommand command = new GetBodyMetricsCommand(
                new BodyMetricsId(1L),
                new UserId(1L)
        );

        when(getBodyMetricsUseCase.execute(command))
                .thenThrow(new ResourceNotFoundException("Métricas corporais não encontradas."));

        mockMvc.perform(get(BODY_METRICS_BASE_ENDPOINT + "/1")
                        .with(authenticatedUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Métricas corporais não encontradas."))
                .andExpect(jsonPath("$.path")
                        .value(BODY_METRICS_BASE_ENDPOINT + "/1"))
                .andExpect(jsonPath("$.fields").isArray());

        verify(getBodyMetricsUseCase).execute(command);
    }
}
