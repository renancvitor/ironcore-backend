package com.ironcore.infrastructure.scheduling.auth;

import com.ironcore.application.auth.service.AuthSessionCleanupService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthSessionCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuthSessionCleanupScheduler.class);
    private final AuthSessionCleanupService cleanupService;

    @Scheduled(cron = "0 0 3 * * *", zone = "America/Sao_Paulo")
    public void deleteExpiredSessions() {
        try {
            cleanupService.deleteExpiredSessions();
        } catch (RuntimeException exception) {
            log.error("Falha ao excluir sessões de autenticação expiradas.", exception);
        }
    }
}
