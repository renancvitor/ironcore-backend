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
| `V23__add_column_week_starts_on_to_workout_cycles.sql` | `workout_cycles` | Adiciona `week_starts_on INTEGER NOT NULL DEFAULT 1`, definindo `SUNDAY` como início da semana nos ciclos existentes e como padrão no banco. |
| `V24__create_table_workout_sessions.sql` | `workout_sessions` | Sessão por pessoa, snapshots, lifecycle e índice único parcial de sessão em andamento. |
| `V25__create_table_workout_session_activities.sql` | `workout_session_activities` | Snapshots da prescrição e execução opcional; ordem única por sessão. |
| `V26__create_table_workout_session_sets.sql` | `workout_session_sets` | Séries, carga, modo, repetições e relato; número único por atividade. |
| `V27__allow_partial_workout_session_set_execution.sql` | `workout_session_sets` | Substitui a constraint de execução, permitindo carga/modo, repetições ou relato isolados. |
| `V28__create_table_auth_sessions.sql` | `auth_sessions` | Sessão UUID por usuário, expiração, revogação e versão. |
| `V29__add_auth_session_refresh_token.sql` | `auth_sessions` | Hash opcional de refresh token, formato hexadecimal de 64 caracteres e índice único. |
| `V30__create_auth_attempt_limits.sql` | `auth_attempt_limits` | Contadores por hash do sujeito e janela, com índice de limpeza por início da janela. |
| `V31__add_auth_session_absolute_expiration.sql` | `auth_sessions` | Limite absoluto obrigatório; registros existentes recebem o expires_at anterior como teto. |

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

O [diagrama ER](../diagram/er-sql/README.md) inclui o modelo implementado até V31, com planejamento, execução de treinos, sessões de autenticação e contadores de tentativas. O Mermaid complementa a imagem com um resumo dos relacionamentos de execução e autenticação. O [fluxo de sessão](../diagram/sequence-log-diagram/README.md) detalha a sequência de execução dos treinos. Geração por IA permanece blueprint de produto.

Somente as tabelas criadas pelas migrations listadas acima devem ser tratadas como estado de banco implementado.

Detalhes funcionais e de contrato: [Workout Planning](../workout-planning/README.md).

## Serviços Locais

`docker-compose.dev.yml` starts:

- PostgreSQL 17
- MongoDB 7

PostgreSQL é usado pela persistência funcional atual. MongoDB está preparado para trabalho futuro e ainda não deve ser descrito como módulo funcional.

## Observação Sobre o Perfil de Produção

`application-prod.yml` habilita o Flyway e mantém `baseline-on-migrate=false`. O banco de produção é PostgreSQL no Supabase, na região de São Paulo, com conexão TLS. As credenciais são configuradas fora do Git, em `/etc/ironcore/ironcore.env` na VPS.

O bootstrap inicial foi executado e posteriormente desabilitado em produção. **O rollback do JAR não desfaz migrations Flyway**; mudanças de schema devem permanecer compatíveis com a versão anterior que poderá ser restaurada. Arquitetura e fluxo de publicação: [Deploy e Produção](../deploy/README.md).

## Constraints de Execução e Autenticação

- V24: `person_id` obrigatório com cascade; ciclo/dia opcionais com `SET NULL`. `ck_workout_sessions_lifecycle` relaciona status, fim e motivo; `ck_workout_sessions_dates` valida data local, limite às 03h do dia seguinte e timestamps. `uk_workout_sessions_person_in_progress` é único por pessoa apenas em `IN_PROGRESS`; há índices de pessoa, ciclo e dia.
- V25: atividade pertence à sessão com cascade; referências a atividade planejada e exercício usam `SET NULL`. Ordem positiva única por sessão, faixa de repetições consistente, valores planejados/executados com checks de sinal. Índices para origem e exercício.
- V26/V27: cascade da atividade para séries, `UNIQUE (workout_session_activity_id, set_number)`, número e repetições positivos; carga `NUMERIC(8,2)` não negativa. A constraint final exige carga e modo juntos e pelo menos carga, repetições ou relato não vazio. A uniformidade de modo entre séries é regra de domínio, não check SQL entre linhas.
- V28: `user_id` referencia `users` com cascade; expiração posterior à criação e revogação não anterior à criação; versão para concorrência e índice por usuário.
- V29: hash aceita `NULL` para transição de registros anteriores, caso contrário exige 64 caracteres `[0-9a-f]`; índice único sobre `refresh_token_hash`.
- V30: `subject_hash` é chave primária, `attempts >= 0` e índice por `window_started_at`.
- V31: `expires_at <= absolute_expires_at`, com limite absoluto posterior à criação e até `created_at + INTERVAL '2160 hours'`. O backfill usa a expiração existente, sem conceder nova janela aos registros antigos.

Modelo: `persons -> workout_sessions -> workout_session_activities -> workout_session_sets` e `users -> auth_sessions`. `auth_attempt_limits` é independente, sem FK para usuário. A validade da sessão e a retenção dos registros têm regras diferentes: [Users e Auth](../users/README.md).

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
