package com.ironcore.domain.user.model;

import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.exception.InvalidUserException;
import com.ironcore.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.user.UserTestFactory.CREATED_AT;
import static com.ironcore.domain.user.UserTestFactory.UPDATED_AT;
import static com.ironcore.domain.user.UserTestFactory.email;
import static com.ironcore.domain.user.UserTestFactory.passwordHash;
import static com.ironcore.domain.user.UserTestFactory.restoredUser;
import static com.ironcore.domain.user.UserTestFactory.sex;
import static com.ironcore.domain.user.UserTestFactory.userWithoutId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserTest {

    @Nested
    class Creation {

        @Test
        void shouldRegisterActiveUserWithoutId() {
            User user = User.register(
                    " Renan ",
                    email("renan@example.com"),
                    passwordHash("hashed-password"),
                    sex(SexType.MALE),
                    CREATED_AT);

            assertThat(user.getId()).isNull();
            assertThat(user.getName()).isEqualTo("Renan");
            assertThat(user.getEmail()).isEqualTo(email("renan@example.com"));
            assertThat(user.getPasswordHash()).isEqualTo(passwordHash("hashed-password"));
            assertThat(user.getSex()).isEqualTo(sex(SexType.MALE));
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
            User user = validUser();

            user.rename(" Novo Nome ", UPDATED_AT);

            assertThat(user.getName()).isEqualTo("Novo Nome");
            assertThat(user.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldChangePasswordAndClearRequiredPasswordChangeFlag() {
            User user = validUser();

            user.changePasswordHash(passwordHash("new-hashed-password"), UPDATED_AT);

            assertThat(user.getPasswordHash()).isEqualTo(passwordHash("new-hashed-password"));
            assertThat(user.mustChangePassword()).isFalse();
            assertThat(user.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldDeactivateAndActivateUser() {
            User user = validUser();

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
                            email("renan@example.com"),
                            passwordHash("hashed-password"),
                            sex(SexType.MALE),
                            CREATED_AT))
                    .withMessage("Nome não pode ser nulo ou vazio");
        }

        @Test
        void shouldRequireUpdateDateWhenChangingState() {
            User user = validUser();

            assertThatExceptionOfType(InvalidUserException.class)
                    .isThrownBy(() -> user.deactivate(null))
                    .withMessage("Data de atualização não pode ser nulo");
        }
    }

    private User validUser() {
        return userWithoutId();
    }
}
