package com.ironcore.infrastructure.persistence.auth.repository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class AuthSessionMigrationPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void migrationPreservesDeadlinesRevocationAndHashesForAllLegacySessions() throws Exception {
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .target("30").load().migrate();
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var sql = connection.createStatement()) {
            sql.execute("""
                    INSERT INTO persons (id, name, sex, birth_date, created_at)
                    VALUES (1, 'Migration', 'MALE', '1990-01-01', CURRENT_TIMESTAMP);
                    INSERT INTO users (id, nickname, person_id, email, password_hash, created_at)
                    VALUES (1, 'Migration', 1, 'migration@example.com', 'hash', CURRENT_TIMESTAMP);
                    INSERT INTO auth_sessions (id, user_id, created_at, expires_at, revoked_at, refresh_token_hash)
                    SELECT gen_random_uuid(), 1, CURRENT_TIMESTAMP - INTERVAL '1 day',
                        CURRENT_TIMESTAMP + INTERVAL '6 days', NULL::timestamptz, repeat('a', 64)
                    UNION ALL
                    SELECT gen_random_uuid(), 1, CURRENT_TIMESTAMP - INTERVAL '8 days',
                        CURRENT_TIMESTAMP - INTERVAL '1 day', NULL::timestamptz, repeat('b', 64)
                    UNION ALL
                    SELECT gen_random_uuid(), 1, CURRENT_TIMESTAMP - INTERVAL '1 day',
                        CURRENT_TIMESTAMP + INTERVAL '6 days', CURRENT_TIMESTAMP, NULL;
                    CREATE TABLE original_sessions AS SELECT * FROM auth_sessions;
                    """);
            Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                    .load().migrate();
            try (var rows = sql.executeQuery("""
                    SELECT count(*) FROM auth_sessions s JOIN original_sessions o USING (id)
                    WHERE s.absolute_expires_at = o.expires_at AND s.expires_at = o.expires_at
                      AND s.created_at = o.created_at AND s.version = o.version
                      AND s.revoked_at IS NOT DISTINCT FROM o.revoked_at
                      AND s.refresh_token_hash IS NOT DISTINCT FROM o.refresh_token_hash
                    """)) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getInt(1)).isEqualTo(3);
            }
        }
    }
}
