package com.ironcore.interfaces.rest.workoutplanning.workoutactivity.mapper;

import com.ironcore.application.workoutplanning.workoutactivity.create.CreateWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.create.CreateWorkoutActivityResult;
import com.ironcore.application.workoutplanning.workoutactivity.delete.DeleteWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.reorder.ReorderWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.update.UpdateWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.update.UpdateWorkoutActivityResult;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.create.CreateWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.create.CreateWorkoutActivityResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.reorder.ReorderWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.update.UpdateWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.update.UpdateWorkoutActivityResponse;

public final class WorkoutActivityRestMapper {

    private WorkoutActivityRestMapper() {
    }

    public static CreateWorkoutActivityCommand toCreateCommand(
            AuthenticatedUser authenticatedUser,
            CreateWorkoutActivityRequest request
    ) {
        return new CreateWorkoutActivityCommand(
                authenticatedUser.userId(),
                new WorkoutDayId(request.workoutDayId()),
                new ExerciseId(request.exerciseId()),
                request.sets(),
                request.repRangeMin(),
                request.repRangeMax(),
                request.targetLoadKg(),
                request.targetLoadText(),
                request.durationMinutes(),
                request.distanceKm(),
                request.intensityText(),
                request.restSeconds(),
                request.notes()
        );
    }

    public static CreateWorkoutActivityResponse toResponse(CreateWorkoutActivityResult result) {
        return new CreateWorkoutActivityResponse(
                result.id().value(),
                result.workoutDayId().value(),
                result.exerciseId().value(),
                result.orderIndex(),
                result.sets(),
                result.repRangeMin(),
                result.repRangeMax(),
                result.targetLoadKg(),
                result.targetLoadText(),
                result.durationMinutes(),
                result.distanceKm(),
                result.intensityText(),
                result.restSeconds(),
                result.notes(),
                result.createdAt()
        );
    }

    public static UpdateWorkoutActivityCommand toUpdateCommand(
            AuthenticatedUser authenticatedUser,
            Long id,
            UpdateWorkoutActivityRequest request
    ) {
        return new UpdateWorkoutActivityCommand(
                authenticatedUser.userId(),
                new WorkoutActivityId(id),
                new ExerciseId(request.exerciseId()),
                request.sets(),
                request.repRangeMin(),
                request.repRangeMax(),
                request.targetLoadKg(),
                request.targetLoadText(),
                request.durationMinutes(),
                request.distanceKm(),
                request.intensityText(),
                request.restSeconds(),
                request.notes()
        );
    }

    public static UpdateWorkoutActivityResponse toResponse(UpdateWorkoutActivityResult result) {
        return  new UpdateWorkoutActivityResponse(
                result.id().value(),
                result.workoutDayId().value(),
                result.exerciseId().value(),
                result.orderIndex(),
                result.sets(),
                result.repRangeMin(),
                result.repRangeMax(),
                result.targetLoadKg(),
                result.targetLoadText(),
                result.durationMinutes(),
                result.distanceKm(),
                result.intensityText(),
                result.restSeconds(),
                result.notes(),
                result.updatedAt()
        );
    }

    public static DeleteWorkoutActivityCommand toDeleteCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new DeleteWorkoutActivityCommand(
                authenticatedUser.userId(),
                new WorkoutActivityId(id)
        );
    }

    public static ReorderWorkoutActivityCommand toReorderCommand(
            AuthenticatedUser authenticatedUser,
            Long id,
            ReorderWorkoutActivityRequest request
    ) {
        return new ReorderWorkoutActivityCommand(
                authenticatedUser.userId(),
                new WorkoutActivityId(id),
                request.orderIndex()
        );
    }
}
