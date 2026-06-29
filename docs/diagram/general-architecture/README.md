# Diagrama de Arquitetura Geral

<p align="center">
    <img src="./General%20Architecture%20Diagram.png" alt="General Architecture Diagram" />
</p>

## Visão Geral

Este documento descreve a arquitetura geral do projeto **IronCore / Personal Trainer AI** a partir do diagrama de visão macro da aplicação.

O objetivo é mostrar como o frontend, o backend, a persistência principal e os componentes de IA se conectam dentro da solução.

O diagrama foi desenhado para priorizar a **legibilidade visual**. Por isso, ele mostra apenas os blocos principais da arquitetura e não entra no detalhamento interno de implementação.

### Leitura rápida da arquitetura

- `Client Layer`: interação do usuário com a aplicação.
- `Application Layer`: backend principal e regras de negócio.
- `Core Services Layer`: autenticação e persistência relacional.
- `AI Layer`: orquestração e integrações de IA.

## Objetivo do diagrama

Este diagrama foi criado para mostrar a estrutura principal do sistema em alto nível.

Ele ajuda a entender:

1. onde acontece a interação do usuário;
2. qual componente centraliza a lógica da aplicação;
3. onde ficam os recursos principais de persistência;
4. como os fluxos de IA entram na arquitetura.

Ele não foi feito para detalhar a arquitetura interna do backend, o modelo relacional ou a sequência completa de chamadas entre módulos.

## Camadas da arquitetura

### `Client Layer`

Camada responsável pela interação com o usuário.

- `User`: representa o usuário que utiliza a aplicação.
- `Angular Web App`: interface web da aplicação, responsável pela experiência do usuário e pelo consumo da API backend.

### `Application Layer`

Camada principal da aplicação backend.

- `Spring Boot API`: ponto central de entrada do sistema. Recebe as requisições do frontend, aplica regras de negócio, valida acesso, integra persistência e aciona os fluxos de IA quando necessário.

### `Core Services Layer`

Camada com os recursos centrais utilizados pela aplicação backend.

- `Authentication & Authorization`: módulo interno da `Spring Boot API`, e não um serviço externo separado nesta visão. É responsável por autenticação, autorização e controle de acesso da aplicação.
- `PostgreSQL`: persistência relacional principal da aplicação. Representa o armazenamento transacional e estruturado do domínio principal.

### `AI Layer`

Camada responsável pelos fluxos ligados à inteligência artificial.

- `AI Orchestrator`: componente que coordena os fluxos de IA. Ele pertence à `AI Layer`, mas é acionado pela `Spring Boot API`. Não é consumido diretamente pelo frontend nem pelo usuário.
- `Document Store (NoSQL)`: persistência documental usada nos fluxos de IA, incluindo artefatos gerados, histórico, contexto, requests/responses estruturadas e documentos semelhantes.
- `LLM Provider API`: integração externa com o provedor do modelo de linguagem.

## Componentes principais

A `Spring Boot API` é o núcleo da arquitetura representada no diagrama. Ela centraliza a lógica da aplicação e faz a ponte entre o frontend, os recursos centrais do domínio e os fluxos de IA.

O módulo de `Authentication & Authorization` faz parte da própria arquitetura do backend e deve ser entendido como capacidade interna da API.

O `PostgreSQL` representa a base relacional principal da aplicação, usada para persistência do domínio transacional.

O `AI Orchestrator` separa os fluxos de IA da lógica principal da API, mantendo a integração com IA organizada em uma camada própria.

O `Document Store (NoSQL)` dá suporte à persistência documental dos processos de IA.

A `LLM Provider API` representa a chamada ao provedor externo responsável pelo modelo de linguagem utilizado pela aplicação.

## Fluxo lógico da arquitetura

De forma resumida, o fluxo da arquitetura funciona assim:

1. o `User` interage com o `Angular Web App`;
2. o `Angular Web App` consome a `Spring Boot API`;
3. a `Spring Boot API` centraliza as regras de negócio e as integrações da aplicação;
4. a API usa o módulo interno de `Authentication & Authorization` para autenticação e autorização;
5. a API usa o `PostgreSQL` como persistência relacional principal;
6. quando um fluxo envolve IA, a `Spring Boot API` delega esse processamento ao `AI Orchestrator`;
7. o `AI Orchestrator` usa o `Document Store (NoSQL)` para persistência documental e a `LLM Provider API` para integração com o modelo de linguagem.

## Responsabilidades transversais

Algumas responsabilidades importantes da arquitetura não aparecem como blocos visuais neste diagrama porque esta visão foi mantida simples e direta.

- Filas ou mensageria, se existirem, devem ser entendidas como mecanismo de suporte para processamento assíncrono, desacoplamento ou resiliência.
- Cache, se adotado, deve ser entendido como recurso transversal e opcional de desempenho.
- Observabilidade, incluindo logs, métricas, tracing e monitoramento, é uma responsabilidade transversal da aplicação.
- Secrets e configuration management fazem parte da configuração segura da aplicação e das integrações externas.
- Rate limiting ou throttling, quando aplicável, faz parte da proteção da API e do controle de uso dos recursos expostos.

Esses itens não foram desenhados para preservar a simplicidade e a legibilidade do architecture overview.

## Limites deste diagrama

Este diagrama:

- mostra a visão macro do sistema;
- destaca os principais blocos e suas conexões;
- posiciona a IA como capacidade acionada pelo backend.

Este diagrama não pretende:

- detalhar a arquitetura interna da `Spring Boot API`;
- detalhar o modelo relacional do `PostgreSQL`;
- detalhar a sequência de chamadas internas entre módulos;
- representar todos os aspectos operacionais da solução.

## Observações finais

- O diagrama deve ser lido como visão estrutural de alto nível da aplicação.
- O backend é o ponto central de coordenação entre interface, domínio, persistência e IA.
- Os elementos transversais foram omitidos da imagem para manter a leitura clara e objetiva.
- A imagem deste diagrama foi criada com o [diagrams.net](https://app.diagrams.net/).

<p align="right"><a href="../../../README.md">🔄 Voltar para a documentação completa</a></p>
