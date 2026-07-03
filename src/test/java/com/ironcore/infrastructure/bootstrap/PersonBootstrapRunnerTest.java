package com.ironcore.infrastructure.bootstrap;

import com.ironcore.application.person.usecase.bootstrap.BootstrapPersonCommand;
import com.ironcore.application.person.usecase.bootstrap.BootstrapPersonUseCase;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.Sex;
import com.ironcore.infrastructure.bootstrap.config.PersonBootstrapProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PersonBootstrapRunnerTest {

    private final BootstrapPersonUseCase useCase = mock(BootstrapPersonUseCase.class);

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                Instant.parse("2099-05-24T15:00:00Z"),
                ZoneId.systemDefault()
        );
    }

    @Nested
    class DisabledBootstrap {

        @Test
        void shouldNotExecuteUseCaseWhenBootstrapIsDisabled() {
            PersonBootstrapProperties properties = new PersonBootstrapProperties(
                    false,
                    "",
                    null,
                    null
            );

            PersonBootstrapRunner runner = new PersonBootstrapRunner(useCase, properties, clock);

            runner.run(null);

            verify(useCase, never()).execute(any());
        }
    }

    @Nested
    class EnabledBootstrap {

        @Test
        void shouldExecuteUseCaseWithCommandBuiltFromProperties() {
            PersonBootstrapProperties properties = new PersonBootstrapProperties(
                    true,
                    "Renan C Vitor",
                    new Sex(SexType.MALE).type(),
                    LocalDate.of(1994, 4, 9)
            );

            PersonBootstrapRunner runner = new PersonBootstrapRunner(useCase, properties, clock);

            runner.run(null);

            ArgumentCaptor<BootstrapPersonCommand> captor =
                    ArgumentCaptor.forClass(BootstrapPersonCommand.class);

            verify(useCase).execute(captor.capture());

            BootstrapPersonCommand command = captor.getValue();

            assertThat(command.name()).isEqualTo("Renan C Vitor");
            assertThat(command.sex()).isEqualTo(new Sex(SexType.MALE));
            assertThat(command.birthDate()).isEqualTo(new BirthDate(LocalDate.of(1994, 4, 9)));
            assertThat(command.createdAt()).isEqualTo(
                    LocalDateTime.ofInstant(clock.instant(), clock.getZone())
            );
        }
    }
}
