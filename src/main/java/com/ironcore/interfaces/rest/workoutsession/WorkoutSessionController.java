package com.ironcore.interfaces.rest.workoutsession;

import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionResult;
import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionUseCase;
import com.ironcore.application.workoutsession.workoutsession.detail.GetWorkoutSessionDetailCommand;
import com.ironcore.application.workoutsession.workoutsession.detail.GetWorkoutSessionDetailUseCase;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionActivityDetailResult;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionDetailResult;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionSetDetailResult;
import com.ironcore.application.workoutsession.workoutsession.inprogress.GetInProgressWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.inprogress.GetInProgressWorkoutSessionUseCase;
import com.ironcore.application.workoutsession.workoutsession.list.ListWorkoutSessionsUseCase;
import com.ironcore.application.workoutsession.workoutsession.notes.UpdateWorkoutSessionNotesCommand;
import com.ironcore.application.workoutsession.workoutsession.notes.UpdateWorkoutSessionNotesUseCase;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionResult;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionUseCase;
import com.ironcore.application.workoutsession.workoutsessionactivity.execution.UpdateWorkoutSessionActivityExecutionCommand;
import com.ironcore.application.workoutsession.workoutsessionactivity.execution.UpdateWorkoutSessionActivityExecutionUseCase;
import com.ironcore.application.workoutsession.workoutsessionactivity.loadmode.ChangeWorkoutSessionActivityLoadModeCommand;
import com.ironcore.application.workoutsession.workoutsessionactivity.loadmode.ChangeWorkoutSessionActivityLoadModeUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.create.CreateWorkoutSessionSetCommand;
import com.ironcore.application.workoutsession.workoutsessionset.create.CreateWorkoutSessionSetUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.delete.DeleteWorkoutSessionSetCommand;
import com.ironcore.application.workoutsession.workoutsessionset.delete.DeleteWorkoutSessionSetUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.update.UpdateWorkoutSessionSetCommand;
import com.ironcore.application.workoutsession.workoutsessionset.update.UpdateWorkoutSessionSetUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutsession.api.WorkoutSessionApi;
import com.ironcore.interfaces.rest.workoutsession.dto.close.CloseWorkoutSessionResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.detail.GetWorkoutSessionDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.detail.WorkoutSessionActivityDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.detail.WorkoutSessionSetDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.execution.UpdateWorkoutSessionActivityExecutionRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.loadmode.ChangeWorkoutSessionActivityLoadModeRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.list.ListWorkoutSessionsResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.notes.UpdateWorkoutSessionNotesRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.set.CreateWorkoutSessionSetRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.set.UpdateWorkoutSessionSetRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.start.StartWorkoutSessionRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.start.StartWorkoutSessionResponse;
import com.ironcore.interfaces.rest.workoutsession.mapper.WorkoutSessionRestMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/users/me/workout-sessions")
public class WorkoutSessionController implements WorkoutSessionApi {

    private final ListWorkoutSessionsUseCase listWorkoutSessionsUseCase;

    private final UpdateWorkoutSessionNotesUseCase updateWorkoutSessionNotesUseCase;
    private final UpdateWorkoutSessionActivityExecutionUseCase updateWorkoutSessionActivityExecutionUseCase;
    private final CreateWorkoutSessionSetUseCase createWorkoutSessionSetUseCase;
    private final UpdateWorkoutSessionSetUseCase updateWorkoutSessionSetUseCase;
    private final DeleteWorkoutSessionSetUseCase deleteWorkoutSessionSetUseCase;
    private final ChangeWorkoutSessionActivityLoadModeUseCase changeWorkoutSessionActivityLoadModeUseCase;

    private final StartWorkoutSessionUseCase startWorkoutSessionUseCase;
    private final GetWorkoutSessionDetailUseCase getWorkoutSessionDetailUseCase;
    private final GetInProgressWorkoutSessionUseCase getInProgressWorkoutSessionUseCase;
    private final CloseWorkoutSessionUseCase closeWorkoutSessionUseCase;

