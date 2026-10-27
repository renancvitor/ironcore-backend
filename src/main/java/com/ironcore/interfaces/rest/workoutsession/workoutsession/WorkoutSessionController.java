package com.ironcore.interfaces.rest.workoutsession.workoutsession;

import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionResult;
import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionUseCase;
import com.ironcore.application.workoutsession.workoutsession.detail.GetWorkoutSessionDetailCommand;
import com.ironcore.application.workoutsession.workoutsession.detail.GetWorkoutSessionDetailUseCase;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionDetailResult;
import com.ironcore.application.workoutsession.workoutsession.inprogress.GetInProgressWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.inprogress.GetInProgressWorkoutSessionUseCase;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionResult;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.api.WorkoutSessionApi;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.close.CloseWorkoutSessionResponse;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.detail.GetWorkoutSessionDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.start.StartWorkoutSessionRequest;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.start.StartWorkoutSessionResponse;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.mapper.WorkoutSessionRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/users/me/workout-sessions")
public class WorkoutSessionController implements WorkoutSessionApi {

    private final StartWorkoutSessionUseCase startWorkoutSessionUseCase;
    private final GetWorkoutSessionDetailUseCase getWorkoutSessionDetailUseCase;
    private final GetInProgressWorkoutSessionUseCase getInProgressWorkoutSessionUseCase;
    private final CloseWorkoutSessionUseCase closeWorkoutSessionUseCase;

    @PatchMapping("/{id}/close")
    public ResponseEntity<CloseWorkoutSessionResponse> close(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        CloseWorkoutSessionCommand command = WorkoutSessionRestMapper.toCloseCommand(authenticatedUser, id);
        CloseWorkoutSessionResult result = closeWorkoutSessionUseCase.execute(command);
        CloseWorkoutSessionResponse response = WorkoutSessionRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<StartWorkoutSessionResponse> start(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody StartWorkoutSessionRequest request
    ) {
        StartWorkoutSessionCommand command = WorkoutSessionRestMapper.toStartCommand(authenticatedUser, request);
        StartWorkoutSessionResult result = startWorkoutSessionUseCase.execute(command);
        StartWorkoutSessionResponse response = WorkoutSessionRestMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetWorkoutSessionDetailResponse> getDetail(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        GetWorkoutSessionDetailCommand command = WorkoutSessionRestMapper.toGetDetailCommand(authenticatedUser, id);
        WorkoutSessionDetailResult result = getWorkoutSessionDetailUseCase.execute(command);
        GetWorkoutSessionDetailResponse response = WorkoutSessionRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/in-progress")
    public ResponseEntity<GetWorkoutSessionDetailResponse> getInProgress(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        GetInProgressWorkoutSessionCommand command = WorkoutSessionRestMapper.toGetInProgressCommand(authenticatedUser);
        WorkoutSessionDetailResult result = getInProgressWorkoutSessionUseCase.execute(command);
        GetWorkoutSessionDetailResponse response = WorkoutSessionRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
