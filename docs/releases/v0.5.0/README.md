# v0.5.0 - Swagger/OpenAPI

## Resumo

`v0.5.0` consolida a documentação OpenAPI dos contratos REST atuais do IronCore Backend.

Esta release está associada ao milestone GitHub `v8.0.0`, cujo foco foi configurar Springdoc, expor Swagger UI e `/v3/api-docs`, documentar os endpoints existentes e padronizar respostas de erro, schemas e autenticação por cookie JWT no contrato OpenAPI.

## Escopo Entregue

- Dependência Springdoc configurada no backend.
- Swagger UI disponível localmente.
- Endpoint `/v3/api-docs` disponível localmente.
- Metadados globais da API configurados.
- Tags OpenAPI para autenticação, usuário, pessoa e medidas corporais.
- Security scheme `access-token-cookie` representando o cookie HTTP-only `access_token`.
- Documentação dos endpoints de autenticação.
- Documentação dos endpoints de usuário autenticado.
- Documentação do endpoint de pessoa vinculada.
- Documentação dos endpoints principais de body metrics.
- Documentação dos endpoints de progresso de body metrics.
- Schemas de erro `ApiErrorResponse` e `FieldErrorResponse`.
- Responses reutilizáveis para erros HTTP comuns.
- Schemas de request/response associados aos endpoints documentados.
- Campos de senha documentados com `format: password`.
- Interfaces documentais por fluxo REST para reduzir poluição de annotations nos controllers.
- Documentação técnica adicionada em `docs/swagger`.

## URLs Locais

```text
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/v3/api-docs
```

## Decisões Técnicas

- A documentação OpenAPI fica concentrada em interfaces documentais por fluxo REST.
- Controllers preservam responsabilidade de entrada HTTP e execução de lógica.
- Componentes OpenAPI genéricos e reutilizáveis ficam em `interfaces.rest.openapi`.
- Endpoints públicos declaram ausência explícita de security no contrato.
- Endpoints protegidos herdam o security scheme global baseado em cookie JWT.
- DTOs recebem `@Schema` apenas quando a anotação acrescenta clareza real ao contrato.
- Model attributes usados em endpoints `GET` são documentados como query params individuais.

## Fluxos Documentados

### Autenticação

- `POST /api/auth/login`
- `POST /api/auth/logout`

### Usuário

- `POST /api/users/change-initial-password`
- `POST /api/users/me/change-password`
- `GET /api/users/me`
- `PUT /api/users/me/change-nickname`

### Pessoa

- `PATCH /api/users/me/person`

### Body Metrics

- `POST /api/users/me/body-metrics`
- `PUT /api/users/me/body-metrics/{id}`
- `DELETE /api/users/me/body-metrics/{id}`
- `GET /api/users/me/body-metrics`
- `GET /api/users/me/body-metrics/{id}`
- `GET /api/users/me/body-metrics/latest`
- `GET /api/users/me/body-metrics/progress/body-composition`
- `GET /api/users/me/body-metrics/progress/circumferences`
- `GET /api/users/me/body-metrics/progress/body-fat`
- `GET /api/users/me/body-metrics/progress/changes`

## Documentação

Documentação técnica principal:

- [Swagger/OpenAPI](../../swagger/README.md)

Documentações relacionadas:

- [Arquitetura](../../architecture/README.md)
- [Exceptions](../../exceptions/README.md)
- [Users e Auth](../../users/README.md)
- [Persons](../../persons/README.md)
- [Body Metrics](../../body-metrics/README.md)
- [Testes Automatizados](../../testing/README.md)
- [Releases](../README.md)

## Validação

Validações locais usadas durante o milestone:

```bash
./mvnw test
./mvnw clean verify --batch-mode
```

Validações manuais:

- Swagger UI acessível em ambiente local.
- `/v3/api-docs` respondendo com JSON OpenAPI.
- Security scheme de cookie JWT presente.
- Endpoints públicos sem security.
- Endpoints protegidos cobertos pelo security scheme global.
- Schemas de erro presentes.
- Schemas de request/response associados às operações REST.

<p align="right"><a href="../README.md">Voltar para releases</a></p>
