package com.ironcore.interfaces.rest.workoutsession.mapper;

import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionResult;
import com.ironcore.application.workoutsession.workoutsession.detail.GetWorkoutSessionDetailCommand;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionActivityDetailResult;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionDetailResult;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionSetDetailResult;
import com.ironcore.application.workoutsession.workoutsession.inprogress.GetInProgressWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.notes.UpdateWorkoutSessionNotesCommand;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionResult;
import com.ironcore.application.workoutsession.workoutsessionactivity.execution.UpdateWorkoutSessionActivityExecutionCommand;
import com.ironcore.application.workoutsession.workoutsessionactivity.loadmode.ChangeWorkoutSessionActivityLoadModeCommand;
import com.ironcore.application.workoutsession.workoutsessionset.create.CreateWorkoutSessionSetCommand;
import com.ironcore.application.workoutsession.workoutsessionset.delete.DeleteWorkoutSessionSetCommand;
import com.ironcore.application.workoutsession.workoutsessionset.update.UpdateWorkoutSessionSetCommand;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.workoutsession.workoutsession.list.ListWorkoutSessionsCommand;
import com.ironcore.application.workoutsession.workoutsession.list.ListWorkoutSessionsResult;
import com.ironcore.interfaces.rest.workoutsession.dto.list.ListWorkoutSessionsResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.list.ListWorkoutSessionsItemResponse;
import java.time.LocalDate;
import com.ironcore.interfaces.rest.workoutsession.dto.close.CloseWorkoutSessionResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.detail.GetWorkoutSessionDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.detail.WorkoutSessionActivityDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.detail.WorkoutSessionSetDetailResponse;
import com.ironcore.interfaces.rest.workoutsession.dto.execution.UpdateWorkoutSessionActivityExecutionRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.loadmode.ChangeWorkoutSessionActivityLoadModeRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.notes.UpdateWorkoutSessionNotesRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.set.CreateWorkoutSessionSetRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.set.UpdateWorkoutSessionSetRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.start.StartWorkoutSessionRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.start.StartWorkoutSessionResponse;

public final class WorkoutSessionRestMapper {

    private WorkoutSessionRestMapper() {
    }

    public static ListWorkoutSessionsCommand toListCommand(
            AuthenticatedUser authenticatedUser, Long cycleId, Long dayId,
            LocalDate startDate, LocalDate endDate, int page, int size, String name) {
        return new ListWorkoutSessionsCommand(authenticatedUser.userId(),
                cycleId == null ? null : new WorkoutCycleId(cycleId),
                dayId == null ? null : new WorkoutDayId(dayId), startDate, endDate, page, size, name);
    }

    public static ListWorkoutSessionsResponse toResponse(ListWorkoutSessionsResult result) {
        var page = result.sessions();
        var content = page.content().stream().map(item -> new ListWorkoutSessionsItemResponse(
                item.id().value(),
                item.workoutCycleId() == null ? null : item.workoutCycleId().value(),
                item.workoutDayId() == null ? null : item.workoutDayId().value(),
                item.workoutCycleNameSnapshot(), item.workoutDayTitleSnapshot(), item.weekDaySnapshot(),
                item.performedOn(), item.startedAt(), item.finishedAt(), item.status(), item.closeReason())).toList();
        return new ListWorkoutSessionsResponse(new PageResult<>(content, page.page(), page.size(),
                page.totalElements(), page.totalPages(), page.last()));
    }

    public static UpdateWorkoutSessionNotesCommand toUpdateWorkoutSessionNotesCommand(
            AuthenticatedUser authenticatedUser, Long id, UpdateWorkoutSessionNotesRequest request
    ) {
        return new UpdateWorkoutSessionNotesCommand(authenticatedUser.userId(), new WorkoutSessionId(id),
                request.getNotes(), request.hasNotes());
    }

