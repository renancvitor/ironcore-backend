# Diagrama Entidade-Relacionamento (ER)

<p align="center">
    <img src="IronCoreERDiagram.png" alt="Diagrama entidade-relacionamento do IronCore" />
</p>

## Visão Geral

Este documento descreve o modelo relacional implementado da aplicação **IronCore**. Migrations Flyway são a fonte de verdade para schema, FKs, cascades e constraints.

A posição visual das tabelas na imagem prioriza legibilidade e não representa prioridade funcional, criticidade técnica ou ordem de entrega.

## Leitura Rapida do Domínio

Modelo implementado:

- `persons`: dados pessoais.
- `users`: autenticação/acesso, com referencia para `persons`.
- `body_metrics`: histórico de medições corporais da pessoa.
- `audit_logs`: registros persistidos de auditoria.
- `error_logs`: registros técnicos de erro.
- `activity_types`, `equipment_types`, `muscle_groups` e `muscle_subgroups`: catálogos auxiliares globais.
- `exercises`: catálogo de exercícios.
- `exercise_muscle_targets`: associação entre exercícios e subgrupos musculares.

Workout planning implementado no diagrama:

- `training_goals`: catálogo de objetivos de treino.
- `workout_cycles`: ciclos ou planos de treino da pessoa.
- `workout_days`: divisão semanal do ciclo.
- `workout_activities`: atividades prescritas em cada dia de treino.

## Decisão Estrutural Principal

O modelo atual separa dados pessoais, conta de acesso e medições corporais:

```text
Person = dados pessoais
User = autenticação/acesso
BodyMetrics = métricas corporais da pessoa
```

Consequências:

- `users.person_id` referencia `persons.id`.
- `body_metrics.person_id` referencia `persons.id`.
- A API pode continuar partindo do usuário autenticado.
- A ownership interna de dados físicos passa a ser baseada em `PersonId`.

## Tabelas Implementadas

### `persons`

Armazena os dados pessoais vinculados ao usuário e aos dados corporais.

| Campo | Tipo | Obrigatório | Descricao |
|---|---|---|---|
| `id` | `BIGINT` | Sim | Identificador único da pessoa. |
| `name` | `VARCHAR` | Sim | Nome da pessoa. Não possui unicidade. |
| `sex` | `VARCHAR` | Sim | Sexo usado em regras corporais, como cálculo Navy. |
| `birth_date` | `DATE` | Sim | Data de nascimento. |
| `created_at` | `TIMESTAMP` | Sim | Data e hora de criação do registro. |
| `updated_at` | `TIMESTAMP` | Não | Data e hora da última atualização. |

### `users`

Armazena credenciais, identificação de conta e estado técnico de acesso.

| Campo | Tipo | Obrigatório | Descricao |
|---|---|---|---|
| `id` | `BIGINT` | Sim | Identificador único do usuário. |
| `nickname` | `VARCHAR` | Sim | Nome de exibição/conta. |
| `person_id` | `BIGINT` | Sim | Pessoa vinculada ao usuário. |
| `email` | `VARCHAR` | Sim | E-mail único para autenticação. |
| `password_hash` | `VARCHAR` | Sim | Hash da senha do usuário. |
| `must_change_password` | `BOOLEAN` | Sim | Indica troca obrigatória de senha inicial. |
| `active` | `BOOLEAN` | Sim | Status técnico de acesso. |
| `created_at` | `TIMESTAMP` | Sim | Data e hora de criação do registro. |
| `updated_at` | `TIMESTAMP` | Não | Data e hora da última atualização. |

### `body_metrics`

Registra o histórico de medições corporais de cada pessoa.

