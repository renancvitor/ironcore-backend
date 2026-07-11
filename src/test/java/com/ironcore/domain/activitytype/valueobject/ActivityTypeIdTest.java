package com.ironcore.domain.activitytype.valueobject;

import com.ironcore.domain.activitytype.exception.InvalidActivityTypeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityTypeIdTest {

    @Test
    void shouldFailWhenActivityTypeIdIsNull() {
        assertThatThrownBy(() -> new ActivityTypeId(null))
                .isInstanceOf(InvalidActivityTypeException.class);
    }

    @Test
    void shouldFailWhenActivityTypeIdIsZero() {
        assertThatThrownBy(() -> new ActivityTypeId(0L))
                .isInstanceOf(InvalidActivityTypeException.class);
    }

    @Test
    void shouldFailWhenActivityTypeIdIsNegative() {
        assertThatThrownBy(() -> new ActivityTypeId(-1L))
                .isInstanceOf(InvalidActivityTypeException.class);
    }
}
