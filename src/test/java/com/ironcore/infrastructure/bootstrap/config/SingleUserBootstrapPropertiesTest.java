package com.ironcore.infrastructure.bootstrap.config;

import com.ironcore.domain.user.enums.SexType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class SingleUserBootstrapPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Nested
    class Binding {

        @Test
        void shouldBindSingleUserBootstrapProperties() {
            contextRunner
                    .withPropertyValues(
                            "ironcore.bootstrap.single-user.enabled=true",
                            "ironcore.bootstrap.single-user.name=Renan",
                            "ironcore.bootstrap.single-user.email=renan@example.com",
                            "ironcore.bootstrap.single-user.password=StrongPass@2026",
                            "ironcore.bootstrap.single-user.sex=MALE"
                    )
                    .run(context -> {
                        SingleUserBootstrapProperties properties =
                                context.getBean(SingleUserBootstrapProperties.class);

                        assertThat(properties.enabled()).isTrue();
                        assertThat(properties.name()).isEqualTo("Renan");
                        assertThat(properties.email()).isEqualTo("renan@example.com");
                        assertThat(properties.password()).isEqualTo("StrongPass@2026");
                        assertThat(properties.sex()).isEqualTo(SexType.MALE);
                    });

        }
    }

    @Nested
    class Defaults {

        @Test
        void shouldUseFalseAsDefaultEnabledValue() {
            contextRunner
                    .run(context -> {
                        SingleUserBootstrapProperties properties =
                                context.getBean(SingleUserBootstrapProperties.class);

                        assertThat(properties.enabled()).isFalse();
                    });
        }
    }

    @Configuration
    @EnableConfigurationProperties(SingleUserBootstrapProperties.class)
    static class TestConfig {
    }
}
