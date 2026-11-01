package com.ironcore.interfaces.rest.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "ironcore.bootstrap.single-user.enabled=false", "ironcore.bootstrap.person.enabled=false", "spring.config.import=",
        "spring.datasource.url=jdbc:h2:mem:proxy-security;DB_CLOSE_DELAY=-1"
})
@ActiveProfiles({"prod", "test"})
@DirtiesContext
class ProductionProxySecurityIntegrationTest {
    @LocalServerPort int port;

    @Test
    void recognizesHttpsFromLoopbackProxyAndEmitsHsts() throws Exception {
        try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/actuator/health"))
                    .header("X-Forwarded-Proto", "https").timeout(Duration.ofSeconds(10)).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Strict-Transport-Security")).hasValueSatisfying(value ->
                    assertThat(value).contains("max-age=", "includeSubDomains"));
            assertThat(response.headers().firstValue("X-Content-Type-Options")).hasValue("nosniff");
        }
    }
}
