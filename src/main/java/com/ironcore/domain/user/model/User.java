package com.ironcore.domain.user.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.Sex;
import com.ironcore.domain.user.valueobject.UserId;

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
        this.email = Objects.requireNonNull(email, "E-mail não pode ser nulo");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Senha hash não pode ser nulo");
        this.sex = Objects.requireNonNull(sex, "Sexo não pode ser nulo");
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação não pode ser nulo");
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
        this.email = Objects.requireNonNull(email, "E-mail não pode ser nulo");
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(PasswordHash passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash, "Senha hash não pode ser nulo");
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = Objects.requireNonNull(sex, "Sexo não pode ser nulo");
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação não pode ser nulo");
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}
