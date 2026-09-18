package com.ironcore.interfaces.rest.person.dto;

import com.ironcore.domain.person.enums.SexType;

import java.time.LocalDate;

public record PersonResponse(
        Long personId,
        String name,
        SexType sex,
        LocalDate birthDate
) {
}
