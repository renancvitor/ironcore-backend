# Estrutura do Projeto

Este documento descreve a organização principal do IronCore Backend.

O projeto segue uma arquitetura em camadas com inspiração pragmática em DDD. A separação principal é:

- `domain`: regras e conceitos de negócio.
- `application`: casos de uso, services de aplicação, ports e eventos.
- `infrastructure`: adapters técnicos, persistência, segurança, bootstrap, configurações e listeners.
- `interfaces`: pontos de entrada REST, DTOs, mappers REST e tratamento HTTP de erros.

## Estrutura Geral

```plaintext
.
├── README.md
├── LICENSE
├── pom.xml
├── mvnw
├── docker-compose.dev.yml
├── docs
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com/ironcore
│   │   └── resources
│   └── test
│       ├── java
│       │   └── com/ironcore
│       └── resources
```

## Documentação

```plaintext
docs
├── README.md
├── architecture
│   └── README.md
├── body-metrics
│   └── README.md
├── database
│   └── README.md
├── exercises
│   └── README.md
├── workout-planning
│   └── README.md
├── diagram
│   ├── README.md
│   ├── er-sql
│   │   ├── README.md
│   │   └── IronCoreERDiagram.png
│   ├── general-architecture
│   ├── internal-architecture
│   ├── sequence-diagram
│   └── sequence-log-diagram
├── exceptions
│   └── README.md
├── filtering
│   └── README.md
├── logging
│   ├── README.md
│   ├── audit-log.md
│   └── error-log.md
├── persons
│   └── README.md
├── project-structure
│   └── README.md
├── releases
│   ├── README.md
│   ├── v0.1.0
│   ├── v0.2.0
│   ├── v0.3.0
│   ├── v0.4.0
│   ├── v0.5.0
│   ├── v0.6.0
│   └── v1.0.0
├── swagger
│   ├── README.md
│   ├── api-docs.md
│   └── swagger-ui.md
├── testing
│   └── README.md
└── users
    └── README.md
```

## Código Principal

```plaintext
src/main/java/com/ironcore
├── IroncoreBackendApplication.java
├── application
│   ├── auth
│   ├── bodymetrics
│   │   ├── component
│   │   ├── create
│   │   ├── delete
│   │   ├── get
│   │   ├── latest
│   │   ├── list
│   │   ├── port
│   │   ├── progress
│   │   └── update
│   ├── exception
│   ├── exercise
│   │   ├── catalog
│   │   ├── port
│   │   └── usecase
│   ├── logging
│   ├── person
│   │   └── usecase
│   │       ├── bootstrap
│   │       └── update
│   ├── shared
│   ├── workoutplanning
│   │   ├── traininggoal
│   │   ├── workoutactivity
│   │   ├── workoutcycle
│   │   └── workoutday
│   └── user
│       ├── service
│       └── usecase
│           ├── bootstrap
│           ├── changepassword
│           ├── getauthenticateduser
│           ├── initialchangepassword
│           └── update
├── domain
│   ├── activitytype
│   │   ├── exception
│   │   ├── model
│   │   ├── repository
│   │   └── valueobject
│   ├── bodymetrics
│   │   ├── exception
│   │   ├── model
│   │   ├── repository
│   │   ├── service
│   │   └── valueobject
│   ├── equipmenttype
│   │   ├── exception
│   │   ├── model
│   │   ├── repository
│   │   └── valueobject
│   ├── exercise
│   │   ├── exception
│   │   ├── model
│   │   ├── repository
│   │   └── valueobject
│   ├── exercisemuscletarget
│   │   ├── enums
│   │   ├── exception
│   │   ├── model
│   │   ├── repository
│   │   └── valueobject
│   ├── exception
│   ├── logging
│   ├── muscle
│   │   ├── musclegroup
│   │   └── musclesubgroup
│   ├── person
│   │   ├── enums
│   │   ├── exception
│   │   ├── model
│   │   ├── repository
│   │   └── valueobject
│   ├── workoutplanning
│   │   ├── traininggoal
│   │   ├── workoutactivity
│   │   ├── workoutcycle
│   │   └── workoutday
│   └── user
│       ├── exception
│       ├── model
│       ├── port
│       ├── repository
│       └── valueobject
├── infrastructure
│   ├── bootstrap
│   │   └── config
│   ├── config
│   ├── events
│   ├── exception
│   ├── persistence
│   │   ├── bodymetrics
│   │   ├── activitytype
│   │   ├── equipmenttype
│   │   ├── exercise
│   │   ├── exercisemuscletarget
│   │   ├── logging
│   │   ├── muscle
│   │   ├── person
│   │   ├── shared
│   │   ├── workoutplanning
│   │   └── user
│   └── security
└── interfaces
    └── rest
        ├── auth
        ├── bodymetrics
        ├── exercise
        ├── exception
        ├── person
        ├── workoutplanning
        └── user
```

