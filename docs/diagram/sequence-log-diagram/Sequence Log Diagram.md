# Sequence Log Diagram

<p align="center">
    <img src="./Sequence%20Log%20Diagram.png" alt="Sequence Log Diagram" />
</p>

## Visão Geral

Este documento descreve o fluxo temporal de registro da execução de treino no projeto **IronCore / Personal Trainer AI**.

O diagrama mostra como usuário, frontend, backend e persistência relacional interagem durante uma sessão real de treino.

Este é um diagrama de sequência. Portanto, ele representa a ordem das interações no tempo, e não a estrutura interna da arquitetura.

## Leitura rápida do fluxo

- `User`: atleta que registra a execução do treino em tempo real.
- `Angular`: cliente web utilizado durante a sessão de treino.
- `Spring Boot`: backend principal que valida, coordena e persiste os registros da sessão.
- `PostgreSQL`: persistência relacional principal da aplicação.

## Objetivo do diagrama

Este diagrama foi criado para mostrar o fluxo principal de registro operacional de uma sessão de treino.

Ele ajuda a entender:

1. como a sessão de treino é iniciada;
2. como atividades executadas são registradas;
3. como séries realizadas são persistidas;
4. como a sessão é encerrada;
5. como o resumo final é retornado ao usuário.

O diagrama prioriza clareza e fluxo principal. Algumas etapas técnicas foram simplificadas propositalmente.

## Participantes do fluxo

- `User`: atleta que inicia, executa e finaliza a sessão de treino.
- `Angular`: aplicação cliente usada durante o treino para enviar registros e exibir o resumo final.
- `Spring Boot`: backend principal. Centraliza autenticação, validações, regras da sessão e persistência dos dados.
- `PostgreSQL`: base relacional principal do sistema, responsável por armazenar os registros da execução.

## Fluxo lógico da execução do treino

De forma resumida, o fluxo de execução funciona assim:

1. o `User` inicia o treino no `Angular`;
2. o `Angular` envia `start workout session` para o `Spring Boot`;
3. o `Spring Boot` cria o registro `workout_session_log` no `PostgreSQL`;
4. o `PostgreSQL` retorna a confirmação `session created`;
5. o `Spring Boot` retorna `session started` para o `Angular`;
6. o `User` registra uma atividade no `Angular`;
7. o `Angular` envia `save activity log` para o `Spring Boot`;
8. o `Spring Boot` persiste `workout_activity_log` no `PostgreSQL`;
9. o `PostgreSQL` retorna a confirmação `persisted`;
10. o `User` registra uma série realizada no `Angular`;
11. o `Angular` envia `save set log` para o `Spring Boot`;
12. o `Spring Boot` persiste `workout_activity_set_log` no `PostgreSQL`;
13. o `PostgreSQL` retorna a confirmação `persisted`;
14. o `User` finaliza o treino no `Angular`;
15. o `Angular` envia `finish session` para o `Spring Boot`;
16. o `Spring Boot` atualiza o `workout_session_log` no `PostgreSQL`;
17. o `PostgreSQL` retorna a confirmação `persisted`;
18. o `Spring Boot` retorna o `workout summary` para o `Angular`;
19. o `Angular` exibe o resumo para o `User`.

## Dados registrados durante a sessão

O `start workout session` pode representar a abertura operacional da sessão de treino.

- abertura da sessão de treino;
- vínculo com `workout_cycle`;
- identificação do usuário;
- data e hora de início;
- status inicial da sessão.

O `log activity` e o `save activity log` podem representar registros de execução por atividade.

- exercício iniciado ou concluído;
- carga utilizada;
- duração;
- observações;
- dificuldade percebida;
- execução parcial.

O `log set` e o `save set log` podem representar registros específicos de séries realizadas.

- número da série;
- repetições realizadas;
- carga real;
- RIR/RPE futuro;
- tempo da série;
- observações específicas.

O `finish session` pode representar o encerramento da sessão de treino.

- horário final;
- duração total;
- status concluído, parcial ou cancelado;
- observações finais.

O `workout summary` pode representar a resposta consolidada exibida ao usuário.

- resumo do treino;
- exercícios executados;
- volume total;
- tempo total;
- indicadores rápidos;
- feedback visual ao usuário.

## Responsabilidades não representadas explicitamente

Algumas responsabilidades importantes não aparecem no diagrama para manter o fluxo simples e legível.

- Autenticação pode ocorrer antes do início da sessão.
- Validações podem ocorrer em cada envio para o `Spring Boot`.
- Erros de rede e retries não foram representados.
- Logs técnicos, métricas e observabilidade não aparecem no desenho.
- Sincronização offline futura pode ser adicionada sem alterar a lógica macro do fluxo.
- Múltiplos logs de atividade ou série podem ocorrer repetidamente durante a sessão.

## Limites deste diagrama

Este diagrama:

- mostra a sequência temporal da execução real do treino;
- destaca o fluxo principal entre frontend, backend e persistência relacional;
- evidencia a criação, atualização e persistência dos registros operacionais da sessão.

Este diagrama não pretende:

- detalhar a arquitetura interna do backend;
- representar todas as validações e regras de negócio;
- detalhar tratamento de falhas, retentativas ou sincronização offline;
- mostrar todos os eventos técnicos de observabilidade;
- substituir diagramas estruturais ou de domínio da aplicação.

## Observações finais

- O `Spring Boot` é o ponto central de validação e persistência do fluxo.
- O `PostgreSQL` mantém os registros relacionais da execução do treino.
- O fluxo pode ter vários registros de atividade e série antes do encerramento da sessão.
- A imagem deste diagrama foi criada com o [diagrams.net](https://app.diagrams.net/).

<p align="right"><a href="../../../README.md">🔄 Voltar para a documentação completa</a></p>
