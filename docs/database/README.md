# Banco de Dados e Migrations

## Visão Geral

Este documento descreve a estratégia de persistência, migrations e leitura do schema do IronCore Backend.

A persistência transacional do projeto usa banco relacional versionado por migrations. Outros serviços de dados devem ser documentados aqui apenas quando houver uso funcional no código.

## Banco Relacional

- Banco: PostgreSQL.
- Ferramenta de migration: Flyway.
- Caminho das migrations: `src/main/resources/db/migration`.
- Local padrão do Flyway: `classpath:db/migration`.

A configuração base da aplicação habilita o Flyway e usa `hibernate.ddl-auto=validate`.

## Migrations Atuais

| Arquivo | Tabela | Finalidade |
|---|---|---|
| `V1__create_table_persons.sql` | `persons` | Armazena dados pessoais: nome, sexo, data de nascimento e timestamps. |
| `V2__create_table_users.sql` | `users` | Armazena conta de acesso: nickname, `person_id`, email, hash de senha, flags de acesso e timestamps. |
| `V3__create_table_body_metrics.sql` | `body_metrics` | Armazena medições corporais da pessoa, valores calculados de composição corporal e observações. |
| `V4__create_table_audit_log.sql` | `audit_logs` | Armazena registros de auditoria para ações relevantes, incluindo actor, action, target e snapshots opcionais. |
| `V5__create_table_error_log.sql` | `error_logs` | Armazena registros técnicos de erro com error code, message, exception class, contexto HTTP, user id opcional e correlation id. |
| `V6__create_tabela_activity_types.sql` | `activity_types` | Armazena tipos de atividade do catálogo global de exercises. |
| `V7__create_table_equipment_types.sql` | `equipment_types` | Armazena tipos de equipamento do catálogo global de exercises. |
| `V8__create_table_muscle_groups.sql` | `muscle_groups` | Armazena grupos musculares controlados pelo sistema. |
| `V9__create_table_muscle_subgroups.sql` | `muscle_subgroups` | Armazena subgrupos musculares vinculados a grupos musculares. |
| `V10__create_table_exercises.sql` | `exercises` | Armazena exercises do catálogo global, vinculados a tipo de atividade e equipamento. |
| `V11__create_table_exercise_muscle_targets.sql` | `exercise_muscle_targets` | Armazena associações entre exercises e subgrupos musculares alvo. |
| `V12__seed_activity_types.sql` | `activity_types` | Insere seed inicial dos tipos de atividade. |
| `V13__seed_equipment_types.sql` | `equipment_types` | Insere seed inicial dos tipos de equipamento. |
| `V14__seed_muscle_groups.sql` | `muscle_groups` | Insere seed inicial dos grupos musculares. |
| `V15__seed_muscle_subgroups.sql` | `muscle_subgroups` | Insere seed inicial dos subgrupos musculares. |
| `V16__seed_exercises.sql` | `exercises` | Insere seed inicial com 341 exercises. |
| `V17__seed_exercise_muscle_targets.sql` | `exercise_muscle_targets` | Insere seed inicial com 2766 associações entre exercises e subgrupos musculares. |
| `V18__create_table_training_goals.sql` | `training_goals` | Armazena objetivos globais de treino. |
| `V19__create_table_workout_cycles.sql` | `workout_cycles` | Armazena ciclos de treino pertencentes a uma pessoa e vinculados a um objetivo. |
| `V20__create_table_workout_days.sql` | `workout_days` | Armazena dias que compõem ciclos, com ordenação por dia da semana. |
| `V21__create_table_workout_activities.sql` | `workout_activities` | Armazena atividades prescritas de cada dia. |
| `V22__seed_training_goals.sql` | `training_goals` | Insere os objetivos iniciais de treino. |

## Modelo Implementado

O schema funcional atual separa dados pessoais, acesso, medições corporais, catálogo global de exercises e workout planning:

```text
persons
  ↑
  ├── users.person_id
  └── body_metrics.person_id

activity_types -> exercises
equipment_types -> exercises
muscle_groups -> muscle_subgroups -> exercise_muscle_targets -> exercises

persons -> workout_cycles -> workout_days -> workout_activities -> exercises
training_goals -> workout_cycles
```

Decisões atuais:

- `persons.name` é obrigatório, mas não único.
- `users.person_id` é obrigatório e único, representando a relação 1:1 atual entre conta de acesso e pessoa.
- `body_metrics.person_id` é obrigatório, representando ownership das medições corporais pela pessoa.
- `users.email` permanece único para autenticação.
- `body_metrics` usa `ON DELETE CASCADE` a partir de `persons`.
- Catálogos de exercise são globais e controlados pelo sistema.
- Catálogos auxiliares usam `code`, `display_name`, `active` e `sort_order`.
- `exercise_muscle_targets` impede duplicidade de associação por `exercise_id + muscle_subgroup_id`.
- A carga atual do catálogo é feita por seeds versionados via Flyway.
- `training_goals` é catálogo global com `code` único; a V22 insere cinco objetivos ativos iniciais.
- `workout_cycles.person_id` usa `ON DELETE CASCADE`; `training_goal_id` referencia o catálogo sem cascade explícito.
- `workout_days.workout_cycle_id` e `workout_activities.workout_day_id` usam `ON DELETE CASCADE`.
- `workout_days` usa `sort_order`, com `UNIQUE (workout_cycle_id, week_day, sort_order) DEFERRABLE INITIALLY DEFERRED`, `week_day BETWEEN 1 AND 7` e `sort_order > 0`.
- `workout_activities` usa `order_index`, com `UNIQUE (workout_day_id, order_index) DEFERRABLE INITIALLY DEFERRED` e `UNIQUE (workout_day_id, exercise_id)`.

## Observações Sobre os Diagramas

O diagrama ER inclui o recorte implementado de objetivos de treino, ciclos, dias e atividades. Diagramas de geração por IA e de Workout Session continuam blueprint de produto.

Somente as tabelas criadas pelas migrations listadas acima devem ser tratadas como estado de banco implementado.

Detalhes funcionais e de contrato: [Workout Planning](../workout-planning/README.md).

## Serviços Locais

`docker-compose.dev.yml` starts:

- PostgreSQL 17
- MongoDB 7

PostgreSQL é usado pela persistência funcional atual. MongoDB está preparado para trabalho futuro e ainda não deve ser descrito como módulo funcional.

## Observação Sobre o Perfil de Produção

`application-prod.yml` atualmente desabilita o Flyway. Isso deve ser revisado antes de um deploy real em produção. Essa é uma limitação conhecida de configuração, não uma decisão validada de produção.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
