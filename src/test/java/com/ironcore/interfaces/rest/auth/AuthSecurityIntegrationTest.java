package com.ironcore.interfaces.rest.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.IroncoreBackendApplication;
import com.ironcore.domain.user.enums.SexType;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import com.ironcore.interfaces.rest.auth.dto.LoginRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = IroncoreBackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String LOGOUT_ENDPOINT = "/api/auth/logout";
    private static final String AUTHENTICATED_USER_ENDPOINT = "/api/users/me";

    private static final String EMAIL = "renan@example.com";
    private static final String RAW_PASSWORD = "StrongPass123@";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userJpaRepository.deleteAll();
        userJpaRepository.save(activeUser());
    }

    @AfterEach
    void tearDown() {
        userJpaRepository.deleteAll();
    }

    @Nested
    class Login {

        @Test
        void shouldAuthenticateValidCredentialsAndReturnAccessTokenCookie() throws Exception {
            LoginRequest request = new LoginRequest(EMAIL, RAW_PASSWORD);

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("access_token=")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=None")))
                    .andExpect(cookie().exists("access_token"))
                    .andExpect(jsonPath("$.accessToken").isString())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresAt").isString())
                    .andExpect(jsonPath("$.userId").isNumber())
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andExpect(jsonPath("$.name").value("Renan"))
                    .andExpect(jsonPath("$.mustChangePassword").value(false))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        @Test
        void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
            LoginRequest request = new LoginRequest(EMAIL, "WrongPassword123@");

            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("Credenciais incorretas."))
                    .andExpect(jsonPath("$.path").value(LOGIN_ENDPOINT))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }
    }

    @Nested
    class ProtectedRoutes {

        @Test
        void shouldBlockProtectedRouteWhenAccessTokenCookieIsMissing() throws Exception {
            mockMvc.perform(get(AUTHENTICATED_USER_ENDPOINT))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldAllowProtectedRouteWhenAccessTokenCookieIsValid() throws Exception {
            Cookie accessTokenCookie = loginAndGetAccessTokenCookie();

            mockMvc.perform(get(AUTHENTICATED_USER_ENDPOINT)
                            .cookie(accessTokenCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").isNumber())
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andExpect(jsonPath("$.name").value("Renan"))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        @Test
        void shouldPopulateAuthenticationContextThroughJwtFilterInIntegratedFlow() throws Exception {
            Cookie accessTokenCookie = loginAndGetAccessTokenCookie();

            mockMvc.perform(get(AUTHENTICATED_USER_ENDPOINT)
                            .cookie(accessTokenCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(EMAIL));
        }
    }

    @Nested
    class Logout {

        @Test
        void shouldLogoutAndExpireAccessTokenCookie() throws Exception {
            Cookie accessTokenCookie = loginAndGetAccessTokenCookie();

            mockMvc.perform(post(LOGOUT_ENDPOINT)
                            .cookie(accessTokenCookie))
                    .andExpect(status().isNoContent())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("access_token=")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=None")));
        }
    }

    private Cookie loginAndGetAccessTokenCookie() throws Exception {
        LoginRequest request = new LoginRequest(EMAIL, RAW_PASSWORD);

        MvcResult result = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie("access_token");

        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getValue()).isNotBlank();

        return accessTokenCookie;
    }

    private UserEntity activeUser() {
        LocalDateTime now = LocalDateTime.now();

        return new UserEntity(
                null,
                "Renan",
                EMAIL,
                passwordEncoder.encode(RAW_PASSWORD),
                SexType.MALE,
                false,
                true,
                now,
                now
        );
    }
}
