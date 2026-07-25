package com.ironcore.application.exercise.catalog.port;

import com.ironcore.application.exercise.catalog.result.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.result.EquipmentTypeItemResult;
import com.ironcore.application.exercise.catalog.result.MuscleGroupItemResult;
import com.ironcore.application.exercise.catalog.result.MuscleSubgroupItemResult;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;

import java.util.List;

public interface ExerciseFilterCatalogQueryPort {

    List<ActivityTypeItemResult> findActiveActivityTypes();

    List<EquipmentTypeItemResult> findActiveEquipmentTypes();

    List<MuscleGroupItemResult> findActiveMuscleGroups();

    List<MuscleSubgroupItemResult> findActiveMuscleSubgroups();

    List<MuscleSubgroupItemResult> findActiveMuscleSubgroupsByMuscleGroupId(MuscleGroupId muscleGroupId);
}
