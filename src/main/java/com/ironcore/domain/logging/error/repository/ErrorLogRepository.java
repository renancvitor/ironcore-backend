package com.ironcore.domain.logging.error.repository;

import com.ironcore.domain.logging.error.model.ErrorLog;

public interface ErrorLogRepository {
    ErrorLog save(ErrorLog errorLog);
}
