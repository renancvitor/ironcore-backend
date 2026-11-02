package com.ironcore.interfaces.rest.exception.handler;

import com.ironcore.application.exception.AuthenticationRateLimitException;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class AuthenticationRateLimitHandlerTest {

    @Test
    void countsRejectionsWithoutPublishingIndividualErrorLogs() {
        var publisher = mock(ErrorLogPublisher.class);
        var handler = new GlobalExceptionHandler(publisher);
        var registry = new SimpleMeterRegistry();
        Metrics.addRegistry(registry);
        try {
            for (int i = 0; i < 100; i++) {
                var response = handler.handleAuthenticationRateLimitException(
                        new AuthenticationRateLimitException(30), new MockHttpServletRequest());
                assertThat(response.getStatusCode().value()).isEqualTo(429);
                assertThat(response.getHeaders().getFirst("Retry-After")).isEqualTo("30");
            }
            assertThat(registry.get("authentication.rate.limit.rejections").counter().count()).isEqualTo(100);
            verifyNoInteractions(publisher);
        } finally {
            Metrics.removeRegistry(registry);
            registry.close();
        }
    }
}
