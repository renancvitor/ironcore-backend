package com.ironcore.interfaces.rest.person;

import com.ironcore.application.person.usecase.update.UpdatePersonCommand;
import com.ironcore.application.person.usecase.update.UpdatePersonResult;
import com.ironcore.application.person.usecase.update.UpdatePersonUseCase;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.person.dto.UpdatePersonRequest;
import com.ironcore.interfaces.rest.person.dto.UpdatePersonResponse;
import com.ironcore.interfaces.rest.person.mapper.PersonRestMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

@Tag(name = "Pessoa")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/users/me/person")
public class PersonController {

    private final UpdatePersonUseCase updatePersonUseCase;
    private final Clock clock;

    @PatchMapping
    public ResponseEntity<UpdatePersonResponse> update(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdatePersonRequest request
    ) {
        UpdatePersonCommand command = PersonRestMapper.toUpdateCommand(
                authenticatedUser,
                request,
                LocalDate.now(clock)
        );
        UpdatePersonResult result = updatePersonUseCase.execute(command);
        UpdatePersonResponse response = PersonRestMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
