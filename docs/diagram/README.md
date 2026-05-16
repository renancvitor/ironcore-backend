# Diagramas

## Propósito

Este diretório contém a documentação visual do backend IronCore e dos fluxos de produto planejados.

Alguns diagramas descrevem a fundação técnica atual, enquanto outros descrevem escopo planejado. Para a `v0.1.0`, somente código, migrations e configurações implementados devem ser tratados como estado atual.

## Índice

| Diagrama | Status na v0.1.0 | Observações |
|---|---|---|
| [ER SQL](./er-sql/IronCoreER%20Diagram.md) | Parcialmente implementado / blueprint | As migrations atuais cobrem `users`, `user_body_metrics`, `audit_logs` e `error_logs`; tabelas de workout/exercise estão planejadas. |
| [General Architecture](./general-architecture/General%20Architecture%20Diagram.md) | Parcialmente implementado / blueprint | Spring Boot e PostgreSQL estão atuais; Angular, IA, LLM e document store funcional estão planejados. |
| [Internal Architecture](./internal-architecture/Internal%20Architecture%20Diagram%20of%20the%20Application.md) | Parcialmente implementado | A estrutura em camadas existe; controllers REST de negócio e contratos DTO ainda não estão implementados. |
| [Workout Generation Sequence](./sequence-diagram/Sequence%20Diagram.md) | Blueprint planejado | O fluxo de geração de treino com IA/LLM ainda não está implementado. |
| [Workout Session Sequence](./sequence-log-diagram/Sequence%20Log%20Diagram.md) | Blueprint planejado | O fluxo de registro de sessões de treino ainda não está implementado. |

## Regra de Leitura

Se um diagrama mencionar módulos que não possuem código, migrations ou testes no repositório, trate esse conteúdo como arquitetura planejada, não como conteúdo da release.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
