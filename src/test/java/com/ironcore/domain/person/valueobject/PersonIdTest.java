package com.ironcore.domain.person.valueobject;

import com.ironcore.domain.person.exception.InvalidPersonException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonIdTest {

    @Test
    void shouldCreatePersonIdWhenValueIsPositive() {
        PersonId personId = new PersonId(1L);

        assertThat(personId.value()).isEqualTo(1L);
    }

    @Test
    void shouldFailWhenPersonIdIsNull() {
        assertThatThrownBy(() -> new PersonId(null))
                .isInstanceOf(InvalidPersonException.class);
    }

    @Test
    void shouldFailWhenPersonIdIsZero() {
        assertThatThrownBy(() -> new PersonId(0L))
                .isInstanceOf(InvalidPersonException.class);
    }

    @Test
    void shouldFailWhenPersonIdIsNegative() {
        assertThatThrownBy(() -> new PersonId(-1L))
                .isInstanceOf(InvalidPersonException.class);
    }
}
