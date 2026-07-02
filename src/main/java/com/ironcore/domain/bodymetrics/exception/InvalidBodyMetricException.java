package com.ironcore.domain.bodymetrics.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidBodyMetricException extends DomainException {

    public InvalidBodyMetricException(String message) {
        super(message);
    }
}
