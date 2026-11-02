package com.ironcore.interfaces.rest.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.IroncoreBackendApplication;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.infrastructure.persistence.auth.repository.AuthSessionJpaRepository;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import com.ironcore.infrastructure.security.jwt.JwtTokenProperties;
import com.ironcore.interfaces.rest.auth.dto.LoginRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = IroncoreBackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "ironcore.bootstrap.single-user.enabled=false"
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityIntegrationTest {
    @Autowired
    private AuthSessionJpaRepository authSessions;


    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String LOGOUT_ENDPOINT = "/api/auth/logout";
    private static final String AUTHENTICATED_USER_ENDPOINT = "/api/users/me";
    private static final String AUTHENTICATED_USER_PERSON_ENDPOINT = "/api/users/me/person";

    private static final String EMAIL = "renan@example.com";
    private static final String RAW_PASSWORD = "StrongPass123@";

    private static final Long PERSON_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PersonJpaRepository personJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProperties jwtTokenProperties;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        authSessions.deleteAll();
        userJpaRepository.deleteAll();
        personJpaRepository.deleteAll();
        userJpaRepository.save(activeUser());
    }

    @AfterEach
    void tearDown() {
        authSessions.deleteAll();
        userJpaRepository.deleteAll();
        personJpaRepository.deleteAll();
    }

    @Nested
    class Login {

        @Test
        void shouldAuthenticateValidCredentialsAndReturnAccessTokenCookie() throws Exception {
            LoginRequest request = new LoginRequest(EMAIL, RAW_PASSWORD);

            mockMvc.perform(post(LOGIN_ENDPOINT).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(containsString("access_token="))))
                    .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(containsString("Path=/"))))
                    .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(containsString("Secure"))))
                    .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(containsString("HttpOnly"))))
                    .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(containsString("SameSite=None"))))
                    .andExpect(cookie().exists("access_token"))
                    .andExpect(jsonPath("$.accessToken").doesNotExist())
                    .andExpect(jsonPath("$.tokenType").doesNotExist())
                    .andExpect(jsonPath("$.expiresAt").isString())
                    .andExpect(jsonPath("$.userId").isNumber())
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andExpect(jsonPath("$.nickname").value("Renan"))
                    .andExpect(jsonPath("$.mustChangePassword").value(false))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        @Test
        void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
            LoginRequest request = new LoginRequest(EMAIL, "WrongPassword123@");

            mockMvc.perform(post(LOGIN_ENDPOINT).with(csrf())
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
    class ProtectedRouteAccess {

        @Test
        void shouldBlockProtectedRouteWhenAccessTokenCookieIsMissing() throws Exception {
            mockMvc.perform(get(AUTHENTICATED_USER_ENDPOINT)
                            .header(HttpHeaders.ORIGIN, "http://localhost:4200"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                    .andExpect(jsonPath("$.timestamp").isString())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("Autenticação necessária."))
                    .andExpect(jsonPath("$.path").value(AUTHENTICATED_USER_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isEmpty());
        }

        @Test
        void shouldBlockPersonRouteWhenAccessTokenCookieIsMissing() throws Exception {
            mockMvc.perform(get(AUTHENTICATED_USER_PERSON_ENDPOINT)
                            .header(HttpHeaders.ORIGIN, "http://localhost:4200"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                    .andExpect(jsonPath("$.timestamp").isString())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("Autenticação necessária."))
                    .andExpect(jsonPath("$.path").value(AUTHENTICATED_USER_PERSON_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isEmpty());
        }

        @Test
        void shouldBlockProtectedRouteWhenAccessTokenCookieIsInvalid() throws Exception {
            assertInvalidTokenResponse(AUTHENTICATED_USER_ENDPOINT, invalidAccessTokenCookie());
        }

        @Test
        void shouldBlockPersonRouteWhenAccessTokenCookieIsInvalid() throws Exception {
            assertInvalidTokenResponse(AUTHENTICATED_USER_PERSON_ENDPOINT, invalidAccessTokenCookie());
        }

        @Test
        void shouldBlockProtectedRouteWhenAccessTokenCookieIsExpired() throws Exception {
            assertInvalidTokenResponse(AUTHENTICATED_USER_ENDPOINT, expiredAccessTokenCookie());
        }

        @Test
        void shouldBlockPersonRouteWhenAccessTokenCookieIsExpired() throws Exception {
            assertInvalidTokenResponse(AUTHENTICATED_USER_PERSON_ENDPOINT, expiredAccessTokenCookie());
        }

        @Test
        void shouldAllowProtectedRouteWhenAccessTokenCookieIsValid() throws Exception {
            Cookie accessTokenCookie = loginAndGetAccessTokenCookie();

            mockMvc.perform(get(AUTHENTICATED_USER_ENDPOINT)
                            .cookie(accessTokenCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").isNumber())
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andExpect(jsonPath("$.nickname").value("Renan"))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        @Test
        void shouldReturnPersonLinkedToAuthenticatedUser() throws Exception {
            Cookie accessTokenCookie = loginAndGetAccessTokenCookie();

            mockMvc.perform(get(AUTHENTICATED_USER_PERSON_ENDPOINT)
                            .cookie(accessTokenCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.personId").isNumber())
                    .andExpect(jsonPath("$.name").value("Renan"))
                    .andExpect(jsonPath("$.sex").value("MALE"))
                    .andExpect(jsonPath("$.birthDate").value("1994-04-09"))
                    .andExpect(jsonPath("$.createdAt").doesNotExist())
                    .andExpect(jsonPath("$.updatedAt").doesNotExist());
        }

    }

    @Nested
    class OpenApiContract {

        @Test
        void shouldDocumentAuthenticatedUserPersonEndpoint() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paths['/api/users/me/person'].get").exists())
                    .andExpect(jsonPath("$.paths['/api/users/me/person'].get.responses['200']").exists())
                    .andExpect(jsonPath("$.components.schemas.PersonResponse").exists())
                    .andExpect(jsonPath("$.components.schemas.PersonResponse.properties.createdAt").doesNotExist())
                    .andExpect(jsonPath("$.components.schemas.PersonResponse.properties.updatedAt").doesNotExist());
        }
    }

    @Nested
    class JwtAuthentication {

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

            mockMvc.perform(post(LOGOUT_ENDPOINT).with(csrf())
                            .cookie(accessTokenCookie))
                    .andExpect(status().isNoContent())
                    .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(containsString("access_token="))))
                    .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(containsString("Max-Age=0"))))
                    .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(containsString("Path=/"))))
                    .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(containsString("Secure"))))
                    .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(containsString("HttpOnly"))))
                    .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(containsString("SameSite=None"))));
        }
    }

    @Test
    void refreshWorksWithExpiredAccessTokenAndDoesNotExposeCredentialsInJson() throws Exception {
        Cookie refreshToken = loginAndGetRefreshTokenCookie();
        var result = mockMvc.perform(post("/api/auth/refresh").servletPath("/api/auth/refresh").with(csrf())
                        .cookie(expiredAccessTokenCookie(), refreshToken))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("refresh_token", true))
                .andExpect(cookie().path("refresh_token", "/api/auth"))
                .andExpect(cookie().attribute("refresh_token", "SameSite", "None"))
                .andExpect(cookie().exists("access_token"))
                .andExpect(jsonPath("$.expiresAt").isString())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        Cookie rotated = result.getResponse().getCookie("refresh_token");
        assertThat(rotated.getValue()).isNotEqualTo(refreshToken.getValue());
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
        mockMvc.perform(get(AUTHENTICATED_USER_ENDPOINT).cookie(result.getResponse().getCookie("access_token")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(rotated)).andExpect(status().isOk());
    }

    @Test
    void refreshRequiresCsrfAndAValidCredential() throws Exception {
        Cookie refreshToken = loginAndGetRefreshTokenCookie();
        mockMvc.perform(post("/api/auth/refresh").cookie(refreshToken)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/auth/refresh").with(csrf().useInvalidToken()).cookie(refreshToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/auth/refresh").with(csrf())).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(new Cookie("refresh_token", "malformed")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(new Cookie("refresh_token", "x".repeat(43))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(refreshToken)).andExpect(status().isOk());
    }

    @Test
    void logoutWithExpiredAccessRevokesOnlyTheSessionIdentifiedByRefresh() throws Exception {
        Cookie first = loginAndGetRefreshTokenCookie();
        Cookie second = loginAndGetRefreshTokenCookie();
        mockMvc.perform(post(LOGOUT_ENDPOINT).servletPath(LOGOUT_ENDPOINT).with(csrf()).cookie(expiredAccessTokenCookie(), first))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().maxAge("refresh_token", 0))
                .andExpect(cookie().path("refresh_token", "/api/auth"));
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(first)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(second)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"inactive", "initial-password", "revoked", "expired", "legacy"})
    void refreshRejectsSessionsAndUsersThatCannotAuthenticate(String state) throws Exception {
        Cookie refreshToken = loginAndGetRefreshTokenCookie();
        switch (state) {
            case "inactive" -> jdbc.update("update users set active = false");
            case "initial-password" -> jdbc.update("update users set must_change_password = true");
            case "revoked" -> jdbc.update("update auth_sessions set revoked_at = current_timestamp");
            case "expired" -> jdbc.update("update auth_sessions set created_at = ?, expires_at = ?",
                    Instant.now().minusSeconds(120), Instant.now().minusSeconds(60));
            case "legacy" -> jdbc.update("update auth_sessions set refresh_token_hash = null");
            default -> throw new AssertionError(state);
        }
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(refreshToken)).andExpect(status().isUnauthorized());
    }

    @Test
    void passwordChangeReplacesBothCookiesAndRevokesRefreshOnOtherDevices() throws Exception {
        Cookie otherDevice = loginAndGetRefreshTokenCookie();
        var loggedIn = mockMvc.perform(post(LOGIN_ENDPOINT).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(EMAIL, RAW_PASSWORD))))
                .andExpect(status().isOk()).andReturn();
        var changed = mockMvc.perform(post("/api/users/me/change-password").with(csrf())
                        .cookie(loggedIn.getResponse().getCookie("access_token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"StrongPass123@\",\"newPassword\":\"NewPass123@\",\"confirmNewPassword\":\"NewPass123@\"}"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(otherDevice)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(loggedIn.getResponse().getCookie("refresh_token")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(changed.getResponse().getCookie("refresh_token")))
                .andExpect(status().isOk());
    }

    @Test
    void sessionsCreatedBeforeRefreshSupportKeepTheirAccessAndCanBeRevoked() throws Exception {
        Cookie accessToken = loginAndGetAccessTokenCookie();
        jdbc.update("update auth_sessions set refresh_token_hash = null");
        mockMvc.perform(get(AUTHENTICATED_USER_ENDPOINT).cookie(accessToken)).andExpect(status().isOk());
        mockMvc.perform(post(LOGOUT_ENDPOINT).servletPath(LOGOUT_ENDPOINT).with(csrf()).cookie(accessToken))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(AUTHENTICATED_USER_ENDPOINT).cookie(accessToken)).andExpect(status().isUnauthorized());
    }

    private Cookie loginAndGetRefreshTokenCookie() throws Exception {
        var result = mockMvc.perform(post(LOGIN_ENDPOINT).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(EMAIL, RAW_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("refresh_token", true))
                .andReturn();
        return result.getResponse().getCookie("refresh_token");
    }

    private Cookie loginAndGetAccessTokenCookie() throws Exception {
        LoginRequest request = new LoginRequest(EMAIL, RAW_PASSWORD);

        MvcResult result = mockMvc.perform(post(LOGIN_ENDPOINT).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie("access_token");

        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getValue()).isNotBlank();

        return accessTokenCookie;
    }

    private Cookie invalidAccessTokenCookie() {
        return new Cookie("access_token", "invalid-token");
    }

    private Cookie expiredAccessTokenCookie() {
        String token = JWT.create()
                .withIssuer(jwtTokenProperties.getIssuer())
                .withSubject("1")
                .withClaim("email", EMAIL)
                .withClaim("mustChangePassword", false)
                .withExpiresAt(Date.from(Instant.now().minusSeconds(60)))
                .sign(Algorithm.HMAC256(jwtTokenProperties.getSecret()));

        return new Cookie("access_token", token);
    }

    private void assertInvalidTokenResponse(String endpoint, Cookie accessTokenCookie) throws Exception {
        mockMvc.perform(get(endpoint)
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Token de autenticação inválido ou expirado."))
                .andExpect(jsonPath("$.path").value(endpoint))
                .andExpect(jsonPath("$.fields").isEmpty());
    }

    private UserEntity activeUser() {
        LocalDateTime now = LocalDateTime.now();

        return new UserEntity(
                null,
                "Renan",
                activePerson(),
                EMAIL,
                passwordEncoder.encode(RAW_PASSWORD),
                false,
                true,
                now,
                now
        );
    }

    private PersonEntity activePerson() {
        LocalDateTime now = LocalDateTime.now();

        return personJpaRepository.save(new PersonEntity(
                null,
                "Renan",
                SexType.MALE,
                LocalDate.of(1994, 4, 9),
                now,
                null
        ));
    }
}
