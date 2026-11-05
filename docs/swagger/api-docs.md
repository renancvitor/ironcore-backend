# `/v3/api-docs`

## Propósito

O endpoint `/v3/api-docs` expõe o contrato OpenAPI em JSON.

Esse contrato é a fonte técnica usada pelo Swagger UI e pode ser consumido por ferramentas externas de documentação, geração de clientes HTTP, validação de contrato ou inspeção automatizada.

## URL Local

```text
http://localhost:8080/v3/api-docs
```

## Conteúdo Principal

O JSON gerado contém:

- metadados globais da API;
- servidor local;
- tags dos módulos REST;
- paths e operações HTTP;
- parâmetros de path/query;
- request bodies;
- responses de sucesso e erro;
- schemas de request/response;
- security scheme baseado no cookie `access_token`.

## Security Scheme

O contrato declara:

```text
access-token-cookie
```

Características:

- tipo: `apiKey`;
- localização: `cookie`;
- nome: `access_token`;
- descrição: JWT de autenticação armazenado e enviado via cookie HTTP-only.

## Endpoints Públicos e Protegidos

O contrato aplica autenticação global aos endpoints protegidos.

Endpoints públicos declaram `security: []`, deixando explícito que não exigem cookie JWT:

- `GET /api/auth/csrf`
- `POST /api/auth/refresh`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/users/change-initial-password`

## Schemas de Erro

As respostas de erro reutilizam os schemas:

- `ApiErrorResponse`
- `FieldErrorResponse`

Esses schemas padronizam campos como `timestamp`, `status`, `error`, `message`, `path` e erros específicos de campo.

## Validação Manual

Para conferir rapidamente o contrato local:

```bash
curl http://localhost:8080/v3/api-docs
```

Pontos principais de validação:

- resposta HTTP `200`;
- presença de `info`, `paths`, `components.schemas` e `components.securitySchemes`;
- endpoints públicos com `security: []`;
- endpoints protegidos cobertos pelo security scheme global;
- schemas de senha com `format: password`;
- parâmetros de progresso expostos como `startDate` e `endDate`.
- parâmetros de listagem de exercises expostos como `page`, `size`, `name`, `activityTypeId`, `equipmentTypeId`, `muscleGroupId`, `muscleSubgroupId` e `targetRole`.
- endpoints de workout planning para `TrainingGoal`, `WorkoutCycle`, `WorkoutDay` e `WorkoutActivity`.
- parâmetros da listagem de ciclos expostos como `workoutStatus`, `trainingGoalId`, `startDate`, `endDate`, `name`, `page` e `size`.

## Contratos de Sessão e Refresh

Na inspeção do JSON gerado, conferir também:

- `LoginResponse` sem `accessToken`/`tokenType` e `RefreshResponse` com `expiresAt`.
- Paths de execução e analytics descritos em [Workout Session](../workout-session/README.md), incluindo `name`, `workoutCycleId`, `workoutDayId` e paginação do histórico.
- PATCH com distinção entre campo omitido e `null` explícito, métricas analíticas anuláveis e respostas `409` de conflito.
- Resposta `429` nos fluxos de autenticação limitados, com `Retry-After`.

As interfaces `WorkoutSessionApi` e `ExerciseAnalyticsApi` e os DTOs são as fontes implementadas; esta tarefa não altera suas anotações Java. O endpoint permanece desabilitado em produção.

<p align="right"><a href="README.md">Voltar para Swagger/OpenAPI</a></p>
