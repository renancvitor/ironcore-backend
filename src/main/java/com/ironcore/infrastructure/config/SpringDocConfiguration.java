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
    public org.springdoc.core.customizers.OpenApiCustomizer mutableOperationSecurity() {
        return api -> api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
            if (method == io.swagger.v3.oas.models.PathItem.HttpMethod.GET
                    || method == io.swagger.v3.oas.models.PathItem.HttpMethod.OPTIONS
                    || method == io.swagger.v3.oas.models.PathItem.HttpMethod.HEAD) return;
            var requirements = operation.getSecurity();
            if (requirements == null) {
                requirements = new java.util.ArrayList<>();
                requirements.add(new SecurityRequirement().addList(ACCESS_TOKEN_COOKIE_SECURITY_SCHEME));
            }
            if (requirements.isEmpty()) requirements = new java.util.ArrayList<>(List.of(new SecurityRequirement()));
            for (var requirement : requirements) {
                requirement.addList("csrf-header");
                if (path.equals("/api/auth/refresh")) requirement.addList("refresh-token-cookie");
            }
            operation.setSecurity(requirements);
        }));
    }

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
                        ).addSecuritySchemes("refresh-token-cookie", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE).name("refresh_token")
                                .description("Credencial aleatória HttpOnly/Secure, Path=/api/auth, host-only. Hash SHA-256 persistido; rotação a cada uso. Prazo absoluto configurável, máximo 7 dias desde login."))
                        .addSecuritySchemes("csrf-header", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-XSRF-TOKEN")
                                .description("Obtenha token mascarado em GET /api/auth/csrf e mantenha o cookie XSRF-TOKEN. Obrigatório em todas as operações mutáveis, inclusive login, refresh e logout. Reobtenha após login/logout.")))
                .addSecurityItem(new SecurityRequirement().addList(ACCESS_TOKEN_COOKIE_SECURITY_SCHEME))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Ambiente local"))
                .info(new Info()
                        .title("IronCore Backend API")
                        .version("v1.0.0")
                        .description("""
                                API REST do IronCore para autenticação single-user, gerenciamento do usuário autenticado,
                                pessoa vinculada, métricas corporais, catálogo de exercícios e planejamento de treinos.
                        
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
                        new Tag().name("Autenticação")
                                .description("Fluxo de autenticação e sessão."),
                        new Tag().name("Usuário")
                                .description("Dados do usuário autenticado."),
                        new Tag().name("Pessoa")
                                .description("Pessoa vinculada ao usuário autenticado."),
                        new Tag().name("Medidas corporais")
                                .description("Métricas corporais e evolução física."),
                        new Tag().name("Dia de treino")
                                .description("Gerenciamento dos dias de treino."),
                        new Tag().name("Ciclo de treino")
                                .description("Gerenciamento dos ciclos de treino."),
                        new Tag().name("Atividade de treino")
                                .description("Gerenciamento das atividades de treino."),
                        new Tag().name("Objetivos de treino")
                                .description("Objetivos disponíveis para os ciclos de treino."),
                        new Tag().name("Catálogo de exercícios")
                                .description("Catálogos auxiliares utilizados na classificação dos exercícios."),
                        new Tag().name("Exercício")
                                .description("Consulta e filtragem dos exercícios disponíveis no sistema.")
                ));
    }
}
