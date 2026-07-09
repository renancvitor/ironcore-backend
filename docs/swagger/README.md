# Swagger/OpenAPI

## Propósito

Este documento descreve a documentação OpenAPI gerada para a API REST do IronCore Backend.

O projeto usa Springdoc para expor os contratos REST atuais em dois formatos complementares:

- Swagger UI, para navegação visual e teste manual dos endpoints.
- `/v3/api-docs`, para consumo técnico do contrato OpenAPI em JSON.

A documentação cobre os fluxos REST já implementados de autenticação, usuário autenticado, pessoa vinculada e métricas corporais.

## Acessos Locais

Com a aplicação executando localmente na porta padrão:

```text
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/v3/api-docs
```

O acesso ao Swagger UI e ao `/v3/api-docs` é público na configuração de segurança atual.

## Documentos

- [Swagger UI](swagger-ui.md): uso da interface visual, autenticação via cookie e organização dos endpoints.
- [`/v3/api-docs`](api-docs.md): leitura do contrato OpenAPI em JSON, schemas, security scheme e respostas de erro.

## Escopo Documentado

Tags atuais:

- `Autenticação`
- `Usuário`
- `Pessoa`
- `Medidas corporais`

Fluxos documentados:

- Login e logout.
- Troca inicial e troca normal de senha.
- Consulta e alteração do usuário autenticado.
- Atualização da pessoa vinculada ao usuário autenticado.
- CRUD, consulta, listagem e progresso de métricas corporais.
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

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
