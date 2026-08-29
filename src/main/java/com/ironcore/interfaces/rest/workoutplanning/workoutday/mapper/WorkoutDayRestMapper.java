package com.ironcore.interfaces.rest.workoutplanning.workoutday.mapper;

import com.ironcore.application.workoutplanning.workoutday.create.CreateWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.create.CreateWorkoutDayResult;
import com.ironcore.application.workoutplanning.workoutday.delete.DeleteWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.reorder.ReorderWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.update.UpdateWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.update.UpdateWorkoutDayResult;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.create.CreateWorkoutDayRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.create.CreateWorkoutDayResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.reorder.ReorderWorkoutDayRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.update.UpdateWorkoutDayRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.update.UpdateWorkoutDayResponse;

public final class WorkoutDayRestMapper {

    private WorkoutDayRestMapper() {
    }

    public static CreateWorkoutDayCommand toCreateCommand(
            AuthenticatedUser authenticatedUser,
            CreateWorkoutDayRequest request
    ) {
        return new CreateWorkoutDayCommand(
                authenticatedUser.userId(),
                new WorkoutCycleId(request.workoutCycleId()),
                request.weekDay(),
                request.title()
        );
    }

    public static CreateWorkoutDayResponse toResponse(CreateWorkoutDayResult result) {
        return new CreateWorkoutDayResponse(
                result.id().value(),
                result.workoutCycleId().value(),
                result.weekDay(),
                result.title(),
                result.sortOrder(),
                result.createdAt()
        );
    }

    public static UpdateWorkoutDayCommand toUpdateCommand(
            AuthenticatedUser authenticatedUser,
            Long id,
            UpdateWorkoutDayRequest request
    ) {
        return new UpdateWorkoutDayCommand(
                authenticatedUser.userId(),
                new WorkoutDayId(id),
                request.title()
        );
    }

    public static UpdateWorkoutDayResponse toResponse(UpdateWorkoutDayResult result) {
        return new UpdateWorkoutDayResponse(
                result.id().value(),
                result.workoutCycleId().value(),
                result.weekDay(),
                result.title(),
                result.sortOrder(),
                result.updatedAt()
        );
    }

    public static DeleteWorkoutDayCommand toDeleteCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new DeleteWorkoutDayCommand(
                authenticatedUser.userId(),
                new WorkoutDayId(id)
        );
    }

    public static ReorderWorkoutDayCommand toReorderCommand(
            AuthenticatedUser authenticatedUser,
            Long id,
            ReorderWorkoutDayRequest request
    ) {
        return new ReorderWorkoutDayCommand(
                authenticatedUser.userId(),
                new WorkoutDayId(id),
                request.weekDay(),
                request.sortOrder()
        );
    }
}
