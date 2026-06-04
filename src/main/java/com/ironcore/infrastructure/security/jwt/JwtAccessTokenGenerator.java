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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class JwtAccessTokenGenerator implements AccessTokenGenerator {

    private static final String TOKEN_TYPE = "Bearer";

    private final JwtTokenProperties properties;
    private final Clock clock;

    public JwtAccessTokenGenerator(JwtTokenProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public GeneratedAccessToken generate(AccessTokenSubject subject) {
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            throw new JwtTokenConfigurationException("JWT secret não configurado.");
        }

        Instant expiresAtInstant = clock.instant().plus(properties.getExpirationMinutes(), ChronoUnit.MINUTES);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(expiresAtInstant, clock.getZone());

        try {
            String token = JWT.create()
                .withIssuer(properties.getIssuer())
                .withSubject(String.valueOf(subject.userId().value()))
                .withClaim("email", subject.email().value())
                .withClaim("mustChangePassword", subject.mustChangePassword())
                .withExpiresAt(expiresAtInstant)
                .sign(Algorithm.HMAC256(properties.getSecret()));

            return new GeneratedAccessToken(token, TOKEN_TYPE, expiresAt);
        } catch (JWTCreationException exception) {
            throw new JwtTokenGenerationException("Falha ao gerar JWT.", exception);
        }
    }
}
