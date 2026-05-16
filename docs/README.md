# Documentação Técnica

Este diretório centraliza a documentação técnica do IronCore Backend.

A release `v0.1.0` representa uma fundação técnica inicial do backend, não um MVP nem uma API pública funcional. Alguns documentos descrevem funcionalidades já implementadas, enquanto outros registram decisões arquiteturais, módulos em preparação ou blueprints planejados.

## Índice

### Fundação e arquitetura

- [Arquitetura](architecture/README.md): visão das camadas, responsabilidades e abordagem DDD pragmática.
- [Banco de dados e migrations](database/README.md): PostgreSQL, Flyway e migrations reais da fundação atual.
- [Exceptions](exceptions/README.md): estratégia de exceptions e tratamento global de erros REST.

### Módulos documentados

- [Users](users/README.md): domínio de usuário, value objects, bootstrap e persistência atual.
- [User Body Metrics](user-body-metrics/README.md): domínio de métricas corporais, value objects e cálculos implementados.

### Observabilidade e registros

- [Logging](logging/README.md): visão geral da estratégia de logging persistido.
  - [Audit Log](logging/audit-log.md): registros de auditoria.
  - [Error Log](logging/error-log.md): registros técnicos de erro.

### Diagramas

- [Diagramas](diagram/README.md): índice dos diagramas arquiteturais, ER e fluxos planejados.

### Releases

- [v0.1.0](releases/v0.1.0.md): notas da primeira release técnica do backend.

## Observações

- A documentação usa termos técnicos em inglês quando eles representam código, camadas, módulos, ferramentas ou conceitos comuns no desenvolvimento backend.
- A `v0.1.0` ainda não possui endpoints REST de negócio consumíveis.
- Funcionalidades como Swagger/OpenAPI, JWT completo, frontend, IA, Kafka, specifications/filtros avançados, catálogo de exercícios e treinos ainda fazem parte da evolução planejada do projeto, salvo quando indicado de forma diferente em documentos específicos.

<p align="right"><a href="../README.md">Voltar para a documentação principal</a></p>
