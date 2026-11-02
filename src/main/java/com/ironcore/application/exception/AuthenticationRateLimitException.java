package com.ironcore.application.exception;

public class AuthenticationRateLimitException extends ApplicationException {

    private final long retryAfterSeconds;

    public AuthenticationRateLimitException(long retryAfterSeconds) {
        super("Muitas tentativas. Aguarde antes de tentar novamente.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
