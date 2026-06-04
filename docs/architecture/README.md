# Documentação de Arquitetura

## Status Atual

**Status:** fundação técnica do repositório, com baseline de autenticação single-user fechado na `v0.2.0`.

O backend está organizado com uma estrutura pragmática inspirada em DDD. A base atual estabelece limites de packages, modelos de domínio, adapters de persistência, logging, tratamento de exceptions, autenticação single-user, testes e CI.

O repositório já inclui controllers REST mínimos para autenticação e usuário autenticado. O módulo de métricas corporais possui base de domínio e banco, mas ainda não possui fluxo REST funcional. Exercícios, treinos e IA permanecem planejados.

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
- `LoginUseCase`
- `InitialChangePasswordUseCase`
- `ChangePasswordUseCase`
- `GetAuthenticatedUserUseCase`
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
- JWT generation/validation
- JWT authentication filter
- single-user bootstrap runner
- configuration properties

Os contratos de repository ficam na camada de domínio, enquanto as implementações JPA ficam em infrastructure. Por exemplo, `UserRepository` é implementado por `UserRepositoryAdapter`.

### `interfaces`

Contém pontos de entrada da aplicação e preocupações de interface externa.

Status atual:

- O tratamento global de exceptions REST existe.
- Models e factories de resposta de erro da API existem.
- Controllers REST de autenticação e usuário autenticado existem.

## Direção das Dependências

A direção de dependência pretendida é:

```text
interfaces -> application -> domain
infrastructure -> application/domain contracts
```

O domínio não deve depender de Spring, JPA, HTTP, entidades de banco ou integrações externas.

## Recortes Atuais

- A `v0.2.0` cobre o baseline single-user de autenticação e usuário autenticado.
- Cadastro público, recuperação de senha, refresh token, blacklist JWT e roles ficam fora do modelo single-user do IronCore.
- User body metrics possui base parcial implementada, mas ainda não possui fluxo funcional completo.
- Swagger/OpenAPI ainda não está implementado.
- MongoDB está disponível como dependência/serviço local, mas ainda não há módulo funcional de persistência documental.
- Fluxos maiores de treino, catálogo de exercícios e IA estão planejados, não implementados.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
