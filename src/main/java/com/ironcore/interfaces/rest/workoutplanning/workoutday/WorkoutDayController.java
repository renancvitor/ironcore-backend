package com.ironcore.interfaces.rest.workoutplanning.workoutday;

import com.ironcore.application.workoutplanning.workoutday.create.CreateWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.create.CreateWorkoutDayResult;
import com.ironcore.application.workoutplanning.workoutday.create.CreateWorkoutDayUseCase;
import com.ironcore.application.workoutplanning.workoutday.delete.DeleteWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.delete.DeleteWorkoutDayUseCase;
import com.ironcore.application.workoutplanning.workoutday.reorder.ReorderWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.reorder.ReorderWorkoutDayUseCase;
import com.ironcore.application.workoutplanning.workoutday.update.UpdateWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.update.UpdateWorkoutDayResult;
import com.ironcore.application.workoutplanning.workoutday.update.UpdateWorkoutDayUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.api.WorkoutDayApi;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.create.CreateWorkoutDayRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.create.CreateWorkoutDayResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.reorder.ReorderWorkoutDayRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.update.UpdateWorkoutDayRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.update.UpdateWorkoutDayResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.mapper.WorkoutDayRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/workout-days")
public class WorkoutDayController implements WorkoutDayApi {

    private final CreateWorkoutDayUseCase createWorkoutDayUseCase;
    private final UpdateWorkoutDayUseCase updateWorkoutDayUseCase;
    private final DeleteWorkoutDayUseCase deleteWorkoutDayUseCase;
    private final ReorderWorkoutDayUseCase reorderWorkoutDayUseCase;

    @PostMapping
    public ResponseEntity<CreateWorkoutDayResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateWorkoutDayRequest request
    ) {
        CreateWorkoutDayCommand command = WorkoutDayRestMapper.toCreateCommand(authenticatedUser, request);
        CreateWorkoutDayResult result = createWorkoutDayUseCase.execute(command);
        CreateWorkoutDayResponse response = WorkoutDayRestMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateWorkoutDayResponse> update(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkoutDayRequest request
    ) {
        UpdateWorkoutDayCommand command = WorkoutDayRestMapper.toUpdateCommand(authenticatedUser, id, request);
        UpdateWorkoutDayResult result = updateWorkoutDayUseCase.execute(command);
        UpdateWorkoutDayResponse response = WorkoutDayRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        DeleteWorkoutDayCommand command = WorkoutDayRestMapper.toDeleteCommand(authenticatedUser, id);
        deleteWorkoutDayUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reorder")
    public ResponseEntity<Void> reorder(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @Valid @RequestBody ReorderWorkoutDayRequest request
    ) {
        ReorderWorkoutDayCommand command = WorkoutDayRestMapper.toReorderCommand(authenticatedUser, id, request);
        reorderWorkoutDayUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }
}
