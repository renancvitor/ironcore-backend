package com.ironcore.interfaces.rest.auth;

import com.auth0.jwt.JWT;
import com.ironcore.application.auth.port.AccessTokenGenerator;
import com.ironcore.application.auth.port.RefreshTokenGenerator;
import com.ironcore.application.auth.port.SessionCredentials;
import com.ironcore.application.auth.service.AuthSessionIssuer;
import com.ironcore.application.auth.usecase.LoginCommand;
import com.ironcore.application.auth.usecase.LoginUseCase;
import com.ironcore.application.auth.usecase.LogoutCommand;
import com.ironcore.application.auth.usecase.LogoutUseCase;
import com.ironcore.application.auth.usecase.RefreshSessionUseCase;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.application.user.usecase.changepassword.ChangePasswordCommand;
import com.ironcore.application.user.usecase.changepassword.ChangePasswordUseCase;
import com.ironcore.application.user.usecase.update.ChangeNicknameCommand;
import com.ironcore.application.user.usecase.update.ChangeNicknameUseCase;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.RawPassword;
import com.ironcore.domain.user.valueobject.UserId;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(properties = {"spring.config.import=", "ironcore.bootstrap.single-user.enabled=false",
        "ironcore.bootstrap.person.enabled=false"})
@ActiveProfiles("test")
@Testcontainers
class AuthSessionConcurrencyIntegrationTest {

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
    }

    @Autowired private LoginUseCase login;
    @Autowired private ChangePasswordUseCase changePassword;
    @Autowired private ChangeNicknameUseCase changeNickname;
    @Autowired private UserJpaRepository users;
    @Autowired private PersonJpaRepository persons;
    @Autowired private AuthSessionJpaRepository sessions;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private RefreshSessionUseCase refresh;
    @Autowired private LogoutUseCase logout;
    @Autowired private RefreshTokenGenerator refreshTokens;
    @MockitoSpyBean private AccessTokenGenerator accessTokens;
    @MockitoSpyBean private AuthSessionIssuer issuer;
    @MockitoSpyBean private UserRepository userRepository;

    private UserId owner;
    private Email email;
    private static final String OLD_PASSWORD = "OldPassword123@";
    private static final String NEW_PASSWORD = "NewPassword123@";

    @BeforeEach
    void prepare() {
        LocalDateTime now = LocalDateTime.now();
        PersonEntity person = persons.save(new PersonEntity(null, "Concurrency", SexType.MALE,
                LocalDate.of(1990, 1, 1), now, null));
        email = new Email(UUID.randomUUID() + "@example.com");
        UserEntity user = users.save(new UserEntity(null, "Original", person, email.value(),
                encoder.encode(OLD_PASSWORD), false, true, now, null));
        owner = new UserId(user.getId());
    }

    @Test
    void passwordChangeRevokesLoginThatAlreadyValidatedTheOldPassword() throws Exception {
        CountDownLatch validated = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (first.getAndSet(false)) {
                validated.countDown();
                assertThat(release.await(20, TimeUnit.SECONDS)).isTrue();
            }
            return invocation.callRealMethod();
        }).when(issuer).issue(any());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var loggedIn = executor.submit(() -> login.execute(new LoginCommand(email, OLD_PASSWORD)));
            try {
                assertThat(validated.await(10, TimeUnit.SECONDS)).isTrue();
                var changed = executor.submit(() -> changePassword.execute(passwordCommand()));
                awaitBlockedOrFinished(changed);
                release.countDown();
                var oldCredentials = loggedIn.get(20, TimeUnit.SECONDS);
                changed.get(20, TimeUnit.SECONDS);
                UUID sessionId = UUID.fromString(JWT.decode(oldCredentials.credentials().accessToken().value()).getClaim("sid").asString());
                assertThat(sessions.findById(sessionId).orElseThrow().getRevokedAt()).isNotNull();
            } finally {
                release.countDown();
            }
        }
    }

    @Test
    void nicknameUpdateCannotRestoreTheOldPassword() throws Exception {
        CountDownLatch read = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (first.getAndSet(false)) {
                read.countDown();
                assertThat(release.await(20, TimeUnit.SECONDS)).isTrue();
            }
            return invocation.callRealMethod();
        }).when(userRepository).save(any());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var nickname = executor.submit(() -> changeNickname.execute(new ChangeNicknameCommand(owner, "Updated")));
            try {
                assertThat(read.await(10, TimeUnit.SECONDS)).isTrue();
                var changed = executor.submit(() -> changePassword.execute(passwordCommand()));
                awaitBlockedOrFinished(changed);
                release.countDown();
                nickname.get(20, TimeUnit.SECONDS);
                changed.get(20, TimeUnit.SECONDS);
                UserEntity stored = users.findById(owner.value()).orElseThrow();
                assertThat(encoder.matches(NEW_PASSWORD, stored.getPasswordHash())).isTrue();
                assertThat(stored.getNickname()).isEqualTo("Updated");
            } finally {
                release.countDown();
            }
        }
    }

    private void awaitBlockedOrFinished(Future<?> operation) {
        await().atMost(Duration.ofSeconds(10)).until(() -> operation.isDone() || Boolean.TRUE.equals(
                jdbc.queryForObject("select exists(select 1 from pg_stat_activity where datname = current_database() "
                        + "and cardinality(pg_blocking_pids(pid)) > 0)", Boolean.class)));
    }

    @Test
    void loginWaitingForPasswordChangeMustValidateTheNewPassword() throws Exception {
        CountDownLatch issued = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (first.getAndSet(false)) {
                issued.countDown();
                assertThat(release.await(20, TimeUnit.SECONDS)).isTrue();
            }
            return invocation.callRealMethod();
        }).when(issuer).issue(any());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var changed = executor.submit(() -> changePassword.execute(passwordCommand()));
            try {
                assertThat(issued.await(10, TimeUnit.SECONDS)).isTrue();
                var oldLogin = executor.submit(() -> login.execute(new LoginCommand(email, OLD_PASSWORD)));
                awaitBlockedOrFinished(oldLogin);
                release.countDown();
                changed.get(20, TimeUnit.SECONDS);
                assertThatThrownBy(() -> oldLogin.get(20, TimeUnit.SECONDS))
                        .hasCauseInstanceOf(InvalidCredentialsException.class);
            } finally {
                release.countDown();
            }
        }
    }

    @Test
    void failedSessionIssuanceRollsBackPasswordAndRevocationTogether() {
        var credentials = login.execute(new LoginCommand(email, OLD_PASSWORD));
        UUID sessionId = UUID.fromString(JWT.decode(credentials.credentials().accessToken().value()).getClaim("sid").asString());
        doThrow(new IllegalStateException("Simulated token issuance failure")).when(issuer).issue(any());

        assertThatThrownBy(() -> changePassword.execute(passwordCommand()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(encoder.matches(OLD_PASSWORD, users.findById(owner.value()).orElseThrow().getPasswordHash())).isTrue();
        assertThat(sessions.findById(sessionId).orElseThrow().getRevokedAt()).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void lockedReadReloadsUserAlreadyLoadedInThePersistenceContext(boolean byEmail) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertThat(userRepository.findById(owner).orElseThrow().getNickname()).isEqualTo("Original");

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                executor.submit(() -> changeNickname.execute(new ChangeNicknameCommand(owner, "Concurrent")))
                        .get(20, TimeUnit.SECONDS);
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }

            var locked = byEmail ? userRepository.findByEmailForUpdate(email) : userRepository.findByIdForUpdate(owner);
            assertThat(locked.orElseThrow().getNickname()).isEqualTo("Concurrent");
        });
    }

    @Test
    void refreshRotatesTheCredentialWithoutExtendingTheSession() {
        var original = login.execute(new LoginCommand(email, OLD_PASSWORD)).credentials();
        UUID sessionId = UUID.fromString(JWT.decode(original.accessToken().value()).getClaim("sid").asString());
        var stored = sessions.findById(sessionId).orElseThrow();
        assertThat(stored.getRefreshTokenHash()).isEqualTo(refreshTokens.hash(original.refreshToken()));
        assertThat(stored.getRefreshTokenHash()).isNotEqualTo(original.refreshToken());

        var renewed = refresh.execute(original.refreshToken());

        assertThat(renewed.refreshToken()).isNotEqualTo(original.refreshToken());
        assertThat(renewed.sessionExpiresAt()).isEqualTo(stored.getExpiresAt());
        assertThat(JWT.decode(renewed.accessToken().value()).getClaim("sid").asString()).isEqualTo(sessionId.toString());
        assertThat(sessions.findById(sessionId).orElseThrow().getRefreshTokenHash())
                .isEqualTo(refreshTokens.hash(renewed.refreshToken()));
        assertThatThrownBy(() -> refresh.execute(original.refreshToken())).isInstanceOf(InvalidCredentialsException.class);
        assertThat(refresh.execute(renewed.refreshToken())).isNotNull();
    }

    @Test
    void simultaneousRefreshesAcceptTheCredentialOnlyOnce() throws Exception {
        var original = login.execute(new LoginCommand(email, OLD_PASSWORD)).credentials();
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> refreshAfter(start, original.refreshToken()));
            var second = executor.submit(() -> refreshAfter(start, original.refreshToken()));
            start.countDown();
            var results = Arrays.asList(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
            assertThat(results.stream().filter(Objects::nonNull).count()).isEqualTo(1);
            var winner = results.stream().filter(Objects::nonNull).findFirst().orElseThrow();
            assertThat(refresh.execute(winner.refreshToken())).isNotNull();
        }
    }

    private SessionCredentials refreshAfter(CountDownLatch start, String token) throws InterruptedException {
        assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
        try {
            return refresh.execute(token);
        } catch (InvalidCredentialsException exception) {
            return null;
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void revocationWaitsForRefreshAndInvalidatesItsResult(boolean passwordChange) throws Exception {
        var original = login.execute(new LoginCommand(email, OLD_PASSWORD)).credentials();
        var sessionId = new AuthSessionId(UUID.fromString(JWT.decode(original.accessToken().value()).getClaim("sid").asString()));
        CountDownLatch rotating = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (first.getAndSet(false)) {
                rotating.countDown();
                assertThat(release.await(20, TimeUnit.SECONDS)).isTrue();
            }
            return invocation.callRealMethod();
        }).when(accessTokens).generate(any());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var renewed = executor.submit(() -> refresh.execute(original.refreshToken()));
            try {
                assertThat(rotating.await(10, TimeUnit.SECONDS)).isTrue();
                var revoked = executor.submit(() -> {
                    if (passwordChange) {
                        changePassword.execute(passwordCommand());
                    } else {
                        logout.execute(new LogoutCommand(owner, sessionId, null));
                    }
                });
                awaitBlockedOrFinished(revoked);
                release.countDown();
                var credentials = renewed.get(20, TimeUnit.SECONDS);
                revoked.get(20, TimeUnit.SECONDS);
                assertThat(sessions.findById(sessionId.value()).orElseThrow().getRevokedAt()).isNotNull();
                assertThatThrownBy(() -> refresh.execute(credentials.refreshToken())).isInstanceOf(InvalidCredentialsException.class);
            } finally {
                release.countDown();
            }
        }
    }

    @Test
    void failedAccessTokenGenerationRollsBackRefreshRotation() {
        var original = login.execute(new LoginCommand(email, OLD_PASSWORD)).credentials();
        doThrow(new IllegalStateException("Simulated signing failure")).when(accessTokens).generate(any());

        assertThatThrownBy(() -> refresh.execute(original.refreshToken())).isInstanceOf(IllegalStateException.class);

        doCallRealMethod().when(accessTokens).generate(any());
        assertThat(refresh.execute(original.refreshToken())).isNotNull();
    }

    @Test
    void logoutByRefreshRevokesTheIdentifiedSessionEvenWhenItsTokenRotatesBeforeTheLock() throws Exception {
        var original = login.execute(new LoginCommand(email, OLD_PASSWORD)).credentials();
        var otherDevice = login.execute(new LoginCommand(email, OLD_PASSWORD)).credentials();
        UUID sessionId = UUID.fromString(JWT.decode(original.accessToken().value()).getClaim("sid").asString());
        CountDownLatch identified = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (first.getAndSet(false)) {
                identified.countDown();
                assertThat(release.await(20, TimeUnit.SECONDS)).isTrue();
            }
            return invocation.callRealMethod();
        }).when(userRepository).findByIdForUpdate(owner);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var loggedOut = executor.submit(() -> logout.execute(new LogoutCommand(null, null, original.refreshToken())));
            try {
                assertThat(identified.await(10, TimeUnit.SECONDS)).isTrue();
                var renewed = refresh.execute(original.refreshToken());
                release.countDown();
                loggedOut.get(20, TimeUnit.SECONDS);

                var stored = sessions.findById(sessionId).orElseThrow();
                assertThat(stored.getRevokedAt()).isNotNull();
                assertThat(stored.getRefreshTokenHash()).isEqualTo(refreshTokens.hash(renewed.refreshToken()));
                assertThatThrownBy(() -> refresh.execute(renewed.refreshToken())).isInstanceOf(InvalidCredentialsException.class);
                assertThat(refresh.execute(otherDevice.refreshToken())).isNotNull();
            } finally {
                release.countDown();
            }
        }
    }

    private ChangePasswordCommand passwordCommand() {
        return new ChangePasswordCommand(owner, new RawPassword(OLD_PASSWORD),
                new RawPassword(NEW_PASSWORD), new RawPassword(NEW_PASSWORD));
    }
}
