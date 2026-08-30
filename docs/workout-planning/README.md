# Workout Planning

## Propósito

O módulo `workoutplanning` implementa o planejamento de treinos da pessoa vinculada ao usuário autenticado. Neste documento, `workout` pode ser usado como abreviação de `WorkoutCycle`.

O escopo implementado cobre objetivos de treino, ciclos, dias, atividades prescritas, composição, lifecycle, consultas e auditoria. Workout Session, execução real, histórico de sessões e geração por IA não fazem parte deste módulo.

## Composição e Ownership

```text
AuthenticatedUser -> User -> PersonId -> WorkoutCycle

WorkoutCycle
  -> WorkoutDay
      -> WorkoutActivity
          -> Exercise
```

`WorkoutCycle` pertence a `PersonId`. `WorkoutDay` e `WorkoutActivity` são acessados pelo encadeamento até o ciclo da pessoa; o cliente não informa `personId` em nenhum endpoint de workout planning.

`Exercise` continua sendo um catálogo global controlado pelo sistema. Uma atividade obrigatoriamente referencia um exercise existente e ativo.

## TrainingGoal

`TrainingGoal` é o catálogo global que define o objetivo principal de um `WorkoutCycle`.

Campos:

| Campo | Descrição |
|---|---|
| `id` | Identificador do objetivo. |
| `code` | Código estável do catálogo. |
| `displayName` | Nome de apresentação. |
| `active` | Indica se o objetivo pode ser utilizado. |
| `sortOrder` | Ordem de apresentação. |

A API expõe somente registros ativos, ordenados por `sort_order` crescente e, em empate, por `display_name` crescente. A migration `V22__seed_training_goals.sql` insere `HYPERTROPHY`, `STRENGTH`, `FAT_LOSS`, `MAINTENANCE` e `ENDURANCE`.

Um ciclo referencia `TrainingGoalId`. Criação, atualização e início exigem que o objetivo referenciado esteja ativo.

## WorkoutCycle

`WorkoutCycle` representa um ciclo de planejamento vinculado a uma pessoa e a um objetivo de treino.

| Campo | Obrigatório | Regra atual |
|---|---:|---|
| `personId` | Sim | Ownership interno do ciclo. |
| `name` | Sim | Não pode ser nulo, vazio ou composto só por espaços. |
| `trainingGoalId` | Sim | Deve referenciar objetivo existente e ativo nos fluxos de criação/atualização/início. |
| `desiredDurationMonths` | Não | Deve ser positivo quando informado. |
| `startDate` | Não na criação | Definida pelo backend ao iniciar o ciclo. |
| `endDate` | Não na criação | Definida pelo backend ao concluir o ciclo. |
| `workoutStatus` | Sim | Definido pelo fluxo. |
| `workoutOrigin` | Sim | O fluxo REST atual cria ciclos com `MANUAL`. |
| `notes` | Não | Texto livre opcional. |
| `createdAt` | Sim | Definido pelo backend. |
| `updatedAt` | Não na criação | Definido nas alterações. |

O domínio também declara `AGENT` como valor de `WorkoutOrigin`, mas não há fluxo REST de geração por IA implementado.

### Lifecycle

As transições implementadas são:

```text
NOT_STARTED -> IN_PROGRESS -> COMPLETED
NOT_STARTED -> CANCELLED
IN_PROGRESS -> CANCELLED
```

- Criação registra o ciclo em `NOT_STARTED`.
- Início é permitido somente em `NOT_STARTED`, com objetivo ativo, pelo menos um `WorkoutDay` e ao menos uma `WorkoutActivity` em cada dia.
- Conclusão é permitida somente em `IN_PROGRESS`; `endDate` é a data atual do backend e não pode ser anterior a `startDate`.
- Cancelamento é permitido em `NOT_STARTED` ou `IN_PROGRESS`; ciclos `COMPLETED` e `CANCELLED` não podem ser cancelados novamente.

### Edição e exclusão

`WorkoutCycle` pode ser atualizado enquanto não estiver `COMPLETED` ou `CANCELLED`; portanto, `IN_PROGRESS` continua editável.

