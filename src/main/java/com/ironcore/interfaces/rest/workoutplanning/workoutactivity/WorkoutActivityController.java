package com.ironcore.interfaces.rest.workoutplanning.workoutactivity;

import com.ironcore.application.workoutplanning.workoutactivity.create.CreateWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.create.CreateWorkoutActivityResult;
import com.ironcore.application.workoutplanning.workoutactivity.create.CreateWorkoutActivityUseCase;
import com.ironcore.application.workoutplanning.workoutactivity.delete.DeleteWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.delete.DeleteWorkoutActivityUseCase;
import com.ironcore.application.workoutplanning.workoutactivity.reorder.ReorderWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.reorder.ReorderWorkoutActivityUseCase;
import com.ironcore.application.workoutplanning.workoutactivity.update.UpdateWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.update.UpdateWorkoutActivityResult;
import com.ironcore.application.workoutplanning.workoutactivity.update.UpdateWorkoutActivityUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.create.CreateWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.create.CreateWorkoutActivityResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.reorder.ReorderWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.update.UpdateWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.update.UpdateWorkoutActivityResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.mapper.WorkoutActivityRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/workout-activities")
public class WorkoutActivityController {

    private final CreateWorkoutActivityUseCase createWorkoutActivityUseCase;
    private final UpdateWorkoutActivityUseCase updateWorkoutActivityUseCase;
    private final DeleteWorkoutActivityUseCase deleteWorkoutActivityUseCase;
    private final ReorderWorkoutActivityUseCase reorderWorkoutActivityUseCase;

    @PostMapping
    public ResponseEntity<CreateWorkoutActivityResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateWorkoutActivityRequest request
    ) {
        CreateWorkoutActivityCommand command = WorkoutActivityRestMapper.toCreateCommand(authenticatedUser, request);
        CreateWorkoutActivityResult result = createWorkoutActivityUseCase.execute(command);
        CreateWorkoutActivityResponse response = WorkoutActivityRestMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateWorkoutActivityResponse> update(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkoutActivityRequest request
    ) {
        UpdateWorkoutActivityCommand command = WorkoutActivityRestMapper.toUpdateCommand(authenticatedUser, id, request);
        UpdateWorkoutActivityResult result = updateWorkoutActivityUseCase.execute(command);
        UpdateWorkoutActivityResponse response = WorkoutActivityRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id
    ) {
        DeleteWorkoutActivityCommand command = WorkoutActivityRestMapper.toDeleteCommand(authenticatedUser, id);
        deleteWorkoutActivityUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reorder")
    public ResponseEntity<Void>  reorder(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @Valid @RequestBody ReorderWorkoutActivityRequest request
    ) {
        ReorderWorkoutActivityCommand command =  WorkoutActivityRestMapper.toReorderCommand(
                authenticatedUser,
                id,
                request
        );
        reorderWorkoutActivityUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }
}