| Campo | Tipo | Obrigatório | Descricao |
|---|---|---|---|
| `id` | `BIGINT` | Sim | Identificador único da medição. |
| `person_id` | `BIGINT` | Sim | Pessoa dona da medição. |
| `measured_at` | `TIMESTAMP` | Sim | Data e hora em que a medição foi registrada. |
| `weight_kg` | `DOUBLE PRECISION` | Sim | Peso corporal em quilogramas. |
| `height_cm` | `DOUBLE PRECISION` | Sim | Altura em centímetros. |
| `neck_cm` | `DOUBLE PRECISION` | Não | Medida do pescoço em centímetros. |
| `chest_cm` | `DOUBLE PRECISION` | Não | Medida do tórax em centímetros. |
| `shoulder_cm` | `DOUBLE PRECISION` | Não | Medida do ombro em centímetros. |
| `arm_cm` | `DOUBLE PRECISION` | Não | Medida do braço em centímetros. |
| `forearm_cm` | `DOUBLE PRECISION` | Não | Medida do antebraço em centímetros. |
| `waist_cm` | `DOUBLE PRECISION` | Não | Medida da cintura em centímetros. |
| `hip_cm` | `DOUBLE PRECISION` | Não | Medida do quadril em centímetros. |
| `thigh_cm` | `DOUBLE PRECISION` | Não | Medida da coxa em centímetros. |
| `calf_cm` | `DOUBLE PRECISION` | Não | Medida da panturrilha em centímetros. |
| `bmi` | `DOUBLE PRECISION` | Não | Índice de Massa Corporal. |
| `body_fat_percentage` | `DOUBLE PRECISION` | Não | Percentual de gordura corporal. |
| `fat_mass_kg` | `DOUBLE PRECISION` | Não | Massa gorda em quilogramas. |
| `lean_mass_kg` | `DOUBLE PRECISION` | Não | Massa magra em quilogramas. |
| `updated_at` | `TIMESTAMP` | Não | Data e hora da última atualização. |
| `notes` | `TEXT` | Não | Observações livres sobre a medição. |

### `audit_logs`

Armazena registros de auditoria de ações relevantes.

A tabela existe nas migrations atuais e é documentada em [Audit Log](../../logging/audit-log.md).

### `error_logs`

Armazena registros técnicos de erro.

A tabela existe nas migrations atuais e é documentada em [Error Log](../../logging/error-log.md).

### `activity_types`

Classifica a natureza da atividade física.

| Campo | Tipo | Obrigatório | Descricao |
|---|---|---|---|
| `id` | `BIGINT` | Sim | Identificador único do tipo de atividade. |
| `code` | `VARCHAR` | Sim | Código estável do catálogo. |
| `display_name` | `VARCHAR` | Sim | Nome para apresentação. |
| `active` | `BOOLEAN` | Sim | Indica se o item está ativo. |
| `sort_order` | `INTEGER` | Sim | Ordem de exibição. |

### `equipment_types`

Classifica o tipo de equipamento exigido no exercício.

| Campo | Tipo | Obrigatório | Descricao |
|---|---|---|---|
| `id` | `BIGINT` | Sim | Identificador único do tipo de equipamento. |
| `code` | `VARCHAR` | Sim | Código estável do catálogo. |
| `display_name` | `VARCHAR` | Sim | Nome para apresentação. |
| `active` | `BOOLEAN` | Sim | Indica se o item está ativo. |
| `sort_order` | `INTEGER` | Sim | Ordem de exibição. |

### `muscle_groups`

Classifica grupamentos musculares principais.

| Campo | Tipo | Obrigatório | Descricao |
|---|---|---|---|
| `id` | `BIGINT` | Sim | Identificador único do grupo muscular. |
| `code` | `VARCHAR` | Sim | Código estável do catálogo. |
| `display_name` | `VARCHAR` | Sim | Nome para apresentação. |
| `active` | `BOOLEAN` | Sim | Indica se o item está ativo. |
| `sort_order` | `INTEGER` | Sim | Ordem de exibição. |

### `muscle_subgroups`

Classifica subgrupos musculares vinculados a um grupamento principal.

