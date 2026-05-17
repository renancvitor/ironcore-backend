package com.ironcore.infrastructure.security.jwt.exception;

public class JwtTokenConfigurationException extends JwtTokenException {

    public JwtTokenConfigurationException(String message) {
        super(message);
    }

    public JwtTokenConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
