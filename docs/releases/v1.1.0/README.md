# v1.1.0 - Produção, Segurança e Evoluções da API

## Resumo

`v1.1.0` consolida a evolução do IronCore Backend após sua primeira versão estável `v1.0.0`.

Esta release marca a implantação inicial do backend em produção, com infraestrutura própria na OVHcloud, banco PostgreSQL no Supabase, domínio público com HTTPS e pipeline de Continuous Deployment (CD) automatizado pelo GitHub Actions.

Além da publicação em produção, a versão introduz proteção CSRF para autenticação baseada em cookies, melhorias no tratamento de autenticação, consulta de dados pessoais, filtros múltiplos no catálogo de exercícios e ajustes de integração com o frontend Angular.

O IronCore passa a disponibilizar sua API em `https://api.ironcore.dev.br`, integrada à interface em `https://ironcore.dev.br`.

## Principais Mudanças

### Segurança e Autenticação

- Implementação de proteção CSRF utilizando `CookieCsrfTokenRepository`.
- Novo endpoint público `GET /api/auth/csrf` para obtenção do token e do nome do header utilizado nas operações de escrita.
- Exigência de token CSRF para operações HTTP protegidas contra falsificação, incluindo login, logout e troca inicial de senha.
- Invalidação do token CSRF anterior durante login e logout.
- Configuração de `SameSite=Lax` nos cookies de produção.
- Manutenção dos cookies de autenticação com `HttpOnly` e `Secure`.
- Padronização das respostas `401 Unauthorized` para falhas de autenticação.
- Correções no tratamento de falhas JWT e na integração entre `SecurityFilterChain` e o mecanismo de autenticação.
- Adequação do Swagger UI em desenvolvimento para envio do token CSRF nas operações de escrita.
- Inclusão de testes de integração voltados às configurações de segurança em produção.

### API e Contratos

- Implementação de `GET /api/users/me/person` para consultar os dados da pessoa vinculada ao usuário autenticado.
- Alinhamento de DTOs de usuário para suportar corretamente a restauração da sessão no frontend.
- Inclusão de suporte ao método HTTP `PATCH` na configuração de CORS.
- Ampliação da listagem de exercícios com filtros de seleção múltipla.
- Novos parâmetros `activityTypeIds`, `equipmentTypeIds`, `muscleGroupIds`, `muscleSubgroupIds` e `targetRoles`.
- Preservação dos filtros individuais existentes, com validação das combinações permitidas.
- Atualização dos contratos REST/OpenAPI e dos testes correspondentes.

### Deploy e Infraestrutura

- Implantação da API em uma VPS OVHcloud no Canadá.
- Execução com Java 21, Spring Boot 3.5 e Ubuntu 26.04 LTS.
- Gerenciamento da aplicação pelo serviço `ironcore-backend.service`, via systemd.
- Configuração do Caddy como proxy reverso, com HTTPS automático.
- Publicação da API em `https://api.ironcore.dev.br`.
- Configuração do PostgreSQL no Supabase, na região de São Paulo, com conexão TLS.
- Habilitação das migrations Flyway no perfil de produção.
- Configuração de CORS para a origem oficial do frontend.
- Desabilitação dos recursos não utilizados em produção, incluindo integrações de MongoDB, Kafka e scheduler de outbox.
- Armazenamento das configurações sensíveis fora do repositório.

### CI/CD

- Criação do workflow `CD - Deploy Backend`.
- Integração automática entre CI aprovado na `main` e publicação do backend.
- Preservação da execução manual por `workflow_dispatch`.
- Build e testes executados antes da publicação.
- Envio do JAR à VPS por SSH/SCP com verificação da identidade do servidor.
- Usuário `ironcore-deploy` dedicado à publicação, com permissões administrativas restritas.
- Script de deploy responsável pela substituição do JAR e reinicialização do serviço.
- Health check após a publicação.
- Backup do JAR anterior e tentativa de rollback automático em caso de falha.

## Decisões Funcionais e Arquiteturais

