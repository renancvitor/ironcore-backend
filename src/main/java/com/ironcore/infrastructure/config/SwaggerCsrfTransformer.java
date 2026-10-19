package com.ironcore.infrastructure.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", matchIfMissing = true)
public class SwaggerCsrfTransformer extends SwaggerIndexPageTransformer {

    public SwaggerCsrfTransformer(SwaggerUiConfigProperties config, SwaggerUiOAuthProperties oauth,
                                  SwaggerWelcomeCommon welcome, ObjectMapperProvider mapper) {
        super(config, oauth, welcome, mapper);
    }

    @Override
    protected String addCSRF(String initializer) {
        return initializer.replace("presets: [", """
                requestInterceptor: (() => {
                  let pendingCsrf;
                  return async (request) => {
                    const api = new URL('../api/', window.location.href);
                    const target = new URL(request.url, window.location.href);
                    if (target.origin !== api.origin || !target.pathname.startsWith(api.pathname)
                        || ['GET', 'HEAD', 'OPTIONS'].includes((request.method || 'GET').toUpperCase())) {
                      return request;
                    }
                    // Share bootstrap across concurrent writes, but never cache across login/logout.
                    pendingCsrf ??= fetch(new URL('auth/csrf', api), {
                      credentials: 'same-origin',
                      cache: 'no-store'
                    }).then(response => {
                      if (!response.ok) throw new Error('Unable to obtain CSRF token');
                      return response.json();
                    }).finally(() => { pendingCsrf = undefined; });
                    const csrf = await pendingCsrf;
                    request.credentials = 'same-origin';
                    request.headers ??= {};
                    request.headers[csrf.headerName] = csrf.token;
                    return request;
                  };
                })(),
                presets: [
                """);
    }
}