A exclusão funcional do ciclo é permitida somente em `NOT_STARTED`. Essa regra de use case é distinta dos cascades físicos do banco descritos em [Banco de dados](#banco-de-dados).

### Detalhe e listagem

O detalhe retorna o ciclo da pessoa autenticada com o objetivo, dias, atividades, exercise e grupos musculares do exercise. Os dias são retornados por `weekDay` e `sortOrder`; as atividades, por `orderIndex`.

A listagem é paginada e restrita à `PersonId` do usuário autenticado. Aceita os filtros opcionais `workoutStatus`, `trainingGoalId`, `startDate`, `endDate` e `name`.

- `page`: padrão `0`, mínimo `0`.
- `size`: padrão `20`, mínimo `1`, máximo `100`.
- `startDate` não pode ser posterior a `endDate` quando ambas forem informadas.
- O filtro de período inclui ciclos cuja janela `[startDate, endDate]` se sobrepõe ao período pesquisado; ciclos sem `endDate` são tratados como em aberto.
- `name` usa busca parcial case-insensitive, com escape de caracteres de `LIKE`.
- A ordenação efetiva da listagem é `name` ascendente e `id` ascendente.

## WorkoutDay

`WorkoutDay` representa uma composição semanal dentro de um ciclo.

| Campo | Regra atual |
|---|---|
| `workoutCycleId` | Vínculo obrigatório com o ciclo. |
| `weekDay` | Obrigatório; usa `SUNDAY(1)` até `SATURDAY(7)` no modelo atual. |
| `title` | Obrigatório e não vazio. |
| `sortOrder` | Obrigatório, positivo e representa a ordem dentro do mesmo dia da semana do ciclo. |
| `createdAt` / `updatedAt` | Timestamps do ciclo de vida do registro. |

Na criação, o backend atribui o próximo `sortOrder` para a combinação de ciclo e `weekDay`. O reorder pode mudar tanto o `weekDay` quanto a posição, e renumera as posições afetadas de forma sequencial a partir de `1`. Após uma exclusão, os itens restantes daquele `weekDay` também são renumerados.

Dia pode ser criado, atualizado, reordenado e excluído enquanto o ciclo pai não estiver `COMPLETED` ou `CANCELLED`. Assim, ações em `IN_PROGRESS` são permitidas.

## WorkoutActivity

`WorkoutActivity` é uma prescrição pertencente a um `WorkoutDay`.

### Referências e ordenação

- `workoutDayId` é obrigatório e define a qual dia a atividade pertence.
- `exerciseId` é obrigatório; deve referenciar exercise existente e ativo.
- `orderIndex` é obrigatório no modelo persistido, positivo e atribuído pelo backend na criação como a próxima posição do dia.
- O mesmo `exerciseId` não pode ser associado duas vezes ao mesmo `WorkoutDay`.
- Reorder renumera as atividades do dia de modo sequencial a partir de `1`; exclusão também recompõe essa sequência.

### Campos opcionais de prescrição

Os campos abaixo são opcionais conforme o modelo atual:

- `sets`
- `repRangeMin`
- `repRangeMax`
- `targetLoadKg`
- `targetLoadText`
- `durationMinutes`
- `distanceKm`
- `intensityText`
- `restSeconds`
- `notes`

Invariantes implementadas:

- valores numéricos devem ser positivos quando presentes;
- textos não podem ser vazios ou conter apenas espaços quando presentes;
- `repRangeMin` não pode ser maior que `repRangeMax` quando ambos estão presentes.

Não há exclusividade entre campos de musculação e cardio. O modelo permite deliberadamente composição híbrida de prescrição, inclusive a combinação de séries/repetições/carga com duração/distância/intensidade.

Atividade pode ser criada, atualizada, reordenada e excluída enquanto o ciclo pai não estiver `COMPLETED` ou `CANCELLED`.

## Banco de dados

As migrations são a fonte de verdade do schema.

| Migration | Conteúdo |
|---|---|
| `V18__create_table_training_goals.sql` | Cria `training_goals`, com `code` único. |
| `V19__create_table_workout_cycles.sql` | Cria ciclos, FKs para pessoa e objetivo. |
| `V20__create_table_workout_days.sql` | Cria dias, ordenação e constraints de composição. |
| `V21__create_table_workout_activities.sql` | Cria atividades prescritas, ordenação e unicidade de exercise por dia. |
| `V22__seed_training_goals.sql` | Insere os objetivos iniciais ativos. |

Relações físicas e cascades:

```text
persons
  -> workout_cycles       ON DELETE CASCADE
      -> workout_days     ON DELETE CASCADE
          -> workout_activities ON DELETE CASCADE
```

`workout_cycles.training_goal_id` referencia `training_goals(id)` sem cascade explícito. `workout_activities.exercise_id` referencia `exercises(id)` sem cascade explícito.

Constraints relevantes:

- `workout_days`: `UNIQUE (workout_cycle_id, week_day, sort_order) DEFERRABLE INITIALLY DEFERRED`; `week_day` entre `1` e `7`; `sort_order > 0`.
- `workout_activities`: `UNIQUE (workout_day_id, order_index) DEFERRABLE INITIALLY DEFERRED`; `UNIQUE (workout_day_id, exercise_id)`.
- `workout_days.sort_order` e `workout_activities.order_index` são campos distintos e não intercambiáveis.

As constraints deferrable permitem que a renumeração de posições durante reorder ocorra dentro da mesma transação antes da validação de unicidade no commit.

## REST e OpenAPI

Todos os endpoints exigem o usuário autenticado por cookie `access_token`.

| Recurso | Endpoints implementados |
|---|---|
| TrainingGoal | `GET /api/training-goals` |
| WorkoutCycle | `POST /api/users/me/workout-cycles`; `PUT`, `DELETE` e `GET /{id}`; `PATCH /{id}/start`, `/complete`, `/cancel`; `GET` paginado e filtrado. |
| WorkoutDay | `POST /api/users/me/workout-days`; `PUT`, `DELETE /{id}`; `PATCH /{id}/reorder`. |
| WorkoutActivity | `POST /api/users/me/workout-activities`; `PUT`, `DELETE /{id}`; `PATCH /{id}/reorder`. |

Os detalhes dos contratos HTTP e schemas são expostos pelo Swagger/OpenAPI. Consulte também [Swagger/OpenAPI](../swagger/README.md).

## Auditoria

Os fluxos de `WorkoutCycle`, `WorkoutDay` e `WorkoutActivity` publicam auditoria após sucesso da transação principal.

- Criação publica `CREATE` com estado posterior.
- Atualização e lifecycle do ciclo publicam `UPDATE` com snapshots anterior e posterior.
- Reorder publica `UPDATE` para o item solicitado, com snapshots anterior e posterior.
- Exclusão publica `DELETE` com estado anterior.

Os alvos de auditoria são `WORKOUT_CYCLE`, `WORKOUT_DAY` e `WORKOUT_ACTIVITY`. A documentação transversal permanece em [Audit Log](../logging/audit-log.md).

## Documentação relacionada

- [Banco de Dados e Migrations](../database/README.md)
- [Estratégia de Filtragem](../filtering/README.md)
- [Swagger/OpenAPI](../swagger/README.md)
- [Testes Automatizados](../testing/README.md)
- [Release v1.0.0](../releases/v1.0.0/README.md)

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
