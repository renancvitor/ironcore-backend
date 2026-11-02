package com.ironcore.interfaces.rest.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.auth.port.AuthenticationAttemptStore;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.infrastructure.persistence.auth.repository.AuthSessionJpaRepository;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.config.import=", "ironcore.bootstrap.single-user.enabled=false",
        "ironcore.bootstrap.person.enabled=false", "security.auth-rate-limit.account-limit=3",
        "security.auth-rate-limit.ip-limit-per-minute=5"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AuthenticationHardeningIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "3");
    }

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private AuthenticationAttemptStore attempts;
    @Autowired private UserJpaRepository users;
    @Autowired private PersonJpaRepository persons;
    @Autowired private AuthSessionJpaRepository sessions;
    @Autowired private PasswordEncoder encoder;

    private static final String PASSWORD = "CurrentPassword123@";
    private String email;
    private Long userId;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM auth_attempt_limits");
        sessions.deleteAll();
        users.deleteAll();
        persons.deleteAll();
        email = UUID.randomUUID() + "@example.com";
        var now = LocalDateTime.now();
        var person = persons.save(new PersonEntity(null, "Auth", SexType.MALE, LocalDate.of(1990, 1, 1), now, null));
        userId = users.save(new UserEntity(null, "Auth", person, email, encoder.encode(PASSWORD), false, true, now, null)).getId();
    }

    @ParameterizedTest
    @ValueSource(strings = {"absent", "active", "inactive", "initial"})
    void wrongCredentialsDoNotRevealAccountState(String state) throws Exception {
        if (state.equals("absent")) {
            email = "absent@example.com";
        } else if (state.equals("inactive")) {
            jdbc.update("UPDATE users SET active = false WHERE id = ?", userId);
        } else if (state.equals("initial")) {
            jdbc.update("UPDATE users SET must_change_password = true WHERE id = ?", userId);
        }
        mvc.perform(login("IncorrectPassword123@"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Credenciais incorretas."));
        mvc.perform(write("/api/users/change-initial-password", Map.of("email", email,
                        "currentPassword", "IncorrectPassword123@", "newPassword", "NewPassword123@", "confirmNewPassword", "Mismatch123@")))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Credenciais incorretas."));
    }

    @Test
    void failedLoginsConsumeTheLimitEvenWhenAuthenticationRollsBack() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(login("IncorrectPassword123@")).andExpect(status().isUnauthorized());
        }
        mvc.perform(login(PASSWORD)).andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").value("Muitas tentativas. Aguarde antes de tentar novamente."));
        assertThat(sessions.count()).isZero();
    }

    @Test
    void initialPasswordChangeSharesTheLoginLimitForNormalizedEmail() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(login("IncorrectPassword123@")).andExpect(status().isUnauthorized());
        }
        mvc.perform(write("/api/users/change-initial-password", Map.of("email", email.toUpperCase(java.util.Locale.ROOT),
                        "currentPassword", PASSWORD, "newPassword", "NewPassword123@", "confirmNewPassword", "NewPassword123@")))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void ipLimitCannotBeBypassedByChangingForwardedHeadersOrAccount() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(write("/api/auth/login", Map.of("email", "missing" + i + "@example.com", "password", PASSWORD))
                            .header("X-Forwarded-For", "198.51.100." + i).header("X-Real-IP", "198.51.100." + i))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(login(PASSWORD).header("X-Forwarded-For", "203.0.113.99"))
                .andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After"));
        mvc.perform(login(PASSWORD).with(request -> { request.setRemoteAddr("203.0.113.20"); return request; }))
                .andExpect(status().isOk());
    }

    @Test
    void sensitivePasswordChangesAreLimitedPerAuthenticatedUser() throws Exception {
        var loggedIn = mvc.perform(login(PASSWORD)).andExpect(status().isOk()).andReturn();
        var cookie = loggedIn.getResponse().getCookie("access_token");
        for (int i = 0; i < 3; i++) {
            mvc.perform(write("/api/users/me/change-password", Map.of("currentPassword", "IncorrectPassword123@",
                            "newPassword", "NewPassword123@", "confirmNewPassword", "NewPassword123@")).cookie(cookie))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(write("/api/users/me/change-password", Map.of("currentPassword", PASSWORD,
                        "newPassword", "NewPassword123@", "confirmNewPassword", "NewPassword123@")).cookie(cookie))
                .andExpect(status().isTooManyRequests());
        assertThat(encoder.matches(PASSWORD, users.findById(userId).orElseThrow().getPasswordHash())).isTrue();
    }

    @Test
    void counterIsAtomicAcrossConcurrentTransactions() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        Instant now = Instant.parse("2026-11-02T12:00:00Z");
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var results = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < 10; i++) {
                results.add(executor.submit(() -> {
                    assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                    return attempts.reserveAttempt("concurrent", now, Duration.ofMinutes(5), 3).accepted();
                }));
            }
            start.countDown();
            int accepted = 0;
            for (var result : results) {
                if (result.get(20, TimeUnit.SECONDS)) accepted++;
            }
            assertThat(accepted).isEqualTo(3);
        }
    }

    @Test
    void concurrentLoginRequestsDoNotExhaustTheConnectionPool() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var results = new ArrayList<Future<Integer>>();
            for (int i = 0; i < 8; i++) {
                results.add(executor.submit(() -> {
                    assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                    return mvc.perform(login("IncorrectPassword123@")).andReturn().getResponse().getStatus();
                }));
            }
            start.countDown();
            int checked = 0;
            for (var result : results) {
                int status = result.get(20, TimeUnit.SECONDS);
                assertThat(status).isIn(401, 429);
                if (status == 401) checked++;
            }
            assertThat(checked).isEqualTo(3);
        }
    }

    @Test
    void denialDoesNotExtendTheWindowAndCleanupPreservesLiveCounters() {
        Instant now = Instant.parse("2026-11-02T12:00:00Z");
        Duration window = Duration.ofMinutes(5);
        assertThat(attempts.registerAttempt("account", now, window, 1)).isEmpty();
        assertThat(attempts.registerAttempt("account", now.plusSeconds(299), window, 1)).contains(now.plus(window));
        assertThat(attempts.registerAttempt("account", now.plus(window), window, 1)).isEmpty();
        assertThat(attempts.registerAttempt("expired", now, window, 1)).isEmpty();
        attempts.deleteExpiredBefore(now.plusSeconds(1));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM auth_attempt_limits", Integer.class)).isEqualTo(1);
        assertThat(attempts.registerAttempt("account", now.plusSeconds(301), window, 1)).contains(now.plusSeconds(600));
        assertThat(jdbc.queryForObject("SELECT subject_hash FROM auth_attempt_limits", String.class))
                .matches("[0-9a-f]{64}").doesNotContain("account");
    }

    @Test
    void successfulLoginsDoNotConsumeAccountFailures() throws Exception {
        for (int i = 0; i < 8; i++) {
            final String ip = "203.0.113." + i;
            mvc.perform(login(PASSWORD).with(request -> { request.setRemoteAddr(ip); return request; }))
                    .andExpect(status().isOk());
        }
        for (int i = 0; i < 3; i++) {
            mvc.perform(login("IncorrectPassword123@")).andExpect(status().isUnauthorized());
        }
        mvc.perform(login(PASSWORD)).andExpect(status().isTooManyRequests());
    }

    @Test
    void logoutRemainsAvailableAfterIpLimitAndStillRequiresCsrf() throws Exception {
        var loggedIn = mvc.perform(login(PASSWORD)).andExpect(status().isOk()).andReturn();
        var cookie = loggedIn.getResponse().getCookie("refresh_token");
        for (int i = 0; i < 4; i++) {
            mvc.perform(write("/api/auth/login", Map.of("email", "absent" + i + "@example.com", "password", PASSWORD)))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(login(PASSWORD)).andExpect(status().isTooManyRequests());
        mvc.perform(post("/api/auth/logout").servletPath("/api/auth/logout").cookie(cookie))
                .andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM auth_sessions WHERE revoked_at IS NULL", Integer.class)).isEqualTo(1);
        mvc.perform(post("/api/auth/logout").servletPath("/api/auth/logout").cookie(cookie).with(csrf()))
                .andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM auth_sessions WHERE revoked_at IS NULL", Integer.class)).isZero();
    }

    @Test
    void concurrentReservationsAndSuccessfulCompletionsPreserveFailures() throws Exception {
        Instant now = Instant.parse("2026-11-02T12:00:00Z");
        Duration window = Duration.ofMinutes(5);
        var failure = attempts.reserveAttempt("mixed", now, window, 3);
        assertThat(failure.accepted()).isTrue();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var results = new ArrayList<Future<?>>();
            for (int i = 0; i < 20; i++) {
                results.add(executor.submit(() -> {
                    var reservation = attempts.reserveAttempt("mixed", now, window, 3);
                    if (reservation.accepted()) attempts.releaseAttempt("mixed", reservation.windowStartedAt());
                }));
            }
            for (var result : results) result.get(20, TimeUnit.SECONDS);
        }
        assertThat(jdbc.queryForObject("SELECT attempts FROM auth_attempt_limits", Integer.class)).isEqualTo(1);
        assertThat(attempts.reserveAttempt("mixed", now, window, 3).accepted()).isTrue();
        assertThat(attempts.reserveAttempt("mixed", now, window, 3).accepted()).isTrue();
        assertThat(attempts.reserveAttempt("mixed", now.plusSeconds(299), window, 3).accepted()).isFalse();
        var nextWindow = attempts.reserveAttempt("mixed", now.plus(window), window, 3);
        assertThat(nextWindow.accepted()).isTrue();
        attempts.releaseAttempt("mixed", failure.windowStartedAt());
        assertThat(jdbc.queryForObject("SELECT attempts FROM auth_attempt_limits", Integer.class)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"inactive", "initial"})
    void validCredentialsRejectedByBusinessRulesDoNotConsumeFailures(String state) throws Exception {
        jdbc.update(state.equals("inactive") ? "UPDATE users SET active = false WHERE id = ?"
                : "UPDATE users SET must_change_password = true WHERE id = ?", userId);
        for (int i = 0; i < 4; i++) {
            mvc.perform(login(PASSWORD)).andExpect(status().is(state.equals("inactive") ? 403 : 401));
        }
    }

    @Test
    void nonexistentAccountHasTheSameFailureLimit() throws Exception {
        email = "absent@example.com";
        for (int i = 0; i < 3; i++) mvc.perform(login(PASSWORD)).andExpect(status().isUnauthorized());
        mvc.perform(login(PASSWORD)).andExpect(status().isTooManyRequests());
    }

    private MockHttpServletRequestBuilder login(String password) throws Exception {
        return write("/api/auth/login", Map.of("email", email, "password", password));
    }

    private MockHttpServletRequestBuilder write(String path, Map<String, String> body) throws Exception {
        return post(path).servletPath(path).with(csrf()).contentType("application/json")
                .content(mapper.writeValueAsString(body));
    }
}
