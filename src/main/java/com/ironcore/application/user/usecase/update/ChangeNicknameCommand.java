package com.ironcore.application.user.usecase.update;

import com.ironcore.domain.user.valueobject.UserId;

public record ChangeNicknameCommand(
        UserId actorUserId,
        String nickname
) {
}
