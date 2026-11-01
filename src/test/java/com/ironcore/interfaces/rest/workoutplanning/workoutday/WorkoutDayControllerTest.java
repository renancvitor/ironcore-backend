package com.ironcore.interfaces.rest.workoutplanning.workoutday;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.workoutplanning.workoutday.create.*;
import com.ironcore.application.workoutplanning.workoutday.delete.*;
import com.ironcore.application.workoutplanning.workoutday.reorder.*;
import com.ironcore.application.workoutplanning.workoutday.update.*;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.create.CreateWorkoutDayRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.reorder.ReorderWorkoutDayRequest;
import com.ironcore.interfaces.rest.workoutplanning.workoutday.dto.update.UpdateWorkoutDayRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutDayController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkoutDayControllerTest {
    private static final String ENDPOINT = "/api/users/me/workout-days";
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private CreateWorkoutDayUseCase createWorkoutDayUseCase;
    @MockitoBean private UpdateWorkoutDayUseCase updateWorkoutDayUseCase;
    @MockitoBean private DeleteWorkoutDayUseCase deleteWorkoutDayUseCase;
    @MockitoBean private ReorderWorkoutDayUseCase reorderWorkoutDayUseCase;
    @MockitoBean private ErrorLogPublisher errorLogPublisher;
    @MockitoBean private JwtAccessTokenValidator jwtAccessTokenValidator;
    @MockitoBean private UserRepository userRepository;

    @Nested class Creation {
        @Test void shouldCreateWorkoutDay() throws Exception {
            CreateWorkoutDayRequest request = new CreateWorkoutDayRequest(1L, WeekDay.MONDAY, "Treino A");
            CreateWorkoutDayCommand command = new CreateWorkoutDayCommand(new UserId(1L), new WorkoutCycleId(1L), WeekDay.MONDAY, "Treino A");
            when(createWorkoutDayUseCase.execute(command)).thenReturn(new CreateWorkoutDayResult(new WorkoutDayId(2L), new WorkoutCycleId(1L), WeekDay.MONDAY, "Treino A", 1, LocalDateTime.of(2026, 8, 29, 10, 0)));
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(2L)).andExpect(jsonPath("$.sortOrder").value(1));
            verify(createWorkoutDayUseCase).execute(command);
        }

        @Test void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {
            CreateWorkoutDayRequest request = new CreateWorkoutDayRequest(null, null, "");
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"));
            verifyNoInteractions(createWorkoutDayUseCase);
        }
    }

    @Test void shouldUpdateWorkoutDay() throws Exception {
        UpdateWorkoutDayRequest request = new UpdateWorkoutDayRequest("Treino B");
        UpdateWorkoutDayCommand command = new UpdateWorkoutDayCommand(new UserId(1L), new WorkoutDayId(2L), "Treino B");
        when(updateWorkoutDayUseCase.execute(command)).thenReturn(new UpdateWorkoutDayResult(new WorkoutDayId(2L), new WorkoutCycleId(1L), WeekDay.MONDAY, "Treino B", 1, LocalDateTime.of(2026, 8, 29, 10, 0)));
        mockMvc.perform(put(ENDPOINT + "/{id}", 2L).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Treino B"));
        verify(updateWorkoutDayUseCase).execute(command);
    }

    @Test void shouldDeleteWorkoutDay() throws Exception {
        DeleteWorkoutDayCommand command = new DeleteWorkoutDayCommand(new UserId(1L), new WorkoutDayId(2L));
        mockMvc.perform(delete(ENDPOINT + "/{id}", 2L).with(authenticatedUser())).andExpect(status().isNoContent());
        verify(deleteWorkoutDayUseCase).execute(command);
    }

    @Test void shouldReorderWorkoutDay() throws Exception {
        ReorderWorkoutDayRequest request = new ReorderWorkoutDayRequest(WeekDay.TUESDAY, 2);
        ReorderWorkoutDayCommand command = new ReorderWorkoutDayCommand(new UserId(1L), new WorkoutDayId(2L), WeekDay.TUESDAY, 2);
        mockMvc.perform(patch(ENDPOINT + "/{id}/reorder", 2L).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());
        verify(reorderWorkoutDayUseCase).execute(command);
    }
}
