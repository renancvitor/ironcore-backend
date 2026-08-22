package com.ironcore.application.workoutplanning.workoutday;

import com.ironcore.application.workoutplanning.workoutday.create.CreateWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.delete.DeleteWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.reorder.ReorderWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.update.UpdateWorkoutDayCommand;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

public final class WorkoutDayUseCaseTestFactory {

    private WorkoutDayUseCaseTestFactory() {
    }

    public static CreateWorkoutDayCommand validCreateCommand() {
        return new CreateWorkoutDayCommand(
                new UserId(1L),
                new WorkoutCycleId(1L),
                WeekDay.MONDAY,
                "Treino A"
        );
    }

    public static UpdateWorkoutDayCommand validUpdateCommand() {
        return new UpdateWorkoutDayCommand(
                new UserId(1L),
                new WorkoutDayId(1L),
                "Treino atualizado"
        );
    }

    public static DeleteWorkoutDayCommand validDeleteCommand() {
        return new DeleteWorkoutDayCommand(
                new UserId(1L),
                new WorkoutDayId(1L),
                WeekDay.MONDAY,
                "Treino A",
                1
        );
    }

    public static ReorderWorkoutDayCommand validReorderCommand() {
        return new ReorderWorkoutDayCommand(
                new UserId(1L),
                new WorkoutDayId(1L),
                WeekDay.MONDAY,
                2
        );
    }
}
