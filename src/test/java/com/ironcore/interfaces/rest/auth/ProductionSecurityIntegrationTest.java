package com.ironcore.interfaces.rest.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import com.ironcore.domain.person.enums.SexType;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "security.cors.allowed-origin-patterns=https://app.ironcore.test",
        "ironcore.bootstrap.single-user.enabled=false",
        "ironcore.bootstrap.person.enabled=false",
        "spring.config.import="
})
@AutoConfigureMockMvc
@ActiveProfiles({"prod", "test"})
class ProductionSecurityIntegrationTest {
    private static final String ORIGIN = "https://app.ironcore.test";
    private static final String EMAIL = "security@ironcore.test";
    private static final String PASSWORD = "StrongPass123@";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserJpaRepository users;
    @Autowired PersonJpaRepository persons;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        users.deleteAll();
        persons.deleteAll();
        var now = LocalDateTime.now();
        var person = persons.save(new PersonEntity(null, "Security Test", SexType.MALE,
                LocalDate.of(1990, 1, 1), now, null));
        users.save(new UserEntity(null, "Security", person, EMAIL, encoder.encode(PASSWORD),
                false, true, now, now));
    }

    @Test
    void allowsConfiguredCorsPreflightWithCredentialsAndCsrfHeader() throws Exception {
        mvc.perform(options("/api/auth/login").header("Origin", ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-xsrf-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("x-xsrf-token")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://evil.test", "http://localhost:4200", "https://app.ironcore.test.evil.test"})
    void rejectsUnconfiguredOrigins(String origin) throws Exception {
        mvc.perform(get("/api/auth/csrf").header("Origin", origin))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/login", "/api/auth/logout", "/api/users/change-initial-password"})
    void rejectsMissingAndInvalidCsrfOnPublicWrites(String path) throws Exception {
        mvc.perform(post(path).header("Origin", ORIGIN).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        var csrf = csrf();
        mvc.perform(post(path).cookie(csrf.cookie()).header(csrf.header(), "invalid")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post(path).header(csrf.header(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void supportsLoginSessionRestorationProtectedWritesAndLogoutWithRealCsrfTokens() throws Exception {
        var loginCsrf = csrf();
        var login = login(PASSWORD, loginCsrf);
        Cookie access = login.getResponse().getCookie("access_token");
        assertThat(access).isNotNull();
        assertThat(access.isHttpOnly()).isTrue();
        assertThat(access.getSecure()).isTrue();
        assertThat(access.getAttribute("SameSite")).isEqualTo("Lax");
        assertThat(login.getResponse().getCookie("XSRF-TOKEN").getMaxAge()).isZero();
        mvc.perform(get("/api/users/me").cookie(access).header("Origin", ORIGIN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(EMAIL));

        var csrf = csrf();
        assertThat(csrf.cookie().getValue()).isNotEqualTo(loginCsrf.cookie().getValue());
        mvc.perform(put("/api/users/me/change-nickname").cookie(access)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"Updated\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/users/me/change-nickname").cookie(access, csrf.cookie())
                        .header(csrf.header(), "invalid").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"Updated\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/users/me/change-nickname").cookie(access, csrf.cookie())
                        .header(csrf.header(), csrf.token()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"Updated\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nickname").value("Updated"));
        var logout = mvc.perform(post("/api/auth/logout").cookie(access, csrf.cookie())
                        .header(csrf.header(), csrf.token()))
                .andExpect(status().isNoContent()).andReturn();
        assertThat(logout.getResponse().getCookie("access_token").getMaxAge()).isZero();
        assertThat(logout.getResponse().getCookie("XSRF-TOKEN").getMaxAge()).isZero();
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        login(PASSWORD, csrf());
    }

    @Test
    void supportsInitialPasswordChangeBeforeLogin() throws Exception {
        var user = users.findAll().getFirst();
        user.setMustChangePassword(true);
        users.save(user);
        var csrf = csrf();
        String newPassword = "ChangedPass456@";
        mvc.perform(post("/api/users/change-initial-password").cookie(csrf.cookie())
                        .header(csrf.header(), csrf.token()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", EMAIL, "currentPassword", PASSWORD,
                                "newPassword", newPassword, "confirmNewPassword", newPassword))))
                .andExpect(status().isNoContent());
        login(newPassword, csrf()).getResponse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/v3/api-docs", "/swagger-ui/index.html", "/swagger-ui.html"})
    void doesNotExposeOpenApiInProduction(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isNotFound());
    }

    private MvcResult login(String password, Token csrf) throws Exception {
        return mvc.perform(post("/api/auth/login").header("Origin", ORIGIN).cookie(csrf.cookie())
                        .header(csrf.header(), csrf.token()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", EMAIL, "password", password))))
                .andExpect(status().isOk()).andReturn();
    }

    private Token csrf() throws Exception {
        var result = mvc.perform(get("/api/auth/csrf").servletPath("/api/auth/csrf").header("Origin", ORIGIN)
                        .cookie(new Cookie("access_token", "expired-token")))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store"))).andReturn();
        var cookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getAttribute("SameSite")).isEqualTo("Lax");
        assertThat(cookie.getDomain()).isNull();
        var body = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("token").asText()).isNotEqualTo(cookie.getValue());
        return new Token(cookie, body.get("headerName").asText(), body.get("token").asText());
    }

    private record Token(Cookie cookie, String header, String token) { }
}
