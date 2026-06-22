package com.ironcore.interfaces.rest.userbodymetrics;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsUseCase;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserBodyMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DeleteUserBodyMetricsControllerTest {

    private static final String BODY_METRICS_BASE_ENDPOINT = "/api/users/me/body-metrics";
    private static final String BODY_METRICS_ENDPOINT = BODY_METRICS_BASE_ENDPOINT + "/{id}";
    private static final Long BODY_METRICS_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeleteUserBodyMetricsUseCase deleteUserBodyMetricsUseCase;

    @MockitoBean
    private UpdateUserBodyMetricsUseCase updateUserBodyMetricsUseCase;

    @MockitoBean
    private CreateUserBodyMetricsUseCase createUserBodyMetricsUseCase;

    @MockitoBean
    private ListUserBodyMetricsUseCase listUserBodyMetricsUseCase;

    @MockitoBean
    private GetUserBodyMetricsUseCase getUserBodyMetricsUseCase;

    @MockitoBean
    private GetLatestUserBodyMetricsUseCase getLatestUserBodyMetricsUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    class SuccessfulDelete {

        @Test
        void shouldDeleteUserBodyMetrics() throws Exception {
            DeleteUserBodyMetricsCommand command = new DeleteUserBodyMetricsCommand(
                    new UserBodyMetricsId(1L),
                    new UserId(1L)
            );

            mockMvc.perform(delete(BODY_METRICS_ENDPOINT, BODY_METRICS_ID)
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());

            verify(deleteUserBodyMetricsUseCase).execute(command);
        }
    }

    @Nested
    class UnsuccessfulDelete {

        @Test
        void shouldReturnNotFoundWhenUserBodyMetricsDoesNotExist() throws Exception {
            DeleteUserBodyMetricsCommand command = new DeleteUserBodyMetricsCommand(
                    new UserBodyMetricsId(1L),
                    new UserId(1L)
            );

            doThrow(new ResourceNotFoundException("Métricas corporais não encontradas."))
                    .when(deleteUserBodyMetricsUseCase)
                    .execute(command);

            mockMvc.perform(delete(BODY_METRICS_ENDPOINT, BODY_METRICS_ID)
                            .with(authenticatedUser()))
                    .andExpect(status().isNotFound());

            verify(deleteUserBodyMetricsUseCase).execute(command);
        }
    }
}
