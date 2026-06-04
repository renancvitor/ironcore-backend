# v0.1.0 - Fundação técnica do backend

## Resumo

`v0.1.0` representa o estado do IronCore Backend antes do fechamento do fluxo de users/auth/security.

Ela estabelece a fundação técnica inicial do backend, incluindo modelo de domínio, persistência relacional, migrations, logging, tratamento de exceptions, testes automatizados e CI.

Esta não é uma release de MVP nem uma release de API pública funcional.

## Principais Mudanças

- Base backend com Java 21 + Spring Boot.
- Build Maven com Maven Wrapper.
- Estrutura de packages pragmática inspirada em DDD.
- Modelo de domínio `user`, value objects, contrato de repository e adapter JPA.
- Modelo de domínio de `user body metrics`, value objects e services de cálculo.
- Persistência PostgreSQL com migrations Flyway.
- Tabelas para `users`, `user_body_metrics`, `audit_logs` e `error_logs`.
- Fluxos de persistência de audit log e error log.
- Fluxo opcional de bootstrap de usuário único.
- Infraestrutura de tratamento global de exceptions REST.
- Hashing de senha com Spring Security BCrypt.
- Testes automatizados para domain, application, infrastructure e startup de contexto.
- CI no GitHub Actions executando build e testes na `main`.

## Notas Técnicas

- PostgreSQL é o banco funcional atual.
- Flyway está habilitado na configuração base e as migrations ficam em `src/main/resources/db/migration`.
- MongoDB está disponível como dependência/serviço local via Docker, mas ainda não possui módulo funcional de domínio.
- Spring Security está presente, mas autenticação/autorização ainda não está completa.
- `security.token.secret` existe na configuração, mas o fluxo JWT ainda não está implementado.

## Limitações Conhecidas

- Não há endpoints REST de negócio disponíveis.
- Não há endpoint público de cadastro de usuário.
- Não há fluxo de login/emissão de JWT.
- Swagger/OpenAPI ainda não está implementado.
- User body metrics ainda não podem ser criadas ou consultadas por API.
- Módulos de workout, catálogo de exercícios e sessões de treino ainda não estão implementados.
- Geração de treino com IA ainda não está implementada.
- Persistência documental em MongoDB ainda não está implementada.
- O comportamento do Flyway em produção deve ser revisado antes de um deploy real.

## Não Incluído Nesta Release

- Fluxos REST de users/auth/security.
- Fluxos de MVP voltados ao usuário.
- Integração com frontend Angular.
- Documentação de API gerada por OpenAPI.
- Pipeline de deploy.
- Kafka/messaging.
- Integração com provedor LLM.
- Fluxos de geração de treino ou logging de treino.

<p align="right"><a href="../README.md">Voltar para releases</a></p>
