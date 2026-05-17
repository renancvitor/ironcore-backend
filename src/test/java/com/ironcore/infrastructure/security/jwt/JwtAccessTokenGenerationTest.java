package com.ironcore.infrastructure.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ironcore.application.auth.port.AccessTokenSubject;
import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.jwt.exception.JwtTokenConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JwtAccessTokenGenerationTest {

    private static final String SECRET = "test-secret-with-enough-length";
    private static final String ISSUER = "IronCore Test";
    private static final Long EXPIRATION_MINUTES = 60L;

    private JwtTokenProperties properties;
    private JwtAccessTokenGenerator generator;

    @BeforeEach
    void setUp() {
        properties = validProperties();
        generator = new JwtAccessTokenGenerator(properties);
    }

    @Nested
    class SuccessfulGeneration {

        @Test
        void shouldGenerateJwtWithExpectedClaims() {
            GeneratedAccessToken result = generator.generate(subject());

            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(SECRET))
                    .withIssuer(ISSUER)
                    .build()
                    .verify(result.value());

            assertThat(result.value()).isNotBlank();
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.expiresAt()).isAfter(LocalDateTime.now());

            assertThat(decodedJWT.getIssuer()).isEqualTo(ISSUER);
            assertThat(decodedJWT.getSubject()).isEqualTo("1");
            assertThat(decodedJWT.getClaim("email").asString()).isEqualTo("renan@example.com");
            assertThat(decodedJWT.getClaim("mustChangePassword").asBoolean()).isTrue();
            assertThat(decodedJWT.getExpiresAt()).isNotNull();
        }
    }

    @Nested
    class ConfigurationValidation {

        @Test
        void shouldFailWhenSecretIsNull() {
            properties.setSecret(null);
            generator = new JwtAccessTokenGenerator(properties);

            assertThatExceptionOfType(JwtTokenConfigurationException.class)
                    .isThrownBy(() -> generator.generate(subject()))
                    .withMessage("JWT secret não configurado.");
        }

        @Test
        void shouldFailWhenSecretIsBlank() {
            properties.setSecret("");
            generator = new JwtAccessTokenGenerator(properties);

            assertThatExceptionOfType(JwtTokenConfigurationException.class)
                    .isThrownBy(() -> generator.generate(subject()))
                    .withMessage("JWT secret não configurado.");
        }
    }

    private JwtTokenProperties validProperties() {
        JwtTokenProperties properties = new JwtTokenProperties();
        properties.setSecret(SECRET);
        properties.setIssuer(ISSUER);
        properties.setExpirationMinutes(EXPIRATION_MINUTES);

        return properties;
    }

    private AccessTokenSubject subject() {
        return new AccessTokenSubject(
                new UserId(1L),
                new Email("renan@example.com"),
                true
        );
    }
}
