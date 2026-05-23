package com.ironcore.infrastructure.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.jwt.exception.JwtTokenConfigurationException;
import com.ironcore.infrastructure.security.jwt.exception.JwtTokenValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JwtAccessTokenValidatorTest {

    private static final String SECRET = "test-secret-with-enough-lenght";
    private static final String ISSUER = "IconCore Test";

    private JwtTokenProperties properties;
    private JwtAccessTokenValidator validator;

    @BeforeEach
    void setUp() {
        properties = validProperties();
        validator = new JwtAccessTokenValidator(properties);
    }

    @Nested
    class SuccessValidation {

        @Test
        void shouldReturnClaimsWhenTokenIsValid() {
            String token = validToken();

            JwtAccessTokenClaims claims = validator.validate(token);

            assertThat(claims.userId()).isEqualTo(new UserId(1L));
            assertThat(claims.email()).isEqualTo(new Email("renan@example.com"));
            assertThat(claims.mustChangePassword()).isTrue();
        }
    }

    @Nested
    class TokenValidation {

        @Test
        void shouldFailWhenTokenIsNull() {
            String token = null;

            assertThatExceptionOfType(JwtTokenValidationException.class)
                    .isThrownBy(() -> validator.validate(token))
                    .withMessage("JWT não informado.");
        }

        @Test
        void shouldFailWhenTokenIsBlank() {
            String token = "";

            assertThatExceptionOfType(JwtTokenValidationException.class)
                    .isThrownBy(() -> validator.validate(token))
                    .withMessage("JWT não informado.");
        }

        @Test
        void shouldFailWhenIssuerIsInvalid() {
            String token =  validToken();
            properties.setIssuer("invalid-issuer");

            assertThatExceptionOfType(JwtTokenValidationException.class)
                    .isThrownBy(() -> validator.validate(token))
                    .withMessage("JWT inválido ou expirado.");
        }

        @Test
        void shouldFailWhenSubjectIsNotNumeric() {
            String token =  token(
                    ISSUER,
                    "abc",
                    "renan@example.com",
                    true,
                    SECRET
            );

            assertThatExceptionOfType(JwtTokenValidationException.class)
                    .isThrownBy(() -> validator.validate(token))
                    .withMessage("JWT inválido ou expirado.");
        }

        @Test
        void shouldFailWhenEmailClaimIsMissing() {
            String token = tokenWithoutEmailClaim();

            assertThatExceptionOfType(JwtTokenValidationException.class)
                    .isThrownBy(() -> validator.validate(token))
                    .withMessage("JWT sem email.");
        }

        @Test
        void shouldFailWhenMustChangePasswordClaimIsMissing() {
            String token = tokenWithoutMustChangePasswordClaim();

            assertThatExceptionOfType(JwtTokenValidationException.class)
                    .isThrownBy(() -> validator.validate(token))
                    .withMessage("JWT sem indicador de troca de senha.");
        }
    }

    @Nested
    class ConfigurationValidation {

        @Test
        void shouldFailWhenSecretIsNull() {
            properties.setSecret(null);
            String token = validToken();

            assertThatExceptionOfType(JwtTokenConfigurationException.class)
                    .isThrownBy(() -> validator.validate(token))
                    .withMessage("JWT secret não configurado.");
        }

        @Test
        void shouldFailWhenSecretIsBlank() {
            properties.setSecret("");
            String token = validToken();

            assertThatExceptionOfType(JwtTokenConfigurationException.class)
                    .isThrownBy(() -> validator.validate(token))
                    .withMessage("JWT secret não configurado.");
        }
    }

    private JwtTokenProperties validProperties() {
        JwtTokenProperties properties = new JwtTokenProperties();
        properties.setSecret(SECRET);
        properties.setIssuer(ISSUER);
        properties.setExpirationMinutes(60L);
        return properties;
    }

    private String validToken() {
        return token(
                ISSUER,
                "1",
                "renan@example.com",
                true,
                SECRET
        );
    }

    public String tokenWithoutEmailClaim() {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject("1")
                .withClaim("mustChangePassword", true)
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .sign(Algorithm.HMAC256(SECRET));
    }

    public String tokenWithoutMustChangePasswordClaim() {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject("1")
                .withClaim("email", "renan@example.com")
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .sign(Algorithm.HMAC256(SECRET));
    }

    private String token(
            String issuer,
            String subject,
            String email,
            Boolean mustChangePassword,
            String secret
    ) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(subject)
                .withClaim("email", email)
                .withClaim("mustChangePassword", mustChangePassword)
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .sign(Algorithm.HMAC256(secret));
    }
}
