package com.ironcore.interfaces.rest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.user.usecase.changepassword.ChangePasswordCommand;
import com.ironcore.application.user.usecase.changepassword.ChangePasswordUseCase;
import com.ironcore.application.user.usecase.getauthenticateduser.GetAuthenticatedUserUseCase;
import com.ironcore.application.user.usecase.getauthenticateduser.UserProfileResult;
import com.ironcore.application.user.usecase.initialchangepassword.InitialChangePasswordCommand;
import com.ironcore.application.user.usecase.initialchangepassword.InitialChangePasswordUseCase;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.RawPassword;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.user.dto.ChangePasswordRequest;
import com.ironcore.interfaces.rest.user.dto.InitialChangePasswordRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final String USER_ENDPOINT = "/api/users/me/change-password";
    private static final String INITIAL_CHANGE_PASSWORD_ENDPOINT = "/api/users/change-initial-password";
    private static final String USER_PROFILE_ENDPOINT = "/api/users/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChangePasswordUseCase changePasswordUseCase;

    @MockitoBean
    private InitialChangePasswordUseCase initialChangePasswordUseCase;

    @MockitoBean
    private GetAuthenticatedUserUseCase getAuthenticatedUserUseCase;

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
        void shouldReturnNoContentWhenInitialPasswordIsChanged() throws Exception {
            InitialChangePasswordRequest request = new InitialChangePasswordRequest(
                    "renan@example.com",
                    "OldPassword",
                    "NewPassword",
                    "NewPassword"
            );
            InitialChangePasswordCommand command = new InitialChangePasswordCommand(
                    new Email("renan@example.com"),
                    new RawPassword("OldPassword"),
                    new RawPassword("NewPassword"),
                    new RawPassword("NewPassword")
            );

            mockMvc.perform(post(INITIAL_CHANGE_PASSWORD_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

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
    class SuccessfulGetAuthenticatedUser {

        @Test
        void shouldReturnAuthenticatedUser() throws Exception {
            UserProfileResult result = new UserProfileResult(
                    new UserId(1L),
                    new Email("renan@example.com"),
                    "Renan"
            );

            when(getAuthenticatedUserUseCase.execute(new UserId(1L))).thenReturn(result);

            mockMvc.perform(get("/api/users/me")
                    .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.email").value("renan@example.com"))
                    .andExpect(jsonPath("$.nickname").value("Renan"))
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());

            verify(getAuthenticatedUserUseCase).execute(new UserId(1L));
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
}
