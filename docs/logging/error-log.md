# Error Log

## Propósito

O `ErrorLog` registra falhas técnicas e informações mínimas de contexto para suporte, debugging e rastreabilidade.

Ele não substitui tratamento de exceções, métricas, tracing distribuído, alertas ou ferramentas externas de observabilidade. O objetivo é manter um registro persistido das falhas relevantes capturadas pela aplicação.

## Casos de Uso Esperados

Use error log para registrar:

- erros de validação;
- falhas de autenticação ou autorização;
- recursos não encontrados;
- violações de regra de negócio;
- falhas de banco de dados;
- falhas em serviços externos;
- erros internos ou desconhecidos.

Esses tipos estão representados no enum `ErrorCodeType`.

## Estrutura Implementada

### Domain

Pacotes principais:

- `com.ironcore.domain.logging.error.model`
- `com.ironcore.domain.logging.error.valueobject`
- `com.ironcore.domain.logging.error.enums`
- `com.ironcore.domain.logging.error.repository`

Modelo de domínio:

- `ErrorLog`

Value Objects:

- `ErrorCode`
- `ErrorRequestContext`

Enum:

- `ErrorCodeType`

Repository contract:

- `ErrorLogRepository`

### Application

Pacotes principais:

- `com.ironcore.application.logging.error.event`
- `com.ironcore.application.logging.error.port`
- `com.ironcore.application.logging.error.service`

Componentes implementados:

- `ErrorLogEvent`
- `ErrorLogPublisher`
- `ErrorLogApplicationService`

### Infrastructure

Pacotes principais:

- `com.ironcore.infrastructure.events.logging.error`
- `com.ironcore.infrastructure.persistence.logging.error.entity`
- `com.ironcore.infrastructure.persistence.logging.error.mapper`
- `com.ironcore.infrastructure.persistence.logging.error.repository`

Componentes implementados:

- `ErrorLogListener`
- `ErrorLogPublisherService`
- `ErrorLogEntity`
- `ErrorLogMapper`
- `ErrorLogJpaRepository`
- `ErrorLogRepositoryImpl`

## Campos do Domínio

`ErrorLog` possui:

| Campo | Tipo no domínio | Obrigatório | Descrição |
|---|---|---:|---|
| `id` | `Long` | Não | Identificador do log. |
| `errorCode` | `ErrorCode` | Sim | Código categórico do erro. |
| `message` | `String` | Sim | Mensagem do erro. |
| `exceptionClass` | `String` | Sim | Classe da exceção associada. |
| `requestContext` | `ErrorRequestContext` | Sim | Contexto HTTP da requisição. |
| `userId` | `Long` | Não | Usuário associado ao erro, quando disponível. |
| `correlationId` | `String` | Sim | Identificador de correlação para rastreio. |
| `createdAt` | `LocalDateTime` | Sim | Data e hora de criação do registro. |

`ErrorCode` encapsula `ErrorCodeType`.

`ErrorRequestContext` contém:

- `path`;
- `httpMethod`.

`userId` é opcional, mas quando informado precisa ser positivo no domínio.

## Campos Persistidos

Tabela Flyway: `error_logs`

Migration: `V4__create_table_error_log.sql`

| Coluna | Tipo SQL | Obrigatória | Observação |
|---|---|---:|---|
| `id` | `BIGSERIAL` | Sim | Primary key. |
| `error_code` | `VARCHAR(30)` | Sim | Valor de `ErrorCodeType`. |
| `message` | `TEXT` | Sim | Mensagem do erro. |
| `exception_class` | `VARCHAR(255)` | Sim | Classe da exceção. |
| `request_path` | `VARCHAR(255)` | Sim | Caminho da requisição. |
| `http_method` | `VARCHAR(10)` | Sim | Método HTTP. |
| `user_id` | `BIGINT` | Não | FK opcional para `users(id)`. |
| `correlation_id` | `VARCHAR(255)` | Sim | Identificador de correlação. |
| `created_at` | `TIMESTAMP` | Sim | Data e hora de criação do registro. |

Índices implementados:

- `idx_error_logs_user_id`
- `idx_error_logs_error_code`
- `idx_error_logs_created_at`
- `idx_error_logs_correlation_id`

## Relação Entre Domain, Persistence e Migration

O domínio usa Value Objects para expressar conceitos relevantes:

- `ErrorCode` vira `error_code`.
- `ErrorRequestContext` vira `request_path` e `http_method`.

`ErrorLogMapper` é responsável por converter entre `ErrorLog` e `ErrorLogEntity`.

`ErrorLogRepositoryImpl` implementa o contrato `ErrorLogRepository` usando `ErrorLogJpaRepository`.

## Fluxo Implementado

O fluxo atual usa eventos Spring:

1. algum componente chama a porta `ErrorLogPublisher`;
2. `ErrorLogPublisherService` publica um `ErrorLogEvent`;
3. `ErrorLogListener` escuta o evento com `TransactionPhase.AFTER_COMPLETION` e `fallbackExecution = true`;
4. `ErrorLogApplicationService` cria o `ErrorLog` de domínio;
5. `ErrorLogRepository` persiste o registro.

Diferente de audit logs, error logs são registrados após a conclusão da transação, inclusive em cenários de rollback, e também podem ser processados quando não houver transação ativa.

## Mensagem, Código e Contexto

`errorCode` categoriza a falha usando `ErrorCodeType`.

`message` armazena a mensagem do erro. Ela deve ser útil para suporte e debugging, mas não deve conter segredos, tokens ou dados sensíveis.

`exceptionClass` registra a classe da exceção associada ao erro.

`requestContext` registra `path` e `httpMethod`, permitindo localizar o ponto HTTP associado à falha.

`correlationId` permite correlacionar o registro com outras trilhas técnicas da aplicação.

## O Que Deve Ser Registrado

Registre:

- código categórico do erro;
- mensagem técnica útil;
- classe da exceção;
- path e método HTTP;
- id do usuário quando disponível;
- correlation id;
- data e hora do evento.

## O Que Não Deve Ser Registrado

Não registre:

- senha, tokens, credenciais ou headers sensíveis;
- stack traces completos sem sanitização;
- request body completo;
- respostas completas de serviços externos;
- dados pessoais sem necessidade de suporte ou debugging.

## Limitações Atuais

- Não há controller, handler global ou filter implementado chamando `ErrorLogPublisher` neste momento.
- Não há consulta específica implementada para error logs.
- Não há política de retenção, expurgo ou arquivamento implementada.
- `exceptionClass` é uma `String`; não há normalização adicional além das validações do domínio.
- `message` não possui mascaramento automático de dados sensíveis.

## Decisões Atuais e Fora do Escopo

Os itens abaixo não fazem parte da implementação atual e não representam compromisso de evolução. Eles podem ser avaliados apenas se surgir necessidade real no projeto.

- Integração com um `ControllerAdvice` global não faz parte do escopo atual.
- Captura automática de correlation id a partir de headers ou contexto MDC não faz parte do escopo atual.
- Mascaramento automático de mensagens não está implementado neste momento.
- Persistência de stack trace resumido ou hash técnico não faz parte do escopo atual.
- Consultas específicas por código de erro, usuário, correlation id e período não fazem parte do escopo atual.
- Integração dos error logs com ferramentas externas de observabilidade não faz parte do escopo atual.

## Veja Também

- [Logging Strategy](./README.md)
- [Audit Log](./audit-log.md)

<p align="right"><a href="../../README.md">🔄 Voltar para a documentação completa</a></p>
