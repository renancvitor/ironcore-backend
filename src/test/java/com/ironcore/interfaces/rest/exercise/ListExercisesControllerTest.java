package com.ironcore.interfaces.rest.exercise;

import com.ironcore.application.exercise.catalog.usecase.ActivityTypeItemResult;
import com.ironcore.application.exercise.catalog.usecase.EquipmentTypeItemResult;
import com.ironcore.application.exercise.usecase.GetExerciseByIdUseCase;
import com.ironcore.application.exercise.usecase.ListExercisesCommand;
import com.ironcore.application.exercise.usecase.ListExercisesItemResult;
import com.ironcore.application.exercise.usecase.ListExercisesResult;
import com.ironcore.application.exercise.usecase.ListExercisesUseCase;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExerciseController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ListExercisesControllerTest {

    private static final String EXERCISES_ENDPOINT = "/api/exercise-catalog/exercises";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListExercisesUseCase listExercisesUseCase;

    @MockitoBean
    private GetExerciseByIdUseCase getExerciseByIdUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    class SuccessfulList {

        @Test
        void shouldListExercisesWithFiltersAndPagination() throws Exception {
            ListExercisesCommand command = new ListExercisesCommand(
                    "supino",
                    new ActivityTypeId(3L),
                    new EquipmentTypeId(2L),
                    new MuscleGroupId(5L),
                    new MuscleSubgroupId(4L),
                    TargetRoleType.PRIMARY,
                    1,
                    2
            );

            when(listExercisesUseCase.execute(command))
                    .thenReturn(listExercisesResult(1, 2, 5, 3, false));

            mockMvc.perform(get(EXERCISES_ENDPOINT)
                            .param("name", "supino")
                            .param("activityTypeId", "3")
                            .param("equipmentTypeId", "2")
                            .param("muscleGroupId", "5")
                            .param("muscleSubgroupId", "4")
                            .param("targetRole", "PRIMARY")
                            .param("page", "1")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exercises.page").value(1))
                    .andExpect(jsonPath("$.exercises.size").value(2))
                    .andExpect(jsonPath("$.exercises.totalElements").value(5))
                    .andExpect(jsonPath("$.exercises.totalPages").value(3))
                    .andExpect(jsonPath("$.exercises.last").value(false))
                    .andExpect(jsonPath("$.exercises.content").isArray())
                    .andExpect(jsonPath("$.exercises.content.length()").value(1))
                    .andExpect(jsonPath("$.exercises.content[0].id").value(1L))
                    .andExpect(jsonPath("$.exercises.content[0].name").value("Supino reto"))
                    .andExpect(jsonPath("$.exercises.content[0].equipmentType.id").value(2L))
                    .andExpect(jsonPath("$.exercises.content[0].equipmentType.code").value("BARBELL"))
                    .andExpect(jsonPath("$.exercises.content[0].equipmentType.name").value("Barra"))
                    .andExpect(jsonPath("$.exercises.content[0].activityType.id").value(3L))
                    .andExpect(jsonPath("$.exercises.content[0].activityType.code").value("STRENGTH"))
                    .andExpect(jsonPath("$.exercises.content[0].activityType.name").value("Força"))
                    .andExpect(jsonPath("$.exercises.content[0].unilateral").value(false))
                    .andExpect(jsonPath("$.exercises.content[0].compound").value(true))
                    .andExpect(jsonPath("$.exercises.content[0].suggestedRestSeconds").value(90));

            verify(listExercisesUseCase).execute(command);
        }

        @Test
        void shouldUseDefaultPaginationWhenParametersAreNotProvided() throws Exception {
            ListExercisesCommand command = new ListExercisesCommand(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    20
            );

            when(listExercisesUseCase.execute(command))
                    .thenReturn(new ListExercisesResult(new PageResult<>(
                            List.of(),
                            0,
                            20,
                            0,
                            0,
                            true
                    )));

            mockMvc.perform(get(EXERCISES_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exercises.page").value(0))
                    .andExpect(jsonPath("$.exercises.size").value(20))
                    .andExpect(jsonPath("$.exercises.totalElements").value(0))
                    .andExpect(jsonPath("$.exercises.totalPages").value(0))
                    .andExpect(jsonPath("$.exercises.last").value(true))
                    .andExpect(jsonPath("$.exercises.content").isArray())
                    .andExpect(jsonPath("$.exercises.content").isEmpty());

            verify(listExercisesUseCase).execute(command);
        }
    }

    @Nested
    class InvalidPagination {

        @ParameterizedTest
        @CsvSource({
                "-1, 20",
                "0, 0",
                "0, 101"
        })
        void shouldReturnBadRequestWhenPaginationIsInvalid(int page, int size) throws Exception {
            mockMvc.perform(get(EXERCISES_ENDPOINT)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message")
                            .value("Erro de validação nos parâmetros da requisição"))
                    .andExpect(jsonPath("$.path").value(EXERCISES_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isArray());

            verify(listExercisesUseCase, never()).execute(any());
        }
    }

    @Nested
    class InvalidFilters {

        @ParameterizedTest
        @ValueSource(strings = {
                "activityTypeId",
                "equipmentTypeId",
                "muscleGroupId",
                "muscleSubgroupId"
        })
        void shouldReturnBadRequestWhenFilterIdIsInvalid(String parameter) throws Exception {
            mockMvc.perform(get(EXERCISES_ENDPOINT)
                            .param(parameter, "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.path").value(EXERCISES_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isArray());

            verify(listExercisesUseCase, never()).execute(any());
        }

        @Test
        void shouldReturnBadRequestWhenTargetRoleIsInvalid() throws Exception {
            mockMvc.perform(get(EXERCISES_ENDPOINT)
                            .param("targetRole", "INVALID"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.path").value(EXERCISES_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isArray());

            verify(listExercisesUseCase, never()).execute(any());
        }
    }

    private static ListExercisesResult listExercisesResult(
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {
        ListExercisesItemResult item = new ListExercisesItemResult(
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
                90
        );

        return new ListExercisesResult(new PageResult<>(
                List.of(item),
                page,
                size,
                totalElements,
                totalPages,
                last
        ));
    }
}
