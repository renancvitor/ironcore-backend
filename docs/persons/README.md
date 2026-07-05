# Documentação de Persons

## Propósito

O módulo `persons` representa dados pessoais no IronCore.

Essa separação evita que o usuário de autenticação concentre informações que pertencem a pessoa física. A decisão estrutural atual é:

```text
Person = dados pessoais
User = autenticação/acesso
BodyMetrics = métricas corporais da pessoa
```

## Escopo Atual

Implementado:

- `Person` domain model.
- Value objects `PersonId`, `Sex` e `BirthDate`.
- Persistência relacional em `persons`.
- Repository contract no domínio e adapter JPA em infrastructure.
- Bootstrap de pessoa inicial.
- Atualização dos dados pessoais da pessoa vinculada ao usuário autenticado.
- Testes de domínio, aplicação, persistência, bootstrap e REST.

## Conceitos de Domínio

Model principal:

- `Person`

Value objects:

- `PersonId`
- `Sex`
- `BirthDate`

Enum:

- `SexType`

Campos principais:

| Campo | Responsabilidade |
|---|---|
| `id` | Identidade técnica da pessoa. |
| `name` | Nome da pessoa. Obrigatório, mas não único. |
| `sex` | Sexo usado em regras corporais, como cálculo Navy de gordura corporal. |
| `birthDate` | Data de nascimento da pessoa. |
| `createdAt` | Data de criação do registro. |
| `updatedAt` | Data da última atualização. |

## Relação com User

`User` referencia `PersonId`.

No modelo atual, `User` não carrega o aggregate `Person` inteiro. Ele mantém apenas o identificador da pessoa vinculada para preservar baixo acoplamento entre aggregates.

```text
users.person_id -> persons.id
```

A relação atual de banco é 1:1, pois `users.person_id` é único.

## Relação com BodyMetrics

`BodyMetrics` referencia `PersonId`.

As medições corporais pertencem à pessoa, não à conta de acesso. A API pode continuar partindo do usuário autenticado, mas o use case resolve `PersonId` antes de operar dados corporais.

```text
body_metrics.person_id -> persons.id
```

## Persistência

Tabela:

- `persons`

Migration:

- `V1__create_table_persons.sql`

Componentes principais:

- `PersonEntity`
- `PersonMapper`
- `PersonJpaRepository`
- `PersonRepositoryAdapter`

O domínio depende do contrato `PersonRepository`. A implementação JPA permanece em `infrastructure`.

## Bootstrap

O bootstrap inicial do IronCore cria primeiro a pessoa e depois o usuário vinculado a ela.

Componentes principais:

- `PersonBootstrapProperties`
- `PersonBootstrapRunner`
- `BootstrapPersonCommand`
- `BootstrapPersonUseCase`
- `SingleUserBootstrapProperties`
- `SingleUserBootstrapRunner`
- `BootstrapSingleUserCommand`
- `BootstrapSingleUserUseCase`

Fluxo:

```text
Aplicação sobe
        ↓
Cria/garante Person inicial
        ↓
Cria/garante User inicial vinculado ao PersonId
```

O bootstrap deve permanecer idempotente: não deve duplicar pessoa nem usuário inicial.

## Endpoint REST

### Atualizar Pessoa do Usuário Autenticado

```http
PATCH /api/users/me/person
```

Autenticação:

- Requer cookie `access_token` válido.

Request:

```json
{
  "name": "Renan",
  "sex": "MALE",
  "birthDate": "1995-01-01"
}
```

Todos os campos são opcionais no contrato de request. Quando um campo é informado, o use case aplica a alteração correspondente.

Resposta:

- `200 OK`

```json
{
  "name": "Renan",
  "sex": "MALE",
  "birthDate": "1995-01-01"
}
```

## Regras de Aplicação

- O cliente não informa `personId` para escolher qual pessoa atualizar.
- O backend resolve a pessoa a partir do usuário autenticado.
- `BirthDate` válida estrutura própria no value object.
- Regras dependentes da data atual, como data futura e idade máxima, são validadas no fluxo de aplicação usando `Clock`.
- O limite de idade máxima preservado no fluxo atual é 120 anos.
- Atualização de `Person` altera `updatedAt`.

## Fora do Escopo Atual

- Cadastro público de pessoas.
- Associar multiplos usuários a uma mesma pessoa.
- Expor busca/listagem administrativa de pessoas.
- Permitir que o cliente manipule `personId` diretamente.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
