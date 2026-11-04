package com.ironcore.infrastructure.scheduling.auth;

import com.ironcore.application.auth.service.AuthSessionCleanupService;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;

import java.time.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class AuthSessionCleanupSchedulerTest {
    @Test
    void shouldScheduleOnceADayAtThreeInSaoPaulo() throws Exception {
        Scheduled scheduled = AuthSessionCleanupScheduler.class.getMethod("deleteExpiredBefore")
                .getAnnotation(Scheduled.class);
        assertThat(scheduled).isNotNull();
        assertThat(scheduled.zone()).isEqualTo("America/Sao_Paulo");
        CronExpression cron = CronExpression.parse(scheduled.cron());
        ZonedDateTime start = ZonedDateTime.of(2026, 11, 3, 0, 0, 0, 0, ZoneId.of(scheduled.zone()));
        ZonedDateTime next = cron.next(start);
        assertThat(next).isEqualTo(start.withHour(3));
        assertThat(cron.next(next)).isEqualTo(next.plusDays(1));
    }

    @Test
    void shouldLogFailureAndAllowFollowingRun(CapturedOutput output) {
        AuthSessionCleanupService service = mock(AuthSessionCleanupService.class);
        doThrow(new PersistenceException("Simulated cleanup failure")).doNothing()
                .when(service).deleteExpiredBefore();
        AuthSessionCleanupScheduler scheduler = new AuthSessionCleanupScheduler(service);

        assertThatCode(scheduler::deleteExpiredBefore).doesNotThrowAnyException();
        scheduler.deleteExpiredBefore();

        verify(service, times(2)).deleteExpiredBefore();
        assertThat(output.getOut()).contains("ERROR", "Falha ao excluir sessões de autenticação expiradas.",
                "Simulated cleanup failure");
    }
}
