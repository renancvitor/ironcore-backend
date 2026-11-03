package com.ironcore.application.auth.service;

import com.ironcore.domain.auth.repository.AuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthSessionCleanupService {

    private final AuthSessionRepository authSessionRepository;
    private final Clock clock;

    @Transactional
    public void deleteExpiredBefore() {
        Instant now = clock.instant();
        Instant cutoff = now.minus(Duration.ofDays(15));

        authSessionRepository.deleteExpiredBefore(cutoff);
    }
}
