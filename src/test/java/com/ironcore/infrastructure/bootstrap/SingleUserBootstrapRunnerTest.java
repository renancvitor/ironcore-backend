package com.ironcore.infrastructure.bootstrap;

import com.ironcore.application.user.usecase.bootstrap.BootstrapSingleUserCommand;
import com.ironcore.application.user.usecase.bootstrap.BootstrapSingleUserUseCase;
import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.Sex;
import com.ironcore.infrastructure.bootstrap.config.SingleUserBootstrapProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SingleUserBootstrapRunnerTest {

    private final BootstrapSingleUserUseCase useCase = mock(BootstrapSingleUserUseCase.class);

    @Nested
    class DisabledBootstrap {

        @Test
        void shouldNotExecuteUseCaseWhenBootstrapIsDisabled() {
            SingleUserBootstrapProperties properties = new SingleUserBootstrapProperties(
                    false,
                    "",
                    "",
                    "",
                    null
            );

            SingleUserBootstrapRunner runner = new SingleUserBootstrapRunner(useCase, properties);

            runner.run(null);

            verify(useCase, never()).execute(any());
        }
    }

    @Nested
    class EnabledBootstrap {

        @Test
        void shouldExecuteUseCaseWithCommandBuiltFromProperties() {
            SingleUserBootstrapProperties properties = new SingleUserBootstrapProperties(
                    true,
                    "Renan",
                    "renan@example.com",
                    "Strong123@",
                    SexType.MALE
            );

            SingleUserBootstrapRunner runner = new SingleUserBootstrapRunner(useCase, properties);

            runner.run(null);

            ArgumentCaptor<BootstrapSingleUserCommand> captor =
                    ArgumentCaptor.forClass(BootstrapSingleUserCommand.class);

            verify(useCase).execute(captor.capture());

            BootstrapSingleUserCommand command = captor.getValue();

            assertThat(command.name()).isEqualTo("Renan");
            assertThat(command.email()).isEqualTo(new Email("renan@example.com"));
            assertThat(command.rawPassword()).isEqualTo("Strong123@");
            assertThat(command.sex()).isEqualTo(new Sex(SexType.MALE));
            assertThat(command.createdAt()).isNotNull();
        }
    }
}
