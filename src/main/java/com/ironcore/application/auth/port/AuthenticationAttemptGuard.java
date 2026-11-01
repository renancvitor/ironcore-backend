package com.ironcore.application.auth.port;

public interface AuthenticationAttemptGuard {
    void check(String account);
}
