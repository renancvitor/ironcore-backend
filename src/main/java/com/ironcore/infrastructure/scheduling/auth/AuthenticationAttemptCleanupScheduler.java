package com.ironcore.infrastructure.scheduling.auth;

import com.ironcore.application.auth.service.AuthenticationAttemptLimiter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class AuthenticationAttemptCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationAttemptCleanupScheduler.class);
    private final AuthenticationAttemptLimiter limiter;

    @Scheduled(fixedDelay = 5, initialDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void deleteExpiredAttempts() {
        try {
            limiter.deleteExpiredAttempts();
        } catch (RuntimeException exception) {
            log.error("Falha na limpeza de limites de autenticação.", exception);
        }
    }
}
