# Documentação Técnica

Este diretório centraliza a documentação técnica do IronCore Backend.

A release `v0.1.0` representa o estado anterior ao fechamento de users/auth/security.

A `v0.2.0` fecha o baseline de users/auth/security single-user.

Alguns documentos descrevem funcionalidades já implementadas, enquanto outros registram decisões arquiteturais, módulos parcialmente preparados ou blueprints planejados.

## Índice

### Fundação e arquitetura

- [Arquitetura](architecture/README.md): visão das camadas, responsabilidades e abordagem DDD pragmática.
- [Estrutura do Projeto](project-structure/README.md): esqueleto completo de diretórios, código, resources, testes e documentação.
- [Banco de dados e migrations](database/README.md): PostgreSQL, Flyway e migrations reais da fundação atual.
- [Exceptions](exceptions/README.md): estratégia de exceptions e tratamento global de erros REST.
- [Testes Automatizados](testing/README.md): estratégia de testes unitários, integração, segurança e CI.

### Módulos documentados

- [Users e Auth](users/README.md): domínio de usuário, bootstrap, autenticação single-user, JWT e endpoints atuais.
- [User Body Metrics](user-body-metrics/README.md): domínio de métricas corporais, value objects e cálculos implementados.

### Observabilidade e registros

- [Logging](logging/README.md): visão geral da estratégia de logging persistido.
  - [Audit Log](logging/audit-log.md): registros de auditoria.
  - [Error Log](logging/error-log.md): registros técnicos de erro.

### Diagramas

- [Diagramas](diagram/README.md): índice dos diagramas arquiteturais, ER e fluxos planejados.

### Releases

- [Releases](releases/README.md): índice histórico de releases.
- [v0.1.0](releases/v0.1.0/README.md): fundação técnica anterior ao fechamento de users.
- [v0.2.0](releases/v0.2.0/README.md): users, auth e security single-user.

## Observações

- A documentação usa termos técnicos em inglês quando eles representam código, camadas, módulos, ferramentas ou conceitos comuns no desenvolvimento backend.
- A `v0.1.0` ainda não possui endpoints REST de negócio consumíveis.
- A `v0.2.0` possui endpoints REST mínimos de autenticação e usuário autenticado, mas ainda não entrega o MVP completo do produto.
- Funcionalidades como Swagger/OpenAPI gerado, frontend, IA, Kafka, specifications/filtros avançados, catálogo de exercícios e treinos ainda fazem parte da evolução planejada do projeto, salvo quando indicado de forma diferente em documentos específicos.

<p align="right"><a href="../README.md">Voltar para a documentação principal</a></p>
