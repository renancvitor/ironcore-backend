package com.ironcore.interfaces.rest.bodymetrics;

import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.delete.DeleteBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.get.GetBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.latest.GetLatestBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsCommand;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsItemResult;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsResult;
import com.ironcore.application.bodymetrics.list.ListBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.progress.GetBodyMetricsProgressChangesUseCase;
import com.ironcore.application.bodymetrics.progress.GetBodyMetricsProgressChartUseCase;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsUseCase;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BodyMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ListBodyMetricsControllerTest {

    private static final String BODY_METRICS_BASE_ENDPOINT = "/api/users/me/body-metrics";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListBodyMetricsUseCase listBodyMetricsUseCase;

    @MockitoBean
    private CreateBodyMetricsUseCase createBodyMetricsUseCase;

    @MockitoBean
    private UpdateBodyMetricsUseCase updateBodyMetricsUseCase;

    @MockitoBean
    private DeleteBodyMetricsUseCase deleteBodyMetricsUseCase;

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
    private BodyMetricsRepository bodyMetricsRepository;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    class SuccessfulList {

        @Test
        void shouldListUserBodyMetricsWithPagination() throws Exception {
            ListBodyMetricsCommand command = new ListBodyMetricsCommand(
                    new UserId(1L),
                    1,
                    2
            );

            ListBodyMetricsItemResult firstItem = new ListBodyMetricsItemResult(
                    new BodyMetricsId(3L),
                    LocalDateTime.of(2026, 6, 18, 10, 0),
                    new BodyWeightKg(65.0),
                    new BodyHeightCm(167.0),
                    "Medição mais recente."
            );

            ListBodyMetricsItemResult secondItem = new ListBodyMetricsItemResult(
                    new BodyMetricsId(2L),
                    LocalDateTime.of(2026, 6, 14, 10, 0),
                    new BodyWeightKg(66.0),
                    new BodyHeightCm(167.0),
                    "Medição anterior."
            );

            PageResult<ListBodyMetricsItemResult> page = new PageResult<>(
                    List.of(firstItem, secondItem),
                    1,
                    2,
                    5,
                    3,
                    false
            );

            when(listBodyMetricsUseCase.execute(command))
                    .thenReturn(new ListBodyMetricsResult(page));

            mockMvc.perform(get(BODY_METRICS_BASE_ENDPOINT)
                            .with(authenticatedUser())
                            .param("page", "1")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.metrics.page").value(1))
                    .andExpect(jsonPath("$.metrics.size").value(2))
                    .andExpect(jsonPath("$.metrics.totalElements").value(5))
                    .andExpect(jsonPath("$.metrics.totalPages").value(3))
                    .andExpect(jsonPath("$.metrics.last").value(false))
                    .andExpect(jsonPath("$.metrics.content").isArray())
                    .andExpect(jsonPath("$.metrics.content.length()").value(2))
                    .andExpect(jsonPath("$.metrics.content[0].id").value(3L))
                    .andExpect(jsonPath("$.metrics.content[0].weightKg").value(65.0))
                    .andExpect(jsonPath("$.metrics.content[0].heightCm").value(167.0))
                    .andExpect(jsonPath("$.metrics.content[0].notes").value("Medição mais recente."))
                    .andExpect(jsonPath("$.metrics.content[1].id").value(2L));

            verify(listBodyMetricsUseCase).execute(command);
        }

        @Test
        void shouldUseDefaultPaginationWhenParametersAreNotProvided() throws Exception {
            ListBodyMetricsCommand command = new ListBodyMetricsCommand(
                    new UserId(1L),
                    0,
                    20
            );

            PageResult<ListBodyMetricsItemResult> page = new PageResult<>(
                    List.of(),
                    0,
                    20,
                    0,
                    0,
                    true
            );

            when(listBodyMetricsUseCase.execute(command))
                    .thenReturn(new ListBodyMetricsResult(page));

            mockMvc.perform(get(BODY_METRICS_BASE_ENDPOINT)
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.metrics.page").value(0))
                    .andExpect(jsonPath("$.metrics.size").value(20))
                    .andExpect(jsonPath("$.metrics.totalElements").value(0))
                    .andExpect(jsonPath("$.metrics.totalPages").value(0))
                    .andExpect(jsonPath("$.metrics.last").value(true))
                    .andExpect(jsonPath("$.metrics.content").isArray())
                    .andExpect(jsonPath("$.metrics.content").isEmpty());

            verify(listBodyMetricsUseCase).execute(command);
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
            mockMvc.perform(get(BODY_METRICS_BASE_ENDPOINT)
                            .with(authenticatedUser())
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message")
                            .value("Erro de validação nos parâmetros da requisição"))
                    .andExpect(jsonPath("$.path")
                            .value(BODY_METRICS_BASE_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isArray());

            verify(listBodyMetricsUseCase, never()).execute(any());
        }
    }
}
