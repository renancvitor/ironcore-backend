package com.ironcore.infrastructure.bootstrap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ironcore.bootstrap.single-user")
public record SingleUserBootstrapProperties(
        boolean enabled,
        String nickname,
        Long personId,
        String email,
        String password
) {
}
