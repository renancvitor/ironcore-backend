package com.ironcore.domain.exercise.repository;

import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.valueobject.ExerciseId;

import java.util.Optional;

public interface ExerciseRepository {

    Optional<Exercise> findById(ExerciseId id);
}
