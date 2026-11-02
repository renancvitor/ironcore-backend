package com.ironcore.application.auth.port;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public interface AuthenticationAttemptStore {

    Optional<Instant> registerAttempt(String subject, Instant now, Duration window, int limit);

    Reservation reserveAttempt(String subject, Instant now, Duration window, int limit);

    void releaseAttempt(String subject, Instant windowStartedAt);

    record Reservation(Instant windowStartedAt, boolean accepted) {}

    void deleteExpiredBefore(Instant cutoff);
}
