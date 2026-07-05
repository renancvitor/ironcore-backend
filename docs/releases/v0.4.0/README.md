# v0.4.0 - Person e ownership por PersonId

## Resumo

`v0.4.0` consolida o refactor estrutural do fluxo `Person/User/BodyMetrics`.

Esta release está associada ao milestone GitHub `v7.0.0`, cujo foco foi separar dados pessoais, autenticação/acesso e métricas corporais. O modelo anterior tratava `UserBodyMetrics` como dados diretamente pertencentes ao usuário de autenticação. O modelo atual introduz `Person`, refatora `User` para referenciar `PersonId` e transforma o módulo em `BodyMetrics`, com ownership interna baseada em `PersonId`.

```text
Person = dados pessoais
User = autenticação/acesso
BodyMetrics = métricas corporais da pessoa
```

## Escopo Entregue

- Domínio de `Person`.
- Value objects `PersonId`, `Sex` e `BirthDate`.
- Persistência relacional em `persons`.
- Refactor de `User` para referenciar `PersonId`.
- Persistência de `users.person_id` com relação 1:1 atual entre user e person.
- Bootstrap inicial em duas etapas: `Person` e depois `User` vinculado.
- Rename estrutural de `UserBodyMetrics` para `BodyMetrics`.
- Refactor de `BodyMetrics` para referenciar `PersonId`.
- Persistência relacional em `body_metrics`.
- Queries, consultas e operações de body metrics usando ownership por `PersonId`.
- Contracts/results/responses de body metrics expondo `personId` onde representa ownership.
- Auditoria de body metrics preservando `actorUserId` como ator autenticado e `personId` como pessoa dona dos dados.
- Atualização de dados pessoais em `PATCH /api/users/me/person`.
- Atualização de nickname em `PUT /api/users/me/change-nickname`.
- Refactor da validação temporal de `BirthDate` para usar `Clock` nos fluxos de aplicação.
- Ajustes finais de persistência, mappers, controllers e testes.
- Novo diagrama ER atualizado para o modelo `Person/User/BodyMetrics`.

## Decisões Funcionais e Arquiteturais

- `Person` representa dados pessoais.
- `User` representa autenticação, acesso, email, senha, nickname e flags técnicas.
- `BodyMetrics` representa medições corporais da pessoa.
- `User` referencia `PersonId`, mas não carrega o aggregate `Person` inteiro.
- `BodyMetrics` referencia `PersonId`, mas não carrega `User` nem `Person` como aggregate inteiro.
- A API pode continuar usando rotas baseadas em `/api/users/me/...`.
- O cliente não envia `personId` para criar ou manipular dados corporais.
- O backend resolve `PersonId` a partir do usuário autenticado.
- `actorUserId` continua representando quem executou uma operação auditada.
- `personId` passa a representar a pessoa dona dos dados corporais.
- `persons.name` é obrigatório, mas não único.
- Ativação/desativação de usuário não foi exposta como fluxo público nesta etapa; permanece como suporte técnico interno.

## Refactor de Body Metrics

O módulo anterior `UserBodyMetrics` foi renomeado para `BodyMetrics`.

O objetivo do rename foi corrigir a linguagem do domínio:

- antes: medições corporais pareciam pertencer diretamente ao usuário de autenticação;
- agora: medições corporais pertencem a pessoa (`PersonId`).

O caminho REST permanece:

```http
/api/users/me/body-metrics
```

Esse path continua adequado porque a entrada da operação e o usuário autenticado. A regra interna, porém, passa por:

```text
AuthenticatedUser -> UserId -> User -> PersonId -> BodyMetrics
```

## Endpoints Relevantes

### Person

```http
PATCH /api/users/me/person
```

Atualiza dados pessoais da pessoa vinculada ao usuário autenticado.

### User

```http
PUT /api/users/me/change-nickname
```

Atualiza o nickname da conta de acesso.

### Body Metrics

Base path:

```http
/api/users/me/body-metrics
```

Endpoints principais preservados:

- `POST /api/users/me/body-metrics`
- `PUT /api/users/me/body-metrics/{id}`
- `DELETE /api/users/me/body-metrics/{id}`
- `GET /api/users/me/body-metrics`
- `GET /api/users/me/body-metrics/{id}`
- `GET /api/users/me/body-metrics/latest`
- `GET /api/users/me/body-metrics/progress/body-composition`
- `GET /api/users/me/body-metrics/progress/circumferences`
- `GET /api/users/me/body-metrics/progress/body-fat`
- `GET /api/users/me/body-metrics/progress/changes`

## Banco de Dados

Migrations atuais relacionadas a está release:

- `V1__create_table_persons.sql`
- `V2__create_table_users.sql`
- `V3__create_table_body_metrics.sql`

Modelo central:

```text
persons
  ↑
  ├── users.person_id
  └── body_metrics.person_id
```

Observação técnica do milestone: como as migrations ainda não haviam sido aplicadas em ambiente persistido, os ajustes estruturais foram feitos diretamente nas migrations iniciais correspondentes, sem migration incremental.

## Documentação

Documentação técnica principal:

- [Persons](../../persons/README.md)
- [Users e Auth](../../users/README.md)
- [Body Metrics](../../body-metrics/README.md)
- [Banco de Dados e Migrations](../../database/README.md)
- [Diagrama ER](../../diagram/er-sql/README.md)

Documentações relacionadas:

- [Arquitetura](../../architecture/README.md)
- [Estrutura do Projeto](../../project-structure/README.md)
- [Testes Automatizados](../../testing/README.md)
- [Releases](../README.md)

## Validação

O milestone `v7.0.0` registrou validacoes locais durante o refactor, incluindo:

```bash
./mvnw test
./mvnw clean verify -q
```

Esta documentação também foi atualizada para refletir a issue #126, que fecha o alinhamento documental do modelo `Person/User/BodyMetrics`.

<p align="right"><a href="../README.md">Voltar para releases</a></p>
