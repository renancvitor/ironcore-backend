package com.ironcore.interfaces.rest.userbodymetrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.delete.DeleteUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.get.GetUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.latest.GetLatestUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsItemResult;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.list.ListUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsUseCase;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import com.ironcore.domain.userbodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.userbodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;
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

@WebMvcTest(UserBodyMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ListUserBodyMetricsControllerTest {

    private static final String BODY_METRICS_BASE_ENDPOINT = "/api/users/me/body-metrics";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListUserBodyMetricsUseCase listUserBodyMetricsUseCase;

    @MockitoBean
    private CreateUserBodyMetricsUseCase createUserBodyMetricsUseCase;

    @MockitoBean
    private UpdateUserBodyMetricsUseCase updateUserBodyMetricsUseCase;

    @MockitoBean
    private DeleteUserBodyMetricsUseCase deleteUserBodyMetricsUseCase;

    @MockitoBean
    private GetUserBodyMetricsUseCase getUserBodyMetricsUseCase;

    @MockitoBean
    private GetLatestUserBodyMetricsUseCase getLatestUserBodyMetricsUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private UserBodyMetricsRepository userBodyMetricsRepository;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    class SuccessfulList {

        @Test
        void shouldListUserBodyMetricsWithPagination() throws Exception {
            ListUserBodyMetricsCommand command = new ListUserBodyMetricsCommand(
                    new UserId(1L),
                    1,
                    2
            );

            ListUserBodyMetricsItemResult firstItem = new ListUserBodyMetricsItemResult(
                    new UserBodyMetricsId(3L),
                    LocalDateTime.of(2026, 6, 18, 10, 0),
                    new BodyWeightKg(65.0),
                    new BodyHeightCm(167.0),
                    "Medição mais recente."
            );

            ListUserBodyMetricsItemResult secondItem = new ListUserBodyMetricsItemResult(
                    new UserBodyMetricsId(2L),
                    LocalDateTime.of(2026, 6, 14, 10, 0),
                    new BodyWeightKg(66.0),
                    new BodyHeightCm(167.0),
                    "Medição anterior."
            );

            PageResult<ListUserBodyMetricsItemResult> page = new PageResult<>(
                    List.of(firstItem, secondItem),
                    1,
                    2,
                    5,
                    3,
                    false
            );

            when(listUserBodyMetricsUseCase.execute(command))
                    .thenReturn(new ListUserBodyMetricsResult(page));

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

            verify(listUserBodyMetricsUseCase).execute(command);
        }

        @Test
        void shouldUseDefaultPaginationWhenParametersAreNotProvided() throws Exception {
            ListUserBodyMetricsCommand command = new ListUserBodyMetricsCommand(
                    new UserId(1L),
                    0,
                    20
            );

            PageResult<ListUserBodyMetricsItemResult> page = new PageResult<>(
                    List.of(),
                    0,
                    20,
                    0,
                    0,
                    true
            );

            when(listUserBodyMetricsUseCase.execute(command))
                    .thenReturn(new ListUserBodyMetricsResult(page));

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

            verify(listUserBodyMetricsUseCase).execute(command);
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

            verify(listUserBodyMetricsUseCase, never()).execute(any());
        }
    }
}
