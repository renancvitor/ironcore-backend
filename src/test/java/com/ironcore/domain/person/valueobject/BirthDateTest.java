package com.ironcore.domain.person.valueobject;

import com.ironcore.domain.person.exception.InvalidPersonException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.ironcore.domain.person.PersonTestFactory.BIRTH_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BirthDateTest {

    @Test
    void shouldCreateBirthDate() {
        LocalDate date = BIRTH_DATE.value();

        BirthDate birthDate = new BirthDate(date);

        assertThat(birthDate.value()).isEqualTo(date);
    }

    @Test
    void shouldRejectNullBirthDate() {
        assertThatThrownBy(() -> BirthDate.from(null, null))
                .isInstanceOf(InvalidPersonException.class);
    }

    @Test
    void shouldRejectFutureBirthDate() {
        LocalDate referenceDate = LocalDate.of(2026, 7, 3);
        LocalDate invalidDate = LocalDate.of(2029, 1, 1);

        assertThatThrownBy(() -> BirthDate.from(invalidDate, referenceDate))
                .isInstanceOf(InvalidPersonException.class);
    }

    @Test
    void shouldRejectBirthDateOlderThan120Years() {
        LocalDate referenceDate = LocalDate.of(2026, 7, 3);
        LocalDate invalidDate = referenceDate.minusYears(120).minusDays(1);

        assertThatThrownBy(() -> BirthDate.from(invalidDate, referenceDate))
                .isInstanceOf(InvalidPersonException.class);
    }

    @Test
    void shouldCalculateAge() {
        BirthDate birthDate = new BirthDate(BIRTH_DATE.value());

        int age = birthDate.ageAt(LocalDate.of(2026, 6, 30));

        assertThat(age).isEqualTo(32);
    }

    @Test
    void shouldCreateBirthDateWithoutTemporalValidation() {
        LocalDate date = LocalDate.of(1800, 1, 1);

        BirthDate birthDate = new BirthDate(date);

        assertThat(birthDate.value()).isEqualTo(date);
    }

    @Test
    void shouldRejectNullReferenceDate() {
        LocalDate birthDate = LocalDate.of(1994, 4, 9);

        assertThatThrownBy(() -> BirthDate.from(birthDate, null))
                .isInstanceOf(InvalidPersonException.class);
    }
}
