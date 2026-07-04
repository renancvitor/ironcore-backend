package com.ironcore.application.user;

import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.domain.user.model.User;

public record UserAuditData(
        Long id,
        String nickname
) implements LoggableData {

    public static UserAuditData from(User user) {
        return new UserAuditData(
                user.getId().value(),
                user.getNickname()
        );
    }
}
