package com.ironcore.interfaces.rest.workoutsession.dto.loadmode;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.interfaces.rest.workoutsession.dto.WorkoutSessionLoadModeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ChangeWorkoutSessionActivityLoadModeRequest(
        @NotNull
        @Schema(description = "Reinterpreta a carga informada, sem convertê-la. PER_SIDE significa dois lados equivalentes; "
                + "não inclui peso presumido de barra, corpo ou equipamento.")
        @JsonDeserialize(using = WorkoutSessionLoadModeDeserializer.class)
        ActualLoadMode actualLoadMode
) {
}
