package com.ironcore.domain.person.model;

import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.exception.InvalidPersonException;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.PersonId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.ironcore.domain.person.PersonTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PersonTest {

    @Nested
    class Creation {

        @Test
        void shouldRegisterPersonWithoutId() {
            Person person = Person.register(
                    " Renan ",
                    sex(SexType.MALE),
                    BIRTH_DATE,
                    CREATED_AT
            );

            assertThat(person.getId()).isNull();
            assertThat(person.getName()).isEqualTo("Renan");
            assertThat(person.getSex()).isEqualTo(sex(SexType.MALE));
            assertThat(person.getBirthDate()).isEqualTo(BIRTH_DATE);
            assertThat(person.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(person.getUpdatedAt()).isNull();
        }

        @Test
        void shouldRestoreExistingPerson() {
            Person person = restoredPerson();

            assertThat(person.getId()).isEqualTo(new PersonId(1L));
            assertThat(person.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class BusinessChange {

        @Test
        void shouldRenamePerson() {
            Person person = personWithoutId();

            person.changeName(" Novo Nome ", UPDATED_AT);

            assertThat(person.getName()).isEqualTo("Novo Nome");
            assertThat(person.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldChangeSex() {
            Person person = personWithoutId();

            person.changeSex(sex(SexType.FEMALE), UPDATED_AT);

            assertThat(person.getSex()).isEqualTo(sex(SexType.FEMALE));
            assertThat(person.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        void shouldChangeBirthDate() {
            Person person = personWithoutId();
            BirthDate newBirthDate = new BirthDate(LocalDate.of(1994, 4, 18));

            person.changeBirthDate(newBirthDate, UPDATED_AT);

            assertThat(person.getBirthDate()).isEqualTo(newBirthDate);
            assertThat(person.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRejectBlankName() {
            assertThatExceptionOfType(InvalidPersonException.class)
                    .isThrownBy(() -> Person.register(
                            " ",
                            sex(SexType.MALE),
                            BIRTH_DATE,
                            CREATED_AT
                    ))
                    .withMessage("Nome não pode ser nulo ou vazio.");
        }

        @Test
        void shouldRequireUpdateDateWhenChangingState() {
            Person person = personWithoutId();

            assertThatExceptionOfType(InvalidPersonException.class)
                    .isThrownBy(() -> person.changeSex(sex(SexType.MALE), null))
                    .withMessage("Data de alteração é obrigatória.");
        }
    }
}
