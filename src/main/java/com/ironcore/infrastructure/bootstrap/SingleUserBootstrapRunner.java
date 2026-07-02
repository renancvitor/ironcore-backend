package com.ironcore.infrastructure.bootstrap;

import com.ironcore.application.user.usecase.bootstrap.BootstrapSingleUserCommand;
import com.ironcore.application.user.usecase.bootstrap.BootstrapSingleUserUseCase;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.infrastructure.bootstrap.config.SingleUserBootstrapProperties;
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
@ConditionalOnProperty(prefix = "ironcore.bootstrap.single-user", name = "enabled", havingValue = "true")
@Order(2)
public class SingleUserBootstrapRunner implements ApplicationRunner {

    private final BootstrapSingleUserUseCase bootstrapSingleUserUseCase;
    private final SingleUserBootstrapProperties properties;
    private final Clock clock;

    @Override
    public void run(ApplicationArguments arguments) {
        if (!properties.enabled()) {
            return;
        }

        BootstrapSingleUserCommand command = new BootstrapSingleUserCommand(
                properties.nickname(),
                properties.personName(),
                new Email(properties.email()),
                properties.password(),
                LocalDateTime.now(clock)
        );

        bootstrapSingleUserUseCase.execute(command);
    }
}
