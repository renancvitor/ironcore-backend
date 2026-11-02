package com.ironcore.infrastructure.security.token;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecureRefreshTokenGeneratorTest {

    private final SecureRefreshTokenGenerator generator = new SecureRefreshTokenGenerator();

    @Test
    void generatesIndependentUrlSafeCredentialsAndHashesThemDeterministically() {
        String first = generator.generate();
        String second = generator.generate();

        assertThat(first).matches("[A-Za-z0-9_-]{43}").isNotEqualTo(second);
        assertThat(java.util.Base64.getUrlDecoder().decode(first)).hasSize(32);
        assertThat(generator.hash(first)).matches("[0-9a-f]{64}")
                .isEqualTo(generator.hash(first)).isNotEqualTo(generator.hash(second));
    }

    @Test
    void rejectsMissingAndMalformedCredentialsWithoutIncludingThemInErrors() {
        assertThatThrownBy(() -> generator.hash(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> generator.hash("sensitive-invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotContaining("sensitive-invalid-token");
    }
}
