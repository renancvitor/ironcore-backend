package com.ironcore.application.person.usecase.update;

import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.Sex;

public record UpdatePersonResult(
        String name,
        Sex sex,
        BirthDate birthDate
) {
}
