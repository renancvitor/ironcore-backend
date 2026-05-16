# Documentação de Arquitetura

## Status Atual

**Status:** fundação técnica atual da `v0.1.0`.

O backend está organizado com uma estrutura pragmática inspirada em DDD. A release atual estabelece limites de packages, modelos de domínio, adapters de persistência, logging, tratamento de exceptions, testes e CI.

Esta release não inclui controllers REST de negócio. O package `interfaces/rest` atualmente contém infraestrutura de tratamento de erros REST, não endpoints públicos de negócio.

## Camadas

### `domain`

Contém o modelo de negócio e os conceitos de domínio.

Exemplos atuais:

- `domain.user`
- `domain.userbodymetrics`
- `domain.logging.audit`
- `domain.logging.error`

A camada de domínio contém models, value objects, domain services, contratos de repository e exceptions de domínio. Ela deve permanecer independente do Spring e de detalhes de infraestrutura.

### `application`

Coordena use cases e fluxos de aplicação.

Exemplos atuais:

- `BootstrapSingleUserUseCase`
- `PasswordHashingService`
- audit and error logging application services
- application exceptions
- application ports/events

O código atual usa anotações Spring na camada de application para registro de services. Isso é aceito no estilo atual do projeto, mas as classes de domínio devem permanecer sem dependência de framework.

### `infrastructure`

Implementa preocupações técnicas necessárias para a aplicação.

Exemplos atuais:

- JPA entities and repositories
- domain-to-persistence mappers
- Spring event publishers/listeners
- password hashing implementation
- single-user bootstrap runner
- configuration properties

Os contratos de repository ficam na camada de domínio, enquanto as implementações JPA ficam em infrastructure. Por exemplo, `UserRepository` é implementado por `UserRepositoryAdapter`.

### `interfaces`

Contém pontos de entrada da aplicação e preocupações de interface externa.

Status atual:

- O tratamento global de exceptions REST existe.
- Models e factories de resposta de erro da API existem.
- Controllers REST de negócio ainda não estão implementados.

## Direção das Dependências

A direção de dependência pretendida é:

```text
interfaces -> application -> domain
infrastructure -> application/domain contracts
```

O domínio não deve depender de Spring, JPA, HTTP, entidades de banco ou integrações externas.

## Limitações Atuais

- Não há endpoints REST de negócio disponíveis na `v0.1.0`.
- O fluxo de autenticação JWT ainda não está implementado.
- Swagger/OpenAPI ainda não está implementado.
- MongoDB está disponível como dependência/serviço local, mas ainda não há módulo funcional de persistência documental.
- Fluxos maiores de treino, catálogo de exercícios e IA estão planejados, não implementados.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
