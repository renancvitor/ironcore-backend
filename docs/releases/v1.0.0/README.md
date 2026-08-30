# v1.0.0 - Primeira versão estável do IronCore Backend

## Resumo

`v1.0.0` consolida a primeira versão estável do IronCore Backend, reunindo a fundação funcional principal e o fluxo completo de workout planning entregue após `v0.6.0`.

Esta versão estabiliza o backend de forma independente. Ela não representa o produto IronCore completo: frontend, Workout Session e histórico de sessões, e geração de treino por IA continuam fora do escopo implementado.

## Escopo Entregue Desde v0.6.0

- Domínio de `TrainingGoal`.
- Domínio de `WorkoutCycle`, `WorkoutDay` e `WorkoutActivity`.
- Persistência JPA e adapters do workout planning.
- Migrations Flyway `V18` a `V21` para schema do módulo.
- Migration `V22` com seed inicial de objetivos de treino.
- Ownership dos ciclos por `PersonId` resolvido a partir do usuário autenticado.
- Composição `WorkoutCycle -> WorkoutDay -> WorkoutActivity -> Exercise`.
- Criação, atualização, reorder e exclusão de dias e atividades.
- Criação, atualização, início, conclusão, cancelamento, exclusão, detalhe e listagem de ciclos.
- Lifecycle de ciclos com os status `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED` e `CANCELLED`.
- Consulta detalhada da composição do ciclo.
- Listagem paginada de ciclos com filtros combináveis.
- Endpoints REST e contratos OpenAPI para objetivos, ciclos, dias e atividades.
- Auditoria operacional de ciclo, dia e atividade.
- Testes de domínio, aplicação, persistência, REST e integração do fluxo.
- Documentação técnica central do módulo.

## Decisões Funcionais e Arquiteturais

- `WorkoutCycle` pertence à pessoa, e não diretamente ao usuário; a API resolve `PersonId` a partir do usuário autenticado.
- `TrainingGoal` é catálogo global. Apenas objetivos ativos são expostos e podem ser usados nos fluxos de ciclo.
- O fluxo REST cria ciclos com origem `MANUAL`. A enumeração também possui `AGENT`, sem fluxo de geração por IA implementado.
- A composição permite vários dias no mesmo `weekDay`, ordenados por `sortOrder`; atividades de um dia são ordenadas por `orderIndex`.
- A atualização de ciclos, dias e atividades é permitida em `NOT_STARTED` e `IN_PROGRESS`, mas bloqueada em `COMPLETED` e `CANCELLED`.
- A exclusão funcional de `WorkoutCycle` é permitida somente em `NOT_STARTED`. Dias e atividades são bloqueados apenas quando o ciclo pai está concluído ou cancelado.
- A prescrição de `WorkoutActivity` aceita campos híbridos de musculação e cardio sem exclusividade artificial entre eles.
- O início exige ciclo não iniciado, objetivo ativo, pelo menos um dia e ao menos uma atividade em cada dia.
- A listagem de ciclos usa `WorkoutCycleSpecification`, restringe os resultados à pessoa autenticada e ordena por `name ASC, id ASC`.
- As constraints de ordenação de dias e atividades são `DEFERRABLE INITIALLY DEFERRED`, permitindo renumeração segura na transação de reorder.

## Endpoints

Todos os endpoints exigem autenticação por cookie JWT `access_token`.

```http
GET    /api/training-goals

POST   /api/users/me/workout-cycles
PUT    /api/users/me/workout-cycles/{id}
DELETE /api/users/me/workout-cycles/{id}
PATCH  /api/users/me/workout-cycles/{id}/start
PATCH  /api/users/me/workout-cycles/{id}/complete
PATCH  /api/users/me/workout-cycles/{id}/cancel
GET    /api/users/me/workout-cycles/{id}
GET    /api/users/me/workout-cycles

POST   /api/users/me/workout-days
PUT    /api/users/me/workout-days/{id}
DELETE /api/users/me/workout-days/{id}
PATCH  /api/users/me/workout-days/{id}/reorder

POST   /api/users/me/workout-activities
PUT    /api/users/me/workout-activities/{id}
DELETE /api/users/me/workout-activities/{id}
PATCH  /api/users/me/workout-activities/{id}/reorder
```

## Filtros e Paginação

`GET /api/users/me/workout-cycles` aceita:

- `workoutStatus`
- `trainingGoalId`
- `startDate`
- `endDate`
- `name`
- `page` (padrão `0`, mínimo `0`)
- `size` (padrão `20`, mínimo `1`, máximo `100`)

## Banco de Dados

Migrations relacionadas:

- `V18__create_table_training_goals.sql`
- `V19__create_table_workout_cycles.sql`
- `V20__create_table_workout_days.sql`
- `V21__create_table_workout_activities.sql`
- `V22__seed_training_goals.sql`

Modelo central:

```text
persons
  -> workout_cycles
      -> workout_days
          -> workout_activities
              -> exercises

training_goals -> workout_cycles
```

Os cascades físicos seguem `persons -> workout_cycles -> workout_days -> workout_activities`. A regra funcional de exclusão de ciclos permanece mais restritiva que o cascade do banco.

## Documentação

Documentação técnica principal:

- [Workout Planning](../../workout-planning/README.md)

Documentações relacionadas:

- [Banco de Dados e Migrations](../../database/README.md)
- [Estratégia de Filtragem](../../filtering/README.md)
- [Swagger/OpenAPI](../../swagger/README.md)
- [Arquitetura](../../architecture/README.md)
- [Testes Automatizados](../../testing/README.md)
- [Releases](../README.md)

## Validação

Validações locais esperadas para esta release:

```bash
./mvnw test --batch-mode
./mvnw clean verify --batch-mode
```

Validações manuais esperadas:

- Swagger UI exibindo as tags de workout planning.
- `/v3/api-docs` contendo operações, schemas e parâmetros do módulo.
- Banco limpo aplicando migrations `V18` a `V22` sem erro.
- Detalhe do ciclo retornando dias, atividades, exercises e grupos musculares na ordenação implementada.
- Listagem respeitando ownership, filtros, paginação e ordenação.
- Lifecycle, regras de edição, exclusão e auditoria respeitando as precondições do módulo.

<p align="right"><a href="../README.md">Voltar para releases</a></p>
