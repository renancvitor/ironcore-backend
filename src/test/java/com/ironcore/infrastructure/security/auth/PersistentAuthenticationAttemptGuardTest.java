package com.ironcore.infrastructure.security.auth;

import com.ironcore.application.auth.port.RefreshTokenGenerator;
import com.ironcore.infrastructure.persistence.auth.repository.AuthenticationAttemptJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.sql.SQLException;
import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PersistentAuthenticationAttemptGuardTest {
    private final AuthenticationAttemptJpaRepository repository = mock(AuthenticationAttemptJpaRepository.class);
    private final RefreshTokenGenerator hash = mock(RefreshTokenGenerator.class);
    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    private PersistentAuthenticationAttemptGuard guard;

    @BeforeEach
    void setup() {
        when(hash.hash("account@example.com")).thenReturn("bucket");
        when(manager.getTransaction(any())).thenAnswer(invocation -> {
            org.assertj.core.api.Assertions.assertThat(invocation.<TransactionDefinition>getArgument(0).getPropagationBehavior())
                    .isEqualTo(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            return new SimpleTransactionStatus();
        });
        when(repository.findLockedById("bucket")).thenReturn(Optional.empty());
        guard = new PersistentAuthenticationAttemptGuard(repository, hash, Clock.systemUTC(), manager);
    }

    @Test
    void doesNotRetryOtherIntegrityViolations() {
        var failure = new DataIntegrityViolationException("check constraint", new SQLException("check", "23514"));
        when(repository.saveAndFlush(any())).thenThrow(failure);
        assertThatThrownBy(() -> guard.check("account@example.com")).isSameAs(failure);
        verify(manager).getTransaction(any());
        verify(manager).rollback(any());
        verify(manager, never()).commit(any());
    }

    @Test
    void retriesDuplicateKeyOnlyOnceAndOnlyAfterRollback() {
        var failure = new DataIntegrityViolationException("duplicate", new SQLException("duplicate", "23505"));
        when(repository.saveAndFlush(any())).thenThrow(failure);
        assertThatThrownBy(() -> guard.check("account@example.com")).isSameAs(failure);
        var order = inOrder(manager);
        order.verify(manager).getTransaction(any());
        order.verify(manager).rollback(any());
        order.verify(manager).getTransaction(any());
        order.verify(manager).rollback(any());
        order.verifyNoMoreInteractions();
    }
}
