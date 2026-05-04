package com.ironcore.application.logging.error.service;

import com.ironcore.application.logging.error.event.ErrorLogEvent;
import com.ironcore.domain.logging.error.model.ErrorLog;
import com.ironcore.domain.logging.error.repository.ErrorLogRepository;
import com.ironcore.domain.logging.error.valueobject.ErrorCode;
import com.ironcore.domain.logging.error.valueobject.ErrorRequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ErrorLogApplicationService {

    private final ErrorLogRepository errorLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void register(ErrorLogEvent event) {
        ErrorLog errorLog = new ErrorLog(
                null,
                new ErrorCode(event.errorCode()),
                event.message(),
                event.exceptionClass(),
                new ErrorRequestContext(event.path(), event.httpMethod()),
                event.userId(),
                event.correlationId(),
                event.createdAt()
        );

        errorLogRepository.save(errorLog);
    }
}
