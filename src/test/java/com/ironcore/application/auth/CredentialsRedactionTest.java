package com.ironcore.application.auth;

import com.ironcore.application.auth.port.GeneratedAccessToken;
import com.ironcore.application.auth.port.SessionCredentials;
import com.ironcore.application.auth.usecase.LoginResult;
import com.ironcore.application.auth.usecase.LogoutCommand;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static com.ironcore.domain.auth.AuthSessionTestFactory.sessionId;
import static org.assertj.core.api.Assertions.assertThat;

class CredentialsRedactionTest {

    @Test
    void passwordRepresentationsHideSecretsIncludingNestedCommands() {
        String password = "PrivatePassword123@";
        var raw = new com.ironcore.domain.user.valueobject.RawPassword(password);
        var hash = new com.ironcore.domain.user.valueobject.PasswordHash("private-password-hash");
        var objects = java.util.List.of(raw, hash,
                new com.ironcore.infrastructure.bootstrap.config.SingleUserBootstrapProperties(false, "User", "Person", "user@example.com", password),
                new com.ironcore.application.auth.usecase.LoginCommand(new Email("user@example.com"), password),
                new com.ironcore.interfaces.rest.auth.dto.LoginRequest("user@example.com", password),
                new com.ironcore.interfaces.rest.user.dto.ChangePasswordRequest(password, password, password),
                new com.ironcore.interfaces.rest.user.dto.InitialChangePasswordRequest("user@example.com", password, password, password),
                new com.ironcore.application.user.usecase.changepassword.ChangePasswordCommand(new UserId(1L), raw, raw, raw),
                new com.ironcore.application.user.usecase.bootstrap.BootstrapSingleUserCommand("User", "Person",
                        new Email("user@example.com"), password, LocalDateTime.now()));
        objects.forEach(value -> assertThat(value.toString()).contains("[REDACTED]")
                .doesNotContain(password, hash.value()));
        assertThat(raw.value()).isEqualTo(password);
    }

    private static final String ACCESS_TOKEN = "secret-access-token";
    private static final String REFRESH_TOKEN = "secret-refresh-token";
    private final GeneratedAccessToken accessToken = new GeneratedAccessToken(
            ACCESS_TOKEN, "Bearer", LocalDateTime.of(2026, 11, 2, 12, 0));

    @Test
    void accessTokenRepresentationHidesItsValueWithoutChangingTheCredential() {
        assertThat(accessToken.toString()).contains("[REDACTED]").doesNotContain(ACCESS_TOKEN);
        assertThat(accessToken.value()).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    void sessionAndLoginRepresentationsHideBothCredentials() {
        var credentials = new SessionCredentials(accessToken, REFRESH_TOKEN, Instant.parse("2026-11-09T12:00:00Z"));
        var login = new LoginResult(credentials, new UserId(1L), new Email("user@example.com"), "User", false);

        assertThat(credentials.toString()).contains("[REDACTED]").doesNotContain(ACCESS_TOKEN, REFRESH_TOKEN);
        assertThat(login.toString()).contains("[REDACTED]").doesNotContain(ACCESS_TOKEN, REFRESH_TOKEN);
        assertThat(credentials.refreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(credentials.accessToken()).isSameAs(accessToken);
    }

    @Test
    void logoutRepresentationHidesTheRefreshCredential() {
        var command = new LogoutCommand(new UserId(1L), sessionId(), REFRESH_TOKEN);

        assertThat(command.toString()).contains("[REDACTED]").doesNotContain(REFRESH_TOKEN);
        assertThat(command.refreshToken()).isEqualTo(REFRESH_TOKEN);
    }
}
