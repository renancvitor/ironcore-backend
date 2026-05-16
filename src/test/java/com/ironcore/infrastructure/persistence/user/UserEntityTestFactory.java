package com.ironcore.infrastructure.persistence.user;

import com.ironcore.domain.user.enums.SexType;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

public final class UserEntityTestFactory {

    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 5, 10, 10, 0);
    public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 5, 10, 11, 0);

    private UserEntityTestFactory() {
    }

    public static UserEntity userEntity() {
        return userEntity(1L);
    }

    public static UserEntity invalidUserEntity() {
        return userEntity(null);
    }

    public static UserEntity inactiveUserEntity() {
        return new UserEntity(
                1L,
                "Renan",
                "renan@example.com",
                "hashed-password",
                SexType.MALE,
                false,
                false,
                CREATED_AT,
                UPDATED_AT
        );
    }

    private static UserEntity userEntity(Long id) {
        return new UserEntity(
                id,
                "Renan",
                "renan@example.com",
                "hashed-password",
                SexType.MALE,
                true,
                true,
                CREATED_AT,
                null
        );
    }
}
