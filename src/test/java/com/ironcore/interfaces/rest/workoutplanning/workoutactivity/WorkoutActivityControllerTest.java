package com.ironcore.interfaces.rest.workoutplanning.workoutactivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.workoutplanning.workoutactivity.create.*;
import com.ironcore.application.workoutplanning.workoutactivity.delete.*;
import com.ironcore.application.workoutplanning.workoutactivity.reorder.*;
import com.ironcore.application.workoutplanning.workoutactivity.update.*;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.create.CreateWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.reorder.ReorderWorkoutActivityRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutactivity.dto.update.UpdateWorkoutActivityRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutActivityController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkoutActivityControllerTest {
    private static final String ENDPOINT = "/api/users/me/workout-activities";
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private CreateWorkoutActivityUseCase createWorkoutActivityUseCase;
    @MockitoBean private UpdateWorkoutActivityUseCase updateWorkoutActivityUseCase;
    @MockitoBean private DeleteWorkoutActivityUseCase deleteWorkoutActivityUseCase;
    @MockitoBean private ReorderWorkoutActivityUseCase reorderWorkoutActivityUseCase;
    @MockitoBean private ErrorLogPublisher errorLogPublisher;
    @MockitoBean private JwtAccessTokenValidator jwtAccessTokenValidator;
    @MockitoBean private UserRepository userRepository;

    @Nested class Creation {
        @Test void shouldCreateWorkoutActivity() throws Exception {
            CreateWorkoutActivityRequest request = activityRequest(1L, 2L);
            CreateWorkoutActivityCommand command = createCommand(new WorkoutDayId(1L), new ExerciseId(2L));
            when(createWorkoutActivityUseCase.execute(command)).thenReturn(createResult());
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(3L)).andExpect(jsonPath("$.exerciseId").value(2L));
            verify(createWorkoutActivityUseCase).execute(command);
        }

        @Test void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {
            CreateWorkoutActivityRequest request = activityRequest(null, null);
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"));
            verifyNoInteractions(createWorkoutActivityUseCase);
        }
    }

    @Test void shouldUpdateWorkoutActivity() throws Exception {
        UpdateWorkoutActivityRequest request = new UpdateWorkoutActivityRequest(2L, 4, 10, 15, new BigDecimal("25.0"), null, null, null, null, 90, "Atualizada");
        UpdateWorkoutActivityCommand command = new UpdateWorkoutActivityCommand(new UserId(1L), new WorkoutActivityId(3L), new ExerciseId(2L), 4, 10, 15, new BigDecimal("25.0"), null, null, null, null, 90, "Atualizada");
        when(updateWorkoutActivityUseCase.execute(command)).thenReturn(new UpdateWorkoutActivityResult(new WorkoutActivityId(3L), new WorkoutDayId(1L), new ExerciseId(2L), 1, 4, 10, 15, new BigDecimal("25.0"), null, null, null, null, 90, "Atualizada", LocalDateTime.of(2026, 8, 29, 10, 0)));
        mockMvc.perform(put(ENDPOINT + "/{id}", 3L).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.sets").value(4));
        verify(updateWorkoutActivityUseCase).execute(command);
    }

    @Test void shouldDeleteWorkoutActivity() throws Exception {
        DeleteWorkoutActivityCommand command = new DeleteWorkoutActivityCommand(new UserId(1L), new WorkoutActivityId(3L));
        mockMvc.perform(delete(ENDPOINT + "/{id}", 3L).with(authenticatedUser())).andExpect(status().isNoContent());
        verify(deleteWorkoutActivityUseCase).execute(command);
    }

    @Test void shouldReorderWorkoutActivity() throws Exception {
        ReorderWorkoutActivityRequest request = new ReorderWorkoutActivityRequest(2);
        ReorderWorkoutActivityCommand command = new ReorderWorkoutActivityCommand(new UserId(1L), new WorkoutActivityId(3L), 2);
        mockMvc.perform(patch(ENDPOINT + "/{id}/reorder", 3L).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());
        verify(reorderWorkoutActivityUseCase).execute(command);
    }

    private CreateWorkoutActivityRequest activityRequest(Long workoutDayId, Long exerciseId) {
        return new CreateWorkoutActivityRequest(workoutDayId, exerciseId, 3, 8, 12, new BigDecimal("20.0"), null, null, null, null, 60, "Controle");
    }

    private CreateWorkoutActivityCommand createCommand(WorkoutDayId workoutDayId, ExerciseId exerciseId) {
        return new CreateWorkoutActivityCommand(new UserId(1L), workoutDayId, exerciseId, 3, 8, 12, new BigDecimal("20.0"), null, null, null, null, 60, "Controle");
    }

    private CreateWorkoutActivityResult createResult() {
        return new CreateWorkoutActivityResult(new WorkoutActivityId(3L), new WorkoutDayId(1L), new ExerciseId(2L), 1, 3, 8, 12, new BigDecimal("20.0"), null, null, null, null, 60, "Controle", LocalDateTime.of(2026, 8, 29, 10, 0));
    }
}
