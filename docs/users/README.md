# Documentação de Users e Auth

## Propósito

O módulo `users` representa a conta de acesso dentro do backend.

Este módulo entrega autenticação single-user: bootstrap opcional do usuário inicial, troca obrigatória de senha inicial, login com JWT e sessões persistidas, cookies de acesso/refresh, rotação de credenciais, logout no servidor, troca normal de senha, atualização de nickname e consulta do usuário autenticado.

Dados pessoais pertencem a `Person`, não a `User`.

```text
Person = dados pessoais
User = autenticação/acesso
BodyMetrics = métricas corporais da pessoa
```

## Escopo Atual

Implementado:

- `User` domain model.
- User value objects.
- `UserRepository` domain contract.
- JPA entity, mapper, Spring Data repository and adapter.
- Password hashing service and BCrypt-based hasher.
- Optional bootstrap flow para `Person` e `User`.
- JWT issuing and validation.
- REST login endpoint.
- REST logout endpoint.
- Refresh com rotação, expiração por inatividade e limite absoluto de sessão.
- Revogação no logout/troca de senha, limites de tentativas e limpeza de registros vencidos.
- Mandatory initial password change endpoint.
- Authenticated password change endpoint.
- Authenticated nickname update endpoint.
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
- `PersonId`

O model de domínio suporta registro/restauração, alteração de nickname, alteração de hash de senha, ativação e desativação.

Ativação e desativação existem como suporte técnico de domínio, mas não há endpoint público para esse fluxo no modelo atual.

## Relação com Person

`User` referencia `PersonId`.

`User` não deve representar nome civil, sexo ou data de nascimento. Esses dados pertencem a `Person`.

```text
users.person_id -> persons.id
```

A relação atual é 1:1 no banco, pois `users.person_id` possui restrição única.

## Persistência

Tabela:

- `users`

Migration:

- `V2__create_table_users.sql`

Campos principais:

| Campo | Responsabilidade |
|---|---|
| `id` | Identidade técnica do usuário. |
| `nickname` | Nome de exibição/conta do usuário. |
| `person_id` | Pessoa vinculada ao usuário. |
| `email` | Identificador de autenticação. Único. |
| `password_hash` | Hash BCrypt da senha. |
| `must_change_password` | Indica troca obrigatória de senha inicial. |
| `active` | Status técnico de acesso. |
| `created_at` | Data de criação do registro. |
| `updated_at` | Data da última atualização. |

Componentes de persistência:

- `UserEntity`
- `UserMapper`
- `UserJpaRepository`
- `UserRepositoryAdapter`

O domínio depende do contrato `UserRepository`. A implementação JPA permanece em infrastructure.

## Bootstrap Inicial

O projeto inclui um fluxo opcional de bootstrap para criar a pessoa e o usuário inicial.

Componentes principais:

- `PersonBootstrapProperties`
- `PersonBootstrapRunner`
- `BootstrapPersonCommand`
- `BootstrapPersonUseCase`
- `SingleUserBootstrapProperties`
- `SingleUserBootstrapRunner`
- `BootstrapSingleUserCommand`
- `BootstrapSingleUserUseCase`

Comportamento esperado:

- Se o bootstrap de pessoa estiver habilitado, o fluxo cria/garante a `Person` inicial.
- Se o bootstrap de usuário estiver habilitado, o fluxo cria/garante o `User` inicial vinculado ao `PersonId`.
- Se o email configurado já existir, o use case retorna sem criar outro usuário.
- Se qualquer outro usuário já existir, o use case rejeita a criação com `OperationNotAllowedException`.
- O usuário criado pelo bootstrap recebe senha inicial criptografada.
- O usuário criado pelo bootstrap deve trocar a senha inicial antes de conseguir login completo.

## Variáveis de Ambiente

As variáveis de bootstrap da pessoa são configuradas sob `ironcore.bootstrap.person`:

- `IRONCORE_BOOTSTRAP_PERSON_ENABLED`
- `IRONCORE_BOOTSTRAP_PERSON_NAME`
- `IRONCORE_BOOTSTRAP_PERSON_SEX`
- `IRONCORE_BOOTSTRAP_PERSON_BIRTH_DATE`

As variáveis de bootstrap do usuário são configuradas sob `ironcore.bootstrap.single-user`:

- `IRONCORE_BOOTSTRAP_USER_ENABLED`
- `IRONCORE_BOOTSTRAP_USER_NICKNAME`
- `IRONCORE_BOOTSTRAP_USER_EMAIL`
- `IRONCORE_BOOTSTRAP_USER_PASSWORD`

Em produção, o bootstrap inicial já foi executado e as flags de habilitação de pessoa e usuário foram posteriormente desativadas. A configuração sensível permanece fora do Git; consulte [Deploy e Produção](../deploy/README.md).

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
- Body com `expiresAt`, `userId`, `email`, `nickname` e `mustChangePassword`; sem tokens no JSON.
- Headers `Set-Cookie` com `access_token` e `refresh_token`.

