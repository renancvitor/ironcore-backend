package com.ironcore.interfaces.rest.workoutsession.workoutsession;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseCardioEvolutionPointResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseStrengthEvolutionPointResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseCardioEvolutionResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseCardioEvolutionUseCase;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseStrengthEvolutionResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseStrengthEvolutionUseCase;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.analytics.service.ExecutionMetricsCalculator;
import com.ironcore.domain.workoutsession.analytics.valueobject.ActivityExecution;
import com.ironcore.domain.workoutsession.analytics.valueobject.SetExecution;
import com.ironcore.domain.workoutsession.analytics.valueobject.StrengthExecutionMetrics;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.infrastructure.security.config.SecurityConfig;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.workoutsession.ExerciseAnalyticsController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.json.JsonCompareMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExerciseAnalyticsController.class)
@Import(SecurityConfig.class)
class ExerciseAnalyticsControllerTest {

    private static final String BASE = "/api/users/me/workout-sessions/analytics/exercises/";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetExerciseStrengthEvolutionUseCase strengthUseCase;

    @MockitoBean
    private GetExerciseCardioEvolutionUseCase cardioUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;
    private final AuthenticatedUser principal = new AuthenticatedUser(new UserId(77L), new Email("actor@example.com"), false);

    @ParameterizedTest
    @ValueSource(strings = {"strength", "cardio"})
    void shouldRequireAuthentication(String metricType) throws Exception {
        mockMvc.perform(get(BASE + "42/" + metricType).param("startDate", "2026-10-01").param("endDate", "2026-10-27"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(strengthUseCase, cardioUseCase);
    }

    @Test
    void shouldReturnStrengthCoverageAndNullsWithAuthenticatedIdentityAndNoPagination() throws Exception {
        var point = new ExerciseStrengthEvolutionPointResult(new WorkoutSessionId(501L), LocalDate.of(2026, 10, 20),
                Instant.parse("2026-10-20T21:00:00Z"), "Historical",
                new StrengthExecutionMetrics(null, null, 3, 2, 0));
        when(strengthUseCase.execute(any())).thenReturn(new GetExerciseStrengthEvolutionResult(new ExerciseId(42L), List.of(point)));
        mockMvc.perform(request("strength").param("personId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseId").value(42))
                .andExpect(jsonPath("$.points[0].sessionId").value(501))
                .andExpect(jsonPath("$.points[0].performedOn").value("2026-10-20"))
                .andExpect(jsonPath("$.points[0].exerciseNameSnapshot").value("Historical"))
                .andExpect(jsonPath("$.points[0].maxNormalizedLoadKg").value(nullValue()))
                .andExpect(jsonPath("$.points[0].volumeKgReps").value(nullValue()))
                .andExpect(jsonPath("$.points[0].registeredSetCount").value(3))
                .andExpect(jsonPath("$.points[0].loadedSetCount").value(2))
                .andExpect(jsonPath("$.points[0].volumeSetCount").value(0))
                .andExpect(jsonPath("$.page").doesNotExist());
        mockMvc.perform(request("strength"))
                .andExpect(content().json("""
                        {
                          "exerciseId": 42,
                          "points": [{
                            "sessionId": 501,
                            "performedOn": "2026-10-20",
                            "startedAt": "2026-10-20T21:00:00Z",
                            "exerciseNameSnapshot": "Historical",
                            "maxNormalizedLoadKg": null,
                            "volumeKgReps": null,
                            "registeredSetCount": 3,
                            "loadedSetCount": 2,
                            "volumeSetCount": 0
                          }]
                        }
                        """, JsonCompareMode.STRICT));
        verify(strengthUseCase, times(2)).execute(argThat(command -> command.actorUserId().equals(principal.userId())
                && command.exerciseId().value() == 42L
                && command.startDate().equals(LocalDate.of(2026, 10, 1))
                && command.endDate().equals(LocalDate.of(2026, 10, 27))));
    }

    @Test
    void shouldReturnCardioPointAndWholePeriodSummaryWithExplicitUnitsAndNulls() throws Exception {
        var metrics = ExecutionMetricsCalculator.calculateCardio(List.of(new ActivityExecution(30, new BigDecimal("5"))));
        var point = new ExerciseCardioEvolutionPointResult(new WorkoutSessionId(510L), LocalDate.of(2026, 10, 21),
                Instant.parse("2026-10-21T10:00:00Z"), "Historical cardio", metrics);
        var summary = ExecutionMetricsCalculator.calculateCardio(List.of(new ActivityExecution(30, new BigDecimal("5")),
                new ActivityExecution(10, null)));
        when(cardioUseCase.execute(any())).thenReturn(new GetExerciseCardioEvolutionResult(new ExerciseId(42L), List.of(point), summary));
        mockMvc.perform(request("cardio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.points[0].distanceKm").value(5))
                .andExpect(jsonPath("$.points[0].durationMinutes").value(30))
                .andExpect(jsonPath("$.points[0].averagePaceMinPerKm").value(6))
                .andExpect(jsonPath("$.points[0].averageSpeedKmPerHour").value(10))
                .andExpect(jsonPath("$.summary.totalDurationMinutes").value(40))
                .andExpect(jsonPath("$.summary.averagePaceMinPerKm").value(6))
                .andExpect(jsonPath("$.summary.durationRecordCount").value(2));
        mockMvc.perform(request("cardio"))
                .andExpect(content().json("""
                        {
                          "exerciseId": 42,
                          "points": [{
                            "sessionId": 510,
                            "performedOn": "2026-10-21",
                            "startedAt": "2026-10-21T10:00:00Z",
                            "exerciseNameSnapshot": "Historical cardio",
                            "distanceKm": 5.00,
                            "durationMinutes": 30,
                            "averagePaceMinPerKm": 6.00,
                            "averageSpeedKmPerHour": 10.00,
                            "activityCount": 1,
                            "distanceRecordCount": 1,
                            "durationRecordCount": 1,
                            "paceRecordCount": 1,
                            "speedRecordCount": 1
                          }],
                          "summary": {
                            "totalDistanceKm": 5.00,
                            "totalDurationMinutes": 40,
                            "averagePaceMinPerKm": 6.00,
                            "averageSpeedKmPerHour": 10.00,
                            "distanceRecordCount": 1,
                            "durationRecordCount": 2,
                            "paceRecordCount": 1,
                            "speedRecordCount": 1
                          }
                        }
                        """, JsonCompareMode.STRICT));
        verify(cardioUseCase, times(2)).execute(argThat(command -> command.actorUserId().equals(principal.userId())));
        when(cardioUseCase.execute(any())).thenReturn(new GetExerciseCardioEvolutionResult(new ExerciseId(42L), List.of(), ExecutionMetricsCalculator.calculateCardio(List.of())));
        mockMvc.perform(request("cardio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.points").isEmpty())
                .andExpect(jsonPath("$.summary.totalDistanceKm").value(nullValue()))
                .andExpect(jsonPath("$.summary.totalDurationMinutes").value(nullValue()))
                .andExpect(jsonPath("$.summary.averagePaceMinPerKm").value(nullValue()))
                .andExpect(jsonPath("$.summary.averageSpeedKmPerHour").value(nullValue()))
                .andExpect(jsonPath("$.summary.distanceRecordCount").value(0));
    }

    @ParameterizedTest
    @CsvSource({"strength,403", "strength,404", "strength,500", "cardio,403", "cardio,404", "cardio,500"})
    void shouldReuseExistingErrorResponses(String metricType, int statusCode) throws Exception {
        RuntimeException failure = switch (statusCode) {
            case 403 -> new UserInactiveException("Inativo.");
            case 404 -> new ResourceNotFoundException("Não encontrado.");
            default -> new PersistenceException("Falha.", new RuntimeException());
        };
        if (metricType.equals("strength")) {
            when(strengthUseCase.execute(any())).thenThrow(failure);
        } else {
            when(cardioUseCase.execute(any())).thenThrow(failure);
        }
        mockMvc.perform(request(metricType)).andExpect(status().is(statusCode));
    }

    @Test
    void shouldSerializeRecordedZerosAndMissingCardioPointMeasurements() throws Exception {
        var date = LocalDate.of(2026, 10, 20);
        var time = Instant.parse("2026-10-20T12:00:00Z");
        var zero = ExecutionMetricsCalculator.calculateStrength(List.of(new SetExecution(BigDecimal.ZERO,
                ActualLoadMode.TOTAL, 1)));
        when(strengthUseCase.execute(any())).thenReturn(new GetExerciseStrengthEvolutionResult(new ExerciseId(42L),
                List.of(new ExerciseStrengthEvolutionPointResult(new WorkoutSessionId(1L), date, time, "Name", zero))));
        mockMvc.perform(request("strength")).andExpect(status().isOk())
                .andExpect(jsonPath("$.points[0].maxNormalizedLoadKg").value(0))
                .andExpect(jsonPath("$.points[0].volumeKgReps").value(0));
        var missing = ExecutionMetricsCalculator.calculateCardio(List.of(new ActivityExecution(null, null)));
        when(cardioUseCase.execute(any())).thenReturn(new GetExerciseCardioEvolutionResult(new ExerciseId(42L),
                List.of(new ExerciseCardioEvolutionPointResult(new WorkoutSessionId(1L), date, time, "Name", missing)), missing));
        mockMvc.perform(request("cardio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.points[0].distanceKm").value(nullValue()))
                .andExpect(jsonPath("$.points[0].durationMinutes").value(nullValue()))
                .andExpect(jsonPath("$.points[0].averagePaceMinPerKm").value(nullValue()))
                .andExpect(jsonPath("$.points[0].averageSpeedKmPerHour").value(nullValue()));
    }

    @ParameterizedTest
    @CsvSource({"strength,startDate", "strength,endDate", "cardio,startDate", "cardio,endDate"})
    void shouldRequireBothDates(String metricType, String missing) throws Exception {
        var request = get(BASE + "42/" + metricType).with(authentication(authenticationToken()));
        request.param(missing.equals("startDate") ? "endDate" : "startDate", "2026-10-27");
        mockMvc.perform(request).andExpect(status().isBadRequest());
        verifyNoInteractions(strengthUseCase, cardioUseCase);
    }

    @ParameterizedTest
    @CsvSource({"strength,0", "strength,-1", "strength,abc", "cardio,0", "cardio,-1", "cardio,abc"})
    void shouldRejectInvalidExerciseIds(String metricType, String id) throws Exception {
        mockMvc.perform(get(BASE + id + "/" + metricType).param("startDate", "2026-10-01").param("endDate", "2026-10-27")
                .with(authentication(authenticationToken()))).andExpect(status().isBadRequest());
        verifyNoInteractions(strengthUseCase, cardioUseCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {"strength", "cardio"})
    void shouldRejectMalformedDatesAndPreserveBusinessErrorContract(String metricType) throws Exception {
        mockMvc.perform(get(BASE + "42/" + metricType).param("startDate", "invalid").param("endDate", "2026-10-27")
                .with(authentication(authenticationToken()))).andExpect(status().isBadRequest());
        if (metricType.equals("strength")) {
            when(strengthUseCase.execute(any())).thenThrow(new OperationNotAllowedException("Período inválido."));
        } else {
            when(cardioUseCase.execute(any())).thenThrow(new OperationNotAllowedException("Período inválido."));
        }
        mockMvc.perform(request(metricType)).andExpect(status().isUnprocessableEntity());
    }

    private MockHttpServletRequestBuilder request(String metricType) {
        return get(BASE + "42/" + metricType).param("startDate", "2026-10-01").param("endDate", "2026-10-27")
                .with(authentication(authenticationToken()));
    }

    private UsernamePasswordAuthenticationToken authenticationToken() {
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }
}
