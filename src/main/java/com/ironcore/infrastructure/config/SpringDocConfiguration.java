package com.ironcore.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SpringDocConfiguration {

    private static final String ACCESS_TOKEN_COOKIE_SECURITY_SCHEME = "access-token-cookie";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(
                                ACCESS_TOKEN_COOKIE_SECURITY_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("access_token")
                                        .description("JWT de autenticação armazenado e enviado via cookie HTTP-only.")
                        ))
                .addSecurityItem(new SecurityRequirement().addList(ACCESS_TOKEN_COOKIE_SECURITY_SCHEME))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Ambiente local"))
                .info(new Info()
                        .title("IronCore Backend API")
                        .version("v0.4.0")
                        .description("""
                                API REST do IronCore para autenticação single-user, gerenciamento do usuário autenticado,
                                pessoa vinculada e métricas corporais.

                                A API utiliza autenticação via JWT armazenado em cookie HTTP-only e possui tratamento
                                padronizado de erros.
                                """)
                        .contact(new Contact()
                                .name("Renan C. Vitor")
                                .url("https://github.com/renancvitor"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://github.com/renancvitor/ironcore-backend/blob/main/LICENSE")))
                .tags(List.of(
                        new Tag().name("Autenticação").description("Fluxo de autenticação e sessão."),
                        new Tag().name("Usuário").description("Dados do usuário autenticado."),
                        new Tag().name("Pessoa").description("Pessoa vinculada ao usuário autenticado."),
                        new Tag().name("Medidas corporais").description("Métricas corporais e evolução física.")
                ));
    }
}
