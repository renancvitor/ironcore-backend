package com.ironcore.interfaces.rest.workoutsession.workoutsession;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.interfaces.rest.workoutsession.dto.execution.UpdateWorkoutSessionActivityExecutionRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.loadmode.ChangeWorkoutSessionActivityLoadModeRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.notes.UpdateWorkoutSessionNotesRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.set.CreateWorkoutSessionSetRequest;
import com.ironcore.interfaces.rest.workoutsession.dto.set.UpdateWorkoutSessionSetRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class WorkoutSessionPatchRequestTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldDistinguishMissingNullAndValueForSessionNotes() throws Exception {
        var missing = mapper.readValue("{}", UpdateWorkoutSessionNotesRequest.class);
        var cleared = mapper.readValue("{\"notes\":null}", UpdateWorkoutSessionNotesRequest.class);
        var replaced = mapper.readValue("{\"notes\":\"Relato\"}", UpdateWorkoutSessionNotesRequest.class);
        assertThat(missing.hasNotes()).isFalse();
        assertThat(missing.isUpdateProvided()).isFalse();
        assertThat(cleared.hasNotes()).isTrue();
        assertThat(cleared.getNotes()).isNull();
        assertThat(replaced.hasNotes()).isTrue();
        assertThat(replaced.getNotes()).isEqualTo("Relato");
    }

    @Test
    void shouldTrackPresenceOfEveryActivityField() throws Exception {
        var missing = mapper.readValue("{}", UpdateWorkoutSessionActivityExecutionRequest.class);
        var cleared = mapper.readValue(
                "{\"actualDurationMinutes\":null,\"actualDistanceKm\":null,\"notes\":null}",
                UpdateWorkoutSessionActivityExecutionRequest.class);
        var replaced = mapper.readValue(
                "{\"actualDurationMinutes\":20,\"actualDistanceKm\":2.50,\"notes\":\"Relato\"}",
                UpdateWorkoutSessionActivityExecutionRequest.class);
        assertThat(missing.hasActualDurationMinutes()).isFalse();
        assertThat(missing.hasActualDistanceKm()).isFalse();
        assertThat(missing.hasNotes()).isFalse();
        assertThat(cleared.hasActualDurationMinutes()).isTrue();
        assertThat(cleared.hasActualDistanceKm()).isTrue();
        assertThat(cleared.hasNotes()).isTrue();
        assertThat(cleared.getActualDurationMinutes()).isNull();
        assertThat(cleared.getActualDistanceKm()).isNull();
        assertThat(cleared.getNotes()).isNull();
        assertThat(replaced.getActualDurationMinutes()).isEqualTo(20);
        assertThat(replaced.getActualDistanceKm()).isEqualByComparingTo("2.50");
        assertThat(replaced.getNotes()).isEqualTo("Relato");
    }

    @Test
    void shouldTrackPresenceOfEverySetField() throws Exception {
        var missing = mapper.readValue("{}", UpdateWorkoutSessionSetRequest.class);
        var cleared = mapper.readValue(
                "{\"actualLoadKg\":null,\"actualLoadMode\":null,\"actualReps\":null,\"notes\":null}",
                UpdateWorkoutSessionSetRequest.class);
        var replaced = mapper.readValue(
                "{\"actualLoadKg\":20.25,\"actualLoadMode\":\"PER_SIDE\",\"actualReps\":8,\"notes\":\"Relato\"}",
                UpdateWorkoutSessionSetRequest.class);
        assertThat(missing.hasActualLoadKg()).isFalse();
        assertThat(missing.hasActualLoadMode()).isFalse();
        assertThat(missing.hasActualReps()).isFalse();
        assertThat(missing.hasNotes()).isFalse();
        assertThat(cleared.hasActualLoadKg()).isTrue();
        assertThat(cleared.hasActualLoadMode()).isTrue();
        assertThat(cleared.hasActualReps()).isTrue();
        assertThat(cleared.hasNotes()).isTrue();
        assertThat(cleared.getActualLoadKg()).isNull();
        assertThat(cleared.getActualLoadMode()).isNull();
        assertThat(cleared.getActualReps()).isNull();
        assertThat(cleared.getNotes()).isNull();
        assertThat(replaced.getActualLoadKg()).isEqualByComparingTo("20.25");
        assertThat(replaced.getActualLoadMode().name()).isEqualTo("PER_SIDE");
        assertThat(replaced.getActualReps()).isEqualTo(8);
        assertThat(replaced.getNotes()).isEqualTo("Relato");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5", "\"8\"", "2147483648", "true"})
    void shouldRejectCoercionOrOverflowOfExecutionIntegers(String value) {
        assertThatThrownBy(() -> mapper.readValue("{\"actualReps\":" + value + "}",
                UpdateWorkoutSessionSetRequest.class)).isInstanceOf(JsonMappingException.class);
        assertThatThrownBy(() -> mapper.readValue("{\"setNumber\":" + value + "}",
                CreateWorkoutSessionSetRequest.class)).isInstanceOf(JsonMappingException.class);
        assertThatThrownBy(() -> mapper.readValue("{\"actualDurationMinutes\":" + value + "}",
                UpdateWorkoutSessionActivityExecutionRequest.class)).isInstanceOf(JsonMappingException.class);
    }

    @Test
    void shouldValidateExplicitNullAsAnUpdateButRejectEmptyPatch() throws Exception {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(mapper.readValue("{}", UpdateWorkoutSessionSetRequest.class))).isNotEmpty();
            assertThat(validator.validate(mapper.readValue("{\"notes\":null}", UpdateWorkoutSessionSetRequest.class))).isEmpty();
            assertThat(validator.validate(mapper.readValue("{\"notes\":null}", UpdateWorkoutSessionNotesRequest.class))).isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "true", "\"UNKNOWN\"", "\"\""})
    void shouldRejectOrdinalOrInvalidLoadModes(String value) {
        String json = "{\"actualLoadMode\":" + value + "}";
        assertThatThrownBy(() -> mapper.readValue(json, UpdateWorkoutSessionSetRequest.class))
                .isInstanceOf(JsonMappingException.class);
        assertThatThrownBy(() -> mapper.readValue(json, CreateWorkoutSessionSetRequest.class))
                .isInstanceOf(JsonMappingException.class);
        assertThatThrownBy(() -> mapper.readValue(json, ChangeWorkoutSessionActivityLoadModeRequest.class))
                .isInstanceOf(JsonMappingException.class);
    }
}
