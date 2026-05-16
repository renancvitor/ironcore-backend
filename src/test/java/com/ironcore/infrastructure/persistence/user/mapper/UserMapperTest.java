package com.ironcore.infrastructure.persistence.user.mapper;

import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.user.UserTestFactory.restoredUser;
import static com.ironcore.infrastructure.persistence.user.UserEntityTestFactory.inactiveUserEntity;
import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapSecurityAndStatusFields() {
            User user = restoredUser(false, false);

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
            UserEntity entity = inactiveUserEntity();

            User user = UserMapper.toDomain(entity);

            assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash("hashed-password"));
            assertThat(user.mustChangePassword()).isFalse();
            assertThat(user.isActive()).isFalse();
        }
    }
}
