package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkoutCycleDetailAssembler {

    public WorkoutCycleDetailResult toResult(
            List<WorkoutCycleDetailProjection> projections
    ) {
        if (projections == null || projections.isEmpty()) {
            return null;
        }

        WorkoutCycleDetailProjection first = projections.getFirst();

        Map<Long, DayBuilder> days = new LinkedHashMap<>();

        for (WorkoutCycleDetailProjection row : projections) {

            if (row.dayId() == null) {
                continue;
            }

            DayBuilder day = days.computeIfAbsent(
                    row.dayId(),
                    ignored -> new DayBuilder(
                            row.dayId(),
                            row.weekDay(),
                            row.dayTitle(),
                            row.sortOrder()
                    )
            );

            if (row.activityId() == null) {
                continue;
            }

            ActivityBuilder activity = day.activities.computeIfAbsent(
                    row.activityId(),
                    ignored -> new ActivityBuilder(
                            row.activityId(),
                            row.orderIndex(),
                            row.sets(),
                            row.repRangeMin(),
                            row.repRangeMax(),
                            row.targetLoadKg(),
                            row.targetLoadText(),
                            row.durationMinutes(),
                            row.distanceKm(),
                            row.intensityText(),
                            row.restSeconds(),
                            row.activityNotes(),
                            row.exerciseId(),
                            row.exerciseName()
                    )
            );

            if (row.muscleGroupId() != null) {
                activity.muscleGroups.putIfAbsent(
                        row.muscleGroupId(),
                        new MuscleGroupDetailResult(
                                new MuscleGroupId(row.muscleGroupId()),
                                row.muscleGroupName()
                        )
                );
            }
        }

        List<WorkoutDayDetailResult> dayResults = days.values()
                .stream()
                .map(DayBuilder::toResult)
                .toList();

        return new WorkoutCycleDetailResult(
                new WorkoutCycleId(first.cycleId()),
                first.cycleName(),
                first.workoutStatus(),
                new TrainingGoalDetailResult(
                        new TrainingGoalId(first.trainingGoalId()),
                        first.trainingGoalName()
                ),
                first.startDate(),
                first.endDate(),
                first.desiredDurationMonths(),
                first.cycleNotes(),
                dayResults
        );
    }

    private static class DayBuilder {

        private final Long id;
        private final WeekDay weekDay;
        private final String title;
        private final Integer sortOrder;

        private final Map<Long, ActivityBuilder> activities =
                new LinkedHashMap<>();

        private DayBuilder(
                Long id,
                WeekDay weekDay,
                String title,
                Integer sortOrder
        ) {
            this.id = id;
            this.weekDay = weekDay;
            this.title = title;
            this.sortOrder = sortOrder;
        }

        private WorkoutDayDetailResult toResult() {
            List<WorkoutActivityDetailResult> activityResults =
                    activities.values()
                            .stream()
                            .map(ActivityBuilder::toResult)
                            .toList();

            return new WorkoutDayDetailResult(
                    new WorkoutDayId(id),
                    weekDay,
                    title,
                    sortOrder,
                    activityResults
            );
        }
    }

    private static class ActivityBuilder {

        private final Long id;
        private final Integer orderIndex;
        private final Integer sets;
        private final Integer repRangeMin;
        private final Integer repRangeMax;
        private final BigDecimal targetLoadKg;
        private final String targetLoadText;
        private final Integer durationMinutes;
        private final BigDecimal distanceKm;
        private final String intensityText;
        private final Integer restSeconds;
        private final String notes;

        private final Long exerciseId;
        private final String exerciseName;

        private final Map<Long, MuscleGroupDetailResult> muscleGroups =
                new LinkedHashMap<>();

        private ActivityBuilder(
                Long id,
                Integer orderIndex,
                Integer sets,
                Integer repRangeMin,
                Integer repRangeMax,
                BigDecimal targetLoadKg,
                String targetLoadText,
                Integer durationMinutes,
                BigDecimal distanceKm,
                String intensityText,
                Integer restSeconds,
                String notes,
                Long exerciseId,
                String exerciseName
        ) {
            this.id = id;
            this.orderIndex = orderIndex;
            this.sets = sets;
            this.repRangeMin = repRangeMin;
            this.repRangeMax = repRangeMax;
            this.targetLoadKg = targetLoadKg;
            this.targetLoadText = targetLoadText;
            this.durationMinutes = durationMinutes;
            this.distanceKm = distanceKm;
            this.intensityText = intensityText;
            this.restSeconds = restSeconds;
            this.notes = notes;
            this.exerciseId = exerciseId;
            this.exerciseName = exerciseName;
        }

        private WorkoutActivityDetailResult toResult() {

            ExerciseDetailResult exercise = new ExerciseDetailResult(
                    new ExerciseId(exerciseId),
                    exerciseName,
                    new ArrayList<>(muscleGroups.values())
            );

            return new WorkoutActivityDetailResult(
                    new WorkoutActivityId(id),
                    orderIndex,
                    sets,
                    repRangeMin,
                    repRangeMax,
                    targetLoadKg,
                    targetLoadText,
                    durationMinutes,
                    distanceKm,
                    intensityText,
                    restSeconds,
                    notes,
                    exercise
            );
        }
    }
}