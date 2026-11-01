package com.ironcore.interfaces.rest.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.config.import=",
        "ironcore.bootstrap.single-user.enabled=false",
        "ironcore.bootstrap.person.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
class SwaggerCsrfIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserJpaRepository users;
    @Autowired PersonJpaRepository persons;
    @Autowired PasswordEncoder encoder;

    @Test
    void swaggerInitializerMustObtainAndSendMaskedCsrfTokens() throws Exception {
        String initializer = mvc.perform(get("/swagger-ui/swagger-initializer.js"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(initializer).contains("requestInterceptor", "auth/csrf", "csrf.token");
        assertThat(initializer).doesNotContain("document.cookie");
        Files.writeString(Path.of("target/swagger-csrf-initializer.js"), initializer);
    }

    @Test
    void cookieAloneAndNativeRawCookieHeaderCannotAuthorizeSwaggerWrites() throws Exception {
        var response = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk()).andReturn().getResponse();
        var cookie = response.getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        var token = mapper.readTree(response.getContentAsString());
        String header = token.get("headerName").asText();
        String masked = token.get("token").asText();
        assertThat(masked).isNotEqualTo(cookie.getValue());
        mvc.perform(post("/api/auth/logout").cookie(cookie))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout").cookie(cookie).header(header, cookie.getValue()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout").cookie(cookie).header(header, masked))
                .andExpect(status().isNoContent());
    }

    @Test
    void maskedTokenIsRenewedAcrossSwaggerLoginWriteLogoutAndRelogin() throws Exception {
        users.deleteAll();
        persons.deleteAll();
        var now = LocalDateTime.now();
        var person = persons.save(new PersonEntity(null, "Swagger Test", SexType.MALE,
                LocalDate.of(1990, 1, 1), now, null));
        users.save(new UserEntity(null, "Swagger", person, "swagger@ironcore.test",
                encoder.encode("StrongPass123@"), false, true, now, now));
        Map<String, Cookie> cookies = new HashMap<>();
        String loginBody = "{\"email\":\"swagger@ironcore.test\",\"password\":\"StrongPass123@\"}";
        try {
            String beforeLogin = swaggerWrite(cookies, HttpMethod.POST, "/api/auth/login", loginBody, 200);
            assertThat(cookies).containsKey("access_token").doesNotContainKey("XSRF-TOKEN");
            String afterLogin = swaggerWrite(cookies, HttpMethod.PUT, "/api/users/me/change-nickname",
                    "{\"nickname\":\"Swagger Updated\"}", 200);
            assertThat(afterLogin).isNotEqualTo(beforeLogin);
            String beforeLogout = swaggerWrite(cookies, HttpMethod.POST, "/api/auth/logout", "{}", 204);
            assertThat(beforeLogout).isNotBlank();
            assertThat(cookies).doesNotContainKeys("access_token", "XSRF-TOKEN");
            String afterLogout = swaggerWrite(cookies, HttpMethod.POST, "/api/auth/login", loginBody, 200);
            assertThat(afterLogout).isNotEqualTo(beforeLogout);
        } finally {
            users.deleteAll();
            persons.deleteAll();
        }
    }

    private String swaggerWrite(Map<String, Cookie> cookies, HttpMethod method, String path,
                                String body, int expectedStatus) throws Exception {
        var tokenRequest = get("/api/auth/csrf").servletPath("/api/auth/csrf");
        if (!cookies.isEmpty()) tokenRequest.cookie(cookies.values().toArray(Cookie[]::new));
        var tokenResponse = mvc.perform(tokenRequest)
                .andExpect(status().isOk()).andReturn().getResponse();
        updateCookies(cookies, tokenResponse);
        var token = mapper.readTree(tokenResponse.getContentAsString());
        String raw = cookies.get("XSRF-TOKEN").getValue();
        assertThat(token.get("token").asText()).isNotEqualTo(raw);
        var response = mvc.perform(request(method, path).servletPath(path)
                        .cookie(cookies.values().toArray(Cookie[]::new))
                        .header(token.get("headerName").asText(), token.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus)).andReturn().getResponse();
        updateCookies(cookies, response);
        return raw;
    }

    private void updateCookies(Map<String, Cookie> cookies, MockHttpServletResponse response) {
        for (Cookie cookie : response.getCookies()) {
            if (cookie.getMaxAge() == 0) cookies.remove(cookie.getName());
            else cookies.put(cookie.getName(), cookie);
        }
    }
}
