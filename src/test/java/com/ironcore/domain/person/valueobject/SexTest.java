package com.ironcore.domain.person.valueobject;

import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.exception.InvalidUserException;
import com.ironcore.domain.user.valueobject.Sex;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SexTest {

    @Test
    void shouldAcceptMale() {
        com.ironcore.domain.user.valueobject.Sex sex = new com.ironcore.domain.user.valueobject.Sex(SexType.MALE);

        assertThat(sex.type()).isEqualTo(SexType.MALE);
    }

    @Test
    void shouldAcceptFemale() {
        com.ironcore.domain.user.valueobject.Sex sex = new com.ironcore.domain.user.valueobject.Sex(SexType.FEMALE);

        assertThat(sex.type()).isEqualTo(SexType.FEMALE);
    }

    @Test
    void shouldFailWhenSexIsNull() {
        assertThatThrownBy(() -> new Sex(null))
                .isInstanceOf(InvalidUserException.class);
    }
}
