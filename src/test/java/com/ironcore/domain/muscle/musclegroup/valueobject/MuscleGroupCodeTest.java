package com.ironcore.domain.muscle.musclegroup.valueobject;

import com.ironcore.domain.muscle.musclegroup.exception.InvalidMuscleGroupException;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.muscle.musclegroup.MuscleGroupTestFactory.code;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MuscleGroupCodeTest {

    @Test
    void shouldNormalizeCode() {
        MuscleGroupCode code = code(" back ");

        assertThat(code.value()).isEqualTo("BACK");
    }

    @Test
    void shouldRejectNullCode() {
        assertThatThrownBy(() -> new MuscleGroupCode(null))
                .isInstanceOf(InvalidMuscleGroupException.class);
    }

    @Test
    void shouldRejectWhenCodeIsBlank() {
        assertThatThrownBy(() -> new MuscleGroupCode(" "))
                .isInstanceOf(InvalidMuscleGroupException.class);
    }

    @Test
    void shouldRejectInvalidCodeFormat() {
        assertThatThrownBy(() -> new MuscleGroupCode("invalid-@"))
                .isInstanceOf(InvalidMuscleGroupException.class);
    }

    @Test
    void shouldRejectLengthGreaterThan50() {
        assertThatThrownBy(() -> new MuscleGroupCode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
                .isInstanceOf(InvalidMuscleGroupException.class);
    }
}
