# Documentação de Arquitetura

## Visão Geral

Este documento descreve a organização arquitetural do backend IronCore.

O backend segue uma arquitetura em camadas com inspiração pragmática em DDD. A intenção é manter regras de domínio isoladas, casos de uso explícitos, entrada REST fina e detalhes técnicos concentrados em infraestrutura.

## Camadas

### `domain`

Contém o modelo de negócio e os conceitos de domínio.

Exemplos atuais:

- `domain.person`
- `domain.user`
- `domain.bodymetrics`
- `domain.activitytype`
- `domain.equipmenttype`
- `domain.exercise`
- `domain.exercisemuscletarget`
- `domain.auth`
- `domain.workoutsession`
- `domain.workoutplanning`
- `domain.muscle`
- `domain.logging.audit`
- `domain.logging.error`

A camada de domínio contém models, value objects, domain services, contratos de repository e exceptions de domínio. Ela deve permanecer independente do Spring e de detalhes de infraestrutura.

### `application`

Coordena use cases e fluxos de aplicação.

Exemplos atuais:

- `BootstrapPersonUseCase`
- `BootstrapSingleUserUseCase`
- `LoginUseCase`
- `InitialChangePasswordUseCase`
- `ChangePasswordUseCase`
- `ChangeNicknameUseCase`
- `GetAuthenticatedUserUseCase`
- `UpdatePersonUseCase`
- body metrics use cases para create, update, delete, get, latest, list e progress
- exercise catalog use cases para catálogos auxiliares, listagem e detalhe
- workout planning use cases para objetivos, ciclos, dias, atividades, lifecycle, reorder, detalhe e listagem
- `PasswordHashingService`
- audit e error logging application services
- application exceptions
- application ports/events

O código atual usa anotações Spring na camada de application para registro de services. Isso é aceito no estilo atual do projeto, mas as classes de domínio devem permanecer sem dependência de framework.

### `infrastructure`

Implementa preocupações técnicas necessárias para a aplicação.

Exemplos atuais:

- JPA entities e repositories
- domain-to-persistence mappers
- Spring event publishers/listeners
- password hashing implementation
- JWT generation/validation
- JWT authentication filter
- person e single-user bootstrap runners
- configuration properties
- persistência JPA, adapters e `WorkoutCycleSpecification` de workout planning

Os contratos de repository ficam na camada de domínio, enquanto as implementações JPA ficam em infrastructure.

### `interfaces`

Contém pontos de entrada da aplicação e preocupações de interface externa.

Status atual:

- O tratamento global de exceptions REST existe.
- Models e factories de resposta de erro da API existem.
- Controllers REST de autenticação, usuário autenticado, pessoa e body metrics existem.
- Controllers REST de exercise catalog existem para consultas de catálogo e exercises.
- Controllers REST de workout planning existem para objetivos, ciclos, dias e atividades.
- Interfaces documentais OpenAPI existem para concentrar documentação dos contratos REST sem poluir os controllers.
- Os controllers recebem HTTP, constroem commands e delegam regras aos use cases.

## Direção das Dependências

A direção de dependência pretendida é:

```text
interfaces -> application -> domain
infrastructure -> application/domain contracts
```

O domínio não deve depender de Spring, JPA, HTTP, entidades de banco ou integrações externas.

## Separação Person/User/BodyMetrics

A modelagem atual separa três responsabilidades:

```text
Person = dados pessoais
User = autenticação/acesso
BodyMetrics = métricas corporais da pessoa
```

Decisões arquiteturais:

- `User` referencia `PersonId`, mas não carrega o aggregate `Person` inteiro.
- `BodyMetrics` referencia `PersonId`, mas não carrega `User` nem `Person` como aggregate inteiro.
- A API pode continuar usando rotas baseadas em `/api/users/me/...`, pois a entrada operacional é o usuário autenticado.
- A ownership interna dos dados físicos é baseada em `PersonId`.
- Em auditoria, `actorUserId` representa quem executou a ação; `personId` representa a pessoa dona dos dados afetados.

## Recortes Atuais

- O baseline single-user cobre bootstrap, autenticação e usuário autenticado.
- O módulo `Person` cobre dados pessoais e atualização da pessoa vinculada ao usuário autenticado.
- Body metrics possui fluxo funcional autenticado com CRUD, consulta, listagem, progresso, auditoria e testes.
- Exercise catalog possui fluxo funcional de consulta com catálogos auxiliares, listagem paginada, filtros combinados, detalhe por id, seeds, OpenAPI e testes.
- Workout planning possui fluxo funcional com ownership por `PersonId`, composição, lifecycle, edição, reorder, exclusão, detalhe, filtros, paginação, auditoria, OpenAPI e testes.
- Autenticação usa `AuthSession` persistida, rotação de refresh token, revogação no servidor e limites de tentativas. `STATELESS` significa ausência de sessão HTTP; não elimina o estado de autenticação no PostgreSQL.
- Cadastro público, recuperação de senha e roles ficam fora do modelo single-user do IronCore.
- Ativação/desativação de usuário permanece como suporte técnico interno e não possui endpoint funcional no modelo atual.
- Swagger/OpenAPI está implementado para os contratos REST atuais via Springdoc.
- MongoDB está disponível como dependência/serviço local, mas ainda não há módulo funcional de persistência documental.
- Workout Session está implementado com ownership por `PersonId`, aggregate sessão/atividades/séries, snapshots imutáveis do planejamento e consultas de histórico/indicadores por ports e projeções JPQL.
- Schedulers cuidam da expiração de treinos, limpeza de sessões de autenticação e limites de tentativas. Geração com IA permanece planejada.

Detalhes funcionais do módulo: [Workout Planning](../workout-planning/README.md).

## Planejamento, Execução e Autenticação

Os aggregates de planejamento e execução têm lifecycle independente. O início copia a prescrição para snapshots; edições de execução não alteram o planejamento. `AuthSession` pertence a `UserId` e controla credenciais de acesso; `WorkoutSession` pertence a `PersonId` e registra treino. São conceitos distintos.

Detalhes: [Workout Session](../workout-session/README.md) e [Users e Auth](../users/README.md).

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
