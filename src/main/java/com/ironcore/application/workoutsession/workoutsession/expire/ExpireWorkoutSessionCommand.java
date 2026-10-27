package com.ironcore.application.workoutsession.workoutsession.expire;

import com.ironcore.domain.person.valueobject.PersonId;

public record ExpireWorkoutSessionCommand(
        PersonId personId
) {
}
