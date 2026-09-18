package com.ironcore.application.person.usecase.get;

import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.person.valueobject.Sex;

public record GetPersonResult(
        PersonId personId,
        String name,
        Sex sex,
        BirthDate birthDate
) {
}
