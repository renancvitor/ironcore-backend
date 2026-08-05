# Swagger UI

## Propósito

O Swagger UI é a interface visual da documentação OpenAPI do IronCore Backend.

Ele permite navegar pelos endpoints REST atuais, consultar request/response schemas, visualizar respostas de erro e executar chamadas manuais em ambiente local.

## URL Local

```text
http://localhost:8080/swagger-ui/index.html
```

## Organização

A interface agrupa os endpoints pelas tags configuradas no contrato OpenAPI:

- `Autenticação`: login e logout.
- `Usuário`: dados e operações do usuário autenticado.
- `Pessoa`: atualização da pessoa vinculada ao usuário autenticado.
- `Medidas corporais`: registros, consultas, listagem e progresso de métricas corporais.
- `Catálogo de exercícios`: catálogos auxiliares globais.
- `Exercício`: listagem filtrada e detalhe do catálogo de exercises.

## Autenticação

O Swagger UI exibe o security scheme `access-token-cookie`.

O backend usa o cookie HTTP-only `access_token` como mecanismo de autenticação JWT.

Fluxo operacional:

1. Execute `POST /api/auth/login`.
2. O backend retorna o usuário autenticado e emite o cookie `access_token`.
3. Requisições protegidas devem enviar esse cookie.
4. Execute `POST /api/auth/logout` para expirar o cookie.

Endpoints públicos aparecem sem exigência de autenticação no contrato:

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/users/change-initial-password`

## Parâmetros e Schemas

O Swagger UI mostra:

- path params, como `id`;
- query params, como `page`, `size`, `startDate`, `endDate`, `name`, `activityTypeId`, `equipmentTypeId`, `muscleGroupId`, `muscleSubgroupId` e `targetRole`;
- request bodies de endpoints `POST`, `PUT` e `PATCH`;
- response schemas de sucesso;
- schemas padronizados de erro.

Campos de senha são documentados com `format: password`.

## Demonstrações Visuais

A documentação visual por GIFs não foi criada nesta etapa.

Como o projeto ainda está em expansão, demonstrações por endpoint tenderiam a gerar alto custo de manutenção e pouco valor técnico. Quando os fluxos funcionais estiverem mais maduros, a documentação visual poderá ser adicionada por jornadas completas, como autenticação, atualização de dados do usuário, ciclo de métricas corporais e consulta do exercise catalog.

<p align="right"><a href="README.md">Voltar para Swagger/OpenAPI</a></p>
