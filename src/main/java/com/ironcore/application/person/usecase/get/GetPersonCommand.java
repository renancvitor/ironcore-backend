package com.ironcore.application.person.usecase.get;

import com.ironcore.domain.user.valueobject.UserId;

public record GetPersonCommand(
        UserId authenticatedUserId
) {
}