- O backend permanece como autoridade para autenticação, autorização, ownership, validações e regras de domínio.
- A arquitetura em camadas e a organização pragmática inspirada em DDD foram preservadas.
- A identidade do usuário autenticado continua sendo validada no backend.
- A proteção CSRF complementa a autenticação JWT baseada em cookies.
- A integração entre frontend e API utiliza HTTPS, CORS com credenciais e cookies `SameSite=Lax`.
- As operações de escrita passam a exigir o fluxo de obtenção e envio do token CSRF.
- Os filtros múltiplos do catálogo representam valores alternativos dentro de cada critério, preservando a combinação dos critérios distintos.
- O catálogo global de exercícios continua controlado pelo sistema, sem CRUD público.
- O backend permanece independente do frontend quanto ao processo de publicação.
- A configuração de produção mantém Swagger UI e `/v3/api-docs` desabilitados.
- O processo de CD não depende de Docker e utiliza diretamente Java, systemd e Caddy na VPS.

## Fluxos e Contratos Relevantes

### CSRF

- `GET /api/auth/csrf`

Retorna os dados necessários para que o cliente envie o token de proteção nas requisições de escrita.

### Person

- `GET /api/users/me/person`
- `PATCH /api/users/me/person`

A consulta e a atualização permanecem restritas à pessoa vinculada ao usuário autenticado.

### Exercise Catalog

`GET /api/exercise-catalog/exercises` passa a aceitar os filtros múltiplos:

- `activityTypeIds`
- `equipmentTypeIds`
- `muscleGroupIds`
- `muscleSubgroupIds`
- `targetRoles`

Os parâmetros de seleção múltipla permitem informar valores repetidos na query string, conforme o contrato da API.

## Banco de Dados e Produção

O PostgreSQL permanece como banco relacional principal, com schema gerenciado pelo Flyway.

A implantação utiliza Supabase PostgreSQL com conexão protegida por TLS e configurações fornecidas por variáveis de ambiente.

O bootstrap inicial de pessoa e usuário foi executado durante a preparação do ambiente e posteriormente desabilitado.

Esta release não adiciona novas migrations de schema em relação à `v1.0.0`. As migrations existentes passam a ser executadas no ambiente de produção.

O rollback automatizado restaura o JAR anterior quando necessário, mas **não reverte migrations Flyway nem alterações já persistidas no banco**.

## Compatibilidade e Limitações Operacionais

A habilitação do CSRF altera o contrato operacional dos clientes que realizam requisições de escrita.

Clientes anteriores que não enviam o token CSRF precisam ser adaptados. O frontend oficial do IronCore já implementa essa integração.

Outras considerações:

- O health check confirma a disponibilidade básica da aplicação, não todos os fluxos funcionais.
- O deploy utiliza reinicialização do processo Java e não oferece garantia de zero downtime.
- O rollback do JAR é uma tentativa de recuperação e não substitui uma estratégia de backup e restauração do banco.
- Frontend e backend possuem ciclos de publicação independentes.
- A arquitetura de produção mantém o backend no Canadá e o PostgreSQL em São Paulo; otimizações de latência permanecem como trabalho futuro.

## Testes e Validação

A evolução desde a `v1.0.0` possui testes para:

- Autenticação JWT e tratamento de respostas `401`.
- Proteção CSRF e integração com endpoints públicos e protegidos.
- Configurações de segurança de produção.
- Integração do Swagger UI com CSRF.
- Consulta de dados pessoais.
- Filtros múltiplos do catálogo de exercícios.
- Controllers REST, mappers e casos de uso afetados.
- Integração dos fluxos existentes.

Validação automatizada final:

- `./mvnw clean verify --batch-mode`
- **923 testes executados.**
- **0 falhas, 0 erros e 0 testes ignorados.**
- Build concluído com sucesso.
- CI da `main` aprovado.
- CD automático executado com sucesso.
- Health check aprovado após a publicação.

## Documentação

Documentação técnica atualizada:

- `README.md`
- `docs/README.md`
- `docs/deploy/README.md`
- `docs/database/README.md`
- `docs/users/README.md`
- `docs/persons/README.md`
- `docs/testing/README.md`

A documentação histórica da `v1.0.0` e dos pre-releases anteriores foi preservada.

A `v1.1.0` marca a consolidação operacional da primeira versão estável do IronCore Backend em produção, com segurança reforçada, contratos evoluídos e publicação automatizada.

Workout Session, histórico de sessões executadas e geração de treinos com IA permanecem como evoluções futuras do produto.

## Fonte Histórica

Registro documental da [release oficial](https://github.com/renancvitor/ironcore-backend/releases/tag/v1.1.0), publicada em 2026-10-22. O conteúdo acima preserva as notas publicadas; resultados de validação ali citados pertencem àquela release e não são uma nova execução.

<p align="right"><a href="../README.md">Voltar para releases</a></p>

