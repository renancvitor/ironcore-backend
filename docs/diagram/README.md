# Diagramas

## Propósito

Este diretório contém a documentação visual do backend IronCore e dos fluxos de produto planejados.

Alguns diagramas descrevem a fundação técnica atual, enquanto outros descrevem escopo planejado. Trate como estado atual apenas código, migrations, configurações, testes e endpoints já implementados.

## Índice

| Diagrama                                                                                                       | Status | Observações |
|----------------------------------------------------------------------------------------------------------------|---|---|
| [ER SQL](./er-sql/README.md)                                                                                   | Parcialmente implementado / blueprint | As migrations atuais cobrem `users`, `user_body_metrics`, `audit_logs` e `error_logs`; tabelas de workout/exercise estão planejadas. |
| [General Architecture](./general-architecture/README.md)                             | Parcialmente implementado / blueprint | Spring Boot, PostgreSQL e autenticação single-user estão atuais; Angular, IA, LLM e document store funcional estão planejados. |
| [Internal Architecture](./internal-architecture/README.md) | Parcialmente implementado | A estrutura em camadas existe; controllers REST de auth/users existem; módulos de treino, exercícios e IA ainda estão planejados. |
| [Workout Generation Sequence](./sequence-diagram/README.md)                                        | Blueprint planejado | O fluxo de geração de treino com IA/LLM ainda não está implementado. |
| [Workout Session Sequence](./sequence-log-diagram/README.md)                                 | Blueprint planejado | O fluxo de registro de sessões de treino ainda não está implementado. |

## Regra de Leitura

Se um diagrama mencionar módulos que não possuem código, migrations ou testes no repositório, trate esse conteúdo como arquitetura planejada, não como conteúdo da release.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
