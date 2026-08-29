package com.ironcore.interfaces.rest.workoutplanning.workoutcycle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.workoutplanning.workoutcycle.cancel.*;
import com.ironcore.application.workoutplanning.workoutcycle.complete.*;
import com.ironcore.application.workoutplanning.workoutcycle.create.*;
import com.ironcore.application.workoutplanning.workoutcycle.delete.*;
import com.ironcore.application.workoutplanning.workoutcycle.detail.*;
import com.ironcore.application.workoutplanning.workoutcycle.list.*;
import com.ironcore.application.workoutplanning.workoutcycle.start.*;
import com.ironcore.application.workoutplanning.workoutcycle.update.*;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.create.CreateWorkoutCycleRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutcycle.dto.update.UpdateWorkoutCycleRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutCycleController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkoutCycleControllerTest {

    private static final String ENDPOINT = "/api/users/me/workout-cycles";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private CreateWorkoutCycleUseCase createWorkoutCycleUseCase;
    @MockitoBean private UpdateWorkoutCycleUseCase updateWorkoutCycleUseCase;
    @MockitoBean private DeleteWorkoutCycleUseCase deleteWorkoutCycleUseCase;
    @MockitoBean private StartWorkoutCycleUseCase startWorkoutCycleUseCase;
    @MockitoBean private CompleteWorkoutCycleUseCase completeWorkoutCycleUseCase;
    @MockitoBean private CancelWorkoutCycleUseCase cancelWorkoutCycleUseCase;
    @MockitoBean private GetWorkoutCycleDetailUseCase getWorkoutCycleDetailUseCase;
    @MockitoBean private ListWorkoutCyclesUseCase listWorkoutCyclesUseCase;
    @MockitoBean private ErrorLogPublisher errorLogPublisher;
    @MockitoBean private JwtAccessTokenValidator jwtAccessTokenValidator;
    @MockitoBean private UserRepository userRepository;

    @Nested class Creation {
        @Test void shouldCreateWorkoutCycle() throws Exception {
            CreateWorkoutCycleRequest request = new CreateWorkoutCycleRequest("Hipertrofia", 2L, 3, "Foco em força");
            CreateWorkoutCycleCommand command = new CreateWorkoutCycleCommand(new UserId(1L), "Hipertrofia", new TrainingGoalId(2L), 3, "Foco em força");
            when(createWorkoutCycleUseCase.execute(command)).thenReturn(new CreateWorkoutCycleResult(new WorkoutCycleId(1L), new PersonId(3L), "Hipertrofia", new TrainingGoalId(2L), 3, WorkoutStatus.NOT_STARTED, WorkoutOrigin.MANUAL, "Foco em força", LocalDateTime.of(2026, 8, 29, 10, 0)));

            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.personId").value(3L)).andExpect(jsonPath("$.workoutStatus").value("NOT_STARTED"));
            verify(createWorkoutCycleUseCase).execute(command);
        }

        @Test void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {
            CreateWorkoutCycleRequest request = new CreateWorkoutCycleRequest("", null, 0, null);
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"));
            verifyNoInteractions(createWorkoutCycleUseCase);
        }
    }

    @Nested class Update {
        @Test void shouldUpdateWorkoutCycle() throws Exception {
            UpdateWorkoutCycleRequest request = new UpdateWorkoutCycleRequest("Hipertrofia atualizada", 2L, 4, "Nova observação");
            UpdateWorkoutCycleCommand command = new UpdateWorkoutCycleCommand(new UserId(1L), new WorkoutCycleId(1L), "Hipertrofia atualizada", new TrainingGoalId(2L), 4, "Nova observação");
            when(updateWorkoutCycleUseCase.execute(command)).thenReturn(new UpdateWorkoutCycleResult(new WorkoutCycleId(1L), "Hipertrofia atualizada", new TrainingGoalId(2L), null, WorkoutStatus.NOT_STARTED, WorkoutOrigin.MANUAL, 4, "Nova observação", LocalDateTime.of(2026, 8, 29, 10, 0)));

            mockMvc.perform(put(ENDPOINT + "/{id}", 1L).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Hipertrofia atualizada"));
            verify(updateWorkoutCycleUseCase).execute(command);
        }
    }

    @Nested class Lifecycle {
        @Test void shouldStartWorkoutCycle() throws Exception {
            StartWorkoutCycleCommand command = new StartWorkoutCycleCommand(new UserId(1L), new WorkoutCycleId(1L));
            when(startWorkoutCycleUseCase.execute(command)).thenReturn(new StartWorkoutCycleResult(new WorkoutCycleId(1L), new TrainingGoalId(2L), LocalDate.of(2026, 8, 29), WorkoutStatus.IN_PROGRESS));
            mockMvc.perform(patch(ENDPOINT + "/{id}/start", 1L).with(authenticatedUser()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.workoutStatus").value("IN_PROGRESS"));
            verify(startWorkoutCycleUseCase).execute(command);
        }

        @Test void shouldCompleteWorkoutCycle() throws Exception {
            CompleteWorkoutCycleCommand command = new CompleteWorkoutCycleCommand(new UserId(1L), new WorkoutCycleId(1L));
            when(completeWorkoutCycleUseCase.execute(command)).thenReturn(new CompleteWorkoutCycleResult(new WorkoutCycleId(1L), new TrainingGoalId(2L), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 29), WorkoutStatus.COMPLETED));
            mockMvc.perform(patch(ENDPOINT + "/{id}/complete", 1L).with(authenticatedUser()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.endDate").value("2026-08-29")).andExpect(jsonPath("$.workoutStatus").value("COMPLETED"));
            verify(completeWorkoutCycleUseCase).execute(command);
        }

        @Test void shouldCancelWorkoutCycle() throws Exception {
            CancelWorkoutCycleCommand command = new CancelWorkoutCycleCommand(new UserId(1L), new WorkoutCycleId(1L));
            when(cancelWorkoutCycleUseCase.execute(command)).thenReturn(new CancelWorkoutCycleResult(new WorkoutCycleId(1L), new TrainingGoalId(2L), WorkoutStatus.CANCELLED));
            mockMvc.perform(patch(ENDPOINT + "/{id}/cancel", 1L).with(authenticatedUser()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.workoutStatus").value("CANCELLED"));
            verify(cancelWorkoutCycleUseCase).execute(command);
        }

        @Test void shouldReturnUnprocessableEntityWhenLifecycleTransitionIsInvalid() throws Exception {
            StartWorkoutCycleCommand command = new StartWorkoutCycleCommand(new UserId(1L), new WorkoutCycleId(1L));
            doThrow(new OperationNotAllowedException("Ciclo já iniciado.")).when(startWorkoutCycleUseCase).execute(command);
            mockMvc.perform(patch(ENDPOINT + "/{id}/start", 1L).with(authenticatedUser())).andExpect(status().isUnprocessableEntity());
            verify(startWorkoutCycleUseCase).execute(command);
        }
    }

    @Nested class Delete {
        @Test void shouldDeleteWorkoutCycle() throws Exception {
            DeleteWorkoutCycleCommand command = new DeleteWorkoutCycleCommand(new UserId(1L), new WorkoutCycleId(1L));
            mockMvc.perform(delete(ENDPOINT + "/{id}", 1L).with(authenticatedUser())).andExpect(status().isNoContent());
            verify(deleteWorkoutCycleUseCase).execute(command);
        }
    }

    @Nested class Detail {
        @Test void shouldReturnWorkoutCycleDetail() throws Exception {
            GetWorkoutCycleDetailCommand command = new GetWorkoutCycleDetailCommand(new UserId(1L), new WorkoutCycleId(1L));
            WorkoutActivityDetailResult activity = new WorkoutActivityDetailResult(new WorkoutActivityId(4L), 1, 3, 8, 12, new BigDecimal("20.0"), null, null, null, null, 60, "Controle", new ExerciseDetailResult(new ExerciseId(5L), "Supino", List.of(new MuscleGroupDetailResult(new MuscleGroupId(6L), "Peitoral"))));
            WorkoutCycleDetailResult result = new WorkoutCycleDetailResult(new WorkoutCycleId(1L), "Hipertrofia", WorkoutStatus.IN_PROGRESS, new TrainingGoalDetailResult(new TrainingGoalId(2L), "Hipertrofia"), LocalDate.of(2026, 8, 1), null, 3, "Notas", List.of(new WorkoutDayDetailResult(new WorkoutDayId(3L), WeekDay.MONDAY, "Treino A", 1, List.of(activity))));
            when(getWorkoutCycleDetailUseCase.execute(command)).thenReturn(result);
            mockMvc.perform(get(ENDPOINT + "/{id}", 1L).with(authenticatedUser()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.trainingGoal.name").value("Hipertrofia")).andExpect(jsonPath("$.days[0].activities[0].exercise.name").value("Supino"));
            verify(getWorkoutCycleDetailUseCase).execute(command);
        }
    }

    @Nested class Listing {
        @Test void shouldListWorkoutCyclesWithFilters() throws Exception {
            ListWorkoutCyclesCommand command = new ListWorkoutCyclesCommand(new UserId(1L), WorkoutStatus.IN_PROGRESS, new TrainingGoalId(2L), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), "Hipertrofia", 1, 10);
            ListWorkoutCyclesItemResult item = new ListWorkoutCyclesItemResult(new WorkoutCycleId(1L), "Hipertrofia", WorkoutStatus.IN_PROGRESS, new TrainingGoalItemResult(new TrainingGoalId(2L), "Hipertrofia"), LocalDate.of(2026, 8, 1), null, 3);
            when(listWorkoutCyclesUseCase.execute(command)).thenReturn(new ListWorkoutCyclesResult(new PageResult<>(List.of(item), 1, 10, 1, 1, true)));
            mockMvc.perform(get(ENDPOINT).with(authenticatedUser()).param("workoutStatus", "IN_PROGRESS").param("trainingGoalId", "2").param("startDate", "2026-08-01").param("endDate", "2026-08-31").param("name", "Hipertrofia").param("page", "1").param("size", "10"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.cycles.content[0].id").value(1L)).andExpect(jsonPath("$.cycles.content[0].trainingGoal.name").value("Hipertrofia"));
            verify(listWorkoutCyclesUseCase).execute(command);
        }

        @ParameterizedTest @CsvSource({"-1,20", "0,0", "0,101"})
        void shouldReturnBadRequestWhenPaginationIsInvalid(int page, int size) throws Exception {
            mockMvc.perform(get(ENDPOINT).with(authenticatedUser()).param("page", String.valueOf(page)).param("size", String.valueOf(size)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Erro de validação nos parâmetros da requisição"));
            verifyNoInteractions(listWorkoutCyclesUseCase);
        }
    }
}
