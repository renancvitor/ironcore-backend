package com.ironcore.infrastructure.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "security.token")
public class JwtTokenProperties {

    private String secret;
    private String issuer = "IronCore";
    private Long expirationMinutes = 120L;

}
