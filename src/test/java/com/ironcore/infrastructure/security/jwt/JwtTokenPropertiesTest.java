package com.ironcore.infrastructure.security.jwt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class JwtTokenPropertiesTest {

    private JwtTokenProperties validProperties() {
        JwtTokenProperties properties = new JwtTokenProperties();
        properties.setSecret("a".repeat(32));
        return properties;
    }

    @Test
    void shouldAcceptMinimumSecretLengthAndDefaultSettings() {
        assertThatCode(() -> validProperties().validate()).doesNotThrowAnyException();
    }

    @Test
    void shouldMeasureSecretLengthInUtf8Bytes() {
        JwtTokenProperties properties = validProperties();
        properties.setSecret("é".repeat(16));
        assertThatCode(properties::validate).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "1234567890123456789012345678901"})
    void shouldRejectInvalidSecret(String secret) {
        JwtTokenProperties properties = validProperties();
        properties.setSecret(secret);
        assertThatIllegalStateException().isThrownBy(properties::validate)
                .withMessage("Configuração inválida: security.token.secret.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectInvalidIssuer(String issuer) {
        JwtTokenProperties properties = validProperties();
        properties.setIssuer(issuer);
        assertThatIllegalStateException().isThrownBy(properties::validate)
                .withMessage("Configuração inválida: security.token.issuer.");
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.NullSource
    @ValueSource(longs = {0, -1})
    void shouldRejectInvalidExpiration(Long expiration) {
        JwtTokenProperties properties = validProperties();
        properties.setExpirationMinutes(expiration);
        assertThatIllegalStateException().isThrownBy(properties::validate)
                .withMessage("Configuração inválida: security.token.expirationMinutes");
    }
}
