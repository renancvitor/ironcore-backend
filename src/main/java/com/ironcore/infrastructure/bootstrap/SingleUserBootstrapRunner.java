package com.ironcore.infrastructure.bootstrap;

import com.ironcore.application.user.usecase.bootstrap.BootstrapSingleUserCommand;
import com.ironcore.application.user.usecase.bootstrap.BootstrapSingleUserUseCase;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.Sex;
import com.ironcore.infrastructure.bootstrap.config.SingleUserBootstrapProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SingleUserBootstrapRunner implements ApplicationRunner {

    private final BootstrapSingleUserUseCase bootstrapSingleUserUseCase;
    private final SingleUserBootstrapProperties properties;

    @Override
    public void run(ApplicationArguments arguments) {
        if (!properties.enabled()) {
            return;
        }

        BootstrapSingleUserCommand command = new BootstrapSingleUserCommand(
                properties.name(),
                new Email(properties.email()),
                properties.password(),
                new Sex(properties.sex()),
                LocalDateTime.now()
        );

        bootstrapSingleUserUseCase.execute(command);
    }
}
