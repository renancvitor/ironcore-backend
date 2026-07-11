package com.ironcore.domain.activitytype.model;

import com.ironcore.domain.activitytype.exception.InvalidActivityTypeException;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.activitytype.ActivityTypeTestFactory.restoreActivityType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ActivityTypeTest {

    @Nested
    class Restoration {

        @Test
        void shouldRestoreExistingActivityType() {
            ActivityType activityType = restoreActivityType();

            assertThat(activityType.getId()).isEqualTo(new ActivityTypeId(1L));
            assertThat(activityType.getCode()).isEqualTo(new ActivityTypeCode("STRENGTH"));
            assertThat(activityType.getDisplayName()).isEqualTo("Força");
            assertThat(activityType.getActive()).isTrue();
            assertThat(activityType.getSortOrder()).isEqualTo(10);
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldRejectBlankDisplayName() {
            assertThatExceptionOfType(InvalidActivityTypeException.class)
                    .isThrownBy(() -> ActivityType.restore(
                            new ActivityTypeId(1L),
                            new ActivityTypeCode("STRENGTH"),
                            " ",
                            true,
                            10
                    ))
                    .withMessage("Nome de exibição não pode ser nulo ou vazio.");
        }

        @Test
        void shouldRequireIdWhenRestoring() {
            assertThatExceptionOfType(InvalidActivityTypeException.class)
                    .isThrownBy(() -> ActivityType.restore(
                            null,
                            new ActivityTypeCode("STRENGTH"),
                            "Força",
                            true,
                            10
                    ))
                    .withMessage("Id não pode ser nulo.");
        }
    }
}
