package com.ironcore.infrastructure.security.filter;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenClaims;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtAccessTokenValidator validator;

    @Mock
    private UserRepository userRepository;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(validator, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateUserWhenAccessTokenCookieIsValid() throws Exception {
        JwtAccessTokenClaims claims = new JwtAccessTokenClaims(
                new UserId(1L),
                new Email("renan@example.com"),
                false
        );

        User user = user(true);

        when(validator.validate("valid-token")).thenReturn(claims);
        when(userRepository.findById(new UserId(1L))).thenReturn(Optional.of(user));

        MockHttpServletRequest request = request("GET", "/api/protected");
        request.setCookies(new Cookie("access_token", "valid-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedUser.class);

        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();

        assertThat(principal.userId()).isEqualTo(new UserId(1L));
        assertThat(principal.email()).isEqualTo(new Email("renan@example.com"));
        assertThat(principal.mustChangePassword()).isFalse();
    }

    @Test
    void shouldNotAuthenticateWhenAccessTokenCookieIsMissing() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(validator, never()).validate(anyString());
    }

    @Test
    void shouldNotAuthenticateWhenUserDoesNotExist() throws Exception {
        JwtAccessTokenClaims claims = new JwtAccessTokenClaims(
                new UserId(1L),
                new Email("renan@example.com"),
                false
        );

        when(validator.validate("valid-token")).thenReturn(claims);
        when(userRepository.findById(new UserId(1L))).thenReturn(Optional.empty());

        MockHttpServletRequest request = request("GET", "/api/protected");
        request.setCookies(new Cookie("access_token", "valid-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldNotAuthenticateWhenUserIsInactive() throws Exception {
        JwtAccessTokenClaims claims = new JwtAccessTokenClaims(
                new UserId(1L),
                new Email("renan@example.com"),
                false
        );

        when(validator.validate("valid-token")).thenReturn(claims);
        when(userRepository.findById(new UserId(1L))).thenReturn(Optional.of(user(false)));

        MockHttpServletRequest request = request("GET", "/api/protected");
        request.setCookies(new Cookie("access_token", "valid-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldSkipPublicEndpoints() {
        assertThat(filter.shouldNotFilter(request("OPTIONS", "/api/protected"))).isTrue();
        assertThat(filter.shouldNotFilter(request("GET", "/actuator/health"))).isTrue();
        assertThat(filter.shouldNotFilter(request("GET", "/v3/api-docs"))).isTrue();
        assertThat(filter.shouldNotFilter(request("GET", "/v3/api-docs/swagger-config"))).isTrue();
        assertThat(filter.shouldNotFilter(request("GET", "/swagger-ui.html"))).isTrue();
        assertThat(filter.shouldNotFilter(request("GET", "/swagger-ui/index.html"))).isTrue();
        assertThat(filter.shouldNotFilter(request("POST", "/api/auth/login"))).isTrue();
        assertThat(filter.shouldNotFilter(request("POST", "/api/auth/logout"))).isTrue();
    }

    @Test
    void shouldFilterPrivateEndpoints() {
        assertThat(filter.shouldNotFilter(request("GET", "/api/protected"))).isFalse();
        assertThat(filter.shouldNotFilter(request("GET", "/api/auth/me"))).isFalse();
        assertThat(filter.shouldNotFilter(request("GET", "/api/auth/login"))).isFalse();
        assertThat(filter.shouldNotFilter(request("GET", "/api/auth/logout"))).isFalse();
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }

    private User user(boolean active) {
        return User.restore(
                new UserId(1L),
                "Renan",
                new PersonId(1L),
                new Email("renan@example.com"),
                new PasswordHash("hashed-psw"),
                false,
                active,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
