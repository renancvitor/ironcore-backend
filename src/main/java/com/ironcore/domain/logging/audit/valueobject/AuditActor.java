package com.ironcore.domain.logging.audit.valueobject;

import com.ironcore.domain.logging.audit.exception.InvalidAuditLogException;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

public record AuditActor(UserId userId, Email email) {

    public AuditActor {
        if (userId == null) {
            throw new InvalidAuditLogException("Id do ator não pode ser nulo");
        }

        if (email == null) {
            throw new InvalidAuditLogException("Email do ator não pode ser nulo");
        }
    }
}
