package com.ironcore.interfaces.rest.bodymetrics;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.delete.DeleteBodyMetricsCommand;
import com.ironcore.application.bodymetrics.delete.DeleteBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.progress.GetBodyMetricsProgressChangesUseCase;
import com.ironcore.application.bodymetrics.progress.GetBodyMetricsProgressChartUseCase;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsUseCase;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;
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

@WebMvcTest(BodyMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DeleteBodyMetricsControllerTest {

    private static final String BODY_METRICS_BASE_ENDPOINT = "/api/users/me/body-metrics";
    private static final String BODY_METRICS_ENDPOINT = BODY_METRICS_BASE_ENDPOINT + "/{id}";
    private static final Long BODY_METRICS_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeleteBodyMetricsUseCase deleteBodyMetricsUseCase;

    @MockitoBean
    private UpdateBodyMetricsUseCase updateBodyMetricsUseCase;

    @MockitoBean
    private CreateBodyMetricsUseCase createBodyMetricsUseCase;

    @MockitoBean
    private ListBodyMetricsUseCase listBodyMetricsUseCase;

    @MockitoBean
    private GetBodyMetricsUseCase getBodyMetricsUseCase;

    @MockitoBean
    private GetLatestBodyMetricsUseCase getLatestBodyMetricsUseCase;

    @MockitoBean
    private GetBodyMetricsProgressChartUseCase getBodyMetricsProgressChartUseCase;

    @MockitoBean
    private GetBodyMetricsProgressChangesUseCase getBodyMetricsProgressChangesUseCase;

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
            DeleteBodyMetricsCommand command = new DeleteBodyMetricsCommand(
                    new BodyMetricsId(1L),
                    new UserId(1L)
            );

            mockMvc.perform(delete(BODY_METRICS_ENDPOINT, BODY_METRICS_ID)
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());

            verify(deleteBodyMetricsUseCase).execute(command);
        }
    }

    @Nested
    class UnsuccessfulDelete {

        @Test
        void shouldReturnNotFoundWhenUserBodyMetricsDoesNotExist() throws Exception {
            DeleteBodyMetricsCommand command = new DeleteBodyMetricsCommand(
                    new BodyMetricsId(1L),
                    new UserId(1L)
            );

            doThrow(new ResourceNotFoundException("Métricas corporais não encontradas."))
                    .when(deleteBodyMetricsUseCase)
                    .execute(command);

            mockMvc.perform(delete(BODY_METRICS_ENDPOINT, BODY_METRICS_ID)
                            .with(authenticatedUser()))
                    .andExpect(status().isNotFound());

            verify(deleteBodyMetricsUseCase).execute(command);
        }
    }
}
