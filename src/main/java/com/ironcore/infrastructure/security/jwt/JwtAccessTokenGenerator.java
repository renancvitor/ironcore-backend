package com.ironcore.infrastructure.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.ironcore.application.auth.port.AccessTokenGenerator;
import com.ironcore.application.auth.port.AccessTokenSubject;
import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.infrastructure.security.jwt.exception.JwtTokenConfigurationException;
import com.ironcore.infrastructure.security.jwt.exception.JwtTokenGenerationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class JwtAccessTokenGenerator implements AccessTokenGenerator {

    private static final String TOKEN_TYPE = "Bearer";

    private final JwtTokenProperties properties;
    private final ZoneId zoneId;

    public JwtAccessTokenGenerator(JwtTokenProperties properties) {
        this.properties = properties;
        this.zoneId = ZoneId.systemDefault();
    }

    @Override
    public GeneratedAccessToken generate(AccessTokenSubject subject) {
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            throw new JwtTokenConfigurationException("JWT secret não configurado.");
        }

        LocalDateTime expiresAt = LocalDateTime.now(zoneId).plusMinutes(properties.getExpirationMinutes());

        try {
            String token = JWT.create()
                .withIssuer(properties.getIssuer())
                .withSubject(String.valueOf(subject.userId().value()))
                .withClaim("email", subject.email().value())
                .withClaim("mustChangePassword", subject.mustChangePassword())
                .withExpiresAt(Date.from(expiresAt.atZone(zoneId).toInstant()))
                .sign(Algorithm.HMAC256(properties.getSecret()));

            return new GeneratedAccessToken(token, TOKEN_TYPE, expiresAt);
        } catch (JWTCreationException exception) {
            throw new JwtTokenGenerationException("Falha ao gerar JWT.", exception);
        }
    }
}
