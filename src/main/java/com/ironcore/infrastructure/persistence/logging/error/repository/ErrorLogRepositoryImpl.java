package com.ironcore.infrastructure.persistence.logging.error.repository;

import com.ironcore.domain.logging.error.model.ErrorLog;
import com.ironcore.domain.logging.error.repository.ErrorLogRepository;
import com.ironcore.infrastructure.persistence.logging.error.entity.ErrorLogEntity;
import com.ironcore.infrastructure.persistence.logging.error.mapper.ErrorLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ErrorLogRepositoryImpl implements ErrorLogRepository {

    private final ErrorLogJpaRepository errorLogJpaRepository;

    @Override
    public ErrorLog save(ErrorLog errorLog) {
        ErrorLogEntity entity = ErrorLogMapper.toEntity(errorLog);
        ErrorLogEntity saveErrorLog = errorLogJpaRepository.save(entity);
        return ErrorLogMapper.toDomain(saveErrorLog);
    }
}
