package com.ironcore.infrastructure.bootstrap.config;

import com.ironcore.domain.user.enums.SexType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ironcore.bootstrap.single-user")
public record SingleUserBootstrapProperties(
        boolean enabled,
        String name,
        String email,
        String password,
        SexType sex
) {
}
