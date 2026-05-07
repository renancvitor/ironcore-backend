package com.ironcore.infrastructure.persistence.logging.error.repository;

import com.ironcore.domain.logging.error.model.ErrorLog;
import com.ironcore.domain.logging.error.repository.ErrorLogRepository;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.logging.error.entity.ErrorLogEntity;
import com.ironcore.infrastructure.persistence.logging.error.mapper.ErrorLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ErrorLogRepositoryImpl implements ErrorLogRepository {

    private final ErrorLogJpaRepository errorLogJpaRepository;

    @Override
    public ErrorLog save(ErrorLog errorLog) {
        ErrorLogEntity entity;
        try {
            entity = Objects.requireNonNull(
                    ErrorLogMapper.toEntity(errorLog),
                    "ErrorLogMapper retornou entidade nula."
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter error log de domínio para entidade.", exception);
        }

        ErrorLogEntity savedEntity;
        try {
            savedEntity = Objects.requireNonNull(
                    errorLogJpaRepository.save(entity),
                    "ErrorLogJpaRepository retornou entidade nula após persistência."
            );
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao persistir error log.", exception);
        }

        try {
            return ErrorLogMapper.toDomain(savedEntity);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter error log persistido para domínio.", exception);
        }
    }
}
