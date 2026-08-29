package com.ironcore.interfaces.rest.workoutplanning.workoutcycle;

import com.ironcore.application.workoutplanning.workoutcycle.cancel.CancelWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.cancel.CancelWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.cancel.CancelWorkoutCycleUseCase;
import com.ironcore.application.workoutplanning.workoutcycle.complete.CompleteWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.complete.CompleteWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.complete.CompleteWorkoutCycleUseCase;
import com.ironcore.application.workoutplanning.workoutcycle.create.CreateWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.create.CreateWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.create.CreateWorkoutCycleUseCase;
import com.ironcore.application.workoutplanning.workoutcycle.delete.DeleteWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.delete.DeleteWorkoutCycleUseCase;
import com.ironcore.application.workoutplanning.workoutcycle.detail.GetWorkoutCycleDetailCommand;
import com.ironcore.application.workoutplanning.workoutcycle.detail.GetWorkoutCycleDetailUseCase;
import com.ironcore.application.workoutplanning.workoutcycle.detail.WorkoutCycleDetailResult;
import com.ironcore.application.workoutplanning.workoutcycle.list.ListWorkoutCyclesCommand;
import com.ironcore.application.workoutplanning.workoutcycle.list.ListWorkoutCyclesResult;
import com.ironcore.application.workoutplanning.workoutcycle.list.ListWorkoutCyclesUseCase;
import com.ironcore.application.workoutplanning.workoutcycle.start.StartWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.start.StartWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.start.StartWorkoutCycleUseCase;
import com.ironcore.application.workoutplanning.workoutcycle.update.UpdateWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.update.UpdateWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.update.UpdateWorkoutCycleUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.cancel.CancelWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.complete.CompleteWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create.CreateWorkoutCycleRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create.CreateWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail.GetWorkoutCycleDetailResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.list.ListWorkoutCyclesResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.start.StartWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.update.UpdateWorkoutCycleRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.update.UpdateWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.mapper.WorkoutCycleRestMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/users/me/workout-cycles")
public class WorkoutCycleController {

    private final CreateWorkoutCycleUseCase createWorkoutCycleUseCase;
    private final UpdateWorkoutCycleUseCase updateWorkoutCycleUseCase;
    private final DeleteWorkoutCycleUseCase deleteWorkoutCycleUseCase;
    private final StartWorkoutCycleUseCase startWorkoutCycleUseCase;
    private final CompleteWorkoutCycleUseCase completeWorkoutCycleUseCase;
    private final CancelWorkoutCycleUseCase cancelWorkoutCycleUseCase;
    private final GetWorkoutCycleDetailUseCase getWorkoutCycleDetailUseCase;
    private final ListWorkoutCyclesUseCase listWorkoutCyclesUseCase;

    @PostMapping
    public ResponseEntity<CreateWorkoutCycleResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateWorkoutCycleRequest request
    ) {
        CreateWorkoutCycleCommand command = WorkoutCycleRestMapper.toCreateCommand(authenticatedUser, request);
        CreateWorkoutCycleResult result = createWorkoutCycleUseCase.execute(command);
        CreateWorkoutCycleResponse response = WorkoutCycleRestMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateWorkoutCycleResponse> update(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkoutCycleRequest request
    ) {
        UpdateWorkoutCycleCommand command = WorkoutCycleRestMapper.toUpdateCommand(authenticatedUser, id, request);
        UpdateWorkoutCycleResult result = updateWorkoutCycleUseCase.execute(command);
        UpdateWorkoutCycleResponse response = WorkoutCycleRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        DeleteWorkoutCycleCommand command = WorkoutCycleRestMapper.toDeleteCommand(authenticatedUser, id);
        deleteWorkoutCycleUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<StartWorkoutCycleResponse> start(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        StartWorkoutCycleCommand command =  WorkoutCycleRestMapper.toStartCommand(authenticatedUser, id);
        StartWorkoutCycleResult result = startWorkoutCycleUseCase.execute(command);
        StartWorkoutCycleResponse response = WorkoutCycleRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<CompleteWorkoutCycleResponse> complete(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        CompleteWorkoutCycleCommand command =  WorkoutCycleRestMapper.toCompleteCommand(authenticatedUser, id);
        CompleteWorkoutCycleResult result = completeWorkoutCycleUseCase.execute(command);
        CompleteWorkoutCycleResponse response = WorkoutCycleRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<CancelWorkoutCycleResponse> cancel(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        CancelWorkoutCycleCommand command =  WorkoutCycleRestMapper.toCancelCommand(authenticatedUser, id);
        CancelWorkoutCycleResult result = cancelWorkoutCycleUseCase.execute(command);
        CancelWorkoutCycleResponse response = WorkoutCycleRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetWorkoutCycleDetailResponse> getDetail(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        GetWorkoutCycleDetailCommand command = WorkoutCycleRestMapper.toGetDetailCommand(authenticatedUser, id);
        WorkoutCycleDetailResult result = getWorkoutCycleDetailUseCase.execute(command);
        GetWorkoutCycleDetailResponse response = WorkoutCycleRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ListWorkoutCyclesResponse> list(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false) WorkoutStatus workoutStatus,
            @RequestParam(required = false) Long trainingGoalId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String name,
            @RequestParam(name = "page", defaultValue = "0")
            @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20")
            @Min(1) @Max(100) int size
    ) {
        ListWorkoutCyclesCommand command = WorkoutCycleRestMapper.toListCommand(
                authenticatedUser,
                workoutStatus,
                trainingGoalId,
                startDate,
                endDate,
                name,
                page,
                size
        );
        ListWorkoutCyclesResult result = listWorkoutCyclesUseCase.execute(command);
        ListWorkoutCyclesResponse response = WorkoutCycleRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
