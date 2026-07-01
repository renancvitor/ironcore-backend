package com.ironcore.infrastructure.persistence.user.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

import java.util.Optional;

import static com.ironcore.domain.user.UserTestFactory.userWithoutId;
import static com.ironcore.infrastructure.persistence.person.PersonEntityTestFactory.personEntity;
import static com.ironcore.infrastructure.persistence.user.UserEntityTestFactory.invalidUserEntity;
import static com.ironcore.infrastructure.persistence.user.UserEntityTestFactory.userEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private PersonJpaRepository personJpaRepository;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Nested
    class Save {

        @Test
        void shouldSaveUser() {
            when(personJpaRepository.getReferenceById(1L)).thenReturn(personEntity());
            doReturn(userEntity()).when(userJpaRepository).save(anyUserEntity());

            User result = adapter.save(userWithoutId());

            assertThat(result.getId()).isEqualTo(new UserId(1L));
            assertThat(result.getEmail()).isEqualTo(new Email("renan@example.com"));
            verify(userJpaRepository).save(anyUserEntity());
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(personJpaRepository.getReferenceById(1L)).thenReturn(personEntity());
            when(userJpaRepository.save(anyUserEntity()))
                    .thenThrow(new RuntimeException("database unavailable"));
            User user = userWithoutId();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(user))
                    .withMessage("Falha ao persistir user.");
        }

        @Test
        void shouldWrapMappingFailureAfterPersistence() {
            when(personJpaRepository.getReferenceById(1L)).thenReturn(personEntity());
            doReturn(invalidUserEntity()).when(userJpaRepository).save(anyUserEntity());
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
    class FindByPersonId {

        @Test
        void shouldFindUserByPersonId() {
            when(userJpaRepository.findByPerson_Id(1L)).thenReturn(Optional.of(userEntity()));

            Optional<User> result = adapter.findByPersonId(new PersonId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new UserId(1L));
            verify(userJpaRepository).findByPerson_Id(1L);
        }

        @Test
        void shouldReturnEmptyWhenPersonIdDoesNotHaveUser() {
            when(userJpaRepository.findByPerson_Id(99L)).thenReturn(Optional.empty());

            Optional<User> result = adapter.findByPersonId(new PersonId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userJpaRepository.findByPerson_Id(1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByPersonId(personId))
                    .withMessage("Falha ao buscar user por id da pessoa.");
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

    @Nested
    class ExistsAny {

        @Test
        void shouldReturnTrueWhenAnyUserExists() {
            when(userJpaRepository.count()).thenReturn(1L);

            boolean result = adapter.existsAny();

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalseWhenNoUserExists() {
            when(userJpaRepository.count()).thenReturn(0L);

            boolean result = adapter.existsAny();

            assertThat(result).isFalse();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userJpaRepository.count()).thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.existsAny())
                    .withMessage("Falha ao verificar existência de qualquer user.");
        }
    }

    @SuppressWarnings("null")
    @NonNull
    private UserEntity anyUserEntity() {
        return notNull(UserEntity.class);
    }
}
