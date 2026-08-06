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

## Modelo Implementado

O schema funcional atual separa dados pessoais, acesso e medições corporais:

```text
persons
  ↑
  ├── users.person_id
  └── body_metrics.person_id

activity_types -> exercises
equipment_types -> exercises
muscle_groups -> muscle_subgroups -> exercise_muscle_targets -> exercises
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

## Observações Sobre os Diagramas

Alguns diagramas descrevem um modelo de dados planejado mais amplo, incluindo objetivos de treino, ciclos, dias e atividades de treino. Essas tabelas são blueprint/escopo futuro quando não estiverem presentes nas migrations atuais.

Somente as tabelas criadas pelas migrations listadas acima devem ser tratadas como estado de banco implementado.

## Serviços Locais

`docker-compose.dev.yml` starts:

- PostgreSQL 17
- MongoDB 7

PostgreSQL é usado pela persistência funcional atual. MongoDB está preparado para trabalho futuro e ainda não deve ser descrito como módulo funcional.

## Observação Sobre o Perfil de Produção

`application-prod.yml` atualmente desabilita o Flyway. Isso deve ser revisado antes de um deploy real em produção. Essa é uma limitação conhecida de configuração, não uma decisão validada de produção.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