    @GetMapping
    public ResponseEntity<ListWorkoutSessionsResponse> list(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false) Long workoutCycleId,
            @RequestParam(required = false) Long workoutDayId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String name
    ) {
        var command = WorkoutSessionRestMapper.toListCommand(
                authenticatedUser, workoutCycleId, workoutDayId, startDate, endDate, page, size, name);
        return ResponseEntity.ok(WorkoutSessionRestMapper.toResponse(listWorkoutSessionsUseCase.execute(command)));
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<GetWorkoutSessionDetailResponse> updateNotes(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkoutSessionNotesRequest request
    ) {
        UpdateWorkoutSessionNotesCommand command = WorkoutSessionRestMapper.toUpdateWorkoutSessionNotesCommand(
                authenticatedUser, id, request);
        WorkoutSessionDetailResult result = updateWorkoutSessionNotesUseCase.execute(command);
        return ResponseEntity.ok()
                .body(WorkoutSessionRestMapper.toResponse(result));
    }

    @PatchMapping("/{id}/activities/{activityId}/execution")
    public ResponseEntity<WorkoutSessionActivityDetailResponse> updateActivityExecution(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @PathVariable Long activityId,
            @Valid @RequestBody UpdateWorkoutSessionActivityExecutionRequest request
    ) {
        UpdateWorkoutSessionActivityExecutionCommand command = WorkoutSessionRestMapper.toUpdateWorkoutSessionActivityExecutionCommand(
                authenticatedUser, id, activityId, request);
        WorkoutSessionActivityDetailResult result = updateWorkoutSessionActivityExecutionUseCase.execute(command);
        return ResponseEntity.ok()
                .body(WorkoutSessionRestMapper.toActivityResponse(result));
    }

    @PostMapping("/{id}/activities/{activityId}/sets")
    public ResponseEntity<WorkoutSessionSetDetailResponse> createSet(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @PathVariable Long activityId,
            @Valid @RequestBody CreateWorkoutSessionSetRequest request
    ) {
        CreateWorkoutSessionSetCommand command = WorkoutSessionRestMapper.toCreateWorkoutSessionSetCommand(
                authenticatedUser, id, activityId, request);
        WorkoutSessionSetDetailResult result = createWorkoutSessionSetUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WorkoutSessionRestMapper.toSetResponse(result));
    }

    @PatchMapping("/{id}/activities/{activityId}/sets/{setId}")
    public ResponseEntity<WorkoutSessionSetDetailResponse> updateSet(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @PathVariable Long activityId,
            @PathVariable Long setId,
            @Valid @RequestBody UpdateWorkoutSessionSetRequest request
    ) {
        UpdateWorkoutSessionSetCommand command = WorkoutSessionRestMapper.toUpdateWorkoutSessionSetCommand(
                authenticatedUser, id, activityId, setId, request);
        WorkoutSessionSetDetailResult result = updateWorkoutSessionSetUseCase.execute(command);
        return ResponseEntity.ok()
                .body(WorkoutSessionRestMapper.toSetResponse(result));
    }

    @DeleteMapping("/{id}/activities/{activityId}/sets/{setId}")
    public ResponseEntity<Void> deleteSet(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @PathVariable Long activityId,
            @PathVariable Long setId
    ) {
        DeleteWorkoutSessionSetCommand command = WorkoutSessionRestMapper.toDeleteWorkoutSessionSetCommand(
                authenticatedUser, id, activityId, setId);
        deleteWorkoutSessionSetUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activities/{activityId}/load-mode")
    public ResponseEntity<WorkoutSessionActivityDetailResponse> changeActivityLoadMode(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @PathVariable Long activityId,
            @Valid @RequestBody ChangeWorkoutSessionActivityLoadModeRequest request
    ) {
        ChangeWorkoutSessionActivityLoadModeCommand command = WorkoutSessionRestMapper.toChangeWorkoutSessionActivityLoadModeCommand(
                authenticatedUser, id, activityId, request);
        WorkoutSessionActivityDetailResult result = changeWorkoutSessionActivityLoadModeUseCase.execute(command);
        return ResponseEntity.ok()
                .body(WorkoutSessionRestMapper.toActivityResponse(result));
    }

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
