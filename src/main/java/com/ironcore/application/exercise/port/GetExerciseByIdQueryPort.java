package com.ironcore.application.exercise.port;

import com.ironcore.application.exercise.usecase.GetExerciseByIdResult;
import com.ironcore.domain.exercise.valueobject.ExerciseId;

import java.util.Optional;

public interface GetExerciseByIdQueryPort {

    Optional<GetExerciseByIdResult> findActiveDetailById(ExerciseId id);

}
