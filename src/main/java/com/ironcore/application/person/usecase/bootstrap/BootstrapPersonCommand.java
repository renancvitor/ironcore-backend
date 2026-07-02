package com.ironcore.application.person.usecase.bootstrap;

import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.Sex;

import java.time.LocalDateTime;

public record BootstrapPersonCommand(
        String name,
        Sex sex,
        BirthDate birthDate,
        LocalDateTime createdAt
) {
}
