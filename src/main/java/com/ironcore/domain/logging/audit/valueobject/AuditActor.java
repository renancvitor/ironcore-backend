package com.ironcore.domain.logging.audit.valueobject;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

public record AuditActor(UserId userId, Email email) {

    public AuditActor {
        if (userId == null) {
            throw new IllegalArgumentException("Id do ator não pode ser nulo");
        }

        if (email == null) {
            throw new IllegalArgumentException("Email do ator não pode ser nulo");
        }
    }
}
