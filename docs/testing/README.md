# Testes Automatizados

## Propósito

Este documento descreve a estratégia atual de testes automatizados do IronCore Backend.

O projeto usa testes para proteger regras de domínio, fluxos de aplicação, adapters de infraestrutura, segurança, controllers REST e integração com contexto Spring.

## Organizacao por Camada

### `domain`

Cobre regras de domínio, value objects e services de cálculo.

Exemplos atuais:

- `PersonTest`
- `PersonIdTest`
- `BirthDateTest`
- `SexTest`
- `UserTest`
- `EmailTest`
- `RawPasswordTest`
- `PasswordHashTest`
- `UserIdTest`
- `BodyMetricsIdTest`
- `BMICalculatorTest`
- `NavyBodyFatCalculatorTest`
- `FatMassCalculatorTest`
- `LeanMassCalculatorTest`

Esses testes validam invariantes, entradas inválidas, criação de objetos de valor e cálculos de composição corporal.

### `application`

Cobre use cases e services de aplicação.

Exemplos atuais:

- `LoginUseCaseTest`
- `BootstrapPersonUseCaseTest`
- `BootstrapSingleUserUseCaseTest`
- `InitialChangePasswordUseCaseTest`
- `ChangePasswordUseCaseTest`
- `ChangeNicknameUseCaseTest`
- `GetAuthenticatedUserUseCaseTest`
- `UpdatePersonUseCaseTest`
- `UserPasswordChangeServiceTest`
- `PasswordHashingServiceTest`
- `AuditLogApplicationServiceTest`
- `ErrorLogApplicationServiceTest`
- `CreateBodyMetricsUseCaseTest`
- `UpdateBodyMetricsUseCaseTest`
- `DeleteBodyMetricsUseCaseTest`
- `GetBodyMetricsUseCaseTest`
- `GetLatestBodyMetricsUseCaseTest`
- `ListBodyMetricsUseCaseTest`
- `GetBodyMetricsProgressChartUseCaseTest`
- `GetBodyMetricsProgressChangesUseCaseTest`

Esses testes validam regras de orquestração, autenticação, troca de senha, bootstrap, atualização de pessoa, atualização de nickname, publicação de logs, métricas corporais e progresso.

### `infrastructure`

Cobre adapters, segurança técnica, bootstrap e persistência.

Exemplos atuais:

- `PersonBootstrapRunnerTest`
- `SingleUserBootstrapRunnerTest`
- `PersonBootstrapPropertiesTest`
- `SingleUserBootstrapPropertiesTest`
- `PersonMapperTest`
- `PersonRepositoryAdapterTest`
- `PersonRepositoryAdapterIntegrationTest`
- `UserMapperTest`
- `UserRepositoryAdapterTest`
- `UserRepositoryAdapterIntegrationTest`
- `BodyMetricsRepositoryAdapterTest`
- `ListBodyMetricsQueryAdapterTest`
- `BodyMetricsProgressQueryAdapterTest`
- `BodyMetricsMapperTest`
- `PageMapperTest`
- `JwtAccessTokenGenerationTest`
- `JwtAccessTokenValidatorTest`
- `JwtAuthenticationFilterTest`
- `SpringSecurityPasswordHasherTest`

Esses testes validam mapping, comportamento de adapter, geração e validação de JWT, filtro de autenticação e configuração de bootstrap.

### `interfaces`

Cobre controllers REST e fluxos HTTP relevantes.

Exemplos atuais:

- `AuthControllerTest`
- `AuthSecurityIntegrationTest`
- `UserControllerTest`
- `PersonControllerTest`
- `CreateBodyMetricsControllerTest`
- `UpdateBodyMetricsControllerTest`
- `DeleteBodyMetricsControllerTest`
- `GetBodyMetricsControllerTest`
- `GetLatestBodyMetricsControllerTest`
- `ListBodyMetricsControllerTest`
- `GetBodyMetricsProgressControllerTest`
- `BodyMetricsIntegrationTest`

Esses testes validam contratos HTTP, autenticação, logout, rotas protegidas, validação de request e respostas dos endpoints atuais.

## Testes de Integração

O projeto possui testes de integração para validar comportamento alem de unidades isoladas.

Exemplos atuais:

- `IroncoreBackendApplicationIntegrationTest`: válida inicializacao do contexto Spring.
- `UserRepositoryAdapterIntegrationTest`: válida persistência real com PostgreSQL via Testcontainers.
- `PersonRepositoryAdapterIntegrationTest`: válida persistência de `Person` com schema real.
- `AuthSecurityIntegrationTest`: válida autenticação e proteção de rotas no contexto REST/security.
- `BodyMetricsIntegrationTest`: válida fluxo REST de métricas corporais no contexto da aplicação.

Os testes com Testcontainers aplicam as migrations Flyway em um banco PostgreSQL temporário, o que aumenta a confiança na compatibilidade entre entidade, repository e schema.

## Recursos de Teste

Arquivo de configuração atual:

- `src/test/resources/application-test.yml`

Esse arquivo concentra propriedades de teste e evita depender diretamente do perfil de desenvolvimento.

## Comandos

Executar a suite padrão:

```bash
./mvnw test --batch-mode
```

Executar a verificacao completa usada pelo CI:

```bash
./mvnw clean verify --batch-mode
```

## CI

O GitHub Actions executa:

```bash
./mvnw clean verify --batch-mode
```

em eventos de `push` e `pull_request` para a branch `main`.

## Regra de Manutenção

- Regras de domínio devem ser testadas preferencialmente em `domain`, sem Spring.
- Use cases devem ser testados em `application`, com dependências mockadas quando possível.
- Adapters de persistência devem ter testes unitários e, quando houver risco de schema/query, testes de integração.
- Controllers devem validar contrato HTTP, status code, payload e cenários de erro relevantes.
- Fluxos de segurança devem cobrir autenticação válida, autenticação inválida e acesso a rotas protegidas.
- Novos módulos funcionais devem incluir testes proporcionais ao risco antes de serem tratados como entrega fechada.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
