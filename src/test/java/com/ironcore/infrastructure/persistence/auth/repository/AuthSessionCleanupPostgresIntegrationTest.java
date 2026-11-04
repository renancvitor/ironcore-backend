package com.ironcore.infrastructure.persistence.auth.repository;

import com.ironcore.application.auth.service.AuthSessionCleanupService;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.infrastructure.persistence.auth.entity.AuthSessionEntity;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataJpaTest(showSql = false, properties = {
        "spring.jpa.hibernate.ddl-auto=validate", "spring.flyway.enabled=true"
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AuthSessionCleanupService.class, AuthSessionRepositoryAdapter.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers
class AuthSessionCleanupPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired AuthSessionCleanupService service;
    @Autowired AuthSessionJpaRepository sessions;
    @Autowired UserJpaRepository users;
    @Autowired PersonJpaRepository persons;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoBean Clock clock;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldDeleteOnlySessionsExpiredMoreThanFifteenDaysAgoAndCommit(boolean revoked) {
        Instant now = Instant.parse("2026-11-03T01:30:00.123456Z");
        Instant cutoff = now.minus(Duration.ofDays(15));
        when(clock.instant()).thenReturn(now);
        List<AuthSessionEntity> fixtures = new TransactionTemplate(transactionManager).execute(status -> {
            LocalDateTime created = LocalDateTime.ofInstant(now.minus(Duration.ofDays(40)), ZoneOffset.UTC);
            PersonEntity person = persons.save(new PersonEntity(null, "Cleanup", SexType.MALE,
                    LocalDate.of(1990, 1, 1), created, null));
            UserEntity user = users.save(new UserEntity(null, "Cleanup", person,
                    UUID.randomUUID() + "@example.com", "hash", false, true, created, null));
            return sessions.saveAllAndFlush(List.of(
                    session(user, cutoff.minusNanos(1_000), revoked),
                    session(user, cutoff, revoked),
                    session(user, cutoff.plusNanos(1_000), revoked),
                    session(user, now.minusSeconds(1), revoked),
                    session(user, now, revoked),
                    session(user, now.plus(Duration.ofDays(7)), revoked)));
        });
        List<UUID> ids = fixtures.stream().map(AuthSessionEntity::getId).toList();
        List<AuthSessionEntity> retained = fixtures.subList(1, fixtures.size());

        service.deleteExpiredBefore();

        assertThat(sessions.findById(ids.getFirst())).isEmpty();
        assertThat(sessions.findAllById(ids)).usingRecursiveFieldByFieldElementComparatorIgnoringFields("user")
                .containsExactlyInAnyOrderElementsOf(retained);

        service.deleteExpiredBefore();

        assertThat(sessions.findAllById(ids)).usingRecursiveFieldByFieldElementComparatorIgnoringFields("user")
                .containsExactlyInAnyOrderElementsOf(retained);
    }

    private AuthSessionEntity session(UserEntity user, Instant expiresAt, boolean revoked) {
        AuthSessionEntity session = new AuthSessionEntity();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setCreatedAt(expiresAt.minus(Duration.ofDays(7)));
        session.setExpiresAt(expiresAt);
        session.setRevokedAt(revoked ? expiresAt.minusSeconds(1) : null);
        session.setRefreshTokenHash(UUID.randomUUID().toString().replace("-", "").repeat(2));
        return session;
    }
}
