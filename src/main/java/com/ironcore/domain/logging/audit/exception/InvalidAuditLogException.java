package com.ironcore.domain.logging.audit.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidAuditLogException extends DomainException {

    public InvalidAuditLogException(String message) {
        super(message);
    }
}
