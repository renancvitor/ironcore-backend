package com.ironcore.infrastructure.security.jwt;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "security.token")
public class JwtTokenProperties {

    private String secret;
    private String issuer = "IronCore";
    private Long expirationMinutes = 120L;

    @PostConstruct
    void validate() {
        if (secret == null || secret.isBlank() || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("Configuração inválida: security.token.secret.");
        }

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("Configuração inválida: security.token.issuer.");
        }

        if (expirationMinutes == null || expirationMinutes <= 0) {
            throw new IllegalStateException("Configuração inválida: security.token.expirationMinutes");
        }
    }
}
