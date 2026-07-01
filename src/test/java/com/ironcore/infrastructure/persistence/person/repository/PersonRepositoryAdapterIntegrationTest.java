package com.ironcore.infrastructure.persistence.person.repository;

import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.Sex;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersonRepositoryAdapterIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ironcore_test")
            .withUsername("ironcore")
            .withPassword("ironcore");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PersonJpaRepository personJpaRepository;

    private PersonRepositoryAdapter adapter;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        POSTGRES.start();

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @BeforeEach
    void setUp() {
        adapter = new PersonRepositoryAdapter(personJpaRepository);
    }

    @Nested
    class Schema {

        @Test
        void shouldCreatePersonsTableThroughFlywayMigration() {
            Integer count = jdbcTemplate.queryForObject(
                    """
                            SELECT COUNT(*)
                            FROM information_schema.tables
                            WHERE table_schema = 'public'
                                AND table_name = 'persons'
                            """,
                    Integer.class
            );

            assertThat(count).isEqualTo(1);
        }

        @Test
        void shouldCreatePersonColumnsInPersonsTable() {
            List<String> columnNames = jdbcTemplate.queryForList(
                    """
                            SELECT column_name
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                                AND table_name = 'persons'
                            """,
                    String.class
            );

            assertThat(columnNames).contains(
                    "name",
                    "sex",
                    "birth_date",
                    "created_at",
                    "updated_at"
            );
        }
    }

    @Nested
    class RepositoryPersistence {

        @Test
        void shouldPersistPersonThroughRepositoryAdapter() {
            Person person = personWithoutId("Renan");

            Person savedPerson = adapter.save(person);

            assertThat(savedPerson.getId()).isNotNull();
            assertThat(savedPerson.getId().value()).isPositive();
            assertThat(savedPerson.getName()).isEqualTo("Renan");
            assertThat(savedPerson.getSex()).isEqualTo(new Sex(SexType.MALE));
            assertThat(savedPerson.getBirthDate()).isEqualTo(new BirthDate(LocalDate.of(1994, 4, 9)));
        }

        @Test
        void shouldPersistAuditTimestampsThroughRepositoryAdapter() {
            Person person = personWithoutId("Renan");

            Person savedPerson = adapter.save(person);

            Optional<Person> result = adapter.findById(savedPerson.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 10, 10, 0));
            assertThat(result.get().getUpdatedAt()).isNull();
        }
    }

    @Nested
    class RepositoryQueries {

        @Test
        void shouldFindPersonByNameThroughRepositoryAdapter() {
            Person savedPerson = adapter.save(personWithoutId("Renan"));

            Optional<Person> result = adapter.findByName("Renan");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(savedPerson.getId());
            assertThat(result.get().getName()).isEqualTo("Renan");
        }

        @Test
        void shouldReturnTrueWhenPersonIdExistsThroughRepositoryAdapter() {
            Person savedPerson = adapter.save(personWithoutId("Renan"));

            boolean exists = adapter.existsById(savedPerson.getId());

            assertThat(exists).isTrue();
        }

        @Test
        void shouldReturnTrueWhenAnyPersonExistsThroughRepositoryAdapter() {
            adapter.save(personWithoutId("Renan"));

            boolean exists = adapter.existsAny();

            assertThat(exists).isTrue();
        }
    }

    @Nested
    class Constraints {

        @Test
        void shouldEnforceUniqueNameConstraint() {
            adapter.save(personWithoutId("Renan"));

            assertThatThrownBy(() -> adapter.save(personWithoutId("Renan")))
                    .isInstanceOf(PersistenceException.class);
        }
    }

    private Person personWithoutId(String name) {
        return Person.register(
                name,
                new Sex(SexType.MALE),
                new BirthDate(LocalDate.of(1994, 4, 9)),
                LocalDateTime.of(2026, 5, 10, 10, 0)
        );
    }
}
