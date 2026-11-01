package com.ironcore.interfaces.rest.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.infrastructure.persistence.auth.repository.AuthSessionJpaRepository;
import com.ironcore.infrastructure.persistence.auth.repository.AuthenticationAttemptJpaRepository;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import com.ironcore.infrastructure.security.jwt.JwtTokenProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.*;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"ironcore.bootstrap.single-user.enabled=false", "ironcore.bootstrap.person.enabled=false", "spring.config.import="})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionLifecycleIntegrationTest {
    static final String EMAIL = "lifecycle@example.com";
    static final String PASSWORD = "StrongPass123@";
    @Autowired MockMvc mvc;
    @Autowired UserJpaRepository users;
    @Autowired PersonJpaRepository persons;
    @Autowired AuthSessionJpaRepository sessions;
    @Autowired AuthenticationAttemptJpaRepository attempts;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtTokenProperties jwt;
    @Autowired JdbcTemplate jdbc;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;
    @Autowired com.ironcore.application.user.usecase.changepassword.ChangePasswordUseCase changePassword;
    @Autowired com.ironcore.application.auth.port.AuthenticationAttemptGuard attemptGuard;
    @Autowired com.ironcore.infrastructure.persistence.logging.audit.repository.AuditLogJpaRepository auditLogs;
    @Autowired com.ironcore.domain.user.repository.UserRepository domainUsers;
    @Autowired com.ironcore.domain.auth.repository.AuthSessionRepository domainSessions;
    @Autowired com.ironcore.application.auth.port.AccessTokenGenerator accessTokens;
    @Autowired com.ironcore.application.auth.port.RefreshTokenGenerator refreshTokens;
    UserEntity user;

    @BeforeEach
    void prepare() {
        cleanup();
        var now = LocalDateTime.now();
        var person = persons.save(new PersonEntity(null, "Lifecycle", SexType.MALE, LocalDate.of(1990, 1, 1), now, null));
        user = users.save(new UserEntity(null, "Lifecycle", person, EMAIL, encoder.encode(PASSWORD), false, true, now, now));
    }

    @org.junit.jupiter.api.AfterEach
    void cleanup() {
        auditLogs.deleteAll();
        sessions.deleteAll();
        attempts.deleteAll();
        users.deleteAll();
        persons.deleteAll();
    }

    @Test
    void loginCreatesIndependentSessionsAndStoresOnlyHashes() throws Exception {
        var first = login();
        var second = login();
        assertThat(sessions.count()).isEqualTo(2);
        assertThat(cookie(first, "refresh_token").getValue()).isNotEqualTo(cookie(second, "refresh_token").getValue());
        var refresh = cookie(first, "refresh_token");
        assertThat(refresh.isHttpOnly()).isTrue();
        assertThat(refresh.getSecure()).isTrue();
        assertThat(refresh.getPath()).isEqualTo("/api/auth");
        assertThat(refresh.getDomain()).isNull();
        var stored = sessions.findById(sid(first)).orElseThrow();
        assertThat(stored.getRefreshTokenHash()).matches("[0-9a-f]{64}").isNotEqualTo(refresh.getValue());
        assertThat(Duration.between(stored.getCreatedAt(), stored.getExpiresAt())).isEqualTo(Duration.ofDays(7));
        assertThat(first.getResponse().getContentAsString()).doesNotContain("accessToken", "tokenType", "refreshToken", refresh.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"active", "initial", "inactive"})
    void initialChangeDoesNotEnumerateAccountsEvenWithMismatchedConfirmation(String state) throws Exception {
        user.setActive(!state.equals("inactive"));
        user.setMustChangePassword(state.equals("initial"));
        users.saveAndFlush(user);
        for (String email : new String[]{EMAIL, "missing@example.com"}) {
            mvc.perform(post("/api/users/change-initial-password").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\",\"currentPassword\":\"WrongPass123@\","
                                    + "\"newPassword\":\"NewStrongPass123@\",\"confirmNewPassword\":\"Different123@\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Credenciais incorretas."))
                    .andExpect(jsonPath("$.fields").isEmpty())
                    .andExpect(header().doesNotExist("Set-Cookie"));
        }
        assertThat(users.findById(user.getId()).orElseThrow().getPasswordHash()).isEqualTo(user.getPasswordHash());
        assertThat(sessions.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"normal", "initial"})
    void legacyPasswordAbove72BytesCanLoginAndBeChanged(String flow) throws Exception {
        String legacy = "a".repeat(72) + "legacy-suffix";
        String body = passwordBody().replace("\"currentPassword\":\"" + PASSWORD + "\"", "\"currentPassword\":\"" + legacy + "\"");
        // Historical BCrypt hashes consume only the first 72 bytes.
        user.setPasswordHash(encoder.encode("a".repeat(72)));
        user.setMustChangePassword(flow.equals("initial"));
        users.saveAndFlush(user);
        if (flow.equals("initial")) {
            mvc.perform(post("/api/users/change-initial-password").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + EMAIL + "\"," + body.substring(1)))
                    .andExpect(status().isNoContent());
            assertThat(sessions.count()).isZero();
        } else {
            var loggedIn = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + legacy + "\"}"))
                    .andExpect(status().isOk()).andReturn();
            mvc.perform(post("/api/users/me/change-password").with(csrf()).cookie(cookie(loggedIn, "access_token"))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNoContent());
        }
        assertThat(encoder.matches("NewStrongPass123@", users.findById(user.getId()).orElseThrow().getPasswordHash())).isTrue();
    }

    @Test
    void oversizedNewPasswordIsRejectedWithoutChangingCredentials() throws Exception {
        var login = login();
        mvc.perform(post("/api/users/me/change-password").with(csrf()).cookie(cookie(login, "access_token"))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordBody().replace("NewStrongPass123@", "á".repeat(37))))
                .andExpect(status().isBadRequest());
        assertThat(encoder.matches(PASSWORD, users.findById(user.getId()).orElseThrow().getPasswordHash())).isTrue();
        access(cookie(login, "access_token"), 200);
    }

    @Test
    void databaseTimestampPrecisionCannotMakeAnImmediatelyIssuedSessionLookFutureDated() {
        var instant = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).plusNanos(999);
        var fixed = Clock.fixed(instant, ZoneId.systemDefault());
        var issuer = new com.ironcore.application.auth.service.AuthSessionIssuer(domainSessions, accessTokens, fixed, refreshTokens);
        var currentUser = domainUsers.findById(new com.ironcore.domain.user.valueobject.UserId(user.getId())).orElseThrow();
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        var credentials = transaction.execute(status -> issuer.issue(currentUser));
        var id = UUID.fromString(JWT.decode(credentials.value()).getClaim("sid").asString());
        var stored = sessions.findById(id).orElseThrow();
        assertThat(stored.getCreatedAt()).isEqualTo(instant.truncatedTo(java.time.temporal.ChronoUnit.MICROS));
        var session = com.ironcore.infrastructure.persistence.auth.mapper.AuthSessionMapper.toDomain(stored);
        assertThat(session.isActiveAt(instant)).isTrue();
        transaction.execute(status -> issuer.renew(currentUser, session));
        var rotated = com.ironcore.infrastructure.persistence.auth.mapper.AuthSessionMapper.toDomain(sessions.findById(id).orElseThrow());
        assertThat(rotated.isRecentlyConsumedRefreshCredential(refreshTokens.hash(credentials.refreshToken()), instant)).isTrue();
    }

    @Test
    void refreshRotatesWithoutExtendingAbsoluteExpiryOrAcceptingOldCredential() throws Exception {
        var login = login();
        var absolute = sessions.findById(sid(login)).orElseThrow().getExpiresAt();
        var renewed = refresh(cookie(login, "refresh_token"), 204);
        assertThat(cookie(renewed, "refresh_token").getValue()).isNotEqualTo(cookie(login, "refresh_token").getValue());
        assertThat(sessions.findById(sid(login)).orElseThrow().getExpiresAt()).isEqualTo(absolute);
        var conflict = refresh(cookie(login, "refresh_token"), 409);
        assertThat(conflict.getResponse().getHeaders("Set-Cookie")).isEmpty();
        assertThat(sessions.findById(sid(login)).orElseThrow().getRevokedAt()).isNull();
        jdbc.update("update auth_sessions set created_at=?, refresh_rotated_at=? where id=?",
                java.sql.Timestamp.from(Instant.now().minusSeconds(30)),
                java.sql.Timestamp.from(Instant.now().minusSeconds(11)), sid(login));
        refresh(cookie(login, "refresh_token"), 401);
        refresh(cookie(renewed, "refresh_token"), 204);
    }

    @Test
    void concurrentTabsHaveOneWinnerWithoutRevokingOrOverwritingCookies() throws Exception {
        var login = login();
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            java.util.concurrent.Callable<MvcResult> request = () -> {
                assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                return mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(cookie(login, "refresh_token"))).andReturn();
            };
            var a = executor.submit(request);
            var b = executor.submit(request);
            start.countDown();
            var first = a.get(20, TimeUnit.SECONDS);
            var second = b.get(20, TimeUnit.SECONDS);
            assertThat(new int[]{first.getResponse().getStatus(), second.getResponse().getStatus()}).containsExactlyInAnyOrder(204, 409);
            var winner = first.getResponse().getStatus() == 204 ? first : second;
            var loser = first.getResponse().getStatus() == 409 ? first : second;
            assertThat(loser.getResponse().getHeaders("Set-Cookie")).isEmpty();
            access(cookie(winner, "access_token"), 200);
            refresh(cookie(winner, "refresh_token"), 204);
        }
    }

    @Test
    void logoutRevokesOnlyCurrentSessionAndIsIdempotent() throws Exception {
        var first = login();
        var second = login();
        mvc.perform(post("/api/auth/logout").with(csrf()).cookie(cookie(first, "access_token")))
                .andExpect(status().isNoContent()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie().maxAge("access_token", 0)).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie().maxAge("refresh_token", 0));
        access(cookie(first, "access_token"), 401);
        refresh(cookie(first, "refresh_token"), 401);
        access(cookie(second, "access_token"), 200);
        mvc.perform(post("/api/auth/logout").with(csrf()).cookie(cookie(first, "access_token"))).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/logout").with(csrf())).andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void logoutUsesRefreshWithExpiredOrAbsentAccess(boolean expiredAccess) throws Exception {
        var login = login();
        var request = post("/api/auth/logout").with(csrf()).cookie(cookie(login, "refresh_token"));
        if (expiredAccess) request.cookie(new Cookie("access_token", signed(sid(login).toString(), user.getId(), Instant.now().minusSeconds(60))));
        mvc.perform(request).andExpect(status().isNoContent());
        assertThat(sessions.findById(sid(login)).orElseThrow().getRevokedAt()).isNotNull();
        access(cookie(login, "access_token"), 401);
    }

    @Test
    void invalidLogoutCredentialsOnlyClearCookies() throws Exception {
        var login = login();
        mvc.perform(post("/api/auth/logout").with(csrf()).cookie(new Cookie("access_token", "bad"), new Cookie("refresh_token", "bad")))
                .andExpect(status().isNoContent()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie().maxAge("refresh_token", 0));
        access(cookie(login, "access_token"), 200);
    }

    @Test
    void passwordChangeRegeneratesCurrentSessionAndRejectsAllPreviousCredentials() throws Exception {
        var first = login();
        var second = login();
        var changed = mvc.perform(post("/api/users/me/change-password").with(csrf()).cookie(cookie(first, "access_token"))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordBody()))
                .andExpect(status().isNoContent()).andReturn();
        access(cookie(changed, "access_token"), 200);
        refresh(cookie(changed, "refresh_token"), 204);
        for (var old : new MvcResult[]{first, second}) {
            access(cookie(old, "access_token"), 401);
            refresh(cookie(old, "refresh_token"), 401);
        }
        assertThat(sessions.findAll().stream().filter(s -> s.getRevokedAt() == null)).hasSize(1);
    }

    @Test
    void initialChangeRevokesOldSessionsWithoutIssuingAuthentication() throws Exception {
        var login = login();
        user = users.findById(user.getId()).orElseThrow();
        user.setMustChangePassword(true);
        users.saveAndFlush(user);
        mvc.perform(post("/api/users/change-initial-password").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\"," + passwordBody().substring(1)))
                .andExpect(status().isNoContent()).andExpect(header().doesNotExist("Set-Cookie"));
        access(cookie(login, "access_token"), 401);
        refresh(cookie(login, "refresh_token"), 401);
        assertThat(sessions.count()).isEqualTo(1);
    }

    @Test
    void invalidPasswordDoesNotRevokeSessionsOrModifyHash() throws Exception {
        var login = login();
        var previous = users.findById(user.getId()).orElseThrow().getPasswordHash();
        mvc.perform(post("/api/users/me/change-password").with(csrf()).cookie(cookie(login, "access_token"))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordBody().replace(PASSWORD, "WrongPass123@")))
                .andExpect(status().isUnauthorized());
        assertThat(users.findById(user.getId()).orElseThrow().getPasswordHash()).isEqualTo(previous);
        access(cookie(login, "access_token"), 200);
    }

    @Test
    void passwordAndCredentialChangesRollBackTogether() throws Exception {
        var login = login();
        var originalHash = users.findById(user.getId()).orElseThrow().getPasswordHash();
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            changePassword.execute(new com.ironcore.application.user.usecase.changepassword.ChangePasswordCommand(
                    new com.ironcore.domain.user.valueobject.UserId(user.getId()),
                    new com.ironcore.domain.user.valueobject.RawPassword(PASSWORD),
                    new com.ironcore.domain.user.valueobject.RawPassword("NewStrongPass123@"),
                    new com.ironcore.domain.user.valueobject.RawPassword("NewStrongPass123@")));
            throw new IllegalStateException("Simulated failure before commit");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(users.findById(user.getId()).orElseThrow().getPasswordHash()).isEqualTo(originalHash);
        assertThat(sessions.count()).isEqualTo(1);
        access(cookie(login, "access_token"), 200);
        refresh(cookie(login, "refresh_token"), 204);
    }

    @Test
    void userLockReloadsPasswordChangedAfterAnEarlierRead() {
        var userId = new com.ironcore.domain.user.valueobject.UserId(user.getId());
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        transaction.executeWithoutResult(status -> {
            var earlierRead = domainUsers.findById(userId).orElseThrow();
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var changed = executor.submit(() -> changePassword.execute(
                        new com.ironcore.application.user.usecase.changepassword.ChangePasswordCommand(userId,
                                new com.ironcore.domain.user.valueobject.RawPassword(PASSWORD),
                                new com.ironcore.domain.user.valueobject.RawPassword("NewStrongPass123@"),
                                new com.ironcore.domain.user.valueobject.RawPassword("NewStrongPass123@"))));
                assertThat(changed.get(20, TimeUnit.SECONDS)).isNotNull();
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
            var locked = domainUsers.findByIdForUpdate(userId).orElseThrow();
            assertThat(locked.getPasswordHash()).isNotEqualTo(earlierRead.getPasswordHash());
            assertThat(encoder.matches("NewStrongPass123@", locked.getPasswordHash().value())).isTrue();
        });
    }

    @Test
    void concurrentPasswordChangeAndRefreshCannotLeaveOldCredentialsActive() throws Exception {
        var login = login();
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var renew = executor.submit(() -> {
                assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                return mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(cookie(login, "refresh_token"))).andReturn();
            });
            var change = executor.submit(() -> {
                assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                return mvc.perform(post("/api/users/me/change-password").with(csrf()).cookie(cookie(login, "access_token"))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordBody())).andReturn();
            });
            start.countDown();
            var renewed = renew.get(20, TimeUnit.SECONDS);
            var changed = change.get(20, TimeUnit.SECONDS);
            assertThat(changed.getResponse().getStatus()).isEqualTo(204);
            assertThat(renewed.getResponse().getStatus()).isIn(204, 401);
            if (renewed.getResponse().getStatus() == 204) {
                access(cookie(renewed, "access_token"), 401);
                refresh(cookie(renewed, "refresh_token"), 401);
            }
            access(cookie(changed, "access_token"), 200);
            refresh(cookie(changed, "refresh_token"), 204);
            access(cookie(login, "access_token"), 401);
        }
    }

    @Test
    void refreshIgnoresExpiredAccessAndHonorsSessionDeadlineInJwtAndCookie() throws Exception {
        var login = login();
        var deadline = Instant.now().plusSeconds(60).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        jdbc.update("update auth_sessions set expires_at=? where id=?", java.sql.Timestamp.from(deadline), sid(login));
        var renewed = mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(cookie(login, "refresh_token"),
                        new Cookie("access_token", signed(sid(login).toString(), user.getId(), Instant.now().minusSeconds(60)))))
                .andExpect(status().isNoContent()).andReturn();
        assertThat(JWT.decode(cookie(renewed, "access_token").getValue()).getExpiresAtAsInstant()).isEqualTo(deadline);
        assertThat(cookie(renewed, "access_token").getMaxAge()).isBetween(1, 60);
        assertThat(cookie(renewed, "refresh_token").getMaxAge()).isBetween(1, 60);
    }

    @Test
    void concurrentNicknameAndPasswordChangesPreserveNewPassword() throws Exception {
        var login = login();
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var nickname = executor.submit(() -> {
                assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                return mvc.perform(put("/api/users/me/change-nickname").with(csrf()).cookie(cookie(login, "access_token"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"Updated\"}")).andReturn();
            });
            var password = executor.submit(() -> {
                assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                return mvc.perform(post("/api/users/me/change-password").with(csrf()).cookie(cookie(login, "access_token"))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordBody())).andReturn();
            });
            start.countDown();
            assertThat(password.get(20, TimeUnit.SECONDS).getResponse().getStatus()).isEqualTo(204);
            assertThat(nickname.get(20, TimeUnit.SECONDS).getResponse().getStatus()).isIn(200, 401);
            var hash = users.findById(user.getId()).orElseThrow().getPasswordHash();
            assertThat(encoder.matches("NewStrongPass123@", hash)).isTrue();
            assertThat(encoder.matches(PASSWORD, hash)).isFalse();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"expired", "revoked", "inactive"})
    void deniesRefreshAndAccessForUnusableSessions(String condition) throws Exception {
        var login = login();
        if (condition.equals("expired")) {
            jdbc.update("update auth_sessions set created_at=?, expires_at=? where id=?",
                    java.sql.Timestamp.from(Instant.now().minus(Duration.ofDays(8))), java.sql.Timestamp.from(Instant.now().minusSeconds(1)), sid(login));
        } else if (condition.equals("revoked")) {
            var session = sessions.findById(sid(login)).orElseThrow();
            session.setRevokedAt(Instant.now());
            sessions.saveAndFlush(session);
        } else {
            user = users.findById(user.getId()).orElseThrow();
            user.setActive(false);
            users.saveAndFlush(user);
        }
        refresh(cookie(login, "refresh_token"), 401);
        access(cookie(login, "access_token"), 401);
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "invalid", "other-user", "unknown"})
    void rejectsSignedJwtWithUnusableSessionClaim(String condition) throws Exception {
        var login = login();
        String session = switch (condition) {
            case "missing" -> null;
            case "invalid" -> "invalid";
            case "unknown" -> UUID.randomUUID().toString();
            default -> sid(login).toString();
        };
        access(new Cookie("access_token", signed(session, condition.equals("other-user") ? user.getId() + 100 : user.getId(), Instant.now().plusSeconds(300))), 401);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/refresh", "/api/auth/logout", "/api/auth/login", "/api/users/me/change-password", "/api/users/change-initial-password"})
    void mutableAuthenticationEndpointsRequireCsrf(String path) throws Exception {
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}" )).andExpect(status().isForbidden());
        mvc.perform(post(path).with(csrf().useInvalidToken()).contentType(MediaType.APPLICATION_JSON).content("{}" )).andExpect(status().isForbidden());
    }

    @Test
    void invalidRefreshCannotBeReplacedWithAccessJwt() throws Exception {
        var login = login();
        refresh(new Cookie("refresh_token", cookie(login, "access_token").getValue()), 401);
        mvc.perform(post("/api/auth/refresh").with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void limitsRepeatedAuthenticationWithoutDisclosingAccountExistence() throws Exception {
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"missing@example.com\",\"password\":\"WrongPass123@\"}")).andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing@example.com\",\"password\":\"WrongPass123@\"}"))
                .andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After", "300"));
    }

    @Test
    void authenticationLimitIsAtomicAcrossConcurrentRequests() throws Exception {
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var requests = new java.util.ArrayList<java.util.concurrent.Future<Boolean>>();
            for (int i = 0; i < 12; i++) requests.add(executor.submit(() -> {
                assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                try {
                    attemptGuard.check("concurrent@example.com");
                    return true;
                } catch (com.ironcore.application.exception.AuthenticationRateLimitException limited) {
                    return false;
                }
            }));
            start.countDown();
            int accepted = 0;
            for (var request : requests) if (request.get(20, TimeUnit.SECONDS)) accepted++;
            assertThat(accepted).isEqualTo(10);
            assertThat(attempts.findAll()).singleElement().satisfies(bucket -> assertThat(bucket.getAttempts()).isEqualTo(10));
        }
    }

    @Test
    void authenticationBucketsResetAfterWindowAndExpiredBucketsAreRemoved() {
        String active = refreshTokens.hash("active@example.com");
        String old = refreshTokens.hash("old@example.com");
        attemptGuard.check("active@example.com");
        attemptGuard.check("old@example.com");
        jdbc.update("update authentication_attempts set window_started_at=?, attempts=10 where id=?",
                java.sql.Timestamp.from(Instant.now().minusSeconds(301)), active);
        jdbc.update("update authentication_attempts set window_started_at=? where id=?",
                java.sql.Timestamp.from(Instant.now().minus(Duration.ofDays(2))), old);
        attemptGuard.check("active@example.com");
        assertThat(attempts.findById(active).orElseThrow().getAttempts()).isEqualTo(1);
        assertThat(attempts.findById(old)).isEmpty();
    }

    @Test
    void concurrentLoginAttemptsDoNotExhaustConnectionPool() throws Exception {
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var requests = new java.util.ArrayList<java.util.concurrent.Future<Integer>>();
            for (int i = 0; i < 12; i++) requests.add(executor.submit(() -> {
                assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                return mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"concurrent-login@example.com\",\"password\":\"WrongPass123@\"}"))
                        .andReturn().getResponse().getStatus();
            }));
            start.countDown();
            var statuses = new java.util.ArrayList<Integer>();
            for (var request : requests) statuses.add(request.get(20, TimeUnit.SECONDS));
            assertThat(statuses).containsOnly(401, 429);
            assertThat(statuses.stream().filter(status -> status == 401)).hasSize(10);
            assertThat(statuses.stream().filter(status -> status == 429)).hasSize(2);
        }
    }

    @Test
    void openApiDescribesRefreshWithoutCredentialSchemas() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/auth/refresh'].post.responses['409']").exists())
                .andExpect(jsonPath("$.components.schemas.LoginResponse.properties.accessToken").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.LoginResponse.properties.refreshToken").doesNotExist());
    }

    MvcResult login() throws Exception {
        return mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn();
    }
    MvcResult refresh(Cookie refresh, int status) throws Exception {
        return mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(refresh))
                .andExpect(status().is(status)).andReturn();
    }
    void access(Cookie access, int status) throws Exception {
        mvc.perform(get("/api/users/me").cookie(access)).andExpect(status().is(status));
    }
    Cookie cookie(MvcResult result, String name) { return result.getResponse().getCookie(name); }
    UUID sid(MvcResult result) { return UUID.fromString(JWT.decode(cookie(result, "access_token").getValue()).getClaim("sid").asString()); }
    String signed(String sid, Long owner, Instant expires) {
        var builder = JWT.create().withIssuer(jwt.getIssuer()).withSubject(owner.toString()).withClaim("email", EMAIL)
                .withClaim("mustChangePassword", false).withExpiresAt(expires);
        if (sid != null) builder.withClaim("sid", sid);
        return builder.sign(Algorithm.HMAC256(jwt.getSecret()));
    }
    String passwordBody() { return "{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"NewStrongPass123@\",\"confirmNewPassword\":\"NewStrongPass123@\"}"; }
}
