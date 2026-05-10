package com.ironcore.infrastructure.persistence.user.mapper;

import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.Sex;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 5, 10, 10, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 5, 10, 11, 0);

    @Nested
    class ToEntity {

        @Test
        void shouldMapSecurityAndStatusFields() {
            User user = User.restore(
                    new UserId(1L),
                    "Renan",
                    new Email("renan@example.com"),
                    new PasswordHash("hashed-password"),
                    new Sex(SexType.MALE),
                    false,
                    false,
                    CREATED_AT,
                    UPDATED_AT
            );

            UserEntity entity = UserMapper.toEntity(user);

            assertThat(entity.getPasswordHash()).isEqualTo("hashed-password");
            assertThat(entity.getMustChangePassword()).isFalse();
            assertThat(entity.getActive()).isFalse();
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreSecurityAndStatusFields() {
            UserEntity entity = new UserEntity(
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

            User user = UserMapper.toDomain(entity);

            assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash("hashed-password"));
            assertThat(user.mustChangePassword()).isFalse();
            assertThat(user.isActive()).isFalse();
        }
    }
}
