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
├── database
│   └── README.md
├── diagram
│   ├── README.md
│   ├── er-sql
│   │   ├── README.md
│   │   └── IronCoreERDiagram.png
│   ├── general-architecture
│   │   ├── README.md
│   │   └── General Architecture Diagram.png
│   ├── internal-architecture
│   │   ├── README.md
│   │   └── Internal Architecture Diagram of the Application.png
│   ├── sequence-diagram
│   │   ├── README.md
│   │   └── Sequence Diagram.png
│   └── sequence-log-diagram
│       ├── README.md
│       └── Sequence Log Diagram.png
├── exceptions
│   └── README.md
├── logging
│   ├── README.md
│   ├── audit-log.md
│   └── error-log.md
├── project-structure
│   └── README.md
├── releases
│   ├── README.md
│   ├── v0.1.0
│   │   └── README.md
│   └── v0.2.0
│       └── README.md
├── testing
│   └── README.md
├── user-body-metrics
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
│   │   ├── port
│   │   └── usecase
│   ├── exception
│   ├── logging
│   │   ├── audit
│   │   │   ├── event
│   │   │   ├── payload
│   │   │   ├── port
│   │   │   └── service
│   │   └── error
│   │       ├── event
│   │       ├── port
│   │       └── service
│   └── user
│       ├── service
│       └── usecase
│           ├── bootstrap
│           ├── changepassword
│           ├── getauthenticateduser
│           └── initialchangepassword
├── domain
│   ├── exception
│   ├── logging
│   │   ├── audit
│   │   │   ├── enums
│   │   │   ├── exception
│   │   │   ├── model
│   │   │   ├── repository
│   │   │   └── valueobject
│   │   └── error
│   │       ├── enums
│   │       ├── exception
│   │       ├── model
│   │       ├── repository
│   │       └── valueobject
│   ├── user
│   │   ├── enums
│   │   ├── exception
│   │   ├── model
│   │   ├── port
│   │   ├── repository
│   │   └── valueobject
│   └── userbodymetrics
│       ├── exception
│       ├── model
│       ├── service
│       └── valueobject
├── infrastructure
│   ├── bootstrap
│   │   └── config
│   ├── config
│   ├── events
│   │   └── logging
│   │       ├── audit
│   │       └── error
│   ├── exception
│   ├── persistence
│   │   ├── logging
│   │   │   ├── audit
│   │   │   └── error
│   │   ├── user
│   │   │   ├── entity
│   │   │   ├── mapper
│   │   │   └── repository
│   │   └── userbodymetrics
│   │       └── entity
│   └── security
│       ├── auth
│       ├── config
│       ├── filter
│       ├── jwt
│       │   └── exception
│       └── password
└── interfaces
    └── rest
        ├── auth
        │   ├── dto
        │   └── mapper
        ├── exception
        │   ├── factory
        │   ├── handler
        │   └── model
        └── user
            ├── dto
            └── mapper
```

## Resources

```plaintext
src/main/resources
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db
    └── migration
        ├── V1__create_table_users.sql
        ├── V2__create_table_user_body_metrics.sql
        ├── V3__create_table_audit_log.sql
        └── V4__create_table_error_log.sql
```

## Testes

```plaintext
src/test/java/com/ironcore
├── IroncoreBackendApplicationIntegrationTest.java
├── application
│   ├── auth
│   │   └── usecase
│   ├── logging
│   │   ├── audit
│   │   │   └── service
│   │   └── error
│   │       └── service
│   └── user
│       ├── service
│       └── usecase
├── domain
│   ├── user
│   │   ├── model
│   │   └── valueobject
│   └── userbodymetrics
│       ├── service
│       └── valueobject
├── infrastructure
│   ├── bootstrap
│   │   └── config
│   ├── persistence
│   │   └── user
│   │       ├── mapper
│   │       └── repository
│   └── security
│       ├── filter
│       ├── jwt
│       └── password
└── interfaces
    └── rest
        ├── auth
        └── user
```

## Regra de Leitura

- O estado funcional de users/auth/security pertence à `v0.2.0`.
- User body metrics possui estruturas de domínio, cálculos e tabela, mas ainda não possui endpoint público concluído.
- Diagramas podem conter blueprint planejado; confirmar o estado real por código, migrations e testes.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
