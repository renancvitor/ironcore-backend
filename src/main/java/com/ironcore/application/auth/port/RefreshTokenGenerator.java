package com.ironcore.application.auth.port;

public interface RefreshTokenGenerator {

    String generate();

    String hash(String token);
}
