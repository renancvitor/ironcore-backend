package com.ironcore.infrastructure.persistence.logging.error.mapper;

import com.ironcore.domain.logging.error.model.ErrorLog;
import com.ironcore.domain.logging.error.valueobject.ErrorCode;
import com.ironcore.domain.logging.error.valueobject.ErrorRequestContext;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.logging.error.entity.ErrorLogEntity;

public class ErrorLogMapper {

    public static ErrorLogEntity toEntity(ErrorLog errorLog) {
        try {
            return new ErrorLogEntity(
                    errorLog.getId(),
                    errorLog.getErrorCode().type(),
                    errorLog.getMessage(),
                    errorLog.getExceptionClass(),
                    errorLog.getRequestContext().path(),
                    errorLog.getRequestContext().httpMethod(),
                    errorLog.getUserId(),
                    errorLog.getCorrelationId(),
                    errorLog.getCreatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter error log de domínio para entidade.", exception);
        }
    }

    public static ErrorLog toDomain(ErrorLogEntity entity) {
        try {
            return new ErrorLog(
                    entity.getId(),
                    new ErrorCode(entity.getErrorCode()),
                    entity.getMessage(),
                    entity.getExceptionClass(),
                    new ErrorRequestContext(entity.getRequestPath(), entity.getHttpMethod()),
                    entity.getUserId(),
                    entity.getCorrelationId(),
                    entity.getCreatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter error log de entidade para domínio.", exception);
        }
    }
}
