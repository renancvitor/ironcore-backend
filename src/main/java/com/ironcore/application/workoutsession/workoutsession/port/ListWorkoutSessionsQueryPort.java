package com.ironcore.application.workoutsession.workoutsession.port;

import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.workoutsession.workoutsession.list.ListWorkoutSessionsItemResult;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import java.time.LocalDate;

public interface ListWorkoutSessionsQueryPort {
    PageResult<ListWorkoutSessionsItemResult> findClosedSessions(
            PersonId personId, WorkoutCycleId workoutCycleId, WorkoutDayId workoutDayId,
            LocalDate startDate, LocalDate endDate, PageQuery pageQuery);
}
