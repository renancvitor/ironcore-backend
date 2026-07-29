package com.ironcore.application.exercise.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exercise.port.GetExerciseByIdQueryPort;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetExerciseByIdUseCase {

    private final GetExerciseByIdQueryPort queryPort;

    @Transactional(readOnly = true)
    public GetExerciseByIdResult execute(ExerciseId id) {
        return queryPort.findActiveDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercício não encontrado."));
    }
}
