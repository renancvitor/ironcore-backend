package com.ironcore.interfaces.rest.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.auth.usecase.LoginCommand;
import com.ironcore.application.auth.usecase.LoginResult;
import com.ironcore.application.auth.usecase.LoginUseCase;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.auth.dto.LoginRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private static final String LOGIN_ENDPOINT = "/api/auth/login";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    class Login {

        @Test
        void shouldReturnAuthenticationDataWhenCredentialsAreValid() throws Exception {
            LoginRequest request = new LoginRequest("renan@example.com", "StrongPass123@");
            LocalDateTime expiresAt = LocalDateTime.of(2026, 5, 24, 15, 17, 30);
            LoginResult result = new LoginResult(
                    "access-token",
                    "Bearer",
                    expiresAt,
                    new UserId(1L),
                    new Email("renan@example.com"),
                    "Renan",
                    true
            );

            when(loginUseCase.execute(new LoginCommand(new Email("renan@example.com"), "StrongPass123@")))
                    .thenReturn(result);

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("access_token=access-token")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=None")))
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresAt").value(expiresAt.toString()))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.email").value("renan@example.com"))
                    .andExpect(jsonPath("$.name").value("Renan"))
                    .andExpect(jsonPath("$.mustChangePassword").value(true))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());

            ArgumentCaptor<LoginCommand> commandCaptor = ArgumentCaptor.forClass(LoginCommand.class);

            verify(loginUseCase).execute(commandCaptor.capture());

            LoginCommand command = commandCaptor.getValue();

            assertThat(command.email()).isEqualTo(new Email("renan@example.com"));
            assertThat(command.rawPassword()).isEqualTo("StrongPass123@");
        }

        @Test
        void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
            LoginRequest request = new LoginRequest("invalid-email", "");

            mockMvc.perform(post(LOGIN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.path").value(LOGIN_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isArray())
                    .andExpect(jsonPath("$.fields[*].field", containsInAnyOrder("email", "password")));

            verify(loginUseCase, never()).execute(any());
        }
    }
}
