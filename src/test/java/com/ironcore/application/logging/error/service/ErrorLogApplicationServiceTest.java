package com.ironcore.application.logging.error.service;

import com.ironcore.application.logging.error.event.ErrorLogEvent;
import com.ironcore.domain.logging.error.enums.ErrorCodeType;
import com.ironcore.domain.logging.error.model.ErrorLog;
import com.ironcore.domain.logging.error.repository.ErrorLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ErrorLogApplicationServiceTest {

    @Mock
    private ErrorLogRepository errorLogRepository;

    @InjectMocks
    private ErrorLogApplicationService errorLogApplicationService;

    @Test
    void shouldRegisterErrorLogFromEvent() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 3, 20, 33);
        ErrorLogEvent event = new ErrorLogEvent(
                ErrorCodeType.AUTHENTICATION_ERROR,
                "Sem permissão",
                "Unauthorized.class",
                "127.0.0.0/admin",
                "PUT",
                1L,
                "404",
                createdAt
        );

        errorLogApplicationService.register(event);

        ArgumentCaptor<ErrorLog> captor = ArgumentCaptor.forClass(ErrorLog.class);
        verify(errorLogRepository).save(captor.capture());

        ErrorLog errorLog = captor.getValue();
        assertThat(errorLog.getId()).isNull();
        assertThat(errorLog.getErrorCode().type()).isEqualTo(ErrorCodeType.AUTHENTICATION_ERROR);
        assertThat(errorLog.getMessage()).isEqualTo("Sem permissão");
        assertThat(errorLog.getExceptionClass()).isEqualTo("Unauthorized.class");
        assertThat(errorLog.getRequestContext().path()).isEqualTo("127.0.0.0/admin");
        assertThat(errorLog.getRequestContext().httpMethod()).isEqualTo("PUT");
        assertThat(errorLog.getUserId()).isEqualTo(1L);
        assertThat(errorLog.getCorrelationId()).isEqualTo("404");
        assertThat(errorLog.getCreatedAt()).isEqualTo(createdAt);
    }
}