| Campo | Tipo | Obrigatório | Descricao |
|---|---|---|---|
| `id` | `BIGINT` | Sim | Identificador único do subgrupo muscular. |
| `muscle_group_id` | `BIGINT` | Sim | Grupo muscular vinculado. |
| `code` | `VARCHAR` | Sim | Código estável do catálogo. |
| `display_name` | `VARCHAR` | Sim | Nome para apresentação. |
| `active` | `BOOLEAN` | Sim | Indica se o item está ativo. |
| `sort_order` | `INTEGER` | Sim | Ordem de exibição. |

### `exercises`

Catalogo central de exercícios disponíveis para a composição atual de `workout_activities`.

| Campo | Tipo | Obrigatório | Descricao |
|---|---|---|---|
| `id` | `BIGINT` | Sim | Identificador único do exercise. |
| `name` | `VARCHAR` | Sim | Nome do exercício. |
| `equipment_type_id` | `BIGINT` | Sim | Tipo de equipamento vinculado. |
| `activity_type_id` | `BIGINT` | Sim | Tipo de atividade vinculado. |
| `unilateral` | `BOOLEAN` | Sim | Indica exercício unilateral. |
| `compound` | `BOOLEAN` | Sim | Indica exercício composto. |
| `suggested_rest_seconds` | `INTEGER` | Não | Descanso sugerido em segundos. |
| `active` | `BOOLEAN` | Sim | Indica se o exercise está ativo. |
| `created_at` | `TIMESTAMP` | Sim | Data e hora de criação. |
| `updated_at` | `TIMESTAMP` | Não | Data e hora da última atualização. |

### `exercise_muscle_targets`

Associa exercises a subgrupos musculares e diferencia o papel do alvo muscular.

| Campo | Tipo | Obrigatório | Descricao |
|---|---|---|---|
| `id` | `BIGINT` | Sim | Identificador único da associação. |
| `exercise_id` | `BIGINT` | Sim | Exercise vinculado. |
| `muscle_subgroup_id` | `BIGINT` | Sim | Subgrupo muscular vinculado. |
| `target_role` | `VARCHAR` | Sim | Papel do alvo muscular: `PRIMARY`, `SECONDARY` ou `STABILIZER`. |
| `active` | `BOOLEAN` | Sim | Indica se a associação está ativa. |
| `created_at` | `TIMESTAMP` | Sim | Data e hora de criação. |
| `updated_at` | `TIMESTAMP` | Não | Data e hora da última atualização. |

## Tabelas de Workout Planning

### `training_goals`

Representa o objetivo principal do ciclo de treino.

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---:|---|
| `id` | `BIGINT` | Sim | Identificador do objetivo. |
| `code` | `VARCHAR(50)` | Sim | Código único do catálogo. |
| `display_name` | `VARCHAR(100)` | Sim | Nome de apresentação. |
| `active` | `BOOLEAN` | Sim | Disponibilidade do objetivo. |
| `sort_order` | `INTEGER` | Sim | Ordem de apresentação. |

### `workout_cycles`

Representa um ciclo de treino associado a uma pessoa e a um objetivo específico.

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---:|---|
| `id` | `BIGINT` | Sim | Identificador do ciclo. |
| `person_id` | `BIGINT` | Sim | Pessoa proprietária. |
| `name` | `VARCHAR(150)` | Sim | Nome do ciclo. |
| `training_goal_id` | `BIGINT` | Sim | Objetivo vinculado. |
| `start_date` / `end_date` | `DATE` | Não | Datas do lifecycle. |
| `desired_duration_months` | `INTEGER` | Não | Duração desejada. |
| `workout_status` / `workout_origin` | `VARCHAR(50)` | Sim | Estado e origem do ciclo. |
| `notes` | `TEXT` | Não | Observações. |
| `created_at` / `updated_at` | `TIMESTAMP` | Sim / Não | Timestamps do registro. |

### `workout_days`

Organiza os dias de treino vinculados a um ciclo.

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---:|---|
| `id` | `BIGINT` | Sim | Identificador do dia. |
| `workout_cycle_id` | `BIGINT` | Sim | Ciclo vinculado. |
| `week_day` | `INTEGER` | Sim | Valor entre 1 e 7. |
| `title` | `VARCHAR(100)` | Sim | Título do dia. |
| `sort_order` | `INTEGER` | Sim | Ordem positiva dentro do ciclo e dia da semana. |
| `created_at` / `updated_at` | `TIMESTAMP` | Sim / Não | Timestamps do registro. |

