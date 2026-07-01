package com.ironcore.domain.user;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.UserId;

import java.time.LocalDateTime;

public final class UserTestFactory {

    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 5, 10, 10, 0);
    public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 5, 10, 11, 0);

    private UserTestFactory() {
    }

    public static User userWithoutId() {
        return User.register(
                "Renan",
                personId(1L),
                email("renan@example.com"),
                passwordHash("hashed-password"),
                CREATED_AT
        );
    }

    public static User restoredUser(boolean mustChangePassword, boolean active) {
        return User.restore(
                new UserId(1L),
                "Renan",
                personId(1L),
                email("renan@example.com"),
                passwordHash("hashed-password"),
                mustChangePassword,
                active,
                CREATED_AT,
                UPDATED_AT
        );
    }

    public static User activeUser() {
        return restoredUser(false, true);
    }

    public static User activeUserWithMustChangePasswordTrue() {
        return restoredUser(true, true);
    }

    public static User inactiveUser() {
        return restoredUser(false, false);
    }

    public static Email email(String value) {
        return new Email(value);
    }

    public static PasswordHash passwordHash(String value) {
        return new PasswordHash(value);
    }

    public static PersonId personId(Long id) {
        return new PersonId(id);
    }
}
