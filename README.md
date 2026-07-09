<h1 align="center">
  IronCore Backend
</h1>
<p align="center">
  <img src="https://img.shields.io/badge/Status-In%20Progress-yellow" width="150" height="30" />
</p>

<!-- Troque o texto e a cor do badge conforme o status do projeto:
     Status-Completed-brightgreen   → Projeto concluído
     Status-In%20Progress-yellow    → Projeto em andamento
     Status-Paused-orange           → Projeto pausado
     Status-Canceled-red            → Projeto cancelado
     Exemplo de uso:
     https://img.shields.io/badge/Status-Completed-brightgreen
-->

---

<h2 align="center">🔗 Frontend</h2>

> 🏗️ Frontend em preparação

---

### 📊 Progresso do Projeto

Planejamento, tarefas e histórico de evolução disponíveis no GitHub Projects:

- 🗺️ [IronCore — Roadmap](https://github.com/users/renancvitor/projects/3)

---

<h2 id="sumario" align="center">Sumário</h2>

- [Visão Geral do Projeto](#visao-geral-do-projeto)
- [Status Atual do Projeto](#status-atual-do-projeto)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Ferramentas Utilizadas](#ferramentas-utilizadas)
- [Migrations e Versionamento de Banco](#migrations-e-versionamento-de-banco)
- [Logging](#logging)
- [Estratégia de Filtragem nas Listagens](#estrategia-de-filtragem-nas-listagens)
- [Escopo Inicial / MVP](#escopo-inicial--mvp)
- [Funcionalidades](#funcionalidades)
- [Documentação Visual](#documentacao-visual)
  - [API - Swagger](#api---swagger)
  - [Documentação Arquitetural](#documentacao-arquitetural)
- [Documentação Técnica](#documentacao-tecnica)
- [Mensageria com Apache Kafka](#mensageria-kafka)
- [Testes Automatizados](#testes-automatizados)
- [Testando a API via Insomnia](#testando-a-api-via-insomnia)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Como Executar o Projeto](#como-executar-o-projeto)
- [Deploy](#deploy)
- [Contribuições](#contribuicoes)
- [Contato](#contato)
- [Licença](#licenca)

---

<h2 id="visao-geral-do-projeto" align="center">Visão Geral do Projeto</h2>

<b>IronCore</b> é um backend em desenvolvimento com <b>[Spring Boot](https://spring.io/projects/spring-boot)</b>, planejado para evoluir como uma API REST para gerenciamento de histórico de treinos, evolução física, catálogo de exercícios e planejamento de ciclos de treino.

O projeto nasce como uma aplicação prática de backend com foco em arquitetura limpa, modelagem de domínio e evolução incremental. A proposta é permitir que o usuário registre medições corporais, organize treinos de musculação ou cárdio, acompanhe sessões executadas e, em uma etapa posterior, gere treinos manualmente ou com apoio de um agente de IA.

A arquitetura foi planejada em camadas e orientada por princípios de DDD (Domain-Driven Design), separando apresentação, aplicação, domínio e infraestrutura. Essa organização busca manter o backend preparado para crescer com segurança, testes, persistência relacional, integrações futuras e suporte a fluxos de IA.

O desenvolvimento do projeto busca consolidar habilidades como:
- 🏗️ Arquitetura RESTful
- 🧪 Testes unitários e de integração com [JUnit 5](https://junit.org/) e 🔧 [Mockito](https://site.mockito.org/)
- ✅ Validações robustas com [Bean Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)
- 🛠️ Tratamento de erros
- 📖 Documentação automatizada com [Swagger (OpenAPI)](https://swagger.io/specification/) conforme os endpoints forem implementados
- 🔒 Segurança com [JWT (JSON Web Token)](https://jwt.io/) aplicada ao baseline single-user

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="status-atual-do-projeto" align="center">Status Atual do Projeto</h2>

O <b>IronCore</b> está em fase inicial de desenvolvimento. O backend já possui uma fundação funcional para autenticação single-user, pessoa vinculada ao usuário autenticado, métricas corporais, persistência relacional, logging, exceptions e testes automatizados.

### Já existe no projeto
- Estrutura inicial do backend com Spring Boot.
- Configuração Maven com Java 21.
- Organização em camadas com abordagem pragmática inspirada em DDD.
- Domínios iniciais de pessoa, usuário, métricas corporais e logging.
- Migrations Flyway para `persons`, `users`, `body_metrics`, `audit_logs` e `error_logs`.
- Persistência relacional com PostgreSQL.
- Logging persistido de auditoria e erro.
- Bootstrap opcional de pessoa e usuário único vinculado.
- Login, logout e autenticação por JWT via cookie `access_token` na base single-user.
- Troca obrigatória de senha inicial, troca normal de senha, alteração de nickname e consulta do usuário autenticado.
- Fluxo autenticado de pessoa com atualização de dados pessoais.
- Fluxo autenticado de métricas corporais com ownership por `PersonId`, criação, atualização, exclusão, consulta por id, consulta do último registro, listagem paginada e progresso.
- Documentação Swagger/OpenAPI para os contratos REST atuais.
- Testes automatizados e CI executando build/test.
- Documentação visual com diagramas de domínio, arquitetura e fluxos principais.

### Parcialmente preparado
- Spring Security e JWT cobrem o baseline single-user.
- MongoDB sobe localmente via Docker Compose, mas ainda não há uso funcional de domínio/documentos.

### Planejado para as próximas etapas
- Fluxos REST para exercícios e treinos.
- Integração futura com frontend Angular.
- Fluxo de geração de treino com apoio de IA.
- Persistência documental para artefatos/contexto de IA, caso necessária.

Histórico de releases: [Releases](docs/releases/README.md).

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="tecnologias-utilizadas" align="center">Tecnologias Utilizadas</h2>

- ☕ **Backend**
  - ☕ [Java 21](https://www.java.com/pt-BR/) ou superior + 🌱 [Spring Boot 3](https://start.spring.io/)
  - 🌐 [Spring Web](https://spring.io/projects/spring-web)
  - 📦 [JPA](https://spring.io/projects/spring-data-jpa) + 🛠️ [Hibernate](https://hibernate.org/)
  - ✅ Validações ([Bean Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html))
  - 🔄 [Spring Boot DevTools](https://docs.spring.io/spring-boot/reference/using/devtools.html)
  - 📄 [Springdoc OpenAPI](https://springdoc.org/) + [Swagger UI](https://swagger.io/tools/swagger-ui/)
  - 🔧 Lombok

- 🗄️ **Banco de Dados**
  - 🛠️ Controle de versionamento de banco com [Flyway](https://flywaydb.org/)
  - 🐘 [PostgreSQL](https://www.postgresql.org/): Banco de dados
  - 🍃 MongoDB: preparado localmente via Docker Compose, ainda sem uso funcional de domínio

- 🧰 **Build e Ambiente**
  - 📦 [Maven](https://maven.apache.org/): Gerenciamento de dependências e build
  - 🐳 [Docker CLI](https://www.docker.com/products/cli/)
  <!-- - 📘 [Apache Kafka](https://kafka.apache.org/) -->
<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="ferramentas-utilizadas" align="center">Ferramentas Utilizadas</h2>

- 💻 [Visual Studio Code](https://code.visualstudio.com/): Ambiente de desenvolvimento integrado (IDE) leve e extensível.
- 💻 [IntelliJ IDEA](https://www.jetbrains.com/idea/): IDE robusta e rica em recursos, ideal para desenvolvimento Java e Spring Boot.
- 🐳 [Docker](https://www.docker.com/): Utilizado via Docker CLI para execução e gerenciamento dos contêineres do projeto.
- 🐘 [PostgreSQL](https://www.postgresql.org/): Banco de dados relacional executado em contêiner Docker, acessado via CLI (psql).
- 📡 [Insomnia](https://insomnia.rest/): Ferramenta prevista para teste de APIs REST conforme os endpoints de negócio forem implementados.

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="migrations-e-versionamento-de-banco" align="center">Migrations e Versionamento de Banco</h2>

O projeto utiliza o [Flyway](https://flywaydb.org/) para gerenciar as **migrations de banco de dados** no [PostgreSQL](https://www.postgresql.org/). Todas as alterações de estrutura no banco, como criação de tabelas e mudanças de schema, são versionadas e controladas. Isso garante consistência entre os ambientes de desenvolvimento e produção.

Migrations existentes no repositório:
- `V1__create_table_persons.sql`: cria a tabela `persons`.
- `V2__create_table_users.sql`: cria a tabela `users`.
- `V3__create_table_body_metrics.sql`: cria a tabela `body_metrics`.
- `V4__create_table_audit_log.sql`: cria a tabela `audit_logs`.
- `V5__create_table_error_log.sql`: cria a tabela `error_logs`.

Documentação detalhada: [Banco de Dados e Migrations](docs/database/README.md).

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="logging" align="center">Logging</h2>

O projeto possui uma estratégia específica para logs persistidos, separando registros de auditoria (`audit_logs`) e registros técnicos de erro (`error_logs`).

Essa estrutura faz parte da fundação técnica preservada do projeto.

A documentação detalhada está disponível em [Estratégia de Logging](docs/logging/README.md).

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="estrategia-de-filtragem-nas-listagens" align="center">Estratégia de Filtragem nas Listagens</h2>

> Esta seção será implementada por completo conforme o projeto evoluir.

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="escopo-inicial--mvp" align="center">Escopo Inicial / MVP</h2>

> Esta seção descreve o escopo planejado do MVP, não apenas o conteúdo já entregue.

O escopo inicial do <b>IronCore</b> foi inferido a partir da modelagem de banco, dos diagramas de arquitetura e dos fluxos principais documentados.

### Núcleo do MVP
- Provisionamento de pessoa e autenticação do usuário single-user.
- Registro e consulta de medições corporais da pessoa.
- Catálogo de exercícios com classificação por grupo muscular, tipo de equipamento e tipo de atividade.
- Cadastro de objetivos de treino.
- Criação de ciclos de treino vinculados a uma pessoa e a um objetivo.
- Organização do ciclo em dias de treino.
- Definição das atividades planejadas para cada dia, incluindo séries, repetições, carga, duração, distância, intensidade e descanso.
- Registro da execução real de sessões de treino.
- Registro de atividades e séries realizadas durante uma sessão.
- Consulta de resumo e histórico de treinos executados.

### Evoluções planejadas após o MVP
- Geração de treinos com apoio de agente de IA.
- Armazenamento de contexto e artefatos gerados por IA em base documental.
- Sugestões baseadas em histórico, preferências e evolução do usuário.
- Observabilidade, auditoria e rastreabilidade dos fluxos principais.
- Integração com frontend Angular.

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="funcionalidades" align="center">Funcionalidades</h2>

As funcionalidades abaixo representam o escopo funcional planejado para o projeto completo. A implementação será feita de forma incremental conforme o backend evoluir.

### Autenticação e Usuários
- Provisionamento de pessoa e usuário único por bootstrap/configuração.
- Login com autenticação baseada em JWT.
- Troca obrigatória de senha inicial e troca normal de senha.
- Alteração de nickname do usuário autenticado.
- Consulta do usuário autenticado.
- Proteção de rotas por usuário autenticado.

### Pessoas
- Dados pessoais separados da conta de acesso.
- Atualização da pessoa vinculada ao usuário autenticado.
- Uso de `PersonId` como referência para dados físicos.

### Evolução Física
- Cadastro de medições corporais por data.
- Histórico de peso, altura, percentual de gordura e medidas corporais.
- Observações livres por medição.
- Consulta da evolução da pessoa ao longo do tempo.

### Catálogo de Exercícios
- Cadastro e manutenção de exercícios.
- Classificação por grupo muscular.
- Classificação por tipo de equipamento.
- Classificação por tipo de atividade, como força, cárdio, core, mobilidade e recuperação.
- Controle de exercícios ativos/inativos.
- Informações auxiliares como descanso sugerido e indicação de exercício unilateral ou composto.

### Planejamento de Treinos
- Criação de ciclos de treino por usuário.
- Associação do ciclo a um objetivo, como hipertrofia, força, perda de gordura, manutenção ou resistência.
- Controle de status do ciclo de treino.
- Identificação da origem do treino: manual ou agente de IA.
- Organização de dias de treino dentro do ciclo.
- Definição das atividades planejadas para cada dia.

### Execução e Histórico de Treinos
- Início de sessão de treino.
- Registro das atividades executadas.
- Registro de séries realizadas, carga usada, repetições, duração e observações.
- Finalização da sessão de treino.
- Retorno de resumo do treino executado.
- Histórico de sessões para acompanhamento de progresso.

### Geração de Treinos com IA
- Solicitação de geração de treino pelo usuário.
- Orquestração do fluxo pelo backend.
- Uso de contexto do usuário para enriquecer a geração.
- Integração futura com provedor LLM.
- Persistência relacional dos metadados do treino gerado.
- Persistência documental de artefatos e contexto, quando aplicável.

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="documentacao-visual" align="center">Documentação Visual</h2>

<h3 id="api---swagger">🌐 <strong>API - Swagger</strong></h3>

O projeto possui documentação [Swagger/OpenAPI](https://swagger.io/specification/) gerada com Springdoc para os contratos REST atuais de autenticação, usuário autenticado, pessoa e métricas corporais.

A documentação técnica está disponível em [Swagger/OpenAPI](./docs/swagger/README.md).

URLs locais principais:

```text
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/v3/api-docs
```

<h3 id="documentacao-arquitetural">🗂️ <strong>Documentação Arquitetural</strong></h3>

Os principais diagramas do projeto já estão disponíveis e funcionam como blueprint técnico da aplicação. Alguns diagramas representam planejamento futuro e não devem ser lidos como estado totalmente implementado. Índice geral: [Diagrama](./docs/diagram/README.md).

- [Diagrama ER — Banco de Dados](./docs/diagram/er-sql/README.md): descreve as entidades, campos, relacionamentos e valores controlados do domínio.
- [Diagrama de Arquitetura Geral](./docs/diagram/general-architecture/README.md): apresenta a visão macro entre frontend, backend, persistência e camada de IA.
- [Diagrama de Arquitetura Interna](./docs/diagram/internal-architecture/README.md): detalha a organização interna da API em camadas.
- [Diagrama de Sequência — Geração de Treino](./docs/diagram/sequence-diagram/README.md): mostra o fluxo planejado para geração de treino com apoio de IA.
- [Diagrama de Sequência — Registro de Treino](./docs/diagram/sequence-log-diagram/README.md): mostra o fluxo planejado para registro de uma sessão real de treino.

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="documentacao-tecnica" align="center">Documentação Técnica</h2>

A documentação técnica detalhada do projeto está disponível no índice central da pasta [docs](docs/README.md).

Esse índice central reúne os principais documentos sobre arquitetura, banco de dados, logging, exceptions, módulos de domínio, diagramas e notas de release, mantendo o README principal como uma visão geral do projeto.

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="mensageria-kafka" align="center"> Mensageria com Apache Kafka</h2>

> Esta seção representa planejamento futuro e será implementada conforme o projeto evoluir.

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="testes-automatizados" align="center"> Testes Automatizados</h2>

O projeto possui testes automatizados cobrindo domínio, aplicação, infraestrutura, interfaces REST, segurança e integração com contexto Spring.

A suíte atual inclui testes unitários de models, value objects, use cases, services, mappers, JWT, filtros de segurança e cálculos de métricas corporais. Também há testes de integração para contexto Spring, autenticação/security e persistência com PostgreSQL via Testcontainers.

Para executar a validação local:
```bash
./mvnw clean verify --batch-mode
```

O CI do GitHub Actions também executa `./mvnw clean verify --batch-mode` em `push` e `pull_request` para a branch `main`.

Documentação detalhada: [Testes Automatizados](docs/testing/README.md).

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="testando-a-api-via-insomnia" align="center">Testando a API via Insomnia</h2>

> Esta seção será atualizada com uma coleção ou roteiro próprio quando os fluxos de API forem estabilizados.
> O projeto já possui endpoints de autenticação, usuário autenticado, pessoa e body metrics.

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="estrutura-do-projeto" align="center">Estrutura do Projeto</h2>

O backend segue uma organização em camadas:

- `domain`: modelos, value objects, services de domínio, exceptions e contratos.
- `application`: use cases, services de aplicação, eventos e ports.
- `infrastructure`: persistência, adapters, configuração, bootstrap, eventos e segurança técnica.
- `interfaces`: entrada REST e tratamento global de erros.

```plaintext
src/main/java/com/ironcore
 ├── domain              # Modelos, value objects, domain services, exceptions e contratos
 ├── application         # Use cases, services de aplicação, ports e eventos
 ├── infrastructure      # Persistência, segurança, bootstrap, configurações e adapters técnicos
 └── interfaces          # Controllers REST, DTOs, mappers REST e tratamento HTTP de erros

src/main/resources
 └── db/migration        # Migrations Flyway

src/test/java/com/ironcore
 ├── domain
 ├── application
 ├── infrastructure
 └── interfaces

docs
 ├── architecture
 ├── body-metrics
 ├── database
 ├── diagram
 ├── exceptions
 ├── logging
 ├── persons
 ├── project-structure
 ├── releases
 ├── swagger
 └── users
```

Documentação detalhada:
- [Documentação da Arquitetura](docs/architecture/README.md)
- [Estrutura Completa do Projeto](docs/project-structure/README.md)

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="como-executar-o-projeto" align="center">Como Executar o Projeto</h2>

### Pré-requisitos:
- ☕ [Java 21](https://www.java.com/pt-BR/) ou superior
- 🐳 [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/) instalados
- 💻 IDE de sua preferência ([IntelliJ IDEA](https://www.jetbrains.com/pt-br/idea/), [VSCode](https://code.visualstudio.com/), [Eclipse](https://eclipseide.org/) etc.)
- 🐧 [WSL](https://ubuntu.com/desktop/wsl) (se estiver usando Windows)

### Passos
1. Clone o repositório:
```bash
git clone git@github.com:renancvitor/ironcore-backend.git
```
2. Acesse a pasta do projeto:
```bash
cd ironcore-backend
```
3. Inicie os serviços necessários no Docker (PostgreSQL e MongoDB)
```bash
docker compose -f docker-compose.dev.yml up -d
```
Isso vai criar os containers locais de PostgreSQL e MongoDB. O PostgreSQL é a persistência funcional atual; o MongoDB está preparado localmente, mas ainda não possui uso funcional de domínio. Certifique-se de que as portas configuradas no docker-compose.yml não estejam sendo usadas por outros serviços.

4. Verifique se todos os containers estão disponíveis
```bash
docker ps
```
⚠️ **Se algum container não estiver ativo, volte ao passo 3.**

5. Configure as variáveis de ambiente esperadas pelo perfil de desenvolvimento. Ao iniciar o projeto, as migrations serão aplicadas automaticamente pelo [Flyway](https://flywaydb.org/).
```properties
DB_URL_DEV=jdbc:postgresql://localhost:5432/nome_do_banco
DB_USER_DEV=seu_usuario
DB_PASSWORD_DEV=sua_senha
MONGO_URL_DEV=<uri_local_do_mongodb>
MONGO_USER_DEV=<usuario_local_do_mongodb>
MONGO_PASSWORD_DEV=<senha_local_do_mongodb>
JWT_SECRET=<segredo_local>
```
Também existem variáveis opcionais para bootstrap inicial de pessoa e usuário: `IRONCORE_BOOTSTRAP_PERSON_ENABLED`, `IRONCORE_BOOTSTRAP_PERSON_NAME`, `IRONCORE_BOOTSTRAP_PERSON_SEX`, `IRONCORE_BOOTSTRAP_PERSON_BIRTH_DATE`, `IRONCORE_BOOTSTRAP_USER_ENABLED`, `IRONCORE_BOOTSTRAP_USER_NICKNAME`, `IRONCORE_BOOTSTRAP_USER_EMAIL` e `IRONCORE_BOOTSTRAP_USER_PASSWORD`.

6. Execute o backend com o Maven Wrapper no perfil de desenvolvimento:
```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```
7. Execute a validação local de build e testes:
```bash
./mvnw clean verify --batch-mode
```

8. Acesse a aplicação pela porta configurada (por padrão http://localhost:8080). O projeto possui endpoints de autenticação, usuário autenticado, pessoa e body metrics. A documentação Swagger UI fica disponível em `http://localhost:8080/swagger-ui/index.html`.<br>
⚠️ **Lembre-se de manter o Docker rodando enquanto estiver utilizando a aplicação.**

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="deploy" align="center">Deploy</h2>

> Esta seção será implementada por completo conforme o projeto evoluir. Deploy ainda não faz parte do estado atual do projeto.

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="contribuicoes" align="center">Contribuições</h2>

Se você quiser contribuir para o projeto, siga estas etapas:

1. Faça um fork deste repositório.
2. Crie uma nova branch (`git checkout -b feature/alguma-coisa`).
3. Faça suas mudanças.
4. Envie um pull request explicando as mudanças realizadas.

Obrigado pelo interesse em contribuir!

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="contato" align="center">Contato</h2>

Se tiver dúvidas ou sugestões, sinta-se à vontade para entrar em contato:

- 📧 **E-mail**: [renan.vitor.cm@gmail.com](mailto:renan.vitor.cm@gmail.com)

- 🟦 **LinkedIn**: [Renan Vitor](https://www.linkedin.com/in/renan-vitor-developer/)

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---

<h2 id="licenca" align="center">Licença</h2>

📌 Este projeto está licenciado sob a [Licença MIT](LICENSE), o que significa que você pode utilizá-lo, modificar, compartilhar e distribuir livremente, desde que mantenha os devidos créditos aos autores e inclua uma cópia da licença original - veja o arquivo [LICENSE](LICENSE) para detalhes ou acesse a [licença MIT oficial](https://opensource.org/licenses/MIT).

<p align="right"><a href="#sumario">⬆️ Voltar ao sumário</a></p>

---
