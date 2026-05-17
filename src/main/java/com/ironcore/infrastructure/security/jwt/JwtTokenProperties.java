package com.ironcore.infrastructure.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.token")
public class JwtTokenProperties {

    private String secret;
    private String issuer = "IronCore";
    private Long expirationMinutes = 60L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(Long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }
}
