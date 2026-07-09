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
│   └── v0.5.0
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
│   ├── bodymetrics
│   │   ├── exception
│   │   ├── model
│   │   ├── repository
│   │   ├── service
│   │   └── valueobject
│   ├── exception
│   ├── logging
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
│   │   ├── logging
│   │   ├── person
│   │   ├── shared
│   │   └── user
│   └── security
└── interfaces
    └── rest
        ├── auth
        ├── bodymetrics
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
        └── V5__create_table_error_log.sql
```

## Testes

```plaintext
src/test/java/com/ironcore
├── IroncoreBackendApplicationIntegrationTest.java
├── application
│   ├── auth
│   ├── bodymetrics
│   ├── logging
│   ├── person
│   ├── shared
│   └── user
├── domain
│   ├── bodymetrics
│   ├── person
│   └── user
├── infrastructure
│   ├── bootstrap
│   ├── persistence
│   │   ├── bodymetrics
│   │   ├── person
│   │   ├── shared
│   │   └── user
│   └── security
└── interfaces
    └── rest
        ├── auth
        ├── bodymetrics
        ├── person
        ├── support
        └── user
```

## Regra de Leitura

- Persons, users/auth/security e body metrics possuem fluxo funcional implementado.
- `Person` representa dados pessoais.
- `User` representa autenticação/acesso.
- `BodyMetrics` representa métricas corporais da pessoa e usa `PersonId` como ownership interna.
- Diagramas podem conter blueprint planejado; confirmar o estado real por código, migrations e testes.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
