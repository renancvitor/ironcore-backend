package com.ironcore.interfaces.rest.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.person.usecase.update.UpdatePersonCommand;
import com.ironcore.application.person.usecase.update.UpdatePersonResult;
import com.ironcore.application.person.usecase.update.UpdatePersonUseCase;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.Sex;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.person.dto.UpdatePersonRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PersonController.class)
@AutoConfigureMockMvc(addFilters = false)
class PersonControllerTest {

    private static final String PERSON_ENDPOINT = "/api/users/me/person";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UpdatePersonUseCase updatePersonUseCase;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private ErrorLogPublisher errorLogPublisher;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserRepository userRepository;

    @Nested
    class SuccessfulUpdate {

        @Test
        void shouldUpdateAuthenticatedUserPerson() throws Exception {
            UpdatePersonRequest request = new UpdatePersonRequest(
                    "Renan Vitor",
                    SexType.MALE,
                    LocalDate.of(1994, 4, 9)
            );
            UpdatePersonCommand command = new UpdatePersonCommand(
                    new UserId(1L),
                    "Renan Vitor",
                    new Sex(SexType.MALE),
                    new BirthDate(LocalDate.of(1994, 4, 9))
            );
            UpdatePersonResult result = new UpdatePersonResult(
                    "Renan Vitor",
                    new Sex(SexType.MALE),
                    new BirthDate(LocalDate.of(1994, 4, 9))
            );

            givenFixedClock();
            when(updatePersonUseCase.execute(command)).thenReturn(result);

            mockMvc.perform(patch(PERSON_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Renan Vitor"))
                    .andExpect(jsonPath("$.sex").value("MALE"))
                    .andExpect(jsonPath("$.birthDate").value("1994-04-09"));

            verify(updatePersonUseCase).execute(command);
        }

        @Test
        void shouldUpdateOnlyProvidedFields() throws Exception {
            UpdatePersonRequest request = new UpdatePersonRequest(
                    "Renan Vitor",
                    null,
                    null
            );
            UpdatePersonCommand command = new UpdatePersonCommand(
                    new UserId(1L),
                    "Renan Vitor",
                    null,
                    null
            );
            UpdatePersonResult result = new UpdatePersonResult(
                    "Renan Vitor",
                    new Sex(SexType.MALE),
                    new BirthDate(LocalDate.of(1994, 4, 9))
            );

            givenFixedClock();
            when(updatePersonUseCase.execute(command)).thenReturn(result);

            mockMvc.perform(patch(PERSON_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Renan Vitor"))
                    .andExpect(jsonPath("$.sex").value("MALE"))
                    .andExpect(jsonPath("$.birthDate").value("1994-04-09"));

            verify(updatePersonUseCase).execute(command);
        }
    }

    @Nested
    class RequestValidation {

        @Test
        void shouldFailWhenNameExceedsMaxSize() throws Exception {
            UpdatePersonRequest request = new UpdatePersonRequest(
                    "a".repeat(101),
                    null,
                    null
            );

            mockMvc.perform(patch(PERSON_ENDPOINT)
                            .with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.path").value(PERSON_ENDPOINT))
                    .andExpect(jsonPath("$.fields").isArray())
                    .andExpect(jsonPath("$.fields[*].field", containsInAnyOrder(
                            "name"
                    )));

            verify(updatePersonUseCase, never()).execute(any());
        }
    }

    private void givenFixedClock() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-07-03T10:00:00Z"),
                ZoneOffset.UTC
        );

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }
}
