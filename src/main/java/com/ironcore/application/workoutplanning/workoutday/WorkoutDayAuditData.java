package com.ironcore.application.workoutplanning.workoutday;

import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;

public record WorkoutDayAuditData(
        Long id,
        Long workoutCycleId,
        WeekDay weekDay,
        String title,
        Integer sortOrder
) implements LoggableData {

    public static WorkoutDayAuditData from(WorkoutDay workoutDay) {
        return new WorkoutDayAuditData(
                workoutDay.getId().value(),
                workoutDay.getWorkoutCycleId().value(),
                workoutDay.getWeekDay(),
                workoutDay.getTitle(),
                workoutDay.getSortOrder()
        );
    }
}
