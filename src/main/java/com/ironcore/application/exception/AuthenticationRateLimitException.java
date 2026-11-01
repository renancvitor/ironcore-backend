package com.ironcore.application.exception;

public class AuthenticationRateLimitException extends ApplicationException {
    public AuthenticationRateLimitException() {
        super("Muitas tentativas. Aguarde antes de tentar novamente.");
    }
}
