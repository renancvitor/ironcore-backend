# Logging Strategy

## Visão Geral

Este documento descreve a estratégia de logging implementada no backend do **IronCore**.

O projeto possui dois fluxos de logs persistidos em banco relacional:

- `audit_logs`: registros de auditoria para rastrear ações relevantes executadas no sistema.
- `error_logs`: registros técnicos de erro para suporte, debugging e rastreabilidade de falhas.

Esses logs são parte da infraestrutura de rastreabilidade da aplicação, mas não substituem observabilidade externa, métricas, tracing distribuído ou alertas operacionais.

## Tipos de Log

### Audit logs

Use `audit_logs` para registrar ações de negócio relevantes, como criação, atualização, remoção, ativação ou desativação de recursos.

O audit log responde principalmente:

- quem executou a ação;
- qual ação foi executada;
- qual recurso foi afetado;
- quando a ação aconteceu;
- qual era o estado anterior e posterior, quando disponível.

Leia mais em [Audit Log](./audit-log.md).

### Error logs

Use `error_logs` para registrar falhas técnicas ou erros tratados pela aplicação.

O error log responde principalmente:

- qual tipo de erro ocorreu;
- qual mensagem técnica foi registrada;
- qual exceção foi associada ao erro;
- em qual caminho e método HTTP a falha ocorreu;
- qual usuário e correlation id estavam associados ao contexto, quando disponíveis.

Leia mais em [Error Log](./error-log.md).

## Quando Usar

Use `audit_logs` quando o objetivo for rastrear uma ação relevante do usuário ou do sistema sobre um recurso de domínio.

Use `error_logs` quando o objetivo for investigar falhas, apoiar suporte técnico ou depurar comportamentos inesperados.

## O Que Não Deve Ser Logado

Não registre:

- senhas, tokens, refresh tokens ou credenciais;
- dados sensíveis sem necessidade operacional clara;
- payloads completos de autenticação;
- informações pessoais além do necessário para rastreabilidade;
- conteúdo que possa expor segredo de infraestrutura ou integração externa.

## Dados Sensíveis

Os logs persistidos em banco devem ser tratados como dados sensíveis. Antes de registrar estados, mensagens ou contexto de requisição, valide se a informação é necessária para auditoria ou suporte.

Quando houver dúvida, prefira registrar identificadores técnicos e metadados mínimos, evitando persistir conteúdo bruto de requests, responses ou exceções externas.

## Documentação Específica

- [Audit Log](./audit-log.md)
- [Error Log](./error-log.md)

<p align="right"><a href="../../README.md">🔄 Voltar para a documentação completa</a></p>