## Resources

```plaintext
src/main/resources
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db
    └── migration
        ├── V1__create_table_persons.sql
        ├── V2__create_table_users.sql
        ├── V3__create_table_body_metrics.sql
        ├── V4__create_table_audit_log.sql
        ├── V5__create_table_error_log.sql
        ├── V6__create_tabela_activity_types.sql
        ├── V7__create_table_equipment_types.sql
        ├── V8__create_table_muscle_groups.sql
        ├── V9__create_table_muscle_subgroups.sql
        ├── V10__create_table_exercises.sql
        ├── V11__create_table_exercise_muscle_targets.sql
        ├── V12__seed_activity_types.sql
        ├── V13__seed_equipment_types.sql
        ├── V14__seed_muscle_groups.sql
        ├── V15__seed_muscle_subgroups.sql
        ├── V16__seed_exercises.sql
        ├── V17__seed_exercise_muscle_targets.sql
        ├── V18__create_table_training_goals.sql
        ├── V19__create_table_workout_cycles.sql
        ├── V20__create_table_workout_days.sql
        ├── V21__create_table_workout_activities.sql
        ├── V22__seed_training_goals.sql
        ├── V23__add_column_week_starts_on_to_workout_cycles.sql
        ├── V24__create_table_workout_sessions.sql
        ├── V25__create_table_workout_session_activities.sql
        ├── V26__create_table_workout_session_sets.sql
        ├── V27__allow_partial_workout_session_set_execution.sql
        ├── V28__create_table_auth_sessions.sql
        ├── V29__add_auth_session_refresh_token.sql
        ├── V30__create_auth_attempt_limits.sql
        └── V31__add_auth_session_absolute_expiration.sql
```

## Testes

```plaintext
src/test/java/com/ironcore
├── IroncoreBackendApplicationIntegrationTest.java
├── application
│   ├── auth
│   ├── bodymetrics
│   ├── exercise
│   ├── logging
│   ├── person
│   ├── shared
│   ├── workoutplanning
│   └── user
├── domain
│   ├── bodymetrics
│   ├── activitytype
│   ├── equipmenttype
│   ├── exercise
│   ├── exercisemuscletarget
│   ├── muscle
│   ├── person
│   ├── workoutplanning
│   └── user
├── infrastructure
│   ├── bootstrap
│   ├── persistence
│   │   ├── bodymetrics
│   │   ├── activitytype
│   │   ├── equipmenttype
│   │   ├── exercise
│   │   ├── exercisemuscletarget
│   │   ├── muscle
│   │   ├── person
│   │   ├── shared
│   │   ├── workoutplanning
│   │   └── user
│   └── security
└── interfaces
    └── rest
        ├── auth
        ├── bodymetrics
        ├── exercise
        ├── person
        ├── support
        ├── workoutplanning
        └── user
```

## Regra de Leitura

- Persons, users/auth/security e body metrics possuem fluxo funcional implementado.
- `Person` representa dados pessoais.
- `User` representa autenticação/acesso.
- `BodyMetrics` representa métricas corporais da pessoa e usa `PersonId` como ownership interna.
- `Exercise Catalog` representa catálogo global controlado pelo sistema, com consulta por endpoints REST e carga por migrations/seeds.
- `Workout Planning` representa objetivos, ciclos, dias e atividades prescritas da pessoa, com composição e ownership por `PersonId`.
- Diagramas podem conter blueprint planejado; confirmar o estado real por código, migrations e testes.

## Módulos Integrados Após v1.2.0

O esqueleto acima deve ser lido junto aos seguintes acréscimos:

```text
domain/auth                         # AuthSession, referência, id e repository
domain/workoutsession
  analytics                         # cálculos de força e cardio
  workoutsession                    # aggregate, snapshots e lifecycle
  workoutsessionactivity            # prescrição capturada e execução
  workoutsessionset                 # séries e normalização de carga
application/auth                    # emissão, refresh, logout, limpeza e limites
application/workoutsession          # início, consultas, edição, expiração e indicadores
infrastructure/persistence/auth     # sessões e contadores PostgreSQL
infrastructure/persistence/workoutsession
infrastructure/scheduling/auth      # limpeza de sessões e tentativas
infrastructure/scheduling/workoutsession
infrastructure/security/cookie      # factories dos dois cookies
infrastructure/security/token       # geração e hash do refresh token
interfaces/rest/workoutsession      # controllers, APIs, DTOs e mappers
docs/workout-session
docs/releases/next
```

O índice completo de migrations V1–V31 fica em [Banco de Dados](../database/README.md); contratos e testes seguem os mesmos módulos/camadas. Configuração de produção inclui confiança restrita no proxy loopback e bind local.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
