package com.ironcore.infrastructure.scheduling.workoutsession;

import com.ironcore.application.workoutsession.workoutsession.expire.ExpireWorkoutSessionsUseCase;
import com.ironcore.infrastructure.config.SchedulingConfig;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WorkoutSessionExpirationSchedulerTest {

    @Test
    void shouldConfigureImmediateExecutionAndSixtySecondDelay() throws Exception {
        Scheduled scheduled = WorkoutSessionExpirationScheduler.class.getMethod("expireSessions")
                .getAnnotation(Scheduled.class);

        assertThat(scheduled.timeUnit().toMillis(scheduled.fixedDelay())).isEqualTo(60_000);
        assertThat(scheduled.initialDelay()).isZero();
    }

    @Test
    void shouldAllowFollowingRunAfterFailure() {
        ExpireWorkoutSessionsUseCase useCase = mock(ExpireWorkoutSessionsUseCase.class);
        doThrow(new PersistenceException("Indisponível")).doNothing().when(useCase).execute();
        WorkoutSessionExpirationScheduler scheduler = new WorkoutSessionExpirationScheduler(useCase);

        scheduler.expireSessions();
        scheduler.expireSessions();

        verify(useCase, times(2)).execute();
    }

    @Test
    void shouldTriggerRecoveryOnEachApplicationContextStartup() throws Exception {
        for (int restart = 0; restart < 2; restart++) {
            CountDownLatch triggered = new CountDownLatch(1);
            ExpireWorkoutSessionsUseCase useCase = mock(ExpireWorkoutSessionsUseCase.class);
            doAnswer(invocation -> {
                triggered.countDown();
                return null;
            }).when(useCase).execute();

            try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
                context.registerBean(ExpireWorkoutSessionsUseCase.class, () -> useCase);
                context.register(SchedulingConfig.class, WorkoutSessionExpirationScheduler.class);
                context.refresh();

                assertThat(triggered.await(5, TimeUnit.SECONDS)).isTrue();
                verify(useCase).execute();
            }
        }
    }
}
