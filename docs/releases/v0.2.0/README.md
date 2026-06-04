# v0.2.0 - Users, auth e security single-user

## Resumo

`v0.2.0` fecha a base de usuários, autenticação e segurança single-user do IronCore Backend.

Esta release estabelece o modelo mínimo de identidade do projeto: usuário único criado por bootstrap, senha armazenada com hash, troca obrigatória de senha inicial, login com JWT, autenticação por cookie `access_token`, logout, troca normal de senha e consulta do usuário autenticado.

O objetivo não é entregar cadastro público ou um sistema multiusuário. O IronCore usa um modelo local/single-user, no qual o usuário inicial é provisionado por configuração e os demais módulos funcionais operam a partir desse usuário autenticado.

## Escopo Entregue

- Domínio de `User`.
- Value objects de usuário.
- Contrato de repository no domínio.
- Persistência JPA para `users`.
- Bootstrap opcional de usuário único.
- Hashing de senha com BCrypt.
- Login REST em `/api/auth/login`.
- Emissão de JWT de acesso.
- Cookie de autenticação `access_token`.
- Logout REST em `/api/auth/logout`.
- Filtro de autenticação JWT para endpoints protegidos.
- Troca obrigatória de senha inicial em `/api/users/change-initial-password`.
- Troca normal de senha em `/api/users/me/change-password`.
- Consulta do usuário autenticado em `/api/users/me`.
- Tratamento global de erros aplicado aos fluxos de users/auth/security.
- Testes unitários e de integração cobrindo os principais fluxos de autenticação e usuário.

## Modelo de Usuário

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

O modelo de domínio suporta registro/restauração, alteração de nome, alteração de hash de senha, alteração de sexo, ativação e desativação.

## Bootstrap do Usuário Único

O bootstrap cria o usuário inicial da aplicação quando habilitado por configuração.

Componentes principais:

- `SingleUserBootstrapProperties`
- `SingleUserBootstrapRunner`
- `BootstrapSingleUserCommand`
- `BootstrapSingleUserUseCase`

Variáveis de ambiente esperadas:

- `IRONCORE_BOOTSTRAP_USER_ENABLED`
- `IRONCORE_BOOTSTRAP_USER_NAME`
- `IRONCORE_BOOTSTRAP_USER_EMAIL`
- `IRONCORE_BOOTSTRAP_USER_PASSWORD`
- `IRONCORE_BOOTSTRAP_USER_SEX`

Comportamento:

- Se o bootstrap estiver desabilitado, nenhum usuário é criado.
- Se o email configurado já existir, o fluxo é idempotente e não cria outro usuário.
- Se já existir outro usuário na base, a criação é bloqueada com `OperationNotAllowedException`.
- Se nenhum usuário existir, o usuário inicial é criado com senha hasheada.
- O usuário criado pelo bootstrap deve trocar a senha inicial antes de conseguir login completo.

Não versionar valores reais de ambiente, senhas ou segredos.

## Login e Cookie `access_token`

Endpoint:

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

Resposta de sucesso:

- HTTP `200 OK`.
- Body com `accessToken`, `tokenType`, `expiresAt`, `userId`, `email`, `name` e `mustChangePassword`.
- Header `Set-Cookie` com `access_token`.

Cookie emitido:

- Nome: `access_token`
- `HttpOnly`
- `Secure`
- `SameSite=None`
- `Path=/`
- `Max-Age` calculado a partir da expiração do token.

Observação operacional: como o cookie é marcado com `Secure`, clientes que aplicam regras de navegador podem exigir HTTPS para persistir/enviar o cookie automaticamente. Em testes locais via ferramenta HTTP, validar se o cookie está sendo armazenado e reenviado.

Decisão documentada: nesta release, o token é retornado no corpo da resposta e também no cookie `access_token`. O cookie é o mecanismo usado pelo filtro de autenticação para recuperar o JWT em requisições protegidas.

## Logout

Endpoint:

