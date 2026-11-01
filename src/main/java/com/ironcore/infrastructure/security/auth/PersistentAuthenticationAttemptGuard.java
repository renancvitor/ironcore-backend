package com.ironcore.infrastructure.security.auth;

import com.ironcore.application.auth.port.AuthenticationAttemptGuard;
import com.ironcore.application.auth.port.RefreshTokenGenerator;
import com.ironcore.application.exception.AuthenticationRateLimitException;
import com.ironcore.infrastructure.persistence.auth.entity.AuthenticationAttemptEntity;
import com.ironcore.infrastructure.persistence.auth.repository.AuthenticationAttemptJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.util.Locale;

@Component
public class PersistentAuthenticationAttemptGuard implements AuthenticationAttemptGuard {
    private static final Duration WINDOW = Duration.ofMinutes(5);
    private final AuthenticationAttemptJpaRepository repository;
    private final RefreshTokenGenerator hash;
    private final Clock clock;
    private final TransactionTemplate transaction;

    public PersistentAuthenticationAttemptGuard(AuthenticationAttemptJpaRepository repository,
            RefreshTokenGenerator hash, Clock clock, PlatformTransactionManager manager) {
        this.repository = repository;
        this.hash = hash;
        this.clock = clock;
        this.transaction = new TransactionTemplate(manager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void check(String account) {
        String id = hash.hash(account.trim().toLowerCase(Locale.ROOT));
        // execute returns only after commit/rollback. A duplicate insert must be
        // retried outside that transaction: PostgreSQL has already aborted it.
        Boolean allowed;
        try {
            allowed = transaction.execute(status -> reserveAttempt(id));
        } catch (DataIntegrityViolationException concurrentCreation) {
            if (!isDuplicateKey(concurrentCreation)) throw concurrentCreation;
            allowed = transaction.execute(status -> reserveAttempt(id));
        }
        if (!Boolean.TRUE.equals(allowed)) throw new AuthenticationRateLimitException();
    }

    private boolean reserveAttempt(String id) {
        repository.deleteByWindowStartedAtBefore(clock.instant().minus(Duration.ofDays(1)));
        var entity = repository.findLockedById(id).orElse(null);
        if (entity == null) {
            entity = new AuthenticationAttemptEntity();
            entity.setId(id);
            entity.setWindowStartedAt(clock.instant());
        } else if (!clock.instant().isBefore(entity.getWindowStartedAt().plus(WINDOW))) {
            entity.setWindowStartedAt(clock.instant());
            entity.setAttempts(0);
        }
        if (entity.getAttempts() >= 10) return false;
        entity.setAttempts(entity.getAttempts() + 1);
        repository.saveAndFlush(entity);
        return true;
    }

    private boolean isDuplicateKey(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && "23505".equals(sql.getSQLState())) return true;
        }
        return false;
    }
}
