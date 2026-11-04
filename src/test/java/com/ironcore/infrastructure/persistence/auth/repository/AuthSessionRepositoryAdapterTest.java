package com.ironcore.infrastructure.persistence.auth.repository;

import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthSessionRepositoryAdapterTest {
    private final AuthSessionJpaRepository jpa = mock(AuthSessionJpaRepository.class);
    private final UserJpaRepository users = mock(UserJpaRepository.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final AuthSessionRepositoryAdapter adapter = new AuthSessionRepositoryAdapter(jpa, users, entityManager);
    private final Instant cutoff = Instant.parse("2026-10-19T12:00:00Z");

    @Test
    void shouldDelegateBulkDeletionWithoutLoadingSessionsOrUsers() {
        adapter.deleteExpiredBefore(cutoff);

        verify(jpa).deleteExpiredBefore(cutoff);
        verifyNoMoreInteractions(jpa);
        verifyNoInteractions(users, entityManager);
    }

    @Test
    void shouldWrapDeletionFailurePreservingItsCause() {
        RuntimeException failure = new IllegalStateException("Database unavailable");
        doThrow(failure).when(jpa).deleteExpiredBefore(cutoff);

        assertThatThrownBy(() -> adapter.deleteExpiredBefore(cutoff))
                .isInstanceOf(PersistenceException.class)
                .hasMessage("Falha ao excluir sessões de autenticação expiradas.")
                .hasCause(failure);
    }
}
