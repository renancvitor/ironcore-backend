package com.ironcore.interfaces.rest.workoutsession;

import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseAnalyticsUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutsession.api.ExerciseAnalyticsApi;
import com.ironcore.interfaces.rest.workoutsession.dto.analytics.*;
import com.ironcore.interfaces.rest.workoutsession.mapper.ExerciseAnalyticsRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/workout-sessions/analytics/exercises/{exerciseId}")
public class ExerciseAnalyticsController implements ExerciseAnalyticsApi {
    private final GetExerciseAnalyticsUseCase useCase;

    @GetMapping("/strength")
    public ResponseEntity<ExerciseStrengthEvolutionResponse> strength(
            @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long exerciseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ExerciseAnalyticsRestMapper.response(useCase.strength(
                ExerciseAnalyticsRestMapper.command(user, exerciseId, startDate, endDate))));
    }

    @GetMapping("/cardio")
    public ResponseEntity<ExerciseCardioEvolutionResponse> cardio(
            @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long exerciseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ExerciseAnalyticsRestMapper.response(useCase.cardio(
                ExerciseAnalyticsRestMapper.command(user, exerciseId, startDate, endDate))));
    }
}
