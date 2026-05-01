package com.ironcore.domain.logging.audit.valueobject;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

import java.util.Objects;

public record AuditActor(UserId userId, Email email) {

    public AuditActor {
        Objects.requireNonNull(userId, "Id do ator não pode ser nulo");
        Objects.requireNonNull(email, "Email do ator não pode ser nulo");
    }
}
