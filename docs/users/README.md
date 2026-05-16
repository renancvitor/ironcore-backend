# Documentação de Users

## Propósito

O módulo `users` é a fundação atual para identidade de usuário dentro do backend.

Na `v0.1.0`, este módulo possui model de domínio, value objects, persistência JPA e um fluxo opcional de bootstrap de usuário único. Ele não expõe endpoints públicos de cadastro, login ou gerenciamento de usuários.

## Escopo Atual

Implementado:

- `User` domain model.
- User value objects.
- `UserRepository` domain contract.
- JPA entity, mapper, Spring Data repository and adapter.
- Password hashing service and BCrypt-based hasher.
- Optional single-user bootstrap flow.

Não implementado:

- Public REST endpoints.
- Public registration flow.
- Login flow.
- JWT issuing/validation.
- Frontend screens.

## Conceitos de Domínio

Model principal:

- `User`

Value objects:

- `UserId`
- `Email`
- `PasswordHash`
- `RawPassword`
- `Sex`

Enum:

- `SexType`

O model de domínio suporta registro/restauração, alteração de nome, alteração de hash de senha, alteração de sexo, ativação e desativação.

## Persistência

Tabela:

- `users`

Migration:

- `V1__create_table_users.sql`

Componentes de persistência:

- `UserEntity`
- `UserMapper`
- `UserJpaRepository`
- `UserRepositoryAdapter`

O domínio depende do contrato `UserRepository`. A implementação JPA permanece em infrastructure.

## Bootstrap de Usuário Único

O projeto inclui um fluxo opcional de bootstrap para criar um usuário inicial.

Componentes principais:

- `SingleUserBootstrapProperties`
- `SingleUserBootstrapRunner`
- `BootstrapSingleUserCommand`
- `BootstrapSingleUserUseCase`

Comportamento:

- Se o bootstrap estiver desabilitado, o runner não executa nenhuma ação.
- Se o email configurado já existir, o use case retorna sem criar outro usuário.
- Se qualquer outro usuário já existir, o use case rejeita a criação com `OperationNotAllowedException`.
- Se nenhum usuário existir, ele gera o hash da senha configurada e persiste o usuário inicial.

## Variáveis de Ambiente

As variáveis de bootstrap são configuradas sob `ironcore.bootstrap.single-user`:

- `IRONCORE_BOOTSTRAP_USER_ENABLED`
- `IRONCORE_BOOTSTRAP_USER_NAME`
- `IRONCORE_BOOTSTRAP_USER_EMAIL`
- `IRONCORE_BOOTSTRAP_USER_PASSWORD`
- `IRONCORE_BOOTSTRAP_USER_SEX`

Não versionar valores reais.

## Limitações Atuais

- Nenhum endpoint REST de negócio expõe operações de usuário.
- Não há cadastro público de usuário.
- Não há fluxo completo de autenticação JWT.
- `security.token.secret` está configurado, mas emissão/validação de tokens ainda não está implementada.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
