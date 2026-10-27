package com.ironcore.interfaces.rest.workoutsession.dto.set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.interfaces.rest.workoutsession.dto.WorkoutSessionIntegerDeserializer;
import com.ironcore.interfaces.rest.workoutsession.dto.WorkoutSessionLoadModeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UpdateWorkoutSessionSetRequest {

    @Schema(description = "Omitido preserva o valor; null solicita limpeza.", nullable = true)
    private BigDecimal actualLoadKg;
    @Getter(AccessLevel.NONE)
    private boolean actualLoadKgPresent;

    @Schema(description = "Omitido preserva o valor; null solicita limpeza.", nullable = true)
    @JsonDeserialize(using = WorkoutSessionLoadModeDeserializer.class)
    private ActualLoadMode actualLoadMode;
    @Getter(AccessLevel.NONE)
    private boolean actualLoadModePresent;

    @JsonDeserialize(using = WorkoutSessionIntegerDeserializer.class)
    @Schema(description = "Omitido preserva o valor; null solicita limpeza.", nullable = true)
    private Integer actualReps;
    @Getter(AccessLevel.NONE)
    private boolean actualRepsPresent;

    @Schema(description = "Omitido preserva o valor; null solicita limpeza.", nullable = true)
    private String notes;
    @Getter(AccessLevel.NONE)
    private boolean notesPresent;

    @JsonSetter(value = "actualLoadKg", nulls = Nulls.SET)
    public void setActualLoadKg(BigDecimal value) {
        this.actualLoadKg = value;
        this.actualLoadKgPresent = true;
    }

    public boolean hasActualLoadKg() {
        return actualLoadKgPresent;
    }

    @JsonSetter(value = "actualLoadMode", nulls = Nulls.SET)
    public void setActualLoadMode(ActualLoadMode value) {
        this.actualLoadMode = value;
        this.actualLoadModePresent = true;
    }

    public boolean hasActualLoadMode() {
        return actualLoadModePresent;
    }

    @JsonSetter(value = "actualReps", nulls = Nulls.SET)
    public void setActualReps(Integer value) {
        this.actualReps = value;
        this.actualRepsPresent = true;
    }

    public boolean hasActualReps() {
        return actualRepsPresent;
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
        return actualLoadKgPresent || actualLoadModePresent || actualRepsPresent || notesPresent;
    }
}
