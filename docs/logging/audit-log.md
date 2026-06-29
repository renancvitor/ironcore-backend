# Documentação de Audit Log

## Propósito

O `AuditLog` registra ações relevantes executadas no sistema para manter rastreabilidade funcional.

Ele é voltado para auditoria de operações de negócio, especialmente quando é necessário saber quem executou uma ação, qual recurso foi afetado e quais estados foram registrados antes e depois da alteração.

## Casos de Uso

Use audit log para ações como:

- criação de recursos (`CREATE`);
- atualização de recursos (`UPDATE`);
- remoção de recursos (`DELETE`);
- ativação de recursos (`ACTIVATE`);
- desativação de recursos (`DEACTIVATE`).

Essas ações estão representadas no enum `AuditActionType`.

## Estrutura Implementada

### Domain

Pacotes principais:

- `com.ironcore.domain.logging.audit.model`
- `com.ironcore.domain.logging.audit.valueobject`
- `com.ironcore.domain.logging.audit.enums`
- `com.ironcore.domain.logging.audit.repository`

Modelo de domínio:

- `AuditLog`

Value Objects:

- `AuditActor`
- `AuditAction`
- `AuditTarget`
- `AuditSnapshot`

Enums:

- `AuditActionType`
- `AuditTargetType`

Repository contract:

- `AuditLogRepository`

### Application

Pacotes principais:

- `com.ironcore.application.logging.audit.event`
- `com.ironcore.application.logging.audit.payload`
- `com.ironcore.application.logging.audit.port`
- `com.ironcore.application.logging.audit.service`

Componentes implementados:

- `AuditLogEvent`
- `LoggableData`
- `AuditLogPublisher`
- `AuditLogApplicationService`

### Infrastructure

Pacotes principais:

- `com.ironcore.infrastructure.events.logging.audit`
- `com.ironcore.infrastructure.persistence.logging.audit.entity`
- `com.ironcore.infrastructure.persistence.logging.audit.mapper`
- `com.ironcore.infrastructure.persistence.logging.audit.repository`

Componentes implementados:

- `AuditLogListener`
- `AuditLogPublisherService`
- `AuditLogEntity`
- `AuditLogMapper`
- `AuditLogJpaRepository`
- `AuditLogRepositoryImpl`

## Campos do Domínio

`AuditLog` possui:

| Campo | Tipo no domínio | Obrigatório | Descrição |
|---|---|---:|---|
| `id` | `Long` | Não | Identificador do log. |
| `actor` | `AuditActor` | Sim | Usuário que executou a ação. |
| `action` | `AuditAction` | Sim | Ação executada. |
| `target` | `AuditTarget` | Sim | Recurso afetado pela ação. |
| `beforeState` | `AuditSnapshot` | Não | Estado anterior, quando disponível. |
| `afterState` | `AuditSnapshot` | Não | Estado posterior, quando disponível. |
| `createdAt` | `LocalDateTime` | Sim | Data e hora de criação do registro. |

`AuditActor` contém `UserId` e `Email`.

`AuditAction` encapsula `AuditActionType`.

`AuditTarget` contém `AuditTargetType` e `id` positivo do recurso afetado.

`AuditSnapshot` contém uma `String` não vazia. A implementação atual não valida se o conteúdo é JSON.

## Campos Persistidos

Tabela Flyway: `audit_logs`

Migration: `V3__create_table_audit_log.sql`

| Coluna | Tipo SQL | Obrigatória | Observação |
|---|---|---:|---|
| `id` | `BIGSERIAL` | Sim | Primary key. |
| `actor_user_id` | `BIGINT` | Sim | FK para `users(id)`. |
| `actor_email` | `VARCHAR(255)` | Sim | Email do ator no momento do registro. |
| `action` | `VARCHAR(20)` | Sim | Valor de `AuditActionType`. |
| `target_type` | `VARCHAR(30)` | Sim | Valor de `AuditTargetType`. |
| `target_id` | `BIGINT` | Sim | Id do recurso afetado. |
| `before_state_json` | `TEXT` | Não | Estado anterior serializado, quando informado. |
| `after_state_json` | `TEXT` | Não | Estado posterior serializado, quando informado. |
| `created_at` | `TIMESTAMP` | Sim | Data e hora de criação do registro. |

Índices implementados:

- `idx_audit_logs_actor_user_id`
- `idx_audit_logs_target`
- `idx_audit_logs_created_at`

## Relação Entre Domain, Persistence e Migration

O domínio usa Value Objects para expressar conceitos ricos:

- `AuditActor` vira `actor_user_id` e `actor_email`.
- `AuditAction` vira `action`.
- `AuditTarget` vira `target_type` e `target_id`.
- `AuditSnapshot` vira `before_state_json` e `after_state_json`.

`AuditLogMapper` é responsável por converter entre `AuditLog` e `AuditLogEntity`.

`AuditLogRepositoryImpl` implementa o contrato `AuditLogRepository` usando `AuditLogJpaRepository`.

## Fluxo Implementado

O fluxo atual usa eventos Spring:

1. algum caso de uso chama a porta `AuditLogPublisher`;
2. `AuditLogPublisherService` publica um `AuditLogEvent`;
3. `AuditLogListener` escuta o evento com `TransactionPhase.AFTER_COMMIT`;
4. `AuditLogApplicationService` cria o `AuditLog` de domínio;
5. `AuditLogRepository` persiste o registro.

Como o listener usa `AFTER_COMMIT`, o audit log é registrado após o sucesso da transação principal.

## Estado Anterior e Posterior

A estrutura suporta `beforeState` e `afterState`.

Na persistência, esses valores são armazenados em:

- `before_state_json`
- `after_state_json`

Apesar do nome das colunas conter `json`, a implementação atual armazena `String` e não valida formalmente o formato JSON.

## O Que Deve Ser Registrado

Registre:

- identificador do usuário que executou a ação;
- email do ator;
- tipo da ação;
- tipo e id do recurso afetado;
- estados anterior e posterior quando forem úteis para auditoria;
- data e hora do evento.

## O Que Não Deve Ser Registrado

Não registre:

- senha, hash de senha, tokens ou credenciais;
- conteúdo sensível sem necessidade de auditoria;
- payload completo de requests;
- dados de autenticação;
- informações externas sem sanitização.

## Limitações Atuais

- Não há FK para `target_id`, pois o alvo é polimórfico e depende de `target_type`.
- `before_state_json` e `after_state_json` não têm validação de JSON no domínio.
- Não há consulta específica implementada para audit logs neste momento.
- Não há política de retenção, expurgo ou arquivamento implementada.
- A publicação do audit log precisa ser chamada explicitamente pelos casos de uso.

## Decisões Atuais e Fora do Escopo

Os itens abaixo não fazem parte da implementação atual e não representam compromisso de evolução. Eles podem ser avaliados apenas se surgir necessidade real no projeto.

- Consultas específicas por ator, recurso, ação e período não fazem parte do escopo atual.
- Validação formal de JSON em `before_state_json` e `after_state_json` não faz parte do escopo atual.
- Padronização adicional da serialização de estados auditáveis não faz parte do escopo atual.
- Política de retenção, expurgo ou arquivamento de audit logs não está implementada neste momento.
- Mascaramento automático de campos sensíveis não está implementado neste momento.
- Integração dos audit logs com ferramentas externas de observabilidade não faz parte do escopo atual.

## Veja Também

- [Estratégia de Logging](./README.md)
- [Error Log](./error-log.md)

<p align="right"><a href="./README.md">🔄 Voltar para Logging</a></p>
