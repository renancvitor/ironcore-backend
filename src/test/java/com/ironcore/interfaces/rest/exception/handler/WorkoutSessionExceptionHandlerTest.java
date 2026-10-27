package com.ironcore.interfaces.rest.exception.handler;

import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkoutSessionExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionProbeController())
                .setControllerAdvice(new GlobalExceptionHandler(mock(ErrorLogPublisher.class)))
                .build();
    }

    @Test
    void shouldReturnConflictForExpectedSessionConflict() throws Exception {
        mockMvc.perform(get("/test/session/conflict"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldKeepDomainValidationAsBadRequest() throws Exception {
        mockMvc.perform(get("/test/session/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldKeepInfrastructureFailureAsInternalServerError() throws Exception {
        mockMvc.perform(get("/test/session/unavailable"))
                .andExpect(status().isInternalServerError());
    }

    @RestController
    private static class ExceptionProbeController {

        @GetMapping("/test/session/conflict")
        public void conflict() {
            throw new WorkoutSessionConflictException("Sessão foi alterada por outra operação.");
        }

        @GetMapping("/test/session/invalid")
        public void invalid() {
            throw new InvalidWorkoutSessionException("Estado inválido.");
        }

        @GetMapping("/test/session/unavailable")
        public void unavailable() {
            throw new PersistenceException("Banco indisponível.");
        }
    }
}
