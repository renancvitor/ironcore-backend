package com.ironcore.interfaces.rest.workoutplanning.traininggoal;

import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.workoutplanning.traininggoal.usecase.ListTrainingGoalsUseCase;
import com.ironcore.application.workoutplanning.traininggoal.usecase.TrainingGoalResult;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalCode;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
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

@WebMvcTest(TrainingGoalController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainingGoalControllerTest {

    private static final String TRAINING_GOALS_ENDPOINT = "/api/training-goals";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListTrainingGoalsUseCase listTrainingGoalsUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    class ListTrainingGoals {

        @Test
        void shouldReturnTrainingGoals() throws Exception {
            when(listTrainingGoalsUseCase.execute()).thenReturn(List.of(
                    new TrainingGoalResult(
                            new TrainingGoalId(1L),
                            new TrainingGoalCode("HYPERTROPHY"),
                            "Hipertrofia"
                    )
            ));

            mockMvc.perform(get(TRAINING_GOALS_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].code").value("HYPERTROPHY"))
                    .andExpect(jsonPath("$[0].name").value("Hipertrofia"));

            verify(listTrainingGoalsUseCase).execute();
        }
    }

    @Nested
    class UnsupportedWriteMethods {

        @ParameterizedTest
        @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
        void shouldNotExposeWriteEndpoints(String method) throws Exception {
            mockMvc.perform(request(HttpMethod.valueOf(method), TRAINING_GOALS_ENDPOINT))
                    .andExpect(status().isMethodNotAllowed());

            verifyNoInteractions(listTrainingGoalsUseCase);
        }
    }
}
