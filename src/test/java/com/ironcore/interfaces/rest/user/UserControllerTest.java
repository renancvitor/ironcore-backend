package com.ironcore.interfaces.rest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.user.usecase.ChangePasswordCommand;
import com.ironcore.application.user.usecase.ChangePasswordUseCase;
import com.ironcore.application.user.usecase.InitialChangePasswordResult;
import com.ironcore.application.user.usecase.InitialChangePasswordUseCase;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.RawPassword;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.user.dto.ChangePasswordRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final String USER_ENDPOINT = "/api/users/me/change-password";
    private static final String INITIAL_CHANGE_PASSWORD_ENDPOINT = "/api/users/me/change-initial-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChangePasswordUseCase changePasswordUseCase;

    @MockitoBean
    private InitialChangePasswordUseCase initialChangePasswordUseCase;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @Nested
    class SuccessfulChangePassword {

        @Test
        void shouldReturnNoContentWhenPasswordIsChanged() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "OldPassword",
                    "NewPassword",
                    "NewPassword"
            );

            mockMvc.perform(post(USER_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(changePasswordUseCase).execute(new ChangePasswordCommand(
                    new UserId(1L),
                    new RawPassword("OldPassword"),
                    new RawPassword("NewPassword"),
                    new RawPassword("NewPassword")
            ));
        }
    }

    @Nested
    class SuccessfulInitialChangePassword {

        @Test
        void shouldReturnNewAccessTokenWhenInitialPasswordIsChanged() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "OldPassword",
                    "NewPassword",
                    "NewPassword"
            );
            ChangePasswordCommand command = new ChangePasswordCommand(
                    new UserId(1L),
                    new RawPassword("OldPassword"),
                    new RawPassword("NewPassword"),
                    new RawPassword("NewPassword")
            );
            InitialChangePasswordResult result = new InitialChangePasswordResult(
                    "new-access-token",
                    "Bearer",
                    LocalDateTime.of(2026, 5, 24, 10, 0)
            );

            when(initialChangePasswordUseCase.execute(command))
                    .thenReturn(result);

            mockMvc.perform(post(INITIAL_CHANGE_PASSWORD_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresAt").value("2026-05-24T10:00:00"));

            verify(initialChangePasswordUseCase).execute(command);
        }
    }

    @Nested
    class RequestValidation {

        @Test
        void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "",
                    "",
                    ""
            );

            mockMvc.perform(post(USER_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.path").value(USER_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isArray())
                    .andExpect(jsonPath("$.fields[*].field", containsInAnyOrder(
                            "currentPassword",
                            "newPassword",
                            "confirmNewPassword"
                    )));

            verify(changePasswordUseCase, never()).execute(any());
        }
    }

    @Nested
    class PasswordFailures {

        @Test
        void shouldReturnUnauthorizedWhenCurrentPasswordIsInvalid() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "WrongOldPassword",
                    "NewPassword",
                    "NewPassword"
            );
            ChangePasswordCommand command = new ChangePasswordCommand(
                    new UserId(1L),
                    new RawPassword("WrongOldPassword"),
                    new RawPassword("NewPassword"),
                    new RawPassword("NewPassword")
            );

            doThrow(new InvalidCredentialsException("Senha atual inválida."))
                    .when(changePasswordUseCase)
                    .execute(command);

            mockMvc.perform(post(USER_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("Credenciais incorretas."))
                    .andExpect(jsonPath("$.path").value(USER_ENDPOINT));

            verify(changePasswordUseCase).execute(command);
        }
    }

    private RequestPostProcessor authenticatedUser() {
        return mockRequest -> {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    new UserId(1L),
                    new Email("renan@example.com"),
                    false
            );
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    List.of()
            );
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            return mockRequest;
        };
    }
}
