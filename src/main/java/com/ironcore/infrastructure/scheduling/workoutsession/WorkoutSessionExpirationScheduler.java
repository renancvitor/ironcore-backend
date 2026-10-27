package com.ironcore.infrastructure.scheduling.workoutsession;

import com.ironcore.application.workoutsession.workoutsession.expire.ExpireWorkoutSessionsUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class WorkoutSessionExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(WorkoutSessionExpirationScheduler.class);

    private final ExpireWorkoutSessionsUseCase expireWorkoutSessionsUseCase;

    @Scheduled(fixedDelay = 60, initialDelay = 0, timeUnit = TimeUnit.SECONDS)
    public void expireSessions() {
        try {
            expireWorkoutSessionsUseCase.execute();
        } catch (RuntimeException exception) {
            log.error("Falha na rotina de expiração de sessões.", exception);
        }
    }
}
