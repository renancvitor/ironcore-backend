package com.ironcore.infrastructure.bootstrap;

import com.ironcore.application.person.usecase.bootstrap.BootstrapPersonCommand;
import com.ironcore.application.person.usecase.bootstrap.BootstrapPersonUseCase;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.Sex;
import com.ironcore.infrastructure.bootstrap.config.PersonBootstrapProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ironcore.bootstrap.person", name = "enabled", havingValue = "true")
@Order(1)
public class PersonBootstrapRunner implements ApplicationRunner {

    private final BootstrapPersonUseCase bootstrapPersonUseCase;
    private final PersonBootstrapProperties properties;
    private final Clock clock;

    @Override
    public void run(ApplicationArguments arguments) {
        if (!properties.enabled()) {
            return;
        }

        BootstrapPersonCommand command = new BootstrapPersonCommand(
                properties.name(),
                new Sex(properties.sex()),
                new BirthDate(properties.birthDate()),
                LocalDateTime.now(clock)
        );

        bootstrapPersonUseCase.execute(command);
    }
}
