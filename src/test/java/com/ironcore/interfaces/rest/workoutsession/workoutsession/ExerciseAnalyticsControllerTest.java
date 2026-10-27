package com.ironcore.interfaces.rest.workoutsession.workoutsession;

import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.workoutsession.workoutsession.analytics.*;
import com.ironcore.domain.user.valueobject.*;
import com.ironcore.domain.workoutsession.analytics.ExecutionMetrics;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.infrastructure.security.config.SecurityConfig;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.workoutsession.ExerciseAnalyticsController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.time.*;
import java.math.BigDecimal;
import java.util.List;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

@WebMvcTest(ExerciseAnalyticsController.class)
@Import(SecurityConfig.class)
class ExerciseAnalyticsControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean GetExerciseAnalyticsUseCase useCase;
    @MockitoBean ErrorLogPublisher errors;
    @MockitoBean JwtAccessTokenValidator jwt;
    @MockitoBean com.ironcore.domain.user.repository.UserRepository users;
    private static final String BASE = "/api/users/me/workout-sessions/analytics/exercises/";
    private final AuthenticatedUser principal = new AuthenticatedUser(new UserId(77L), new Email("actor@example.com"), false);

    @ParameterizedTest
    @ValueSource(strings = {"strength", "cardio"})
    void shouldRequireAuthentication(String kind) throws Exception {
        mvc.perform(get(BASE + "42/" + kind).param("startDate", "2026-10-01").param("endDate", "2026-10-27"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(useCase);
    }

    @Test
    void shouldReturnStrengthCoverageAndNullsWithAuthenticatedIdentityAndNoPagination() throws Exception {
        var point = new ExerciseAnalyticsResult.StrengthPoint(501L, LocalDate.of(2026, 10, 20),
                Instant.parse("2026-10-20T21:00:00Z"), "Historical",
                new ExecutionMetrics.Strength(null, null, 3, 2, 0));
        when(useCase.strength(any())).thenReturn(new ExerciseAnalyticsResult.Strength(42L, List.of(point)));
        mvc.perform(request("strength").param("personId", "999"))
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
        verify(useCase).strength(argThat(c -> c.actorUserId().equals(principal.userId())
                && c.exerciseId().value() == 42L && c.startDate().equals(LocalDate.of(2026, 10, 1))
                && c.endDate().equals(LocalDate.of(2026, 10, 27))));
    }

    @Test
    void shouldReturnCardioPointAndWholePeriodSummaryWithExplicitUnitsAndNulls() throws Exception {
        var metrics = ExecutionMetrics.cardio(List.of(new ExecutionMetrics.ActivityExecution(30, new BigDecimal("5"))));
        var point = new ExerciseAnalyticsResult.CardioPoint(510L, LocalDate.of(2026, 10, 21),
                Instant.parse("2026-10-21T10:00:00Z"), "Historical cardio", metrics);
        var summary = ExecutionMetrics.cardio(List.of(new ExecutionMetrics.ActivityExecution(30, new BigDecimal("5")),
                new ExecutionMetrics.ActivityExecution(10, null)));
        when(useCase.cardio(any())).thenReturn(new ExerciseAnalyticsResult.Cardio(42L, List.of(point), summary));
        mvc.perform(request("cardio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.points[0].distanceKm").value(5))
                .andExpect(jsonPath("$.points[0].durationMinutes").value(30))
                .andExpect(jsonPath("$.points[0].averagePaceMinPerKm").value(6))
                .andExpect(jsonPath("$.points[0].averageSpeedKmPerHour").value(10))
                .andExpect(jsonPath("$.summary.totalDurationMinutes").value(40))
                .andExpect(jsonPath("$.summary.averagePaceMinPerKm").value(6))
                .andExpect(jsonPath("$.summary.durationRecordCount").value(2));
        verify(useCase).cardio(argThat(c -> c.actorUserId().equals(principal.userId())));
        when(useCase.cardio(any())).thenReturn(new ExerciseAnalyticsResult.Cardio(42L, List.of(), ExecutionMetrics.cardio(List.of())));
        mvc.perform(request("cardio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.points").isEmpty())
                .andExpect(jsonPath("$.summary.totalDistanceKm").value(nullValue()))
                .andExpect(jsonPath("$.summary.totalDurationMinutes").value(nullValue()))
                .andExpect(jsonPath("$.summary.averagePaceMinPerKm").value(nullValue()))
                .andExpect(jsonPath("$.summary.averageSpeedKmPerHour").value(nullValue()))
                .andExpect(jsonPath("$.summary.distanceRecordCount").value(0));
    }

    @ParameterizedTest
    @CsvSource({"strength,403", "strength,404", "strength,500", "cardio,403", "cardio,404", "cardio,500"})
    void shouldReuseExistingErrorResponses(String kind, int code) throws Exception {
        RuntimeException failure = switch (code) {
            case 403 -> new com.ironcore.application.exception.UserInactiveException("Inativo.");
            case 404 -> new com.ironcore.application.exception.ResourceNotFoundException("Não encontrado.");
            default -> new com.ironcore.infrastructure.exception.PersistenceException("Falha.", new RuntimeException());
        };
        if (kind.equals("strength")) when(useCase.strength(any())).thenThrow(failure);
        else when(useCase.cardio(any())).thenThrow(failure);
        mvc.perform(request(kind)).andExpect(status().is(code));
    }

    @Test
    void shouldSerializeRecordedZerosAndMissingCardioPointMeasurements() throws Exception {
        var date = LocalDate.of(2026, 10, 20);
        var time = Instant.parse("2026-10-20T12:00:00Z");
        var zero = ExecutionMetrics.strength(List.of(new ExecutionMetrics.SetExecution(BigDecimal.ZERO,
                com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode.TOTAL, 1)));
        when(useCase.strength(any())).thenReturn(new ExerciseAnalyticsResult.Strength(42L,
                List.of(new ExerciseAnalyticsResult.StrengthPoint(1L, date, time, "Name", zero))));
        mvc.perform(request("strength")).andExpect(status().isOk())
                .andExpect(jsonPath("$.points[0].maxNormalizedLoadKg").value(0))
                .andExpect(jsonPath("$.points[0].volumeKgReps").value(0));
        var missing = ExecutionMetrics.cardio(List.of(new ExecutionMetrics.ActivityExecution(null, null)));
        when(useCase.cardio(any())).thenReturn(new ExerciseAnalyticsResult.Cardio(42L,
                List.of(new ExerciseAnalyticsResult.CardioPoint(1L, date, time, "Name", missing)), missing));
        mvc.perform(request("cardio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.points[0].distanceKm").value(nullValue()))
                .andExpect(jsonPath("$.points[0].durationMinutes").value(nullValue()))
                .andExpect(jsonPath("$.points[0].averagePaceMinPerKm").value(nullValue()))
                .andExpect(jsonPath("$.points[0].averageSpeedKmPerHour").value(nullValue()));
    }

    @ParameterizedTest
    @CsvSource({"strength,startDate", "strength,endDate", "cardio,startDate", "cardio,endDate"})
    void shouldRequireBothDates(String kind, String missing) throws Exception {
        var request = get(BASE + "42/" + kind).with(authentication(auth()));
        request.param(missing.equals("startDate") ? "endDate" : "startDate", "2026-10-27");
        mvc.perform(request).andExpect(status().isBadRequest());
        verifyNoInteractions(useCase);
    }

    @ParameterizedTest
    @CsvSource({"strength,0", "strength,-1", "strength,abc", "cardio,0", "cardio,-1", "cardio,abc"})
    void shouldRejectInvalidExerciseIds(String kind, String id) throws Exception {
        mvc.perform(get(BASE + id + "/" + kind).param("startDate", "2026-10-01").param("endDate", "2026-10-27")
                .with(authentication(auth()))).andExpect(status().isBadRequest());
        verifyNoInteractions(useCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {"strength", "cardio"})
    void shouldRejectMalformedDatesAndPreserveBusinessErrorContract(String kind) throws Exception {
        mvc.perform(get(BASE + "42/" + kind).param("startDate", "invalid").param("endDate", "2026-10-27")
                .with(authentication(auth()))).andExpect(status().isBadRequest());
        if (kind.equals("strength")) when(useCase.strength(any())).thenThrow(new OperationNotAllowedException("Período inválido."));
        else when(useCase.cardio(any())).thenThrow(new OperationNotAllowedException("Período inválido."));
        mvc.perform(request(kind)).andExpect(status().isUnprocessableEntity());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request(String kind) {
        return get(BASE + "42/" + kind).param("startDate", "2026-10-01").param("endDate", "2026-10-27")
                .with(authentication(auth()));
    }
    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }
}
