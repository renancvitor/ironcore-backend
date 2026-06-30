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
        assertThatThrownBy(() -> new BirthDate(null))
                .isInstanceOf(InvalidPersonException.class);
    }

    @Test
    void shouldRejectFutureBirthDate() {
        LocalDate invalidDate = LocalDate.of(2029, 1, 1);

        assertThatThrownBy(() -> new BirthDate(invalidDate))
                .isInstanceOf(InvalidPersonException.class);
    }

    @Test
    void shouldRejectBirthDateOlderThan120Years() {
        LocalDate invalidDate = LocalDate.now().minusYears(120).minusDays(1);

        assertThatThrownBy(() -> new BirthDate(invalidDate))
                .isInstanceOf(InvalidPersonException.class);
    }

    @Test
    void shouldCalculateAge() {
        BirthDate birthDate = new BirthDate(BIRTH_DATE.value());

        int age = birthDate.ageAt(LocalDate.of(2026, 6, 30));

        assertThat(age).isEqualTo(32);
    }
}
