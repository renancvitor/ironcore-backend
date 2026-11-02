package com.ironcore.infrastructure.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import com.ironcore.domain.exception.DomainException;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.jwt.exception.JwtTokenConfigurationException;
import com.ironcore.infrastructure.security.jwt.exception.JwtTokenValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtAccessTokenValidator {

    private final JwtTokenProperties properties;

    public JwtAccessTokenClaims validate(String token) {
        validateConfiguration();
        validateTokenPresence(token);

        try {
            DecodedJWT decodedJWT = verify(token);

            if (decodedJWT.getExpiresAt() == null) {
                throw new JwtTokenValidationException("JWT sem expiração.");
            }

            return toClaims(decodedJWT);
        } catch (JWTVerificationException | IllegalArgumentException | DomainException exception) {
            throw new JwtTokenValidationException("JWT inválido ou expirado.", exception);
        }
    }

    private void validateConfiguration() {
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            throw new JwtTokenConfigurationException("JWT secret não configurado.");
        }
    }

    private void validateTokenPresence(String token) {
        if (token == null || token.isBlank()) {
            throw new JwtTokenValidationException("JWT não informado.");
        }
    }

    private DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(properties.getSecret()))
                .withIssuer(properties.getIssuer())
                .build()
                .verify(token);
    }

    private JwtAccessTokenClaims toClaims(DecodedJWT decodedJWT) {
        String subject = decodedJWT.getSubject();
        String email = decodedJWT.getClaim("email").asString();
        Boolean mustChangePassword = decodedJWT.getClaim("mustChangePassword").asBoolean();
        String sessionId = decodedJWT.getClaim("sid").asString();

        validateSubject(subject);
        validateEmail(email);
        validateMustChangePassword(mustChangePassword);
        validateSessionId(sessionId);

        return new JwtAccessTokenClaims(
                new UserId(Long.valueOf(subject)),
                new Email(email),
                mustChangePassword,
                new AuthSessionId(UUID.fromString(sessionId))
        );
    }

    private void validateSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new JwtTokenValidationException("JWT sem subject.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new JwtTokenValidationException("JWT sem email.");
        }
    }

    private void validateMustChangePassword(Boolean mustChangePassword) {
        if (mustChangePassword == null) {
            throw new JwtTokenValidationException("JWT sem indicador de troca de senha.");
        }
    }

    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new JwtTokenValidationException("JWT sem identificador de sessão.");
        }
    }
}
