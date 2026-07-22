package com.ironcore.domain.exercisemuscletarget.repository;

import com.ironcore.domain.exercisemuscletarget.model.ExerciseMuscleTarget;
import com.ironcore.domain.exercisemuscletarget.valueobject.ExerciseMuscleTargetId;

import java.util.Optional;

public interface ExerciseMuscleTargetRepository {

    Optional<ExerciseMuscleTarget> findById(ExerciseMuscleTargetId id);
}
