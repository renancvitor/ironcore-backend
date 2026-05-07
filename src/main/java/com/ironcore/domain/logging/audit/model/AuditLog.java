package com.ironcore.domain.logging.audit.model;

import com.ironcore.domain.logging.audit.exception.InvalidAuditLogException;
import com.ironcore.domain.logging.audit.valueobject.AuditAction;
import com.ironcore.domain.logging.audit.valueobject.AuditActor;
import com.ironcore.domain.logging.audit.valueobject.AuditSnapshot;
import com.ironcore.domain.logging.audit.valueobject.AuditTarget;

import java.time.LocalDateTime;

public class AuditLog {

    private Long id;

    private AuditActor actor;
    private AuditAction action;
    private AuditTarget target;

    private AuditSnapshot beforeState;
    private AuditSnapshot afterState;

    private LocalDateTime createdAt;

    public AuditLog() {}

    public AuditLog(Long id, AuditActor actor, AuditAction action, AuditTarget target,
                    AuditSnapshot beforeState, AuditSnapshot afterState, LocalDateTime createdAt) {
        this.id = id;
        this.actor = requireNonNull(actor, "Ator da auditoria não pode ser nulo");
        this.action = requireNonNull(action, "Ação da auditoria não pode ser nulo");
        this.target = requireNonNull(target, "Alvo da auditoria não pode ser nulo");
        this.beforeState = beforeState;
        this.afterState = afterState;
        this.createdAt = requireNonNull(createdAt, "Data de criação da auditoria não pode ser nulo");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuditActor getActor() {
        return actor;
    }

    public void setActor(AuditActor actor) {
        this.actor = requireNonNull(actor, "Ator da auditoria não pode ser nulo");
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = requireNonNull(action, "Ação da auditoria não pode ser nulo");
    }

    public AuditTarget getTarget() {
        return target;
    }

    public void setTarget(AuditTarget target) {
        this.target = requireNonNull(target, "Alvo da auditoria não pode ser nulo");
    }

    public AuditSnapshot getBeforeState() {
        return beforeState;
    }

    public void setBeforeState(AuditSnapshot beforeState) {
        this.beforeState = beforeState;
    }

    public AuditSnapshot getAfterState() {
        return afterState;
    }

    public void setAfterState(AuditSnapshot afterState) {
        this.afterState = afterState;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = requireNonNull(createdAt, "Data de criação da auditoria não pode ser nulo");
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidAuditLogException(message);
        }

        return value;
    }
}
