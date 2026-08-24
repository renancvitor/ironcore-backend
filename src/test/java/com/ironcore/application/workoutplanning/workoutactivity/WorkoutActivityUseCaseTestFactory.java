package com.ironcore.application.workoutplanning.workoutactivity;

import com.ironcore.application.workoutplanning.workoutactivity.create.CreateWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.delete.DeleteWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.reorder.ReorderWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.update.UpdateWorkoutActivityCommand;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import java.math.BigDecimal;

public final class WorkoutActivityUseCaseTestFactory {

    private WorkoutActivityUseCaseTestFactory() {}

    public static CreateWorkoutActivityCommand validCreateCommand() {
        return new CreateWorkoutActivityCommand(
                new UserId(1L),
                new WorkoutDayId(1L),
                new ExerciseId(1L),
                4,
                8,
                12,
                new BigDecimal("80.50"),
                "RPE 8",
                45,
                new BigDecimal("5.50"),
                "Moderada",
                90,
                "Priorizar a técnica");
    }

    public static UpdateWorkoutActivityCommand validUpdateCommand() {
        return new UpdateWorkoutActivityCommand(
                new UserId(1L),
                new WorkoutActivityId(1L),
                new ExerciseId(1L),
                5,
                6,
                10,
                new BigDecimal("90.00"),
                "RPE 9",
                50,
                new BigDecimal("6.00"),
                "Alta",
                120,
                "Manter cadência controlada");
    }

    public static DeleteWorkoutActivityCommand validDeleteCommand() {
        return new DeleteWorkoutActivityCommand(new UserId(1L), new WorkoutActivityId(1L));
    }

    public static ReorderWorkoutActivityCommand validReorderCommand() {
        return new ReorderWorkoutActivityCommand(new UserId(1L), new WorkoutActivityId(1L), 2);
    }
}
