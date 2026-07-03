package com.ironcore.interfaces.rest.person.dto;

import com.ironcore.domain.person.enums.SexType;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdatePersonRequest(
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres.")
        String name,

        SexType sex,
        LocalDate birthDate
) {
}
