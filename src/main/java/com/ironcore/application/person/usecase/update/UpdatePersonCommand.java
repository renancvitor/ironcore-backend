package com.ironcore.application.person.usecase.update;

import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.Sex;
import com.ironcore.domain.user.valueobject.UserId;

public record UpdatePersonCommand(
        UserId actorUserId,
        String name,
        Sex sex,
        BirthDate birthDate
) {
}