O login é bloqueado enquanto `mustChangePassword` estiver ativo. Nesse caso, o usuário deve usar o endpoint de troca inicial de senha.

### Logout

```http
POST /api/auth/logout
```

Resposta:

- `204 No Content`
- Headers `Set-Cookie` removendo `access_token` e `refresh_token` com `Max-Age=0`.

O logout revoga no servidor a sessão identificada pelo usuário autenticado ou pelo refresh token, limpa ambos os cookies e invalida o CSRF anterior. Pode ser chamado sem access token válido, usando o refresh token; não exige autenticação na configuração HTTP, mas exige CSRF. Não há blacklist individual de JWT: a validação consulta a sessão persistida.

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
  "confirmNewPassword": "nova-senha"
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

### Alterar Nickname

```http
PUT /api/users/me/change-nickname
```

Autenticação:

- Requer cookie `access_token` válido.

Body:

```json
{
  "nickname": "renan"
}
```

Resposta:

- `200 OK`

```json
{
  "nickname": "renan"
}
```

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
  "nickname": "renan"
}
```

## Cookie de Autenticação

O cookie de autenticação emitido no login possui:

- Nome: `access_token`
- `HttpOnly`
- `Secure`
- `SameSite` configurável: `None` como padrão da configuração base e `Lax` no perfil de produção
- `Path=/`
- `Max-Age` baseado na expiração do JWT

Como o cookie é marcado com `Secure`, clientes que aplicam regras de navegador podem exigir HTTPS para persistir/enviar o cookie automaticamente. Em testes locais via ferramenta HTTP, confirme se o cookie está sendo armazenado e reenviado.

Tokens não são retornados no JSON de login ou refresh. O filtro autentica pelo cookie `access_token`; `Authorization: Bearer` não é o mecanismo implementado.

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

- API sem sessão HTTP com `SessionCreationPolicy.STATELESS`, mas com estado de autenticação persistido em PostgreSQL (`auth_sessions`).
- CSRF habilitado com `CookieCsrfTokenRepository`. O cliente obtém token e nome do header por `GET /api/auth/csrf` e envia o header nas operações de escrita, incluindo autenticação. Login e logout invalidam o token CSRF anterior.
- JWT assinado com HMAC256.
- Issuer padrão: `IronCore`.
- Expiração padrão: `120` minutos.
- Claims atuais: `sub` com user id, `email`, `mustChangePassword`, `sid` UUID da sessão e `exp`. JWT sem `sid` é rejeitado.
- `JWT_SECRET` exige pelo menos 32 bytes UTF-8; issuer não vazio e duração positiva são validados na inicialização.
- CORS local habilitado para `http://localhost:*` e `http://127.0.0.1:*`, com credenciais. Em produção, `FRONTEND_ORIGIN` define a origem, com padrão `https://ironcore.dev.br`.

## Erros Relevantes

| Status | Cenário                                                                              |
|---|--------------------------------------------------------------------------------------|
| `400 Bad Request` | Campos obrigatórios ausentes, e-mail inválido ou body mal formatado.                 |
| `401 Unauthorized` | Credenciais incorretas, troca inicial obrigatória ou JWT inválido/expirado.          |
| `403 Forbidden` | Usuário inativo.                                                                     |
| `404 Not Found` | Usuário autenticado não encontrado.                                                  |
| `422 Unprocessable Entity` | Confirmação de senha divergente, nova senha igual à atual ou operação não permitida. |
| `429 Too Many Requests` | Limite de tentativas; header `Retry-After` em segundos. |
| `500 Internal Server Error` | Falha interna de JWT/configuração ou erro inesperado.                                |

## Fora do Escopo Deliberado

> Os itens abaixo não fazem parte do modelo single-user do IronCore e não são pendências planejadas para este módulo.

- Cadastro público de usuário.
- Recuperação de senha.
- Roles/perfis de autorização.
- Endpoint público para ativar/desativar usuário.

## Sessões Persistidas, Refresh e Concorrência

A Issue #325/PR #326 introduziu `AuthSession`, refresh com rotação e revogação server-side. O filtro valida assinatura, issuer, expiração e claims, consulta a sessão por `sid` e usuário e verifica usuário ativo. Login e refresh também rejeitam usuário com troca inicial pendente. Sessão ausente, revogada ou vencida invalida o JWT mesmo antes de seu `exp`.

```http
POST /api/auth/refresh
```

Sem body; usa cookie `refresh_token` e header CSRF. Não exige access token válido. Retorna `200` com `{ "expiresAt": "2026-11-04T12:00:00" }` (valor ilustrativo, `LocalDateTime` do access token) e renova ambos os cookies. Credencial ausente, inválida, reutilizada após rotação, usuário inativo ou sessão expirada retorna `401`.

