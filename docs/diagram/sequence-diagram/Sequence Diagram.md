# Sequence Diagram

<p align="center">
    <img src="./Sequence%20Diagram.png" alt="Sequence Diagram" />
</p>

## Visão Geral

Este documento descreve o fluxo temporal de geração de treino pelo agente no projeto **IronCore / Personal Trainer AI**.

O diagrama mostra como o usuário, o frontend, o backend, o orquestrador de IA, a persistência documental, o provedor LLM e a persistência relacional interagem durante a geração de um treino.

Este é um diagrama de sequência. Portanto, ele representa a ordem das interações no tempo, e não a estrutura interna da arquitetura.

## Leitura rápida do fluxo

- `User`: solicita a geração do treino.
- `Angular`: cliente web que envia a solicitação ao backend.
- `Spring Boot`: backend principal que valida, coordena e persiste dados relacionais.
- `AI Orchestrator`: coordena o fluxo de geração com os componentes de IA.
- `NoSQL`: armazena e consulta dados documentais ou contextuais.
- `LLM API`: provedor externo do modelo de linguagem.
- `PostgreSQL`: persistência relacional principal da aplicação.

## Objetivo do diagrama

Este diagrama foi criado para mostrar o fluxo principal de geração de treino com apoio do agente de IA.

Ele ajuda a entender:

1. como a solicitação sai da interface web;
2. como o backend centraliza a entrada do fluxo;
3. como o orquestrador usa contexto e modelo de linguagem;
4. onde os artefatos documentais são persistidos;
5. onde os metadados relacionais do treino são salvos.

O diagrama prioriza clareza e fluxo principal. Algumas etapas técnicas foram simplificadas propositalmente.

## Participantes do fluxo

- `User`: usuário que solicita a geração de um treino.
- `Angular`: cliente web da aplicação, responsável por enviar a solicitação e exibir o resultado.
- `Spring Boot`: backend principal. Centraliza autenticação, validações, regras da aplicação e integração com a camada de IA.
- `AI Orchestrator`: componente responsável por coordenar o fluxo de geração com os recursos de IA.
- `NoSQL`: armazenamento documental e contextual usado durante o processo de geração.
- `LLM API`: provedor externo do modelo de linguagem utilizado para gerar o conteúdo do treino.
- `PostgreSQL`: persistência relacional principal da aplicação.

## Fluxo lógico da geração de treino

De forma resumida, o fluxo de geração funciona assim:

1. o `User` solicita a geração de treino no `Angular`;
2. o `Angular` envia a `generate workout request` para o `Spring Boot`;
3. o `Spring Boot` recebe a solicitação e inicia a `orchestration request`;
4. o `AI Orchestrator` busca contexto no `NoSQL` por meio do `fetch context`;
5. o `NoSQL` retorna os `context data`;
6. o `AI Orchestrator` monta o `prompt` e envia para a `LLM API`;
7. a `LLM API` retorna o `generated workout`;
8. o `AI Orchestrator` persiste o artefato no `NoSQL` com `persist artifact`;
9. o `AI Orchestrator` retorna o `structured workout` para o `Spring Boot`;
10. o `Spring Boot` persiste metadados no `PostgreSQL` com `persist metadata`;
11. o `PostgreSQL` retorna a confirmação `persisted`;
12. o `Spring Boot` devolve a `workout response` para o `Angular`;
13. o `Angular` exibe o treino gerado para o `User`.

## Dados persistidos durante o processo

O `fetch context` no `NoSQL` pode representar a busca de dados usados para enriquecer a geração do treino.

- histórico anterior;
- preferências do usuário;
- documentos auxiliares;
- artefatos prévios;
- contexto persistido.

O `persist artifact` no `NoSQL` pode representar a persistência de dados documentais produzidos ou usados no fluxo de IA.

- treino gerado;
- resposta estruturada;
- prompt e resultados relevantes;
- histórico de geração;
- auditoria funcional.

O `persist metadata` no `PostgreSQL` pode representar a persistência relacional dos dados principais associados ao treino.

- vínculo do treino ao usuário;
- ciclo criado;
- status;
- referências internas;
- datas;
- entidades relacionais principais.

## Responsabilidades não representadas explicitamente

Algumas responsabilidades importantes não aparecem no diagrama para manter o fluxo simples e legível.

- Autenticação e autorização podem ocorrer no `Spring Boot` antes da orquestração.
- Validações de entrada podem ocorrer antes do início do fluxo principal.
- Tratamento de erros, retries e timeouts não foram representados.
- Logs, métricas e observabilidade não aparecem no desenho.
- Cache ou filas assíncronas podem existir futuramente sem alterar a lógica macro do fluxo.

## Limites deste diagrama

Este diagrama:

- mostra a sequência temporal da geração de treino;
- destaca o fluxo principal entre frontend, backend, IA e persistências;
- diferencia persistência documental em `NoSQL` e persistência relacional em `PostgreSQL`.

Este diagrama não pretende:

- detalhar a arquitetura interna do backend;
- representar todas as validações e regras de negócio;
- detalhar tratamento de falhas, retentativas ou timeouts;
- mostrar todos os eventos técnicos de observabilidade;
- substituir diagramas estruturais da aplicação.

## Observações finais

- O `Spring Boot` é o ponto central de entrada e coordenação do fluxo.
- O `AI Orchestrator` isola a geração com IA da lógica principal da API.
- O `NoSQL` apoia o fluxo documental e contextual da IA.
- O `PostgreSQL` mantém os dados relacionais principais da aplicação.
- A imagem deste diagrama foi criada com o [diagrams.net](https://app.diagrams.net/).

<p align="right"><a href="../../../README.md">🔄 Voltar para a documentação completa</a></p>
