package com.ironcore.interfaces.rest.person.mapper;

import com.ironcore.application.person.usecase.get.GetPersonCommand;
import com.ironcore.application.person.usecase.get.GetPersonResult;
import com.ironcore.application.person.usecase.update.UpdatePersonCommand;
import com.ironcore.application.person.usecase.update.UpdatePersonResult;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.Sex;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.person.dto.PersonResponse;
import com.ironcore.interfaces.rest.person.dto.UpdatePersonRequest;
import com.ironcore.interfaces.rest.person.dto.UpdatePersonResponse;

import java.time.LocalDate;

public final class PersonRestMapper {

    private PersonRestMapper() {
    }

    public static GetPersonCommand toGetCommand(AuthenticatedUser authenticatedUser) {
        return new GetPersonCommand(authenticatedUser.userId());
    }

    public static PersonResponse toResponse(GetPersonResult result) {
        return new PersonResponse(
                result.personId().value(),
                result.name(),
                result.sex().type(),
                result.birthDate().value()
        );
    }

    public static UpdatePersonCommand toUpdateCommand(
            AuthenticatedUser authenticatedUser,
            UpdatePersonRequest request,
            LocalDate referenceDate
    ) {
        return new UpdatePersonCommand(
                authenticatedUser.userId(),
                request.name(),
                request.sex() == null ? null : new Sex(request.sex()),
                request.birthDate() == null ? null : BirthDate.from(request.birthDate(), referenceDate)
        );
    }

    public static UpdatePersonResponse toResponse(UpdatePersonResult result) {
        return new UpdatePersonResponse(
                result.name(),
                result.sex() == null ? null : result.sex().type(),
                result.birthDate() == null ? null : result.birthDate().value()
        );
    }
}
