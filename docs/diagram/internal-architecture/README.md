# Diagrama de Arquitetura Interna da Aplicação

**Status:** Parcialmente implementado — a estrutura de packages em camadas existe; na `v0.2.0`, os controllers REST de auth/users e o baseline de autenticação single-user estão implementados.

<p align="center">
    <img src="./Internal%20Architecture%20Diagram%20of%20the%20Application.png" alt="Internal Architecture Diagram of the Application" />
</p>

## Visão Geral

Este documento descreve a arquitetura interna da aplicação backend **Spring Boot API** do projeto **IronCore / Personal Trainer AI**.

O diagrama complementa a visão geral da arquitetura ao mostrar como o backend principal está organizado internamente seguindo princípios de Domain-Driven Design (DDD).

O foco é apresentar as camadas internas, as responsabilidades principais e o fluxo de dependências entre apresentação, aplicação, domínio e infraestrutura.

## Leitura rápida da arquitetura interna

- `Presentation Layer`: entrada HTTP da aplicação.
- `Application Layer`: coordenação dos fluxos de uso.
- `Domain Layer`: regras de negócio e modelo de domínio.
- `Infrastructure Layer`: implementações técnicas, persistência, segurança, integrações e configuração.

## Objetivo do diagrama

Este diagrama foi criado para mostrar a organização interna da `Spring Boot API`.

Ele ajuda a entender:

1. onde as requisições HTTP entram na aplicação;
2. onde os casos de uso são coordenados;
3. onde ficam as regras de negócio;
4. como o domínio acessa persistência sem depender de detalhes técnicos;
5. onde ficam implementações de banco, IA, segurança e configuração.

O diagrama prioriza as dependências principais e a clareza visual. Ele não representa todas as chamadas possíveis entre classes, módulos ou componentes internos.

## Camadas da arquitetura

### `Presentation Layer`

Camada responsável pela entrada da aplicação.

- `REST Controllers`: expõem endpoints HTTP, recebem requisições do frontend e direcionam a execução para os casos de uso correspondentes.

### `Application Layer`

Camada responsável por coordenar os fluxos da aplicação.

- `Use Cases`: orquestram a execução dos casos de uso, conectando entrada, regras de negócio e recursos necessários.
- `DTOs`: representam contratos de entrada e saída usados na comunicação entre a API e seus consumidores.

### `Domain Layer`

Camada responsável pelo modelo de domínio e pelas regras de negócio.

- `Domain Services`: concentram regras de negócio que não pertencem naturalmente a uma única entidade.
- `Entities`: representam objetos centrais do domínio, com identidade própria e comportamento relevante para a aplicação.
- `Value Objects`: representam conceitos imutáveis, sem identidade própria, usados para expressar valores do domínio.
- `Repository Contracts`: definem abstrações de acesso a dados em camada superior, sem expor detalhes de banco ou tecnologia.

### `Infrastructure Layer`

Camada responsável pelas implementações técnicas necessárias para a aplicação funcionar.

- `Repository Implementations`: implementam tecnicamente os `Repository Contracts` definidos em camada superior.
- `PostgreSQL Access`: representa o acesso à persistência relacional da aplicação.
- `AI Integrations`: representa integrações técnicas com componentes externos de IA.
- `Security`: representa autenticação, autorização e mecanismos técnicos de proteção da aplicação.
- `Config`: representa configurações técnicas da aplicação e de suas integrações.

## Componentes principais

Os `REST Controllers` fazem a ponte entre o mundo HTTP e os casos de uso da aplicação. Eles não devem concentrar regra de negócio.

Os `Use Cases` coordenam o fluxo da aplicação. Eles recebem dados, acionam o domínio e delegam persistência ou integrações por meio de abstrações.

Os `DTOs` organizam os contratos de entrada e saída, evitando que detalhes internos do domínio sejam expostos diretamente.

A `Domain Layer` concentra o modelo principal da aplicação. Ela não deve depender de detalhes técnicos como banco de dados, frameworks ou integrações externas.

Os `Repository Contracts` permitem que o domínio e a aplicação dependam de abstrações. Isso evita acoplamento direto com a persistência.

A `Infrastructure Layer` implementa as capacidades técnicas necessárias para atender aos contratos e integrações usados pelas camadas superiores.

## Fluxo lógico da arquitetura interna

De forma resumida, o fluxo interno da aplicação funciona assim:

1. os `REST Controllers` recebem requisições HTTP;
2. os controllers acionam os `Use Cases`;
3. os `Use Cases` orquestram a execução do fluxo;
4. as regras de negócio passam pela `Domain Layer`;
5. a persistência é acessada por meio de `Repository Contracts`;
6. as `Repository Implementations` implementam esses contratos na `Infrastructure Layer`;
7. o acesso relacional é feito por meio do `PostgreSQL Access`;
8. integrações técnicas, segurança e configurações são resolvidas na `Infrastructure Layer`.

## Responsabilidades transversais

Algumas capacidades aparecem na `Infrastructure Layer` como suporte técnico da aplicação, mas não fazem parte do fluxo central desenhado.

- `AI Integrations`: suporte técnico para comunicação com componentes externos de IA.
- `Security`: autenticação, autorização e mecanismos de proteção da aplicação.
- `Config`: configuração técnica da aplicação, credenciais, parâmetros e integrações.

Algumas setas foram omitidas propositalmente para preservar a clareza visual e evitar poluição no diagrama.

## Limites deste diagrama

Este diagrama:

- mostra a arquitetura interna da `Spring Boot API`;
- destaca as camadas principais da aplicação;
- evidencia o uso de contratos para desacoplar domínio e persistência;
- posiciona infraestrutura como implementação técnica das necessidades da aplicação.

Este diagrama não pretende:

- detalhar todos os pacotes, classes ou módulos do backend;
- representar todas as chamadas possíveis entre componentes;
- detalhar implementação de segurança, configuração ou integrações de IA;
- substituir diagramas específicos de fluxo, sequência ou domínio.

## Observações finais

- O diagrama deve ser lido como visão estrutural interna do backend.
- A `Domain Layer` deve permanecer isolada de detalhes técnicos.
- A `Infrastructure Layer` implementa persistência, integrações e recursos técnicos usados pela aplicação.
- A imagem deste diagrama foi criada com o [diagrams.net](https://app.diagrams.net/).

<p align="right"><a href="../../../README.md">🔄 Voltar para a documentação completa</a></p>
