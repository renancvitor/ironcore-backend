package com.ironcore.interfaces.rest.auth;

import com.ironcore.infrastructure.persistence.auth.repository.AuthenticationAttemptJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.reset;

@Testcontainers
class SessionLifecyclePostgresIntegrationTest extends SessionLifecycleIntegrationTest {
    @MockitoSpyBean
    AuthenticationAttemptJpaRepository attemptRepository;

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
        properties.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        properties.add("spring.flyway.enabled", () -> "true");
    }

    @Test
    void duplicateBucketInsertRollsBackAndRetriesInANewPostgresTransaction() throws Exception {
        String id = refreshTokens.hash("forced-collision@example.com");
        var bothObservedMissingBucket = new CyclicBarrier(2);
        var reads = new AtomicInteger();
        var duplicateInserts = new AtomicInteger();
        var transactionIds = new ConcurrentLinkedQueue<Long>();
        var delegate = mockingDetails(attemptRepository).getMockCreationSettings().getDefaultAnswer();
        doAnswer(invocation -> {
            var result = delegate.answer(invocation);
            transactionIds.add(jdbc.queryForObject("select txid_current()", Long.class));
            if (reads.incrementAndGet() <= 2) {
                assertThat((java.util.Optional<?>) result).isEmpty();
                bothObservedMissingBucket.await(10, TimeUnit.SECONDS);
            }
            return result;
        }).when(attemptRepository).findLockedById(id);
        doAnswer(invocation -> {
            try {
                return delegate.answer(invocation);
            } catch (DataIntegrityViolationException duplicate) {
                duplicateInserts.incrementAndGet();
                throw duplicate;
            }
        }).when(attemptRepository).saveAndFlush(any());
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> attemptGuard.check("forced-collision@example.com"));
            var second = executor.submit(() -> attemptGuard.check("forced-collision@example.com"));
            first.get(20, TimeUnit.SECONDS);
            second.get(20, TimeUnit.SECONDS);
            assertThat(duplicateInserts.get()).isEqualTo(1);
            assertThat(transactionIds).hasSize(3).doesNotHaveDuplicates();
            assertThat(attemptRepository.findById(id).orElseThrow().getAttempts()).isEqualTo(2);
        } finally {
            reset(attemptRepository);
        }
    }
}
