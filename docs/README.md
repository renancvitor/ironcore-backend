# Documentação Técnica

Este diretório centraliza a documentação técnica do IronCore Backend.

Alguns documentos descrevem funcionalidades já implementadas, enquanto outros registram decisões arquiteturais, módulos parcialmente preparados ou blueprints planejados.

## Índice

### Fundação e arquitetura

- [Arquitetura](architecture/README.md): visão das camadas, responsabilidades e abordagem DDD pragmática.
- [Estrutura do Projeto](project-structure/README.md): esqueleto completo de diretórios, código, resources, testes e documentação.
- [Banco de dados e migrations](database/README.md): PostgreSQL, Flyway e leitura das migrations do projeto.
- [Exceptions](exceptions/README.md): estratégia de exceptions e tratamento global de erros REST.
- [Swagger/OpenAPI](swagger/README.md): documentação gerada dos contratos REST, Swagger UI e `/v3/api-docs`.
- [Testes Automatizados](testing/README.md): estratégia de testes unitários, integração, segurança e CI.
- [Estratégia de Filtragem](filtering/README.md): uso de derived queries, JPQL e Specifications nas listagens.

### Módulos documentados

- [Persons](persons/README.md): domínio de dados pessoais, relação com user, bootstrap e endpoint de atualização.
- [Users e Auth](users/README.md): domínio de usuário, bootstrap, autenticação single-user, JWT e endpoints atuais.
- [Body Metrics](body-metrics/README.md): fluxo funcional de métricas corporais, endpoints REST, cálculos, progresso e ownership por `PersonId`.
- [Catálogo de Exercises](exercises/README.md): catálogo global controlado pelo sistema, catálogos auxiliares, filtros combinados, endpoints e seeds.

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
- [v0.3.0](releases/v0.3.0/README.md): user body metrics funcional.
- [v0.4.0](releases/v0.4.0/README.md): person, refactor para body metrics e ownership por `PersonId`.
- [v0.5.0](releases/v0.5.0/README.md): Swagger/OpenAPI dos contratos REST atuais.
- [v0.6.0](releases/v0.6.0/README.md): exercise catalog com catálogos auxiliares, filtros, paginação, OpenAPI e seeds.

## Observações

- A documentação usa termos técnicos em inglês quando eles representam código, camadas, módulos, ferramentas ou conceitos comuns no desenvolvimento backend.
- Funcionalidades como frontend, IA, Kafka e treinos ainda fazem parte da evolução planejada do projeto, salvo quando indicado de forma diferente em documentos específicos.
- Releases anteriores preservam história do projeto e não devem ser reescritos para refletir o estado atual.

<p align="right"><a href="../README.md">Voltar para a documentação principal</a></p>
