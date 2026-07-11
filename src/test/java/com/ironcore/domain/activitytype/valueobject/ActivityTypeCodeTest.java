package com.ironcore.domain.activitytype.valueobject;

import com.ironcore.domain.activitytype.exception.InvalidActivityTypeException;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.activitytype.ActivityTypeTestFactory.code;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityTypeCodeTest {

    @Test
    void shouldNormalizeCode() {
        ActivityTypeCode code = code(" strength ");

        assertThat(code.value()).isEqualTo("STRENGTH");
    }

    @Test
    void shouldRejectNullCode() {
        assertThatThrownBy(() -> new ActivityTypeCode(null))
                .isInstanceOf(InvalidActivityTypeException.class);
    }

    @Test
    void shouldRejectWhenCodeIsBlank() {
        assertThatThrownBy(() -> new ActivityTypeCode(" "))
                .isInstanceOf(InvalidActivityTypeException.class);
    }

    @Test
    void shouldRejectInvalidCodeFormat() {
        assertThatThrownBy(() -> new ActivityTypeCode("invalid-@"))
                .isInstanceOf(InvalidActivityTypeException.class);
    }

    @Test
    void shouldRejectLengthGreaterThan50() {
        assertThatThrownBy(() -> new ActivityTypeCode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
                .isInstanceOf(InvalidActivityTypeException.class);
    }
}
