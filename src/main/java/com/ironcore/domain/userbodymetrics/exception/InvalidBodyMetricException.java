package com.ironcore.domain.userbodymetrics.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidBodyMetricException extends DomainException {

    public InvalidBodyMetricException(String message) {
        super(message);
    }
}
