package com.ironcore.infrastructure.security.jwt.exception;

import com.ironcore.infrastructure.exception.InfrastructureException;

public class JwtTokenException extends InfrastructureException {

    public JwtTokenException(String message) {
        super(message);
    }

    public JwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
