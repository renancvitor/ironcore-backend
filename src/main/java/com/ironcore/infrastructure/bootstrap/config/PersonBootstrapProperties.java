package com.ironcore.infrastructure.bootstrap.config;

import com.ironcore.domain.person.enums.SexType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;

@ConfigurationProperties(prefix = "ironcore.bootstrap.person")
public record PersonBootstrapProperties(
        boolean enabled,
        String name,
        SexType sex,
        LocalDate birthDate
) {
}
