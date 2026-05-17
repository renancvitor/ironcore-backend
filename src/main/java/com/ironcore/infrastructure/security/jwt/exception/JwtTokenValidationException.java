package com.ironcore.infrastructure.security.jwt.exception;

public class JwtTokenValidationException extends JwtTokenException {

    public JwtTokenValidationException(String message) {
        super(message);
    }

    public JwtTokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
