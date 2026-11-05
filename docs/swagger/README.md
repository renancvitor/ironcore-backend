# Swagger/OpenAPI

## Propósito

Este documento descreve a documentação OpenAPI gerada para a API REST do IronCore Backend.

O projeto usa Springdoc para expor os contratos REST atuais em dois formatos complementares:

- Swagger UI, para navegação visual e teste manual dos endpoints.
- `/v3/api-docs`, para consumo técnico do contrato OpenAPI em JSON.

A documentação cobre os fluxos REST já implementados de autenticação, usuário autenticado, pessoa vinculada, métricas corporais, exercise catalog e workout planning.

## Acessos Locais

Com a aplicação executando localmente na porta padrão:

```text
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/v3/api-docs
```

O acesso ao Swagger UI e ao `/v3/api-docs` é público quando habilitado; ambos estão desabilitados no perfil de produção.

## Documentos

- [Swagger UI](swagger-ui.md): uso da interface visual, autenticação via cookie e organização dos endpoints.
- [`/v3/api-docs`](api-docs.md): leitura do contrato OpenAPI em JSON, schemas, security scheme e respostas de erro.

## Escopo Documentado

Tags atuais:

- `Autenticação`
- `Usuário`
- `Pessoa`
- `Medidas corporais`
- `Catálogo de exercícios`
- `Exercício`
- `Objetivos de treino`
- `Ciclo de treino`
- `Dia de treino`
- `Atividade de treino`
- `Sessão de treino`
- `Indicadores de execução`

Fluxos documentados:

- Login, refresh e logout com revogação no servidor.
- Workout Session, histórico e indicadores por exercício.
- Troca inicial e troca normal de senha.
- Consulta e alteração do usuário autenticado.
- Atualização da pessoa vinculada ao usuário autenticado.
- CRUD, consulta, listagem e progresso de métricas corporais.
- Consulta de catálogos auxiliares de exercises.
- Listagem paginada e filtrada de exercises.
- Consulta detalhada de exercise por id.
- Listagem de objetivos de treino ativos.
- Criação, lifecycle, detalhe, listagem paginada e filtrada de ciclos de treino.
- Criação, atualização, exclusão e reorder de dias e atividades de treino.
- Respostas padronizadas de erro.

## Autenticação no OpenAPI

O contrato OpenAPI declara o security scheme `access-token-cookie`.

Esse scheme representa o JWT armazenado no cookie HTTP-only `access_token`.

Endpoints protegidos herdam a exigência global de autenticação. Endpoints públicos declaram `security: []` no contrato gerado.

## Manutenção

- Controllers devem manter lógica HTTP e execução de use cases.
- Interfaces documentais em `interfaces.rest.<fluxo>.api` concentram `@Operation`, `@ApiResponse`, responses de erro e detalhes OpenAPI.
- Componentes OpenAPI reutilizáveis permanecem em `interfaces.rest.openapi`.
- DTOs devem receber `@Schema` apenas quando isso acrescentar clareza real ao contrato.
- Endpoints `GET` que recebem model attributes devem expor query params reais no OpenAPI.

## Contratos de Sessão e Refresh

O contrato atual inclui `POST /api/auth/refresh` (público quanto ao access token, com cookie de refresh e CSRF), login sem tokens no JSON, cookies `access_token` e `refresh_token`, e resposta `429`/`Retry-After` nos fluxos limitados. Logout revoga a sessão no servidor e limpa ambos os cookies.

Workout Session expõe início, recuperação, detalhe, fechamento, relatos, execução de atividade, séries, modo de carga, histórico e evolução por exercício. Os nomes de campos e caminhos estão em [Workout Session](../workout-session/README.md); `WorkoutSessionApi` e `ExerciseAnalyticsApi` concentram as anotações. Não há mudança Java/OpenAPI nesta tarefa documental.

Antes de uma escrita no Swagger, obtenha `GET /api/auth/csrf` e envie o token no header informado. Repita a obtenção após login/logout. A documentação é acessível nos perfis que a habilitam; Swagger UI e `/v3/api-docs` permanecem desabilitados em produção.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
