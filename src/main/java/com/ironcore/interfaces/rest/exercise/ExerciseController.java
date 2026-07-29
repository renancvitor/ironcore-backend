package com.ironcore.interfaces.rest.exercise;

import com.ironcore.application.exercise.usecase.GetExerciseByIdResult;
import com.ironcore.application.exercise.usecase.GetExerciseByIdUseCase;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.interfaces.rest.exercise.dto.GetExerciseByIdResponse;
import com.ironcore.interfaces.rest.exercise.mapper.ExerciseRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercise-catalog/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final GetExerciseByIdUseCase getExerciseByIdUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<GetExerciseByIdResponse> getExerciseById(@PathVariable Long id) {
        GetExerciseByIdResult result = getExerciseByIdUseCase.execute(new ExerciseId(id));
        return ResponseEntity.ok(ExerciseRestMapper.toResponse(result));
    }
}
