# Documentação de Users e Auth

## Propósito

O módulo `users` é a fundação atual para identidade de usuário dentro do backend.

Na `v0.2.0`, este módulo entrega o baseline de autenticação single-user: bootstrap opcional do usuário inicial, troca obrigatória de senha inicial, login com JWT, cookie `access_token`, logout, troca normal de senha e consulta do usuário autenticado.

## Escopo Atual

Implementado:

- `User` domain model.
- User value objects.
- `UserRepository` domain contract.
- JPA entity, mapper, Spring Data repository and adapter.
- Password hashing service and BCrypt-based hasher.
- Optional single-user bootstrap flow.
- JWT issuing and validation.
- REST login endpoint.
- REST logout endpoint.
- Mandatory initial password change endpoint.
- Authenticated password change endpoint.
- Authenticated user profile endpoint.
- JWT authentication filter based on the `access_token` cookie.

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

O projeto inclui um fluxo opcional de bootstrap para criar o usuário inicial.

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
- O usuário criado pelo bootstrap deve trocar a senha inicial antes de conseguir login completo.

## Variáveis de Ambiente

As variáveis de bootstrap são configuradas sob `ironcore.bootstrap.single-user`:

- `IRONCORE_BOOTSTRAP_USER_ENABLED`
- `IRONCORE_BOOTSTRAP_USER_NAME`
- `IRONCORE_BOOTSTRAP_USER_EMAIL`
- `IRONCORE_BOOTSTRAP_USER_PASSWORD`
- `IRONCORE_BOOTSTRAP_USER_SEX`

O segredo JWT é configurado em:

- `JWT_SECRET`

Não versionar valores reais.

## Endpoints

### Login

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "user@example.com",
  "password": "senha-atual"
}
```

Resposta:

- `200 OK`
- Body com `accessToken`, `tokenType`, `expiresAt`, `userId`, `email`, `name` e `mustChangePassword`.
- Header `Set-Cookie` com o cookie `access_token`.

O login é bloqueado enquanto `mustChangePassword` estiver ativo. Nesse caso, o usuário deve usar o endpoint de troca inicial de senha.

### Logout

```http
POST /api/auth/logout
```

Resposta:

- `204 No Content`
- Header `Set-Cookie` removendo o cookie `access_token` com `Max-Age=0`.

O logout atual remove o cookie no cliente. Não há blacklist server-side de JWT nesta release.

### Troca Inicial de Senha

```http
POST /api/users/change-initial-password
```

Body:

```json
{
  "email": "user@example.com",
  "currentPassword": "senha-inicial",
  "newPassword": "nova-senha",
  "confirmPassword": "nova-senha"
}
```

Resposta:

- `204 No Content`

Esse endpoint é público porque o usuário ainda não consegue autenticar enquanto a troca inicial for obrigatória.

### Troca Normal de Senha

```http
POST /api/users/me/change-password
```

Autenticação:

- Requer cookie `access_token` válido.

Body:

```json
{
  "currentPassword": "senha-atual",
  "newPassword": "nova-senha",
  "confirmNewPassword": "nova-senha"
}
```

Resposta:

- `204 No Content`

### Usuário Autenticado

```http
GET /api/users/me
```

Autenticação:

- Requer cookie `access_token` válido.

Resposta:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "User Name"
}
```

## Cookie de Autenticação

O cookie de autenticação emitido no login possui:

- Nome: `access_token`
- `HttpOnly`
- `Secure`
- `SameSite=None`
- `Path=/`
- `Max-Age` baseado na expiração do JWT

Como o cookie é marcado com `Secure`, clientes que aplicam regras de navegador podem exigir HTTPS para persistir/enviar o cookie automaticamente. Em testes locais via ferramenta HTTP, confirme se o cookie está sendo armazenado e reenviado.

Na `v0.2.0`, o token também é retornado no corpo do `LoginResponse`. O filtro de autenticação, porém, recupera o token pelo cookie `access_token`.

## Regras de Senha

As trocas de senha seguem as mesmas regras centrais:

- Usuário inativo não pode trocar senha.
- Senha atual precisa conferir.
- Nova senha e confirmação precisam ser iguais.
- Nova senha precisa ser diferente da senha atual.
- Após alteração, a senha é persistida como hash BCrypt.

## Security/JWT

Componentes principais:

- `SecurityConfig`
- `JwtAuthenticationFilter`
- `JwtAccessTokenGenerator`
- `JwtAccessTokenValidator`
- `JwtTokenProperties`
- `AuthenticatedUser`

Decisões atuais:

- API stateless com `SessionCreationPolicy.STATELESS`.
- CSRF desabilitado.
- JWT assinado com HMAC256.
- Issuer padrão: `IronCore`.
- Expiração padrão: `120` minutos.
- Claims atuais: `sub` com user id, `email` e `mustChangePassword`.
- CORS local habilitado para `http://localhost:*` e `http://127.0.0.1:*`, com credenciais.

## Erros Relevantes

| Status | Cenário |
|---|---|
| `400 Bad Request` | Campos obrigatórios ausentes, e-mail inválido ou body mal formatado. |
| `401 Unauthorized` | Credenciais incorretas, troca inicial obrigatória ou JWT inválido/expirado. |
| `403 Forbidden` | Usuário inativo. |
| `404 Not Found` | Usuário autenticado não encontrado. |
| `422 Unprocessable Entity` | Confirmação de senha divergente, nova senha igual à atual ou operação não permitida. |
| `500 Internal Server Error` | Falha interna de JWT/configuração ou erro inesperado. |

## Fora do Escopo Deliberado

> Os itens abaixo não fazem parte do modelo single-user do IronCore e não são pendências planejadas para releases futuras.

- Cadastro público de usuário.
- Edição de dados cadastrais pelo usuário.
- Recuperação de senha.
- Refresh token.
- Blacklist/revogação server-side de JWT.
- Roles/perfis de autorização.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
