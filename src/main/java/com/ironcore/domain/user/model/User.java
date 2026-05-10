package com.ironcore.domain.user.model;

import com.ironcore.domain.user.exception.InvalidUserException;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.Sex;
import com.ironcore.domain.user.valueobject.UserId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class User {

    private UserId id;
    private String name;
    private Email email;
    private PasswordHash passwordHash;
    private Sex sex;
    private Boolean mustChangePassword;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User(UserId id, String name, Email email, PasswordHash passwordHash, Sex sex, Boolean mustChangePassword,
                 Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = requireNonBlank(name, "Nome não pode ser nulo ou vazio");
        this.email = requireNonNull(email, "E-mail não pode ser nulo");
        this.passwordHash = requireNonNull(passwordHash, "Senha hash não pode ser nulo");
        this.sex = requireNonNull(sex, "Sexo não pode ser nulo");
        this.mustChangePassword = requireNonNull(mustChangePassword, "Tag de troca de senha não pode ser nulo");
        this.active = requireNonNull(active, "Tag de usuário ativo não pode ser nulo");
        this.createdAt = requireNonNull(createdAt, "Data de criação não pode ser nulo");
        this.updatedAt = updatedAt;
    }

    public static User register(String name, Email email, PasswordHash passwordHash, Sex sex, LocalDateTime createdAt) {
        return new User(null, name, email, passwordHash, sex, true, true, createdAt, null);
    }

    public static User restore(UserId id, String name, Email email, PasswordHash passwordHash, Sex sex,
                               Boolean mustChangePassword, Boolean active, LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
        return new User(id, name, email, passwordHash, sex, mustChangePassword, active, createdAt, updatedAt);
    }

    public boolean isActive() {
        return active;
    }

    public boolean mustChangePassword() {
        return mustChangePassword;
    }

    public void rename(String name, LocalDateTime updatedAt) {
        this.name = requireNonBlank(name, "Nome não pode ser nulo ou vazio");
        markUpdatedAt(updatedAt);
    }

    public void changePasswordHash(PasswordHash passwordHash, LocalDateTime updatedAt) {
        this.passwordHash = requireNonNull(passwordHash, "Senha hash não pode ser nulo");
        this.mustChangePassword = false;
        markUpdatedAt(updatedAt);
    }

    public void changeSex(Sex sex, LocalDateTime updatedAt) {
        this.sex = requireNonNull(sex, "Sexo não pode ser nulo");
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
        this.updatedAt = requireNonNull(updatedAt, "Data de atualização não pode ser nulo");
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