### `workout_activities`

Detalha cada atividade prescrita dentro de um dia de treino.

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---:|---|
| `id` | `BIGINT` | Sim | Identificador da atividade. |
| `workout_day_id` / `exercise_id` | `BIGINT` | Sim | Dia e exercise vinculados. |
| `order_index` | `INTEGER` | Sim | Ordem da atividade no dia. |
| `sets`, `rep_range_min`, `rep_range_max`, `duration_minutes`, `rest_seconds` | `INTEGER` | Não | Dados opcionais de prescrição. |
| `target_load_kg`, `distance_km` | `NUMERIC(5,2)` | Não | Dados numéricos opcionais. |
| `target_load_text`, `intensity_text` | `VARCHAR(100)` | Não | Dados textuais opcionais. |
| `notes` | `TEXT` | Não | Observações opcionais. |
| `created_at` / `updated_at` | `TIMESTAMP` | Sim / Não | Timestamps do registro. |

## Relacionamentos

Relacionamentos implementados:

- `persons` 1:1 `users`
- `persons` 1:N `body_metrics`
- `equipment_types` 1:N `exercises`
- `activity_types` 1:N `exercises`
- `muscle_groups` 1:N `muscle_subgroups`
- `muscle_subgroups` 1:N `exercise_muscle_targets`
- `exercises` 1:N `exercise_muscle_targets`

Relacionamentos implementados de workout planning:

- `persons` 1:N `workout_cycles`
- `training_goals` 1:N `workout_cycles`
- `workout_cycles` 1:N `workout_days`
- `workout_days` 1:N `workout_activities`
- `exercises` 1:N `workout_activities`

## Constraints de Workout Planning

- `workout_cycles.person_id` usa `ON DELETE CASCADE`; `training_goal_id` não possui cascade explícito.
- `workout_days.workout_cycle_id` e `workout_activities.workout_day_id` usam `ON DELETE CASCADE`; `exercise_id` não possui cascade explícito.
- `workout_days` possui `UNIQUE (workout_cycle_id, week_day, sort_order) DEFERRABLE INITIALLY DEFERRED`, `CHECK (week_day BETWEEN 1 AND 7)` e `CHECK (sort_order > 0)`.
- `workout_activities` possui `UNIQUE (workout_day_id, order_index) DEFERRABLE INITIALLY DEFERRED` e `UNIQUE (workout_day_id, exercise_id)`.

## Fluxo Logico do Domínio

De forma resumida, o fluxo implementado funciona assim:

1. uma `person` possui dados pessoais;
2. um `user` referencia uma `person` para autenticar e acessar o sistema;
3. `body_metrics` registra medições corporais da `person`;
4. `exercises` registra o catálogo global controlado pelo sistema;
5. cada exercise é classificado por equipamento, tipo de atividade e alvos musculares;
6. a mesma `person` pode possuir vários `workout_cycles`;
7. cada `workout_cycle` será criado com base em um `training_goal`;
8. cada ciclo será dividido em varios `workout_days`;
9. cada dia contera varias `workout_activities`;
10. cada atividade apontará para um item do catálogo `exercises`.

## Observações de Modelagem

- `Person` é a base de dados pessoais.
- `User` é a conta de autenticação/acesso.
- `BodyMetrics` pertence a `PersonId`.
- `users.person_id` é único no modelo atual.
- `body_metrics.person_id` permite histórico N:1 de medições corporais por pessoa.
- Catálogos de exercises são globais e controlados pelo sistema.
- A API atual do catálogo expõe consultas e não CRUD público.
- A imagem deste diagrama foi criada com o [dbdiagram.io](https://dbdiagram.io/).

<p align="right"><a href="../../../README.md">Voltar para a documentação completa</a></p>
