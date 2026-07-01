package com.ironcore.infrastructure.persistence.user.repository;

import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryAdapterIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ironcore_test")
            .withUsername("ironcore")
            .withPassword("ironcore");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private UserRepositoryAdapter adapter;

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
        adapter = new UserRepositoryAdapter(userJpaRepository);
    }

    @Nested
    class Schema {

        @Test
        void shouldCreateUsersTableThroughFlywayMigration() {
            Integer count = jdbcTemplate.queryForObject(
                    """
                            SELECT COUNT(*)
                            FROM information_schema.tables
                            WHERE table_schema = 'public'
                                AND table_name = 'users'
                            """,
                    Integer.class
            );

            assertThat(count).isEqualTo(1);
        }

        @Test
        void shouldCreateAuthenticationColumnsInUsersTable() {
            List<String> columnNames = jdbcTemplate.queryForList(
                    """
                            SELECT column_name
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                                AND table_name = 'users'
                            """,
                    String.class
            );

            assertThat(columnNames).contains(
                    "password_hash",
                    "must_change_password",
                    "active"
            );
        }
    }

    @Nested
    class RepositoryPersistence {

        @Test
        void shouldPersistUserThroughRepositoryAdapter() {
            User user = activeUserWithoutId();

            User savedUser = adapter.save(user);

            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getId().value()).isPositive();
            assertThat(savedUser.getName()).isEqualTo("Renan");
            assertThat(savedUser.getEmail()).isEqualTo(new Email("renan@example.com"));
        }

        @Test
        void shouldPersistAuthenticationFieldsThroughRepositoryAdapter() {
            User user = activeUserWithoutId();

            User savedUser = adapter.save(user);

            Optional<User> result = adapter.findById(savedUser.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getPasswordHash()).isEqualTo(new PasswordHash("hashed-password"));
            assertThat(result.get().mustChangePassword()).isFalse();
            assertThat(result.get().isActive()).isTrue();
        }
    }

    @Nested
    class RepositoryQueries {

        @Test
        void shouldFindUserByEmailThroughRepositoryAdapter() {
            User savedUser = adapter.save(activeUserWithoutId());

            Optional<User> result = adapter.findByEmail(new Email("renan@example.com"));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(savedUser.getId());
            assertThat(result.get().getEmail()).isEqualTo(new Email("renan@example.com"));
            assertThat(result.get().getName()).isEqualTo("Renan");
        }

        @Test
        void shouldReturnTrueWhenUserEmailExistsThroughRepositoryAdapter() {
            adapter.save(activeUserWithoutId());

            boolean exists = adapter.existsByEmail(new Email("renan@example.com"));

            assertThat(exists).isTrue();
        }

        @Test
        void shouldReturnTrueWhenAnyUserExistsThroughRepositoryAdapter() {
            adapter.save(activeUserWithoutId());

            boolean exists = adapter.existsAny();

            assertThat(exists).isTrue();
        }
    }

    @Nested
    class Constraints {

        @Test
        void shouldEnforceUniqueEmailConstraint() {
            adapter.save(activeUserWithoutId());

            assertThatThrownBy(() -> adapter.save(anotherUserWithSameEmail()))
                    .isInstanceOf(PersistenceException.class);
        }
    }

    private User anotherUserWithSameEmail() {
        LocalDateTime now = LocalDateTime.now();

        return User.restore(
                null,
                "Renan Duplicado",
                new Email("renan@example.com"),
                new PasswordHash("another-hashed-password"),
                new Sex(SexType.MALE),
                false,
                true,
                now,
                now
        );
    }

    private User activeUserWithoutId() {
        LocalDateTime now = LocalDateTime.now();

        return User.restore(
                null,
                "Renan",
                new Email("renan@example.com"),
                new PasswordHash("hashed-password"),
                new Sex(SexType.MALE),
                false,
                true,
                now,
                now
        );
    }
}
