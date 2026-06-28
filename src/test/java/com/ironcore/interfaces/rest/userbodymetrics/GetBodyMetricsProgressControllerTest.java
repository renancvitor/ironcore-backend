package com.ironcore.interfaces.rest.userbodymetrics;

import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.progress.*;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsUseCase;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserBodyMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
class GetBodyMetricsProgressControllerTest {

    private static final String BODY_METRICS_BASE_ENDPOINT = "/api/users/me/body-metrics";
    private static final String BODY_COMPOSITION_ENDPOINT =
            BODY_METRICS_BASE_ENDPOINT + "/progress/body-composition";
    private static final String CIRCUMFERENCES_ENDPOINT =
            BODY_METRICS_BASE_ENDPOINT + "/progress/circumferences";
    private static final String BODY_FAT_ENDPOINT =
            BODY_METRICS_BASE_ENDPOINT + "/progress/body-fat";
    private static final String CHANGES_ENDPOINT =
            BODY_METRICS_BASE_ENDPOINT + "/progress/changes";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetBodyMetricsProgressChartUseCase getBodyMetricsProgressChartUseCase;

    @MockitoBean
    private GetBodyMetricsProgressChangesUseCase getBodyMetricsProgressChangesUseCase;

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
    private GetLatestUserBodyMetricsUseCase getLatestUserBodyMetricsUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private UserBodyMetricsRepository userBodyMetricsRepository;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    class SuccessfulGetChart {

        @Test
        void shouldReturnBodyCompositionProgressChart() throws Exception {
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    new UserId(1L),
                    BodyMetricsProgressChartType.BODY_COMPOSITION,
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 27)
            );
            GetBodyMetricsProgressChartResult result = new GetBodyMetricsProgressChartResult(
                    command.startDate(),
                    command.endDate(),
                    command.chartType(),
                    List.of(new BodyMetricsProgressSeriesResult(
                            BodyMetricsProgressMetric.WEIGHT_KG,
                            "Peso",
                            "kg",
                            List.of(
                                    new BodyMetricsProgressPointResult("2026-06-01", 80.0),
                                    new BodyMetricsProgressPointResult("2026-06-27", 78.0)
                            )
                    ))
            );

            when(getBodyMetricsProgressChartUseCase.execute(command)).thenReturn(result);

            mockMvc.perform(get(BODY_COMPOSITION_ENDPOINT)
                            .with(authenticatedUser())
                            .param("startDate", "2026-06-01")
                            .param("endDate", "2026-06-27"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startDate").value("2026-06-01"))
                    .andExpect(jsonPath("$.endDate").value("2026-06-27"))
                    .andExpect(jsonPath("$.chartType").value("BODY_COMPOSITION"))
                    .andExpect(jsonPath("$.series").isArray())
                    .andExpect(jsonPath("$.series.length()").value(1))
                    .andExpect(jsonPath("$.series[0].metric").value("WEIGHT_KG"))
                    .andExpect(jsonPath("$.series[0].label").value("Peso"))
                    .andExpect(jsonPath("$.series[0].unit").value("kg"))
                    .andExpect(jsonPath("$.series[0].points.length()").value(2))
                    .andExpect(jsonPath("$.series[0].points[0].period").value("2026-06-01"))
                    .andExpect(jsonPath("$.series[0].points[0].value").value(80.0))
                    .andExpect(jsonPath("$.series[0].points[1].period").value("2026-06-27"))
                    .andExpect(jsonPath("$.series[0].points[1].value").value(78.0));

            verify(getBodyMetricsProgressChartUseCase).execute(command);
        }

        @Test
        void shouldReturnCircumferencesProgressChart() throws Exception {
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    new UserId(1L),
                    BodyMetricsProgressChartType.CIRCUMFERENCES,
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 27)
            );
            GetBodyMetricsProgressChartResult result = new GetBodyMetricsProgressChartResult(
                    command.startDate(),
                    command.endDate(),
                    command.chartType(),
                    List.of()
            );

            when(getBodyMetricsProgressChartUseCase.execute(command)).thenReturn(result);

