package com.ironcore.interfaces.rest.workoutplanning.workoutcycle.mapper;

import com.ironcore.application.workoutplanning.workoutcycle.cancel.CancelWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.cancel.CancelWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.complete.CompleteWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.complete.CompleteWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.create.CreateWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.create.CreateWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.delete.DeleteWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.detail.ExerciseDetailResult;
import com.ironcore.application.workoutplanning.workoutcycle.detail.GetWorkoutCycleDetailCommand;
import com.ironcore.application.workoutplanning.workoutcycle.detail.MuscleGroupDetailResult;
import com.ironcore.application.workoutplanning.workoutcycle.detail.TrainingGoalDetailResult;
import com.ironcore.application.workoutplanning.workoutcycle.detail.WorkoutActivityDetailResult;
import com.ironcore.application.workoutplanning.workoutcycle.detail.WorkoutDayDetailResult;
import com.ironcore.application.workoutplanning.workoutcycle.detail.WorkoutCycleDetailResult;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.workoutplanning.workoutcycle.list.ListWorkoutCyclesCommand;
import com.ironcore.application.workoutplanning.workoutcycle.list.ListWorkoutCyclesItemResult;
import com.ironcore.application.workoutplanning.workoutcycle.list.ListWorkoutCyclesResult;
import com.ironcore.application.workoutplanning.workoutcycle.list.TrainingGoalItemResult;
import com.ironcore.application.workoutplanning.workoutcycle.start.StartWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.start.StartWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.update.UpdateWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.update.UpdateWorkoutCycleResult;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.cancel.CancelWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.complete.CompleteWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create.CreateWorkoutCycleRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create.CreateWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail.ExerciseDetailResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail.GetWorkoutCycleDetailResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail.MuscleGroupDetailResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail.TrainingGoalDetailResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail.WorkoutActivityDetailResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.detail.WorkoutDayDetailResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.list.ListWorkoutCyclesItemResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.list.ListWorkoutCyclesResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.list.TrainingGoalItemResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.start.StartWorkoutCycleResponse;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.update.UpdateWorkoutCycleRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.update.UpdateWorkoutCycleResponse;

import java.time.LocalDate;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;

public final class WorkoutCycleRestMapper {

    private WorkoutCycleRestMapper() {
    }

    public static CreateWorkoutCycleCommand toCreateCommand(
            AuthenticatedUser authenticatedUser,
            CreateWorkoutCycleRequest request
    ) {
        return new CreateWorkoutCycleCommand(
                authenticatedUser.userId(),
                request.name(),
                new TrainingGoalId(request.trainingGoalId()),
                request.desiredDurationMonths(),
                request.notes()
        );
    }

    public static CreateWorkoutCycleResponse toResponse(CreateWorkoutCycleResult result) {
        return new CreateWorkoutCycleResponse(
                result.id().value(),
                result.personId().value(),
                result.name(),
                result.trainingGoalId().value(),
                result.desiredDurationMonths(),
                result.workoutStatus(),
                result.workoutOrigin(),
                result.notes(),
                result.createdAt()
        );
    }

    public static UpdateWorkoutCycleCommand toUpdateCommand(
            AuthenticatedUser authenticatedUser,
            Long id,
            UpdateWorkoutCycleRequest request
    ) {
        return new UpdateWorkoutCycleCommand(
                authenticatedUser.userId(),
                new WorkoutCycleId(id),
                request.name(),
                new TrainingGoalId(request.trainingGoalId()),
                request.desiredDurationMonths(),
                request.notes()
        );
    }

    public static UpdateWorkoutCycleResponse toResponse(UpdateWorkoutCycleResult result) {
        return new UpdateWorkoutCycleResponse(
                result.id().value(),
                result.name(),
                result.trainingGoalId().value(),
                result.startDate(),
                result.workoutStatus(),
                result.workoutOrigin(),
                result.desiredDurationMonths(),
                result.notes(),
                result.updatedAt()
        );
    }

