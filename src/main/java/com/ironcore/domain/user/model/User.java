package com.ironcore.domain.user.model;

import com.ironcore.domain.user.exception.InvalidUserException;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.Sex;
import com.ironcore.domain.user.valueobject.UserId;

import java.time.LocalDateTime;

public class User {

    private UserId id;
    private String name;
    private Email email;
    private PasswordHash passwordHash;
    private Sex sex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
    }

    public User(UserId id, String name, Email email, PasswordHash passwordHash, Sex sex, LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id = id;
        this.name = requireNonBlank(name, "Nome não pode ser nulo ou vazio");
        this.email = requireNonNull(email, "E-mail não pode ser nulo");
        this.passwordHash = requireNonNull(passwordHash, "Senha hash não pode ser nulo");
        this.sex = requireNonNull(sex, "Sexo não pode ser nulo");
        this.createdAt = requireNonNull(createdAt, "Data de criação não pode ser nulo");
        this.updatedAt = updatedAt;
    }

    public UserId getId() {
        return id;
    }

    public void setId(UserId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireNonBlank(name, "Nome não pode ser nulo ou vazio");
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = requireNonNull(email, "E-mail não pode ser nulo");
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(PasswordHash passwordHash) {
        this.passwordHash = requireNonNull(passwordHash, "Senha hash não pode ser nulo");
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = requireNonNull(sex, "Sexo não pode ser nulo");
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = requireNonNull(createdAt, "Data de criação não pode ser nulo");
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
