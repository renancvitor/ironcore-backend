package com.ironcore.application.workoutplanning.workoutcycle;

import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;

import java.time.LocalDate;

public record WorkoutCycleAuditData(
        Long id,
        Long personId,
        String name,
        Long trainingGoalId,
        LocalDate startDate,
        LocalDate endDate,
        Integer desiredDurationMonths,
        WorkoutStatus workoutStatus,
        WorkoutOrigin workoutOrigin,
        String notes
) implements LoggableData {

    public static WorkoutCycleAuditData from(WorkoutCycle workoutCycle) {
        return new WorkoutCycleAuditData(
                workoutCycle.getId().value(),
                workoutCycle.getPersonId().value(),
                workoutCycle.getName(),
                workoutCycle.getTrainingGoalId().value(),
                workoutCycle.getStartDate(),
                workoutCycle.getEndDate(),
                workoutCycle.getDesiredDurationMonths(),
                workoutCycle.getWorkoutStatus(),
                workoutCycle.getWorkoutOrigin(),
                workoutCycle.getNotes()
        );
    }
}
