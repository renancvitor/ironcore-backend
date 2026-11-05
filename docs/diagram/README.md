# Diagramas

## Propósito

Este diretório contém a documentação visual do backend IronCore e dos fluxos de produto planejados.

Alguns diagramas descrevem a fundação técnica atual, enquanto outros descrevem escopo planejado. Trate como estado atual apenas código, migrations, configurações, testes e endpoints já implementados.

## Índice

| Diagrama                                                                                                       | Status | Observações |
|----------------------------------------------------------------------------------------------------------------|---|---|
| [ER SQL](./er-sql/README.md) | Implementado, com complemento | Imagem do recorte anterior e complemento Mermaid de execução/autenticação; migrations são a fonte de verdade para constraints físicas. |
| [General Architecture](./general-architecture/README.md)                             | Parcialmente implementado / blueprint | Use código, configurações e documentação específica para confirmar o estado funcional. |
| [Internal Architecture](./internal-architecture/README.md) | Parcialmente implementado | Use a árvore de packages e os testes como fonte de verdade para o que já existe. |
| [Workout Generation Sequence](./sequence-diagram/README.md)                                        | Blueprint planejado | O fluxo de geração de treino com IA/LLM ainda não está implementado. |
| [Workout Session Sequence](./sequence-log-diagram/README.md)                                 | Implementado | Fluxo Mermaid de snapshots, execução parcial e encerramento; imagem anterior identificada como blueprint. |

## Regra de Leitura

Se um diagrama mencionar módulos que não possuem código, migrations ou testes no repositório, trate esse conteúdo como arquitetura planejada, não como conteúdo da release.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
