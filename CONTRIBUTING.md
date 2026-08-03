# Contribuindo com o IronCore Backend

Obrigado pelo interesse em contribuir com o IronCore Backend.

Este documento apresenta as regras gerais para propor alterações, implementar funcionalidades, corrigir problemas e atualizar a documentação do projeto.

O IronCore está em desenvolvimento ativo e evolui de forma incremental por meio de Issues, milestones, branches e Pull Requests.

## Antes de contribuir

Antes de iniciar uma alteração:

1. Consulte as Issues existentes para verificar se o trabalho já está planejado ou em andamento.
2. Leia o [README principal](README.md) para conhecer o escopo e o estado atual do projeto.
3. Consulte a [documentação técnica](docs/README.md), especialmente os documentos relacionados ao módulo que será alterado.
4. Confirme que a alteração pertence ao escopo atual do projeto.
5. Para mudanças relevantes, crie ou associe uma Issue antes de abrir o Pull Request.

Mudanças amplas de arquitetura, domínio, persistência, segurança ou contratos REST devem ser discutidas antes da implementação.

## Tecnologias e requisitos

O ambiente de desenvolvimento utiliza principalmente:

- Java 21;
- Spring Boot;
- Maven Wrapper;
- PostgreSQL;
- Flyway;
- Docker e Docker Compose;
- MongoDB, atualmente preparado para evoluções futuras;
- JUnit 5;
- Mockito;
- Testcontainers;
- Springdoc OpenAPI.

Não é necessário instalar o Maven globalmente. Utilize o Maven Wrapper incluído no repositório.

## Executando o projeto

Inicie os serviços locais necessários utilizando o arquivo Docker Compose indicado no [README principal](README.md).

Execute a aplicação com o perfil de desenvolvimento:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Executando os testes

Para executar a suíte padrão:

```bash
./mvnw test --batch-mode
```

Para executar a verificação completa utilizada pelo CI:

```bash
./mvnw clean verify --batch-mode
```

Antes de abrir um Pull Request, a verificação completa deve passar localmente.

## Organização arquitetural

O IronCore utiliza uma arquitetura em camadas com inspiração pragmática em DDD.

A direção pretendida das dependências é:

```text
interfaces -> application -> domain
infrastructure -> application/domain contracts
```

### `domain`

Contém regras e conceitos de domínio, como:

- models;
- value objects;
- domain services;
- contratos de repository;
- exceptions de domínio.

A camada de domínio não deve depender de:

- Spring;
- HTTP;
- JPA;
- entidades de persistência;
- controllers;
- integrações externas.

### `application`

Coordena os fluxos da aplicação por meio de:

- use cases;
- commands;
- application services;
- ports;
- eventos;
- exceptions de aplicação.

Regras de negócio não devem ser deslocadas para controllers ou adapters de persistência.

### `infrastructure`

Implementa detalhes técnicos, como:

- persistência JPA;
- adapters de repository;
- mappers de persistência;
- autenticação JWT;
- configuração do Spring;
- bootstrap;
- integração com banco de dados;
- listeners e publishers técnicos.

Os contratos de repository pertencem ao domínio. Suas implementações técnicas pertencem à infraestrutura.

### `interfaces`

Contém os pontos de entrada da aplicação, incluindo:

- controllers REST;
- request e response DTOs;
- tratamento global de erros;
- documentação OpenAPI dos contratos REST.

Controllers devem permanecer enxutos: recebem a requisição, validam o contrato de entrada, constroem os dados necessários e delegam o fluxo aos use cases.

## Regras de modelagem

As separações de responsabilidade existentes devem ser preservadas.

```text
Person = dados pessoais
User = autenticação e acesso
BodyMetrics = métricas corporais da pessoa
```

Algumas regras importantes:

- `User` referencia `PersonId`, mas não deve carregar o aggregate completo de `Person`;
- dados pessoais e físicos pertencem à `Person`;
- ownership de dados físicos deve ser validada por `PersonId`;
- entidades JPA não devem ser retornadas diretamente pela API;
- DTOs de interface não devem substituir models de domínio;
- `actorUserId` identifica quem executou uma ação auditável;
- `personId` identifica a pessoa proprietária dos dados afetados.

Novos módulos devem seguir o mesmo princípio de separação entre domínio, aplicação, infraestrutura e interface.

## Banco de dados e migrations

O projeto utiliza Flyway para versionamento do banco PostgreSQL.

Ao alterar o schema:

1. Crie uma nova migration.
2. Não altere migrations que já façam parte do histórico do projeto.
3. Utilize uma descrição objetiva no nome do arquivo.
4. Mantenha compatibilidade entre schema, entidades e adapters.
5. Atualize a documentação do banco quando aplicável.
6. Adicione testes de integração quando houver risco relacionado a schema, queries, constraints ou mappings.

Exemplo de nomenclatura:

```text
V6__create_table_example.sql
```

A numeração deve seguir a próxima versão disponível no repositório.

## Contratos REST e OpenAPI

Alterações em endpoints devem manter consistência entre:

- controller;
- request e response DTOs;
- status HTTP;
- tratamento de erros;
- autenticação e autorização;
- documentação OpenAPI;
- testes de controller ou integração.

A documentação OpenAPI deve permanecer separada dos controllers conforme o padrão atual do projeto.

Não exponha entities JPA, estruturas internas de persistência, hashes, tokens, segredos ou informações técnicas desnecessárias nos contratos públicos.

