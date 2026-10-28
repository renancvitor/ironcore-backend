package com.ironcore.interfaces.rest.workoutsession;

import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseCardioEvolutionCommand;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseCardioEvolutionResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseCardioEvolutionUseCase;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseStrengthEvolutionCommand;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseStrengthEvolutionResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.GetExerciseStrengthEvolutionUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutsession.api.ExerciseAnalyticsApi;
import com.ironcore.interfaces.rest.workoutsession.dto.analytics.ExerciseCardioEvolutionResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.analytics.ExerciseStrengthEvolutionResponse;
import com.ironcore.interfaces.rest.workoutsession.mapper.ExerciseAnalyticsRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/workout-sessions/analytics/exercises/{exerciseId}")
public class ExerciseAnalyticsController implements ExerciseAnalyticsApi {

    private final GetExerciseStrengthEvolutionUseCase getExerciseStrengthEvolutionUseCase;
    private final GetExerciseCardioEvolutionUseCase getExerciseCardioEvolutionUseCase;

    @GetMapping("/strength")
    public ResponseEntity<ExerciseStrengthEvolutionResponse> getStrengthEvolution(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long exerciseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        GetExerciseStrengthEvolutionCommand command = ExerciseAnalyticsRestMapper.toStrengthEvolutionCommand(
                authenticatedUser, exerciseId, startDate, endDate
        );
        GetExerciseStrengthEvolutionResult result = getExerciseStrengthEvolutionUseCase.execute(command);

        return ResponseEntity.ok(ExerciseAnalyticsRestMapper.toResponse(result));
    }

    @GetMapping("/cardio")
    public ResponseEntity<ExerciseCardioEvolutionResponse> getCardioEvolution(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long exerciseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        GetExerciseCardioEvolutionCommand command = ExerciseAnalyticsRestMapper.toCardioEvolutionCommand(
                authenticatedUser, exerciseId, startDate, endDate
        );
        GetExerciseCardioEvolutionResult result = getExerciseCardioEvolutionUseCase.execute(command);

        return ResponseEntity.ok(ExerciseAnalyticsRestMapper.toResponse(result));
    }
}
