package com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.execution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.WorkoutSessionIntegerDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UpdateWorkoutSessionActivityExecutionRequest {

    @JsonDeserialize(using = WorkoutSessionIntegerDeserializer.class)
    @Schema(description = "Omitido preserva o valor; null solicita limpeza.", nullable = true)
    private Integer actualDurationMinutes;
    @Getter(AccessLevel.NONE)
    private boolean actualDurationMinutesPresent;

    @Schema(description = "Omitido preserva o valor; null solicita limpeza.", nullable = true)
    private BigDecimal actualDistanceKm;
    @Getter(AccessLevel.NONE)
    private boolean actualDistanceKmPresent;

    @Schema(description = "Omitido preserva o valor; null solicita limpeza.", nullable = true)
    private String notes;
    @Getter(AccessLevel.NONE)
    private boolean notesPresent;

    @JsonSetter(value = "actualDurationMinutes", nulls = Nulls.SET)
    public void setActualDurationMinutes(Integer value) {
        this.actualDurationMinutes = value;
        this.actualDurationMinutesPresent = true;
    }

    public boolean hasActualDurationMinutes() {
        return actualDurationMinutesPresent;
    }

    @JsonSetter(value = "actualDistanceKm", nulls = Nulls.SET)
    public void setActualDistanceKm(BigDecimal value) {
        this.actualDistanceKm = value;
        this.actualDistanceKmPresent = true;
    }

    public boolean hasActualDistanceKm() {
        return actualDistanceKmPresent;
    }

    @JsonSetter(value = "notes", nulls = Nulls.SET)
    public void setNotes(String value) {
        this.notes = value;
        this.notesPresent = true;
    }

    public boolean hasNotes() {
        return notesPresent;
    }

    @JsonIgnore
    @AssertTrue(message = "Informe ao menos um campo para atualização.")
    public boolean isUpdateProvided() {
        return actualDurationMinutesPresent || actualDistanceKmPresent || notesPresent;
    }
}