## Logging e auditoria

Ao implementar ou alterar um fluxo, avalie se a operação exige:

- registro de auditoria;
- registro técnico de erro;
- identificação do usuário responsável;
- identificação da pessoa proprietária dos dados;
- proteção contra exposição de dados sensíveis nos logs.

Logs não devem armazenar:

- senhas;
- hashes de senha;
- tokens JWT;
- cookies de autenticação;
- segredos;
- dados pessoais desnecessários;
- payloads completos quando não forem necessários para diagnóstico.

## Testes

Os testes devem ser proporcionais ao risco e ao tipo da alteração.

### Domínio

Use testes unitários para:

- invariantes;
- value objects;
- regras de domínio;
- cálculos;
- transições de estado;
- entradas inválidas.

### Aplicação

Use testes unitários para:

- use cases;
- orquestração de fluxos;
- validação de ownership;
- publicação de eventos;
- chamadas a ports;
- cenários de sucesso e erro.

### Infraestrutura

Use testes para:

- adapters;
- mappers;
- persistência;
- queries;
- autenticação;
- filtros;
- configuração;
- bootstrap.

Quando a alteração depender do schema real do PostgreSQL, dê preferência a testes de integração com Testcontainers.

### Interfaces

Use testes de controller ou integração para:

- status HTTP;
- contratos de request e response;
- validações;
- autenticação;
- autorização;
- serialização;
- tratamento de erros.

Uma funcionalidade não deve ser considerada concluída apenas porque compila.

## Documentação

A documentação faz parte da entrega.

Atualize o conteúdo relevante em `/docs` quando a alteração afetar:

- arquitetura;
- estrutura do projeto;
- domínio;
- endpoints;
- banco de dados;
- migrations;
- autenticação;
- segurança;
- logging;
- testes;
- Swagger/OpenAPI;
- diagramas;
- releases;
- estado atual do projeto.

Documentos históricos de releases não devem ser reescritos para representar o estado atual. Uma release deve preservar o contexto do momento em que foi publicada.

## Issues

Os títulos das Issues seguem, em geral, o padrão:

```text
[escopo] descrição objetiva
```

Exemplos:

```text
[backend] implementar consulta detalhada de workout
[docs] atualizar documentação do fluxo de workout planning
[infra] configurar execução do serviço no ambiente local
```

A Issue deve conter:

- contexto;
- objetivo;
- critérios de aceite;
- observações, quando necessárias.

Os critérios específicos devem ser adicionados conforme o trabalho planejado. O template é uma base e não substitui o detalhamento técnico da entrega.

## Branches

Utilize nomes objetivos e relacionados à alteração.

Padrão recomendado:

```text
tipo/descricao-curta
```

Exemplos:

```text
feat/workout-details
fix/body-metrics-ownership
docs/workout-planning
refactor/user-authentication
chore/community-standards
```

Evite nomes genéricos, como:

```text
update
changes
new-feature
test
```

## Commits

Utilize mensagens objetivas e em inglês, seguindo o padrão já adotado no projeto.

Exemplos:

```text
feat(workout): add detailed workout query
fix(body-metrics): validate person ownership
docs(workout): document planning lifecycle
test(auth): cover invalid token scenario
refactor(user): isolate password change service
chore(github): add community standards
```

Cada commit deve representar uma unidade coerente de alteração.

Evite misturar, no mesmo commit:

- refatorações não relacionadas;
- formatação ampla;
- alterações funcionais;
- atualização de dependências;
- documentação de outro módulo.

## Pull Requests

O Pull Request deve:

- possuir título objetivo;
- estar relacionado a uma Issue quando aplicável;
- limitar-se ao escopo proposto;
- passar pela verificação completa;
- incluir os testes necessários;
- atualizar a documentação afetada;
- evitar alterações paralelas não relacionadas.

O título deve seguir o padrão de commits do projeto.

Exemplo:

```text
feat(workout): add detailed workout query
```

Pull Requests grandes devem explicar claramente suas decisões, limitações e impactos.

## Critérios gerais de conclusão

Antes de considerar uma contribuição concluída, valide:

- [ ] O objetivo da Issue foi atendido.
- [ ] O escopo foi respeitado.
- [ ] A estrutura arquitetural do projeto foi preservada.
- [ ] Regras de domínio permanecem fora de controllers e infraestrutura.
- [ ] Ownership e segurança foram avaliadas, quando aplicáveis.
- [ ] Os testes necessários foram adicionados ou atualizados.
- [ ] `./mvnw clean verify --batch-mode` foi executado com sucesso.
- [ ] Migrations históricas não foram alteradas.
- [ ] Contratos REST e OpenAPI permanecem consistentes.
- [ ] Logs não expõem informações sensíveis.
- [ ] A documentação relacionada foi atualizada.
- [ ] Não há código comentado, temporário ou desnecessário.
- [ ] Não foram introduzidos breaking changes não intencionais.

## Segurança

Não abra uma Issue pública contendo detalhes de vulnerabilidades exploráveis, credenciais, tokens, segredos ou dados pessoais.

Consulte a [Política de Segurança](SECURITY.md) para reportar vulnerabilidades.

## Código de Conduta

Ao participar do projeto, siga o [Código de Conduta](CODE_OF_CONDUCT.md).

## Licença

Ao contribuir, você concorda que sua contribuição será disponibilizada sob a mesma licença aplicada ao repositório.
