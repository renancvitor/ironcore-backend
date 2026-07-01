package com.ironcore.infrastructure.persistence.person.repository;

import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

import java.util.Optional;

import static com.ironcore.domain.person.PersonTestFactory.personWithoutId;
import static com.ironcore.infrastructure.persistence.person.PersonEntityTestFactory.personEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonRepositoryAdapterTest {

    @Mock
    private PersonJpaRepository personJpaRepository;

    @InjectMocks
    private PersonRepositoryAdapter adapter;

    @Nested
    class Save {

        @Test
        void shouldSavePerson() {
            doReturn(personEntity()).when(personJpaRepository).save(anyPersonEntity());

            Person result = adapter.save(personWithoutId());

            assertThat(result.getId()).isEqualTo(new PersonId(1L));
            assertThat(result.getName()).isEqualTo("Renan");
            verify(personJpaRepository).save(anyPersonEntity());
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(personJpaRepository.save(anyPersonEntity()))
                    .thenThrow(new RuntimeException("database unavailable"));
            Person person = personWithoutId();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(person))
                    .withMessage("Falha ao persistir person.");
        }

        @Test
        void shouldWrapMappingFailureAfterPersistence() {
            doReturn(invalidPersonEntity()).when(personJpaRepository).save(anyPersonEntity());
            Person person = personWithoutId();

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.save(person))
                    .withMessage("Falha ao converter person persistido para domínio.");
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldFindPersonById() {
            when(personJpaRepository.findById(1L)).thenReturn(Optional.of(personEntity()));

            Optional<Person> result = adapter.findById(new PersonId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new PersonId(1L));
            assertThat(result.get().getName()).isEqualTo("Renan");
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(personJpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Person> result = adapter.findById(new PersonId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(personJpaRepository.findById(1L)).thenThrow(new RuntimeException("database unavailable"));
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findById(personId))
                    .withMessage("Falha ao buscar person por id.");
        }
    }

    @Nested
    class FindByName {

        @Test
        void shouldFindPersonByName() {
            when(personJpaRepository.findByName("Renan")).thenReturn(Optional.of(personEntity()));

            Optional<Person> result = adapter.findByName("Renan");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new PersonId(1L));
            assertThat(result.get().getName()).isEqualTo("Renan");
        }

        @Test
        void shouldReturnEmptyWhenNameDoesNotExist() {
            when(personJpaRepository.findByName("Missing")).thenReturn(Optional.empty());

            Optional<Person> result = adapter.findByName("Missing");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(personJpaRepository.findByName("Renan"))
                    .thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByName("Renan"))
                    .withMessage("Falha ao buscar person por nome.");
        }
    }

    @Nested
    class ExistsById {

        @Test
        void shouldReturnTrueWhenIdExists() {
            when(personJpaRepository.existsById(1L)).thenReturn(true);

            boolean result = adapter.existsById(new PersonId(1L));

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalseWhenIdDoesNotExist() {
            when(personJpaRepository.existsById(99L)).thenReturn(false);

            boolean result = adapter.existsById(new PersonId(99L));

            assertThat(result).isFalse();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(personJpaRepository.existsById(1L)).thenThrow(new RuntimeException("database unavailable"));
            PersonId personId = new PersonId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.existsById(personId))
                    .withMessage("Falha ao verificar existência de person por id.");
        }
    }

    @Nested
    class ExistsAny {

        @Test
        void shouldReturnTrueWhenAnyPersonExists() {
            when(personJpaRepository.count()).thenReturn(1L);

            boolean result = adapter.existsAny();

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalseWhenNoPersonExists() {
            when(personJpaRepository.count()).thenReturn(0L);

            boolean result = adapter.existsAny();

            assertThat(result).isFalse();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(personJpaRepository.count()).thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.existsAny())
                    .withMessage("Falha ao verificar existência de qualquer person.");
        }
    }

    private PersonEntity invalidPersonEntity() {
        PersonEntity entity = personEntity();
        entity.setName(null);
        return entity;
    }

    @SuppressWarnings("null")
    @NonNull
    private PersonEntity anyPersonEntity() {
        return notNull(PersonEntity.class);
    }
}