```http
POST /api/auth/logout
```

Resposta de sucesso:

- HTTP `204 No Content`.
- Header `Set-Cookie` removendo o cookie `access_token`.

Como o backend usa JWT stateless nesta release, o logout remove o cookie no cliente. Não há blacklist de token no servidor por decisão de escopo.

## Troca Obrigatória de Senha Inicial

Endpoint:

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

Resposta de sucesso:

- HTTP `204 No Content`.

Comportamento:

- O endpoint é público porque o usuário ainda não possui token válido enquanto a troca inicial for obrigatória.
- O login é bloqueado com `InitialPasswordChangeRequiredException` enquanto `mustChangePassword` estiver ativo.
- A senha atual precisa conferir.
- A nova senha e a confirmação precisam ser iguais.
- A nova senha precisa ser diferente da senha atual.
- Após a troca, o usuário fica apto a fazer login.

## Troca Normal de Senha

Endpoint:

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

Resposta de sucesso:

- HTTP `204 No Content`.

## Usuário Autenticado

Endpoint:

```http
GET /api/users/me
```

Autenticação:

- Requer cookie `access_token` válido.

Resposta de sucesso:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "User Name"
}
```

## Decisões de Security/JWT

- A autenticação é stateless: `SessionCreationPolicy.STATELESS`.
- CSRF está desabilitado na configuração atual.
- CORS permite origens locais via `http://localhost:*` e `http://127.0.0.1:*`, com credenciais habilitadas.
- JWT é assinado com HMAC256 usando `security.token.secret`.
- Issuer padrão: `IronCore`.
- Expiração padrão: `120` minutos.
- Claims atuais: subject com `userId`, `email` e `mustChangePassword`.
- O filtro JWT lê o token do cookie `access_token`.
- Endpoints públicos: healthcheck, Swagger/OpenAPI quando presente, login, logout e troca inicial de senha.
- Endpoints demais exigem autenticação.

## Principais Respostas de Erro

| Status | Quando ocorre |
|---|---|
| `400 Bad Request` | Body inválido, campos obrigatórios ausentes ou formato de e-mail inválido. |
| `401 Unauthorized` | Credenciais incorretas, troca inicial obrigatória ou JWT inválido/expirado. |
| `403 Forbidden` | Usuário inativo. |
| `404 Not Found` | Usuário autenticado não encontrado em fluxo protegido. |
| `422 Unprocessable Entity` | Confirmação de senha divergente, nova senha igual à atual ou operação não permitida. |
| `500 Internal Server Error` | Falha interna de configuração/geração de JWT ou erro inesperado. |

Mensagens de credenciais inválidas são normalizadas para evitar expor se o e-mail existe.

## Testes Relevantes

Cobertura de application:

- `LoginUseCaseTest`
- `BootstrapSingleUserUseCaseTest`
- `InitialChangePasswordUseCaseTest`
- `ChangePasswordUseCaseTest`
- `GetAuthenticatedUserUseCaseTest`
- `UserPasswordChangeServiceTest`
- `PasswordHashingServiceTest`

Cobertura de infrastructure/security:

- `SingleUserBootstrapRunnerTest`
- `SingleUserBootstrapPropertiesTest`
- `JwtAccessTokenGenerationTest`
- `JwtAccessTokenValidatorTest`
- `JwtAuthenticationFilterTest`
- `SpringSecurityPasswordHasherTest`

Cobertura REST/integração:

- `AuthControllerTest`
- `UserControllerTest`
- `AuthSecurityIntegrationTest`

## Fora do Escopo Deliberado

Os itens abaixo não fazem parte do modelo single-user do IronCore e não são pendências planejadas para releases futuras:

- Cadastro público de usuário.
- Edição de dados cadastrais pelo usuário.
- Recuperação de senha.
- Refresh token.
- Blacklist/revogação server-side de JWT.
- Roles/perfis de autorização.

<p align="right"><a href="../README.md">Voltar para releases</a></p>
