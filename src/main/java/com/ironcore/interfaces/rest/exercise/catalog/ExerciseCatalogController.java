package com.ironcore.interfaces.rest.exercise.catalog;

import com.ironcore.application.exercise.catalog.usecase.*;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.interfaces.rest.exercise.catalog.api.ExerciseCatalogApi;
import com.ironcore.interfaces.rest.exercise.catalog.dto.ActivityTypeItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.dto.EquipmentTypeItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.dto.MuscleGroupItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.dto.MuscleSubgroupItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.mapper.ExerciseCatalogRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exercise-catalog")
public class ExerciseCatalogController implements ExerciseCatalogApi {

    private final ListActivityTypesUseCase listActivityTypesUseCase;
    private final ListEquipmentTypesUseCase listEquipmentTypesUseCase;
    private final ListMuscleGroupsUseCase listMuscleGroupsUseCase;
    private final ListMuscleSubgroupsUseCase listMuscleSubgroupsUseCase;

    @GetMapping("/activity-types")
    public ResponseEntity<List<ActivityTypeItemResponse>> getActivityTypes() {
        List<ActivityTypeItemResponse> response = listActivityTypesUseCase.execute()
                .stream()
                .map(ExerciseCatalogRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/equipment-types")
    public ResponseEntity<List<EquipmentTypeItemResponse>> getEquipmentTypes() {
        List<EquipmentTypeItemResponse> responses = listEquipmentTypesUseCase.execute()
                .stream()
                .map(ExerciseCatalogRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/muscle-groups")
    public ResponseEntity<List<MuscleGroupItemResponse>> getMuscleGroups() {
        List<MuscleGroupItemResponse> responses = listMuscleGroupsUseCase.execute()
                .stream()
                .map(ExerciseCatalogRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/muscle-subgroups")
    public ResponseEntity<List<MuscleSubgroupItemResponse>> getMuscleSubgroups(
            @RequestParam(name = "muscleGroupId", required = false) Long muscleGroupId
    ) {
        MuscleGroupId groupId = muscleGroupId == null ? null : new MuscleGroupId(muscleGroupId);

        List<MuscleSubgroupItemResponse> responses = listMuscleSubgroupsUseCase.execute(groupId)
                .stream()
                .map(ExerciseCatalogRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
