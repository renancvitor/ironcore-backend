package com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.notes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class UpdateWorkoutSessionNotesRequest {

    @Schema(description = "Omitido preserva o valor; null solicita limpeza.", nullable = true)
    private String notes;
    @Getter(AccessLevel.NONE)
    private boolean notesPresent;

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
        return notesPresent;
    }
}
