package com.ironcore.interfaces.rest.support.security;

import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

public final class AuthenticatedUserRequestPostProcessorFactory {

    private AuthenticatedUserRequestPostProcessorFactory() {
    }

    public static RequestPostProcessor authenticatedUser() {
        return authenticatedUser(1L, "renan@example.com", false);
    }

    public static RequestPostProcessor authenticatedUser(
            Long userId,
            String email,
            boolean initialPassword
    ) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                new UserId(userId),
                new Email(email),
                initialPassword
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                List.of()
        );

        return request -> {
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            return request;
        };
    }
}
