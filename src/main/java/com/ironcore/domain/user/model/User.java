package com.ironcore.domain.user.model;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.exception.InvalidUserException;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.UserId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class User {

    private final UserId id;
    private String nickname;
    private final Email email;
    private final PersonId personId;
    private PasswordHash passwordHash;
    private Boolean mustChangePassword;
    private Boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User(UserId id, String nickname, PersonId personId, Email email, PasswordHash passwordHash,
                 Boolean mustChangePassword, Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nickname = requireNonBlank(nickname, "Nome não pode ser nulo ou vazio.");
        this.personId = requireNonNull(personId, "PersonId não pode ser nulo.");
        this.email = requireNonNull(email, "E-mail não pode ser nulo.");
        this.passwordHash = requireNonNull(passwordHash, "Senha hash não pode ser nulo.");
        this.mustChangePassword = requireNonNull(mustChangePassword, "Tag de troca de senha não pode ser nulo.");
        this.active = requireNonNull(active, "Tag de usuário ativo não pode ser nulo.");
        this.createdAt = requireNonNull(createdAt, "Data de criação não pode ser nulo.");
        this.updatedAt = updatedAt;
    }

    public static User register(String nickname, PersonId personId, Email email, PasswordHash passwordHash,
                                LocalDateTime createdAt) {
        return new User(null, nickname, personId, email, passwordHash, true, true,
                createdAt, null);
    }

    public static User restore(UserId id, String nickname, PersonId personId, Email email, PasswordHash passwordHash,
                               Boolean mustChangePassword, Boolean active, LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
        return new User(id, nickname, personId, email, passwordHash, mustChangePassword, active, createdAt, updatedAt);
    }

    public boolean isActive() {
        return active;
    }

    public boolean mustChangePassword() {
        return mustChangePassword;
    }

    public void changeNickname(String nickname, LocalDateTime updatedAt) {
        this.nickname = requireNonBlank(nickname, "Apelido não pode ser nulo ou vazio.");
        markUpdatedAt(updatedAt);
    }

    public void changePasswordHash(PasswordHash passwordHash, LocalDateTime updatedAt) {
        this.passwordHash = requireNonNull(passwordHash, "Senha hash não pode ser nulo.");
        this.mustChangePassword = false;
        markUpdatedAt(updatedAt);
    }

    public void activate(LocalDateTime updatedAt) {
        this.active = true;
        markUpdatedAt(updatedAt);
    }

    public void deactivate(LocalDateTime updatedAt) {
        this.active = false;
        markUpdatedAt(updatedAt);
    }

    private void markUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = requireNonNull(updatedAt, "Data de atualização não pode ser nulo.");
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserException(message);
        }

        return value.trim();
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidUserException(message);
        }

        return value;
    }
}
