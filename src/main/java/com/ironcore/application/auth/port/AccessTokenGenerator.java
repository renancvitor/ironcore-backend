package com.ironcore.application.auth.port;

public interface AccessTokenGenerator {

    GeneratedAccessToken generate(AccessTokenSubject subject);
}
