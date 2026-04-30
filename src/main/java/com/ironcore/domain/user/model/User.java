package com.ironcore.domain.user.model;

import java.time.LocalDateTime;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
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
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.sex = sex;
        this.createdAt = createdAt;
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
        this.name = name;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(PasswordHash passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
