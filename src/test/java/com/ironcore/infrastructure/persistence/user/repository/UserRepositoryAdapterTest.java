package com.ironcore.infrastructure.persistence.user.repository;

import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.Sex;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 5, 10, 10, 0);

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Nested
    class Save {

        @Test
        void shouldSaveUser() {
            doReturn(userEntity()).when(userJpaRepository).save(anyUserEntity());

            User result = adapter.save(userWithoutId());

            assertThat(result.getId()).isEqualTo(new UserId(1L));
            assertThat(result.getEmail()).isEqualTo(new Email("renan@example.com"));
            verify(userJpaRepository).save(anyUserEntity());
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userJpaRepository.save(anyUserEntity()))
                    .thenThrow(new RuntimeException("database unavailable"));
            User user = userWithoutId();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(user))
                    .withMessage("Falha ao persistir user.");
        }

        @Test
        void shouldWrapMappingFailureAfterPersistence() {
            doReturn(invalidEntity()).when(userJpaRepository).save(anyUserEntity());
            User user = userWithoutId();

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.save(user))
                    .withMessage("Falha ao converter user persistido para domínio.");
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldFindUserById() {
            when(userJpaRepository.findById(1L)).thenReturn(Optional.of(userEntity()));

            Optional<User> result = adapter.findById(new UserId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new UserId(1L));
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(userJpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<User> result = adapter.findById(new UserId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userJpaRepository.findById(1L)).thenThrow(new RuntimeException("database unavailable"));
            UserId userId = new UserId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findById(userId))
                    .withMessage("Falha ao buscar user por id.");
        }
    }

    @Nested
    class FindByEmail {

        @Test
        void shouldFindUserByEmail() {
            when(userJpaRepository.findByEmail("renan@example.com")).thenReturn(Optional.of(userEntity()));

            Optional<User> result = adapter.findByEmail(new Email("Renan@Example.com"));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new UserId(1L));
            assertThat(result.get().getEmail()).isEqualTo(new Email("renan@example.com"));
        }

        @Test
        void shouldReturnEmptyWhenEmailDoesNotExist() {
            when(userJpaRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

            Optional<User> result = adapter.findByEmail(new Email("missing@example.com"));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userJpaRepository.findByEmail("renan@example.com"))
                    .thenThrow(new RuntimeException("database unavailable"));
            Email email = new Email("renan@example.com");

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByEmail(email))
                    .withMessage("Falha ao buscar user por email.");
        }
    }

    @Nested
    class ExistsById {

        @Test
        void shouldReturnTrueWhenIdExists() {
            when(userJpaRepository.existsById(1L)).thenReturn(true);

            boolean result = adapter.existsById(new UserId(1L));

            assertThat(result).isTrue();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userJpaRepository.existsById(1L)).thenThrow(new RuntimeException("database unavailable"));
            UserId userId = new UserId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.existsById(userId))
                    .withMessage("Falha ao verificar existência de user por id.");
        }
    }

    @Nested
    class ExistsByEmail {

        @Test
        void shouldReturnTrueWhenEmailExists() {
            when(userJpaRepository.existsByEmail("renan@example.com")).thenReturn(true);

            boolean result = adapter.existsByEmail(new Email("renan@example.com"));

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalseWhenEmailDoesNotExist() {
            when(userJpaRepository.existsByEmail("missing@example.com")).thenReturn(false);

            boolean result = adapter.existsByEmail(new Email("missing@example.com"));

            assertThat(result).isFalse();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userJpaRepository.existsByEmail("renan@example.com"))
                    .thenThrow(new RuntimeException("database unavailable"));
            Email email = new Email("renan@example.com");

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.existsByEmail(email))
                    .withMessage("Falha ao verificar existência de user por email.");
        }
    }

    @SuppressWarnings("null")
    @NonNull
    private UserEntity anyUserEntity() {
        return notNull(UserEntity.class);
    }

    @NonNull
    private User userWithoutId() {
        return Objects.requireNonNull(User.register(
                "Renan",
                new Email("renan@example.com"),
                new PasswordHash("hashed-password"),
                new Sex(SexType.MALE),
                CREATED_AT
        ));
    }

    @NonNull
    private UserEntity userEntity() {
        return new UserEntity(
                1L,
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

    @NonNull
    private UserEntity invalidEntity() {
        return new UserEntity(
                null,
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
