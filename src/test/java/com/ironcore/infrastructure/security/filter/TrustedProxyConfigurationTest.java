package com.ironcore.infrastructure.security.filter;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.RemoteIpValve;
import org.apache.catalina.valves.ValveBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TrustedProxyConfigurationTest {

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "::1", "0:0:0:0:0:0:0:1"})
    void trustsOnlyTheClientAddressAppendedByTheLocalProxy(String peer) throws Exception {
        assertThat(resolveAddress(peer, "192.0.2.99, 198.51.100.7")).isEqualTo("198.51.100.7");
    }

    @ParameterizedTest
    @ValueSource(strings = {"203.0.113.9", "10.0.0.9", "127.0.0.2"})
    void ignoresForwardedHeadersFromUntrustedPeers(String peer) throws Exception {
        assertThat(resolveAddress(peer, "192.0.2.99")).isEqualTo(peer);
    }

    @Test
    void doesNotUseRealIpHeaderWithoutForwardedFor() throws Exception {
        assertThat(resolveAddress("127.0.0.1", null)).isEqualTo("127.0.0.1");
    }

    private String resolveAddress(String peer, String forwarded) throws Exception {
        PropertySource<?> settings = productionSettings();
        assertThat(settings.getProperty("server.forward-headers-strategy")).isEqualTo("native");
        assertThat(settings.getProperty("server.address")).isEqualTo("127.0.0.1");
        RemoteIpValve valve = new RemoteIpValve();
        valve.setInternalProxies((String) settings.getProperty("server.tomcat.remoteip.internal-proxies"));
        valve.setRemoteIpHeader((String) settings.getProperty("server.tomcat.remoteip.remote-ip-header"));
        valve.setProtocolHeader((String) settings.getProperty("server.tomcat.remoteip.protocol-header"));
        AtomicReference<String> address = new AtomicReference<>();
        valve.setNext(new ValveBase() {
            @Override
            public void invoke(Request request, Response response) {
                address.set(request.getRemoteAddr());
            }
        });

        Request request = new Request(new Connector());
        var coyote = new org.apache.coyote.Request();
        request.setCoyoteRequest(coyote);
        coyote.scheme().setString("http");
        coyote.serverName().setString("localhost");
        coyote.setServerPort(8080);
        request.setRemoteAddr(peer);
        request.setRemoteHost(peer);
        if (forwarded != null) {
            coyote.getMimeHeaders().addValue("X-Forwarded-For").setString(forwarded);
        }
        coyote.getMimeHeaders().addValue("X-Real-IP").setString("192.0.2.100");
        valve.invoke(request, mock(Response.class));
        return address.get();
    }

    private PropertySource<?> productionSettings() throws IOException {
        return new YamlPropertySourceLoader().load("prod", new ClassPathResource("application-prod.yml")).getFirst();
    }
}