            mockMvc.perform(get(CIRCUMFERENCES_ENDPOINT)
                            .with(authenticatedUser())
                            .param("startDate", "2026-06-01")
                            .param("endDate", "2026-06-27"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.chartType").value("CIRCUMFERENCES"))
                    .andExpect(jsonPath("$.series").isArray())
                    .andExpect(jsonPath("$.series").isEmpty());

            verify(getBodyMetricsProgressChartUseCase).execute(command);
        }

        @Test
        void shouldReturnBodyFatProgressChart() throws Exception {
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    new UserId(1L),
                    BodyMetricsProgressChartType.BODY_FAT,
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 27)
            );
            GetBodyMetricsProgressChartResult result = new GetBodyMetricsProgressChartResult(
                    command.startDate(),
                    command.endDate(),
                    command.chartType(),
                    List.of()
            );

            when(getBodyMetricsProgressChartUseCase.execute(command)).thenReturn(result);

            mockMvc.perform(get(BODY_FAT_ENDPOINT)
                            .with(authenticatedUser())
                            .param("startDate", "2026-06-01")
                            .param("endDate", "2026-06-27"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.chartType").value("BODY_FAT"))
                    .andExpect(jsonPath("$.series").isArray())
                    .andExpect(jsonPath("$.series").isEmpty());

            verify(getBodyMetricsProgressChartUseCase).execute(command);
        }
    }

    @Nested
    class SuccessfulGetChanges {

        @Test
        void shouldReturnProgressChanges() throws Exception {
            BodyMetricsProgressChangesCommand command = new BodyMetricsProgressChangesCommand(
                    new UserId(1L),
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 27)
            );
            GetBodyMetricsProgressChangesResult result = new GetBodyMetricsProgressChangesResult(
                    command.startDate(),
                    command.endDate(),
                    List.of(new BodyMetricsProgressChangeResult(
                            BodyMetricsProgressMetric.WEIGHT_KG,
                            "Peso",
                            "kg",
                            LocalDate.of(2026, 6, 1),
                            80.0,
                            LocalDate.of(2026, 6, 27),
                            78.0,
                            -2.0,
                            -2.5
                    ))
            );

            when(getBodyMetricsProgressChangesUseCase.execute(command)).thenReturn(result);

            mockMvc.perform(get(CHANGES_ENDPOINT)
                            .with(authenticatedUser())
                            .param("startDate", "2026-06-01")
                            .param("endDate", "2026-06-27"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startDate").value("2026-06-01"))
                    .andExpect(jsonPath("$.endDate").value("2026-06-27"))
                    .andExpect(jsonPath("$.changes").isArray())
                    .andExpect(jsonPath("$.changes.length()").value(1))
                    .andExpect(jsonPath("$.changes[0].metric").value("WEIGHT_KG"))
                    .andExpect(jsonPath("$.changes[0].label").value("Peso"))
                    .andExpect(jsonPath("$.changes[0].unit").value("kg"))
                    .andExpect(jsonPath("$.changes[0].firstDate").value("2026-06-01"))
                    .andExpect(jsonPath("$.changes[0].firstValue").value(80.0))
                    .andExpect(jsonPath("$.changes[0].lastDate").value("2026-06-27"))
                    .andExpect(jsonPath("$.changes[0].lastValue").value(78.0))
                    .andExpect(jsonPath("$.changes[0].absoluteChange").value(-2.0))
                    .andExpect(jsonPath("$.changes[0].percentageChange").value(-2.5));

            verify(getBodyMetricsProgressChangesUseCase).execute(command);
        }
    }

    @Nested
    class RequestValidation {

        @Test
        void shouldFailWhenChartDatesAreNotProvided() throws Exception {
            mockMvc.perform(get(BODY_COMPOSITION_ENDPOINT)
                            .with(authenticatedUser()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.path").value(BODY_COMPOSITION_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isArray())
                    .andExpect(jsonPath("$.fields[*].field", containsInAnyOrder(
                            "startDate",
                            "endDate"
                    )));

            verify(getBodyMetricsProgressChartUseCase, never()).execute(any());
        }

        @Test
        void shouldFailWhenChangesDatesAreNotProvided() throws Exception {
            mockMvc.perform(get(CHANGES_ENDPOINT)
                            .with(authenticatedUser()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.path").value(CHANGES_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isArray())
                    .andExpect(jsonPath("$.fields[*].field", containsInAnyOrder(
                            "startDate",
                            "endDate"
                    )));

            verify(getBodyMetricsProgressChangesUseCase, never()).execute(any());
        }
    }
}
