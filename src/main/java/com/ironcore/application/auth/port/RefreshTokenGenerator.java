package com.ironcore.application.auth.port;

import com.ironcore.domain.auth.valueobject.AuthSessionId;

public interface RefreshTokenGenerator {
    String generate(AuthSessionId sessionId);
    String hash(String token);
    AuthSessionId sessionId(String token);
}
