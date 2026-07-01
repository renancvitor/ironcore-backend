package com.ironcore.domain.user.model;

import com.ironcore.domain.user.exception.InvalidUserException;
import com.ironcore.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.user.UserTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserTest {

    @Nested
    class Creation {

        @Test
        void shouldRegisterActiveUserWithoutId() {
            User user = User.register(
                    " Renan ",
                    personId(1L),
                    email("renan@example.com"),
                    passwordHash("hashed-password"),
                    CREATED_AT);

            assertThat(user.getId()).isNull();
            assertThat(user.getNickName()).isEqualTo("Renan");
            assertThat(user.getPersonId().value()).isEqualTo(1L);
            assertThat(user.getEmail()).isEqualTo(email("renan@example.com"));
            assertThat(user.getPasswordHash()).isEqualTo(passwordHash("hashed-password"));
            assertThat(user.mustChangePassword()).isTrue();
            assertThat(user.isActive()).isTrue();
            assertThat(user.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(user.getUpdatedAt()).isNull();
        }

        @Test
        void shouldRestoreExistingUserState() {
            User user = restoredUser(true, false);

            assertThat(user.getId()).isEqualTo(new UserId(1L));
            assertThat(user.mustChangePassword()).isTrue();
            assertThat(user.isActive()).isFalse();
            assertThat(user.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class BusinessChanges {

        @Test
        void shouldRenameUser() {
            User user = userWithoutId();

            user.changeNickName(" Novo Apelido ", UPDATED_AT);

            assertThat(user.getNickName()).isEqualTo("Novo Apelido");
            assertThat(user.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldChangePasswordAndClearRequiredPasswordChangeFlag() {
            User user = userWithoutId();

            user.changePasswordHash(passwordHash("new-hashed-password"), UPDATED_AT);

            assertThat(user.getPasswordHash()).isEqualTo(passwordHash("new-hashed-password"));
            assertThat(user.mustChangePassword()).isFalse();
            assertThat(user.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldDeactivateAndActivateUser() {
            User user = userWithoutId();

            user.deactivate(UPDATED_AT);
            assertThat(user.isActive()).isFalse();

            user.activate(UPDATED_AT.plusHours(1));
            assertThat(user.isActive()).isTrue();
            assertThat(user.getUpdatedAt()).isEqualTo(UPDATED_AT.plusHours(1));
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRejectBlankName() {
            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> User.register(
                            " ",
                            personId(1L),
                            email("renan@example.com"),
                            passwordHash("hashed-password"),
                            CREATED_AT))
                    .withMessage("Nome não pode ser nulo ou vazio");
        }

        @Test
        void shouldRequireUpdateDateWhenChangingState() {
            User user = userWithoutId();

            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> user.deactivate(null))
                    .withMessage("Data de atualização não pode ser nulo");
        }
    }
}
