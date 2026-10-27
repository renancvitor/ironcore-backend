package com.ironcore.interfaces.rest.workoutsession.workoutsession.mapper;

import com.ironcore.application.workoutsession.workoutsession.detail.GetWorkoutSessionDetailCommand;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionActivityDetailResult;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionDetailResult;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionSetDetailResult;
import com.ironcore.application.workoutsession.workoutsession.inprogress.GetInProgressWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionResult;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.detail.GetWorkoutSessionDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.detail.WorkoutSessionActivityDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.detail.WorkoutSessionSetDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.start.StartWorkoutSessionRequest;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.start.StartWorkoutSessionResponse;

public final class WorkoutSessionRestMapper {

    private WorkoutSessionRestMapper() {
    }

    public static StartWorkoutSessionCommand toStartCommand(
            AuthenticatedUser authenticatedUser,
            StartWorkoutSessionRequest request
    ) {
        return new StartWorkoutSessionCommand(
                authenticatedUser.userId(),
                new WorkoutCycleId(request.workoutCycleId()),
                new WorkoutDayId(request.workoutDayId())
        );
    }

    public static GetWorkoutSessionDetailCommand toGetDetailCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new GetWorkoutSessionDetailCommand(authenticatedUser.userId(), new WorkoutSessionId(id));
    }

    public static GetInProgressWorkoutSessionCommand toGetInProgressCommand(AuthenticatedUser authenticatedUser) {
        return new GetInProgressWorkoutSessionCommand(authenticatedUser.userId());
    }

    public static StartWorkoutSessionResponse toResponse(StartWorkoutSessionResult result) {
        return new StartWorkoutSessionResponse(
                result.id().value(),
                result.workoutCycleId().value(),
                result.workoutDayId().value(),
                result.performedOn(),
                result.startedAt(),
                result.status(),
                result.createdAt()
        );
    }

    public static GetWorkoutSessionDetailResponse toResponse(WorkoutSessionDetailResult result) {
        return new GetWorkoutSessionDetailResponse(
                result.id().value(),
                result.personId().value(),
                result.workoutCycleId() == null ? null : result.workoutCycleId().value(),
                result.workoutDayId() == null ? null : result.workoutDayId().value(),
                result.workoutCycleNameSnapshot(),
                result.workoutDayTitleSnapshot(),
                result.weekDaySnapshot(),
                result.performedOn(),
                result.startedAt(),
                result.finishedAt(),
                result.status(),
                result.closeReason(),
                result.notes(),
                result.createdAt(),
                result.updatedAt(),
                result.activities().stream().map(WorkoutSessionRestMapper::toActivityResponse).toList()
        );
    }

    private static WorkoutSessionActivityDetailResponse toActivityResponse(WorkoutSessionActivityDetailResult result) {
        return new WorkoutSessionActivityDetailResponse(
                result.id().value(),
                result.workoutActivityId() == null ? null : result.workoutActivityId().value(),
                result.exerciseId() == null ? null : result.exerciseId().value(),
                result.exerciseNameSnapshot(),
                result.activityTypeSnapshot(),
                result.orderIndexSnapshot(),
                result.plannedSetsSnapshot(),
                result.plannedRepRangeMinSnapshot(),
                result.plannedRepRangeMaxSnapshot(),
                result.plannedTargetLoadKgSnapshot(),
                result.plannedTargetLoadTextSnapshot(),
                result.plannedDurationMinutesSnapshot(),
                result.plannedDistanceKmSnapshot(),
                result.plannedIntensityTextSnapshot(),
                result.plannedRestSecondsSnapshot(),
                result.plannedNotesSnapshot(),
                result.actualDurationMinutes(),
                result.actualDistanceKm(),
                result.notes(),
                result.createdAt(),
                result.updatedAt(),
                result.sets().stream().map(WorkoutSessionRestMapper::toSetResponse).toList()
        );
    }

    private static WorkoutSessionSetDetailResponse toSetResponse(WorkoutSessionSetDetailResult result) {
        return new WorkoutSessionSetDetailResponse(
                result.id().value(),
                result.setNumber(),
                result.actualLoadKg(),
                result.actualLoadMode(),
                result.actualReps(),
                result.notes(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
