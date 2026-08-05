package com.ironcore.interfaces.rest.exercise;

import com.ironcore.application.exercise.usecase.*;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.interfaces.rest.exercise.api.ExerciseApi;
import com.ironcore.interfaces.rest.exercise.dto.GetExerciseByIdResponse;
import com.ironcore.interfaces.rest.exercise.dto.ListExercisesRequest;
import com.ironcore.interfaces.rest.exercise.dto.ListExercisesResponse;
import com.ironcore.interfaces.rest.exercise.mapper.ExerciseRestMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/exercise-catalog/exercises")
@RequiredArgsConstructor
public class ExerciseController implements ExerciseApi {

    private final GetExerciseByIdUseCase getExerciseByIdUseCase;
    private final ListExercisesUseCase listExercisesUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<GetExerciseByIdResponse> getExerciseById(@PathVariable Long id) {
        GetExerciseByIdResult result = getExerciseByIdUseCase.execute(new ExerciseId(id));
        return ResponseEntity.ok(ExerciseRestMapper.toGetByIdResponse(result));
    }

    @GetMapping
    public ResponseEntity<ListExercisesResponse> list(
            @RequestParam(name = "page", defaultValue = "0")
            @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20")
            @Min(1) @Max(100) int size,
            @ModelAttribute ListExercisesRequest request
    ) {
        ListExercisesCommand command = ExerciseRestMapper.toCommand(
                request,
                page,
                size
        );
        ListExercisesResult result = listExercisesUseCase.execute(command);
        ListExercisesResponse response = ExerciseRestMapper.toListResponse(result);

        return ResponseEntity.ok(response);
    }
}
