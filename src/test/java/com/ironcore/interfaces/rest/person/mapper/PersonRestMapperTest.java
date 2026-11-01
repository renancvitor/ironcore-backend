package com.ironcore.interfaces.rest.person.mapper;

import com.ironcore.application.person.usecase.get.GetPersonCommand;
import com.ironcore.application.person.usecase.get.GetPersonResult;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.person.dto.PersonResponse;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static org.assertj.core.api.Assertions.assertThat;

class PersonRestMapperTest {

    @Test
    void shouldMapAuthenticatedUserToGetCommand() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                new UserId(1L),
                new Email("renan@example.com"),
                false
        );

        GetPersonCommand command = PersonRestMapper.toGetCommand(authenticatedUser);

        assertThat(command.authenticatedUserId()).isEqualTo(authenticatedUser.userId());
    }

    @Test
    void shouldMapGetByIdResultToResponse() {
        Person person = restoredPerson();
        GetPersonResult result = new GetPersonResult(
                person.getId(),
                person.getName(),
                person.getSex(),
                person.getBirthDate()
        );

        PersonResponse response = PersonRestMapper.toResponse(result);

        assertThat(response.personId()).isEqualTo(person.getId().value());
        assertThat(response.name()).isEqualTo(person.getName());
        assertThat(response.sex()).isEqualTo(person.getSex().type());
        assertThat(response.birthDate()).isEqualTo(person.getBirthDate().value());
    }
}