O refresh token é opaco, gerado com aleatoriedade segura e persistido somente como hash SHA-256. A rotação substitui o hash na mesma sessão: a credencial anterior deixa de ser aceita. Não existe período de tolerância para refresh concorrente. Dois refreshes com a mesma credencial aceitam-na apenas uma vez; clientes devem coordenar a renovação.

Login, refresh, logout e troca de senha coordenam concorrência por bloqueio do usuário e releitura do estado persistido; a sessão também tem versão. Falha na emissão de credenciais faz rollback da operação transacional. Troca de senha revoga todas as sessões anteriores; a troca normal emite nova sessão e ambos os cookies para a requisição atual, mantendo `204`. A troca inicial mantém `204` sem autenticar automaticamente. Alteração de nickname não deve restaurar senha antiga em concorrência.

O cookie `refresh_token` tem `HttpOnly`, `Secure`, `Path=/api/auth`, `SameSite` configurável (base `None`, produção `Lax`) e `Max-Age` até `sessionExpiresAt`. O access cookie mantém `Path=/` e idade até a expiração do JWT. Cookies não usam `Domain` explícito.

Após login/logout, obtenha novamente `GET /api/auth/csrf` antes de outra escrita. O CSRF é exigido também no refresh e nas trocas de senha, mesmo em endpoints públicos. CORS permite credenciais e métodos GET, PATCH, POST, PUT, DELETE e OPTIONS, expondo `Retry-After`.

## Expiração, Transição e Limpeza

A Issue #329/PR #330 define a política atual:

| Conceito | Regra |
|---|---|
| Access token | Padrão de 120 minutos, limitado também pelo `expiresAt` da sessão na emissão |
| Nova sessão | `expiresAt = createdAt + 30 dias`, `absoluteExpiresAt = createdAt + 90 dias` |
| Refresh válido | `expiresAt = min(agora + 30 dias, absoluteExpiresAt)` |
| Requisição autenticada comum | Não renova a janela de inatividade |
| Limite absoluto | Não se desloca no refresh; no limite é necessário novo login |
| Limpeza física | Job diário às 03h em `America/Sao_Paulo`, exclui `expires_at < agora - 15 dias` |

Expiração e revogação bloqueiam acesso imediatamente na validação, sem depender da limpeza (Issue #327/PR #328). Revogação não muda o critério da limpeza: uma sessão revogada não é removida apenas por ter sido revogada; aguarda a expiração mais a retenção. O job não remove usuários nem logs. Falhas são registradas e o agendamento permanece disponível para nova tentativa.

Transição: JWT antigo sem `sid` exige novo login. A V29 permite hash nulo em sessões anteriores, que não possuem credencial de refresh. A V31 preenche `absolute_expires_at` com o `expires_at` já existente, preservando o teto anterior. Não concede automaticamente 30/90 dias aos registros migrados nem recupera sessões já vencidas. Sessões novas seguem a política completa. Um access token já emitido mantém seu próprio `exp`; refresh não prolonga aquele JWT, emite outro.

## Limites de Tentativas e Proteções de Credenciais

Os limites são persistidos em `auth_attempt_limits`, identificados por hash do sujeito, e compartilhados entre instâncias:

- IP: padrão 120 requisições por minuto, nos POST de login, refresh, troca inicial e troca normal de senha.
- Credenciais por e-mail (login/troca inicial) e troca normal por usuário: padrão 20 tentativas por janela de 300 segundos. Reservas são liberadas quando o resultado não é credencial inválida; não constituem limite geral da API.
- Configuração: `security.auth-rate-limit.account-limit`, `account-window-seconds` e `ip-limit-per-minute`. Rejeição retorna `429` com `Retry-After` e incrementa `authentication.rate.limit.rejections`.
- Limpeza dos contadores a cada cinco minutos, mantendo a maior janela configurada (conta ou um minuto).
- IP vem de `request.getRemoteAddr()`. Em produção, o servidor escuta `127.0.0.1` e confia em headers encaminhados somente pelo proxy loopback configurado, evitando aceitar indiscriminadamente `X-Forwarded-For` do cliente.
- Senhas exigem ao menos oito caracteres; a geração de hash BCrypt rejeita mais de 72 bytes UTF-8. DTOs/commands sensíveis redigem credenciais em `toString`; respostas não expõem JWT ou refresh token.

Testes de referência: `AuthenticationHardeningIntegrationTest`, `AuthSessionConcurrencyIntegrationTest`, `AuthSessionExpirationTest`, `AuthSessionCleanupPostgresIntegrationTest` e `AuthSessionMigrationPostgresIntegrationTest`. Ver [Testes](../testing/README.md).

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
