package com.ironcore.application.auth.service;

import com.ironcore.application.auth.port.AuthenticationAttemptStore;
import com.ironcore.application.exception.AuthenticationRateLimitException;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class AuthenticationAttemptLimiter {

    private final AuthenticationAttemptStore store;
    private final Clock clock;
    private final int accountLimit;
    private final Duration accountWindow;
    private final int ipLimit;

    public AuthenticationAttemptLimiter(AuthenticationAttemptStore store, Clock clock,
            @Value("${security.auth-rate-limit.account-limit:20}") int accountLimit,
            @Value("${security.auth-rate-limit.account-window-seconds:300}") long accountWindowSeconds,
            @Value("${security.auth-rate-limit.ip-limit-per-minute:120}") int ipLimit) {
        if (accountLimit < 1 || accountLimit == Integer.MAX_VALUE || accountWindowSeconds < 1
                || ipLimit < 1 || ipLimit == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Configuração de limite de autenticação inválida.");
        }
        this.store = store;
        this.clock = clock;
        this.accountLimit = accountLimit;
        this.accountWindow = Duration.ofSeconds(accountWindowSeconds);
        this.ipLimit = ipLimit;
    }

    public Instant checkCredentials(Email email) {
        return reserve("credentials:" + email.value());
    }

    public Instant checkPasswordChange(UserId userId) {
        return reserve("password-change:" + userId.value());
    }

    public void releaseCredentials(Email email, Instant windowStartedAt) {
        store.releaseAttempt("credentials:" + email.value(), windowStartedAt);
    }

    public void releasePasswordChange(UserId userId, Instant windowStartedAt) {
        store.releaseAttempt("password-change:" + userId.value(), windowStartedAt);
    }

    private Instant reserve(String subject) {
        Instant now = clock.instant();
        var reservation = store.reserveAttempt(subject, now, accountWindow, accountLimit);
        if (!reservation.accepted()) {
            long milliseconds = Duration.between(now, reservation.windowStartedAt().plus(accountWindow)).toMillis();
            throw new AuthenticationRateLimitException(Math.max(1, (milliseconds + 999) / 1000));
        }
        return reservation.windowStartedAt();
    }

    public void checkClientIp(String ip) {
        check("ip:" + ip, Duration.ofMinutes(1), ipLimit);
    }

    public void deleteExpiredAttempts() {
        Duration retention = accountWindow.compareTo(Duration.ofMinutes(1)) > 0 ? accountWindow : Duration.ofMinutes(1);
        store.deleteExpiredBefore(clock.instant().minus(retention));
    }

    private void check(String subject, Duration window, int limit) {
        Instant now = clock.instant();
        store.registerAttempt(subject, now, window, limit).ifPresent(retryAt -> {
            long milliseconds = Duration.between(now, retryAt).toMillis();
            throw new AuthenticationRateLimitException(Math.max(1, (milliseconds + 999) / 1000));
        });
    }
}
