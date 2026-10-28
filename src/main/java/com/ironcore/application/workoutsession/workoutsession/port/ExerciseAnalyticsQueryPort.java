package com.ironcore.application.workoutsession.workoutsession.port;

import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseCardioExecutionResult;
import com.ironcore.application.workoutsession.workoutsession.analytics.ExerciseStrengthExecutionResult;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.valueobject.PersonId;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseAnalyticsQueryPort {

    List<ExerciseStrengthExecutionResult> findStrengthExecution(
            PersonId personId,
            ExerciseId exerciseId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<ExerciseCardioExecutionResult> findCardioExecution(
            PersonId personId,
            ExerciseId exerciseId,
            LocalDate startDate,
            LocalDate endDate
    );
}
