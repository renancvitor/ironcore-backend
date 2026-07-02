package com.ironcore.infrastructure.bootstrap.config;

import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.valueobject.Sex;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class PersonBootstrapPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Nested
    class Binding {

        @Test
        void shouldBindPersonBootstrapProperties() {
            contextRunner
                    .withPropertyValues(
                            "ironcore.bootstrap.person.enabled=true",
                            "ironcore.bootstrap.person.name=Renan C Vitor",
                            "ironcore.bootstrap.person.sex=MALE",
                            "ironcore.bootstrap.person.birth-date=1994-04-09"
                    )
                    .run(context -> {
                        PersonBootstrapProperties properties =
                                context.getBean(PersonBootstrapProperties.class);

                        assertThat(properties.enabled()).isTrue();
                        assertThat(properties.name()).isEqualTo("Renan C Vitor");
                        assertThat(properties.sex()).isEqualTo(new Sex(SexType.MALE).type());
                        assertThat(properties.birthDate()).isEqualTo("1994-04-09");
                    });
        }
    }

    @Nested
    class Defaults {

        @Test
        void shouldUseFalseAsDefaultEnabledValue() {
            contextRunner
                    .run(context -> {
                        PersonBootstrapProperties properties =
                                context.getBean(PersonBootstrapProperties.class);

                        assertThat(properties.enabled()).isFalse();
                    });
        }
    }

    @Configuration
    @EnableConfigurationProperties(PersonBootstrapProperties.class)
    static class TestConfig {
    }
}

