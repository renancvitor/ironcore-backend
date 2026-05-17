package com.ironcore.infrastructure.security.jwt.exception;

public class JwtTokenGenerationException extends JwtTokenException {

    public JwtTokenGenerationException(String message) {
        super(message);
    }

    public JwtTokenGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