    public static DeleteWorkoutCycleCommand toDeleteCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new DeleteWorkoutCycleCommand(
                authenticatedUser.userId(),
                new WorkoutCycleId(id)
        );
    }

    public static StartWorkoutCycleCommand toStartCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new StartWorkoutCycleCommand(
                authenticatedUser.userId(),
                new WorkoutCycleId(id)
        );
    }

    public static StartWorkoutCycleResponse toResponse(StartWorkoutCycleResult result) {
        return new StartWorkoutCycleResponse(
                result.id().value(),
                result.trainingGoalId().value(),
                result.startDate(),
                result.workoutStatus()
        );
    }

    public static CompleteWorkoutCycleCommand toCompleteCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new CompleteWorkoutCycleCommand(
                authenticatedUser.userId(),
                new WorkoutCycleId(id)
        );
    }

    public static CompleteWorkoutCycleResponse toResponse(CompleteWorkoutCycleResult result) {
        return new CompleteWorkoutCycleResponse(
                result.id().value(),
                result.trainingGoalId().value(),
                result.startDate(),
                result.endDate(),
                result.workoutStatus()
        );
    }

    public static CancelWorkoutCycleCommand toCancelCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new CancelWorkoutCycleCommand(
                authenticatedUser.userId(),
                new WorkoutCycleId(id)
        );
    }

    public static CancelWorkoutCycleResponse toResponse(CancelWorkoutCycleResult result) {
        return new CancelWorkoutCycleResponse(
                result.id().value(),
                result.trainingGoalId().value(),
                result.workoutStatus()
        );
    }

    public static GetWorkoutCycleDetailCommand toGetDetailCommand(
            AuthenticatedUser authenticatedUser,
            Long id
    ) {
        return new GetWorkoutCycleDetailCommand(
                authenticatedUser.userId(),
                new WorkoutCycleId(id)
        );
    }

    public static GetWorkoutCycleDetailResponse toResponse(WorkoutCycleDetailResult result) {
        return new GetWorkoutCycleDetailResponse(
                result.id().value(),
                result.name(),
                result.workoutStatus(),
                toTrainingGoalDetailResponse(result.trainingGoal()),
                result.startDate(),
                result.endDate(),
                result.desiredDurationMonths(),
                result.notes(),
                result.days().stream()
                        .map(WorkoutCycleRestMapper::toWorkoutDayDetailResponse)
                        .toList()
        );
    }

    private static TrainingGoalDetailResponse toTrainingGoalDetailResponse(TrainingGoalDetailResult result) {
        return new TrainingGoalDetailResponse(
                result.id().value(),
                result.name()
        );
    }

    private static WorkoutDayDetailResponse toWorkoutDayDetailResponse(WorkoutDayDetailResult result) {
        return new WorkoutDayDetailResponse(
                result.id().value(),
                result.weekDay(),
                result.title(),
                result.sortOrder(),
                result.activities().stream()
                        .map(WorkoutCycleRestMapper::toWorkoutActivityDetailResponse)
                        .toList()
        );
    }

    private static WorkoutActivityDetailResponse toWorkoutActivityDetailResponse(WorkoutActivityDetailResult result) {
        return new WorkoutActivityDetailResponse(
                result.id().value(),
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
                toExerciseDetailResponse(result.exercise())
        );
    }

    private static ExerciseDetailResponse toExerciseDetailResponse(ExerciseDetailResult result) {
        return new ExerciseDetailResponse(
                result.id().value(),
                result.name(),
                result.muscleGroups().stream()
                        .map(WorkoutCycleRestMapper::toMuscleGroupDetailResponse)
                        .toList()
        );
    }

    private static MuscleGroupDetailResponse toMuscleGroupDetailResponse(MuscleGroupDetailResult result) {
        return new MuscleGroupDetailResponse(
                result.id().value(),
                result.name()
        );
    }

    public static ListWorkoutCyclesCommand toListCommand(
            AuthenticatedUser authenticatedUser,
            WorkoutStatus workoutStatus,
            Long trainingGoalId,
            LocalDate startDate,
            LocalDate endDate,
            String name,
            int page,
            int size
    ) {
        return new ListWorkoutCyclesCommand(
                authenticatedUser.userId(),
                workoutStatus,
                trainingGoalId == null ? null : new TrainingGoalId(trainingGoalId),
                startDate,
                endDate,
                name,
                page,
                size
        );
    }

    public static ListWorkoutCyclesResponse toResponse(ListWorkoutCyclesResult result) {
        PageResult<ListWorkoutCyclesItemResult> cycles = result.cycles();

        PageResult<ListWorkoutCyclesItemResponse> responsePage = new PageResult<>(
                cycles.content().stream()
                        .map(WorkoutCycleRestMapper::toListItemResponse)
                        .toList(),
                cycles.page(),
                cycles.size(),
                cycles.totalElements(),
                cycles.totalPages(),
                cycles.last()
        );

        return new ListWorkoutCyclesResponse(responsePage);
    }

    private static ListWorkoutCyclesItemResponse toListItemResponse(ListWorkoutCyclesItemResult result) {
        return new ListWorkoutCyclesItemResponse(
                result.id().value(),
                result.name(),
                result.workoutStatus(),
                toTrainingGoalItemResponse(result.trainingGoal()),
                result.startDate(),
                result.endDate(),
                result.desiredDurationMonths()
        );
    }

    private static TrainingGoalItemResponse toTrainingGoalItemResponse(TrainingGoalItemResult result) {
        return new TrainingGoalItemResponse(
                result.id().value(),
                result.name()
        );
    }
}
