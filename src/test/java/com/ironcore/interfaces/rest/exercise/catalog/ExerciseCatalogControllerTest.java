package com.ironcore.interfaces.rest.exercise.catalog;

import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.ListActivityTypesUseCase;
import com.ironcore.application.exercise.catalog.usecase.ListEquipmentTypesUseCase;
import com.ironcore.application.exercise.catalog.usecase.ListMuscleGroupsUseCase;
import com.ironcore.application.exercise.catalog.usecase.ListMuscleSubgroupsUseCase;
import com.ironcore.application.exercise.catalog.usecase.MuscleGroupItemResult;
import com.ironcore.application.exercise.catalog.usecase.MuscleSubgroupItemResult;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExerciseCatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExerciseCatalogControllerTest {

    private static final String EXERCISE_CATALOG_BASE_ENDPOINT = "/api/exercise-catalog";
    private static final String ACTIVITY_TYPES_ENDPOINT = EXERCISE_CATALOG_BASE_ENDPOINT + "/activity-types";
    private static final String EQUIPMENT_TYPES_ENDPOINT = EXERCISE_CATALOG_BASE_ENDPOINT + "/equipment-types";
    private static final String MUSCLE_GROUPS_ENDPOINT = EXERCISE_CATALOG_BASE_ENDPOINT + "/muscle-groups";
    private static final String MUSCLE_SUBGROUPS_ENDPOINT = EXERCISE_CATALOG_BASE_ENDPOINT + "/muscle-subgroups";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListActivityTypesUseCase listActivityTypesUseCase;

    @MockitoBean
    private ListEquipmentTypesUseCase listEquipmentTypesUseCase;

    @MockitoBean
    private ListMuscleGroupsUseCase listMuscleGroupsUseCase;

    @MockitoBean
    private ListMuscleSubgroupsUseCase listMuscleSubgroupsUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    class ActivityTypes {

        @Test
        void shouldReturnActivityTypes() throws Exception {
            when(listActivityTypesUseCase.execute()).thenReturn(List.of(
                    new ActivityTypeItemResult(
                            new ActivityTypeId(1L),
                            new ActivityTypeCode("STRENGTH"),
                            "Força"
                    )
            ));

            mockMvc.perform(get(ACTIVITY_TYPES_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].code").value("STRENGTH"))
                    .andExpect(jsonPath("$[0].name").value("Força"));

            verify(listActivityTypesUseCase).execute();
        }
    }

    @Nested
    class EquipmentTypes {

        @Test
        void shouldReturnEquipmentTypes() throws Exception {
            when(listEquipmentTypesUseCase.execute()).thenReturn(List.of(
                    new EquipmentTypeItemResult(
                            new EquipmentTypeId(2L),
                            new EquipmentTypeCode("DUMBBELL"),
                            "Halteres"
                    )
            ));

            mockMvc.perform(get(EQUIPMENT_TYPES_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(2L))
                    .andExpect(jsonPath("$[0].code").value("DUMBBELL"))
                    .andExpect(jsonPath("$[0].name").value("Halteres"));

            verify(listEquipmentTypesUseCase).execute();
        }
    }

    @Nested
    class MuscleGroups {

        @Test
        void shouldReturnMuscleGroups() throws Exception {
            when(listMuscleGroupsUseCase.execute()).thenReturn(List.of(
                    new MuscleGroupItemResult(
                            new MuscleGroupId(3L),
                            new MuscleGroupCode("CHEST"),
                            "Peitoral"
                    )
            ));

            mockMvc.perform(get(MUSCLE_GROUPS_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(3L))
                    .andExpect(jsonPath("$[0].code").value("CHEST"))
                    .andExpect(jsonPath("$[0].name").value("Peitoral"));

            verify(listMuscleGroupsUseCase).execute();
        }
    }

    @Nested
    class MuscleSubgroups {

        @Test
        void shouldReturnAllMuscleSubgroupsWhenMuscleGroupIdIsNotProvided() throws Exception {
            when(listMuscleSubgroupsUseCase.execute(null)).thenReturn(List.of(
                    muscleSubgroupItemResult()
            ));

            mockMvc.perform(get(MUSCLE_SUBGROUPS_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(4L))
                    .andExpect(jsonPath("$[0].code").value("PECTORALIS_MAJOR"))
                    .andExpect(jsonPath("$[0].muscleGroupId").value(3L))
                    .andExpect(jsonPath("$[0].name").value("Peitoral maior"));

            verify(listMuscleSubgroupsUseCase).execute(null);
        }

        @Test
        void shouldReturnMuscleSubgroupsFilteredByMuscleGroupId() throws Exception {
            MuscleGroupId muscleGroupId = new MuscleGroupId(3L);
            when(listMuscleSubgroupsUseCase.execute(muscleGroupId)).thenReturn(List.of(
                    muscleSubgroupItemResult()
            ));

            mockMvc.perform(get(MUSCLE_SUBGROUPS_ENDPOINT)
                            .param("muscleGroupId", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(4L))
                    .andExpect(jsonPath("$[0].code").value("PECTORALIS_MAJOR"))
                    .andExpect(jsonPath("$[0].muscleGroupId").value(3L))
                    .andExpect(jsonPath("$[0].name").value("Peitoral maior"));

            verify(listMuscleSubgroupsUseCase).execute(muscleGroupId);
        }

        @Test
        void shouldReturnBadRequestWhenMuscleGroupIdIsNotPositive() throws Exception {
            mockMvc.perform(get(MUSCLE_SUBGROUPS_ENDPOINT)
                            .param("muscleGroupId", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message")
                            .value("Id do grupo muscular deve ser positivo."))
                    .andExpect(jsonPath("$.path").value(MUSCLE_SUBGROUPS_ENDPOINT));

            verifyNoInteractions(listMuscleSubgroupsUseCase);
        }
    }

    @Nested
    class UnsupportedWriteMethods {

        @ParameterizedTest
        @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
        void shouldNotExposeWriteEndpoints(String method) throws Exception {
            mockMvc.perform(request(HttpMethod.valueOf(method), ACTIVITY_TYPES_ENDPOINT))
                    .andExpect(status().isMethodNotAllowed());

            verifyNoInteractions(
                    listActivityTypesUseCase,
                    listEquipmentTypesUseCase,
                    listMuscleGroupsUseCase,
                    listMuscleSubgroupsUseCase
            );
        }
    }

    private static MuscleSubgroupItemResult muscleSubgroupItemResult() {
        return new MuscleSubgroupItemResult(
                new MuscleSubgroupId(4L),
                new MuscleSubgroupCode("PECTORALIS_MAJOR"),
                new MuscleGroupId(3L),
                "Peitoral maior"
        );
    }
}
