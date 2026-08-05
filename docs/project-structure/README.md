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
│   └── v0.6.0
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
│   │   └── user
│   └── security
└── interfaces
    └── rest
        ├── auth
        ├── bodymetrics
        ├── exercise
        ├── exception
        ├── person
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
        └── V17__seed_exercise_muscle_targets.sql
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
│   └── user
├── domain
│   ├── bodymetrics
│   ├── activitytype
│   ├── equipmenttype
│   ├── exercise
│   ├── exercisemuscletarget
│   ├── muscle
│   ├── person
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
│   │   └── user
│   └── security
└── interfaces
    └── rest
        ├── auth
        ├── bodymetrics
        ├── exercise
        ├── person
        ├── support
        └── user
```

## Regra de Leitura

- Persons, users/auth/security e body metrics possuem fluxo funcional implementado.
- `Person` representa dados pessoais.
- `User` representa autenticação/acesso.
- `BodyMetrics` representa métricas corporais da pessoa e usa `PersonId` como ownership interna.
- `Exercise Catalog` representa catálogo global controlado pelo sistema, com consulta por endpoints REST e carga por migrations/seeds.
- Diagramas podem conter blueprint planejado; confirmar o estado real por código, migrations e testes.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
