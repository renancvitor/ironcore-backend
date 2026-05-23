package com.ironcore.infrastructure.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ironcore.domain.exception.DomainException;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.jwt.exception.JwtTokenConfigurationException;
import com.ironcore.infrastructure.security.jwt.exception.JwtTokenValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtAccessTokenValidator {

    private final JwtTokenProperties properties;

    public JwtAccessTokenClaims validate(String token) {
        validateConfiguration();
        validateTokenPresence(token);

        try {
            DecodedJWT decodedJWT = verify(token);
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

        validateSubject(subject);
        validateEmail(email);
        validateMustChangePassword(mustChangePassword);

        return new JwtAccessTokenClaims(
                new UserId(Long.valueOf(subject)),
                new Email(email),
                mustChangePassword
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
}