    public static UpdateWorkoutSessionActivityExecutionCommand toUpdateWorkoutSessionActivityExecutionCommand(
            AuthenticatedUser authenticatedUser, Long id, Long activityId, UpdateWorkoutSessionActivityExecutionRequest request
    ) {
        return new UpdateWorkoutSessionActivityExecutionCommand(authenticatedUser.userId(), new WorkoutSessionId(id),
                new WorkoutSessionActivityId(activityId),
                request.getActualDurationMinutes(), request.hasActualDurationMinutes(),
                request.getActualDistanceKm(), request.hasActualDistanceKm(), request.getNotes(), request.hasNotes());
    }

    public static CreateWorkoutSessionSetCommand toCreateWorkoutSessionSetCommand(
            AuthenticatedUser authenticatedUser, Long id, Long activityId, CreateWorkoutSessionSetRequest request
    ) {
        return new CreateWorkoutSessionSetCommand(authenticatedUser.userId(), new WorkoutSessionId(id),
                new WorkoutSessionActivityId(activityId),
                request.setNumber(), request.actualLoadKg(), request.actualLoadMode(), request.actualReps(), request.notes());
    }

    public static UpdateWorkoutSessionSetCommand toUpdateWorkoutSessionSetCommand(
            AuthenticatedUser authenticatedUser, Long id, Long activityId, Long setId, UpdateWorkoutSessionSetRequest request
    ) {
        return new UpdateWorkoutSessionSetCommand(authenticatedUser.userId(), new WorkoutSessionId(id),
                new WorkoutSessionActivityId(activityId), new WorkoutSessionSetId(setId),
                request.getActualLoadKg(), request.hasActualLoadKg(), request.getActualLoadMode(), request.hasActualLoadMode(),
                request.getActualReps(), request.hasActualReps(), request.getNotes(), request.hasNotes());
    }

    public static DeleteWorkoutSessionSetCommand toDeleteWorkoutSessionSetCommand(
            AuthenticatedUser authenticatedUser, Long id, Long activityId, Long setId
    ) {
        return new DeleteWorkoutSessionSetCommand(authenticatedUser.userId(), new WorkoutSessionId(id),
                new WorkoutSessionActivityId(activityId), new WorkoutSessionSetId(setId));
    }

    public static ChangeWorkoutSessionActivityLoadModeCommand toChangeWorkoutSessionActivityLoadModeCommand(
            AuthenticatedUser authenticatedUser, Long id, Long activityId, ChangeWorkoutSessionActivityLoadModeRequest request
    ) {
        return new ChangeWorkoutSessionActivityLoadModeCommand(authenticatedUser.userId(), new WorkoutSessionId(id),
                new WorkoutSessionActivityId(activityId),
                request.actualLoadMode());
    }

    public static CloseWorkoutSessionCommand toCloseCommand(AuthenticatedUser authenticatedUser, Long id) {
        return new CloseWorkoutSessionCommand(authenticatedUser.userId(), new WorkoutSessionId(id));
    }

    public static CloseWorkoutSessionResponse toResponse(CloseWorkoutSessionResult result) {
        return new CloseWorkoutSessionResponse(
                result.id().value(),
                result.performedOn(),
                result.startedAt(),
                result.finishedAt(),
                result.status(),
                result.closeReason(),
                result.updatedAt()
        );
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

    public static WorkoutSessionActivityDetailResponse toActivityResponse(WorkoutSessionActivityDetailResult result) {
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
                result.sets().stream().map(WorkoutSessionRestMapper::toSetResponse).toList(),
                result.actualLoadMode()
        );
    }

    public static WorkoutSessionSetDetailResponse toSetResponse(WorkoutSessionSetDetailResult result) {
        return new WorkoutSessionSetDetailResponse(
                result.id().value(),
                result.setNumber(),
                result.actualLoadKg(),
                result.actualLoadMode(),
                result.actualReps(),
                result.notes(),
                result.createdAt(),
                result.updatedAt(),
                result.normalizedLoadKg()
        );
    }
}
