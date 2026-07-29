package com.ironcore.interfaces.rest.exercise;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.MuscleSubgroupItemResult;
import com.ironcore.application.exercise.usecase.ExerciseMuscleTargetItemResult;
import com.ironcore.application.exercise.usecase.GetExerciseByIdResult;
import com.ironcore.application.exercise.usecase.GetExerciseByIdUseCase;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExerciseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExerciseControllerTest {

    private static final String EXERCISES_ENDPOINT = "/api/exercise-catalog/exercises";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetExerciseByIdUseCase getExerciseByIdUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    class GetExerciseById {

        @Test
        void shouldReturnExerciseDetailById() throws Exception {
            ExerciseId exerciseId = new ExerciseId(1L);
            when(getExerciseByIdUseCase.execute(exerciseId)).thenReturn(getExerciseByIdResult());

            mockMvc.perform(get(EXERCISES_ENDPOINT + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Supino reto"))
                    .andExpect(jsonPath("$.equipmentType.id").value(2L))
                    .andExpect(jsonPath("$.equipmentType.code").value("BARBELL"))
                    .andExpect(jsonPath("$.equipmentType.name").value("Barra"))
                    .andExpect(jsonPath("$.activityType.id").value(3L))
                    .andExpect(jsonPath("$.activityType.code").value("STRENGTH"))
                    .andExpect(jsonPath("$.activityType.name").value("Força"))
                    .andExpect(jsonPath("$.unilateral").value(false))
                    .andExpect(jsonPath("$.compound").value(true))
                    .andExpect(jsonPath("$.suggestedRestSeconds").value(90))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.muscleTargets").isArray())
                    .andExpect(jsonPath("$.muscleTargets.length()").value(1))
                    .andExpect(jsonPath("$.muscleTargets[0].muscleSubgroup.id").value(4L))
                    .andExpect(jsonPath("$.muscleTargets[0].muscleSubgroup.code").value("PECTORALIS_MAJOR"))
                    .andExpect(jsonPath("$.muscleTargets[0].muscleSubgroup.muscleGroupId").value(5L))
                    .andExpect(jsonPath("$.muscleTargets[0].muscleSubgroup.name").value("Peitoral maior"))
                    .andExpect(jsonPath("$.muscleTargets[0].targetRole").value("PRIMARY"));

            verify(getExerciseByIdUseCase).execute(exerciseId);
        }

        @Test
        void shouldReturnNotFoundWhenExerciseDoesNotExist() throws Exception {
            ExerciseId exerciseId = new ExerciseId(99L);
            when(getExerciseByIdUseCase.execute(exerciseId))
                    .thenThrow(new ResourceNotFoundException("Exercício não encontrado."));

            mockMvc.perform(get(EXERCISES_ENDPOINT + "/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Exercício não encontrado."))
                    .andExpect(jsonPath("$.path").value(EXERCISES_ENDPOINT + "/99"))
                    .andExpect(jsonPath("$.fields").isArray());

            verify(getExerciseByIdUseCase).execute(exerciseId);
        }
    }

    private static GetExerciseByIdResult getExerciseByIdResult() {
        return new GetExerciseByIdResult(
                new ExerciseId(1L),
                "Supino reto",
                new EquipmentTypeItemResult(
                        new EquipmentTypeId(2L),
                        new EquipmentTypeCode("BARBELL"),
                        "Barra"
                ),
                new ActivityTypeItemResult(
                        new ActivityTypeId(3L),
                        new ActivityTypeCode("STRENGTH"),
                        "Força"
                ),
                false,
                true,
                90,
                true,
                List.of(new ExerciseMuscleTargetItemResult(
                        new MuscleSubgroupItemResult(
                                new MuscleSubgroupId(4L),
                                new MuscleSubgroupCode("PECTORALIS_MAJOR"),
                                new MuscleGroupId(5L),
                                "Peitoral maior"
                        ),
                        TargetRoleType.PRIMARY
                ))
        );
    }
}
