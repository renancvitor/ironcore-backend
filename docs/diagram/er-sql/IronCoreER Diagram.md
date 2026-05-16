# Diagrama Entidade-Relacionamento (ER)

**Status:** Parcialmente implementado / blueprint planejado — na `v0.1.0`, somente `users`, `user_body_metrics`, `audit_logs` e `error_logs` possuem migrations Flyway reais. As tabelas de exercise e workout neste diagrama representam escopo planejado.

<p align="center">
    <img src="./IronCoreERDiagram.png" alt="Diagrama entidade-relacionamento do IronCore" />
</p>

## Visão Geral

Este documento descreve o modelo relacional planejado da aplicação **IronCore**. Parte dele já existe na release `v0.1.0`, e parte representa blueprint para módulos futuros.

O diagrama foi organizado para priorizar a **legibilidade visual**. Portanto, a posição das tabelas na imagem **não representa** prioridade funcional, criticidade técnica ou peso no domínio.

### Leitura rápida do domínio

- `users`: base cadastral dos usuários.
- `user_body_metrics`: histórico de medições físicas.
- `muscle_group`, `equipment_type`, `activity_type` e `training_goal`: tabelas de domínio e classificação.
- `exercises`: catálogo de exercícios.
- `workout_cycles`: ciclo ou plano de treino do usuário.
- `workout_days`: divisão semanal do ciclo.
- `workout_activities`: atividades prescritas em cada dia de treino.

## Objetivo do modelo

O modelo foi desenhado para atender quatro frentes principais:

1. armazenar os dados de autenticação e identificação do usuário;
2. registrar a evolução corporal ao longo do tempo;
3. estruturar um catálogo padronizado de exercícios e classificações;
4. permitir a montagem de ciclos de treino com dias e atividades detalhadas.

## Tabelas

### `users`

Armazena os dados principais do usuário.

| Campo | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| `id` | `BIGINT` | Sim | Identificador único do usuário. |
| `name` | `VARCHAR` | Sim | Nome do usuário. |
| `email` | `VARCHAR` | Sim | E-mail único para autenticação e contato. |
| `password_hash` | `VARCHAR` | Sim | Hash da senha do usuário. |
| `sex` | `VARCHAR` | Sim | Sexo utilizado em cálculos corporais, como estimativa de percentual de gordura. |
| `created_at` | `TIMESTAMP` | Sim | Data e hora de criação do registro. |
| `updated_at` | `TIMESTAMP` | Não | Data e hora da última atualização. |

### `user_body_metrics`

Registra o histórico de medições corporais de cada usuário, permitindo acompanhamento de evolução física.

| Campo                | Tipo | Obrigatório | Descrição                                    |
|----------------------| --- | --- |----------------------------------------------|
| `id`                 | `BIGINT` | Sim | Identificador único da medição.              |
| `user_id`            | `BIGINT` | Sim | Referência ao usuário dono da medição.       |
| `measured_at`        | `TIMESTAMP` | Sim | Data e hora em que a medição foi registrada. |
| `weight_kg`          | `NUMERIC(5,2)` | Sim | Peso corporal em quilogramas.                |
| `height_cm`          | `NUMERIC(5,2)` | Sim | Altura em centímetros.                       |
| `neck_cm`            | `NUMERIC(5,2)` | Não | Medida do pescoço em centímetros.            |
| `chest_cm`           | `NUMERIC(5,2)` | Não | Medida do tórax em centímetros.              |
| `shoulder_cm`        | `NUMERIC(5,2)` | Não | Medida do ombro em centímetros.              |
| `arm_cm`             | `NUMERIC(5,2)` | Não | Medida do braço em centímetros.              |
| `forearm_cm`         | `NUMERIC(5,2)` | Não | Medida do antebraço em centímetros.          |
| `waist_cm`           | `NUMERIC(5,2)` | Não | Medida da cintura em centímetros.            |
| `hip_cm`             | `NUMERIC(5,2)` | Não | Medida do quadril em centímetros.            |
| `thigh_cm`           | `NUMERIC(5,2)` | Não | Medida da coxa em centímetros.               |
| `calf_cm`            | `NUMERIC(5,2)` | Não | Medida da panturrilha em centímetros.        |
| `bmi`                | `NUMERIC(5,2)` | Não | Índice de Massa Corporal.                    |
| `body_fat_percentage`| `NUMERIC(5,2)` | Não | Percentual de gordura corporal.              |
| `fat_mass_kg`        | `NUMERIC(5,2)` | Não | Percentual de massa gorda.                   |
| `lean_mass_kg`       | `NUMERIC(5,2)` | Não | Percentual de massa magra.                   |
| `notes`              | `TEXT` | Não | Observações livres sobre a medição.          |

### `muscle_group`

Tabela de domínio para classificar o grupamento muscular principal do exercício.

| Campo | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| `id` | `BIGINT` | Sim | Identificador do grupamento muscular. |
| `name` | `VARCHAR` | Sim | Nome técnico/chave única do grupamento. |
| `display_name` | `VARCHAR` | Sim | Nome amigável exibido para o usuário. |

Valores de referência:

- `CHEST` - Peitoral
- `BACK` - Costas
- `SHOULDERS` - Ombros
- `BICEPS` - Bíceps
- `TRICEPS` - Tríceps
- `QUADRICEPS` - Quadríceps
- `HAMSTRINGS` - Posterior de coxa
- `GLUTES` - Glúteos
- `CALVES` - Panturrilhas
- `ABS` - Abdômen

### `equipment_type`

Tabela de domínio para classificar o tipo de equipamento exigido no exercício.

| Campo | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| `id` | `BIGINT` | Sim | Identificador do tipo de equipamento. |
| `name` | `VARCHAR` | Sim | Nome técnico/chave única. |
| `display_name` | `VARCHAR` | Sim | Nome amigável exibido para o usuário. |

Valores de referência:

- `BODYWEIGHT` - Peso corporal
- `DUMBBELL` - Halter
- `BARBELL` - Barra
- `MACHINE` - Máquina
- `CABLE` - Cabo
- `KETTLEBELL` - Kettlebell
- `BAND` - Elástico

### `activity_type`

Tabela de domínio para classificar a natureza da atividade física.

| Campo | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| `id` | `BIGINT` | Sim | Identificador do tipo de atividade. |
| `name` | `VARCHAR` | Sim | Nome técnico/chave única. |
| `display_name` | `VARCHAR` | Sim | Nome amigável exibido para o usuário. |

Valores de referência:

- `STRENGTH` - Força
- `CARDIO` - Cárdio
- `CORE` - Core
- `MOBILITY` - Mobilidade
- `RECOVERY` - Recuperação

### `exercises`

Catálogo central de exercícios disponíveis para composição dos treinos.

| Campo | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| `id` | `BIGINT` | Sim | Identificador do exercício. |
| `name` | `VARCHAR` | Sim | Nome do exercício. |
| `muscle_group_id` | `BIGINT` | Sim | Grupamento muscular principal associado. |
| `equipment_type_id` | `BIGINT` | Sim | Tipo de equipamento utilizado. |
| `activity_type_id` | `BIGINT` | Sim | Categoria da atividade. |
| `unilateral` | `BOOLEAN` | Sim | Indica se o exercício é executado unilateralmente. |
| `compound` | `BOOLEAN` | Sim | Indica se o exercício é multiarticular. |
| `suggested_rest_seconds` | `INTEGER` | Não | Tempo sugerido de descanso entre séries. |
| `active` | `BOOLEAN` | Sim | Indica se o exercício está disponível para uso. |

### `training_goal`

Tabela de domínio para representar o objetivo principal do ciclo de treino.

| Campo | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| `id` | `BIGINT` | Sim | Identificador do objetivo. |
| `name` | `VARCHAR` | Sim | Nome técnico/chave única. |
| `display_name` | `VARCHAR` | Sim | Nome amigável exibido para o usuário. |

Valores de referência:

- `HYPERTROPHY` - Hipertrofia
- `STRENGTH` - Força
- `FAT_LOSS` - Perda de gordura
- `MAINTENANCE` - Manutenção
- `ENDURANCE` - Resistência

### `workout_cycles`

Representa um ciclo de treino associado a um usuário e a um objetivo específico.

| Campo | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| `id` | `BIGINT` | Sim | Identificador do ciclo de treino. |
| `user_id` | `BIGINT` | Sim | Usuário responsável pelo ciclo. |
| `name` | `VARCHAR` | Sim | Nome do ciclo de treino. |
| `training_goal_id` | `BIGINT` | Sim | Objetivo principal do ciclo. |
| `start_date` | `DATE` | Sim | Data de início do ciclo. |
| `end_date` | `DATE` | Sim | Data de encerramento prevista. |
| `workout_status` | `VARCHAR` | Sim | Status atual do ciclo. |
| `workout_origin` | `VARCHAR` | Sim | Origem da criação do ciclo. |
| `notes` | `TEXT` | Não | Observações complementares. |
| `created_at` | `TIMESTAMP` | Sim | Data e hora de criação do registro. |

### `workout_days`

Organiza os dias de treino vinculados a um ciclo.

| Campo | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| `id` | `BIGINT` | Sim | Identificador do dia de treino. |
| `workout_cycle_id` | `BIGINT` | Sim | Ciclo ao qual o dia pertence. |
| `week_day` | `VARCHAR` | Sim | Dia da semana planejado para execução. |
| `title` | `VARCHAR` | Sim | Título descritivo do treino do dia. |

### `workout_activities`

Detalha cada atividade prescrita dentro de um dia de treino.

| Campo | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| `id` | `BIGINT` | Sim | Identificador da atividade de treino. |
| `workout_day_id` | `BIGINT` | Sim | Dia de treino ao qual a atividade pertence. |
| `exercise_id` | `BIGINT` | Sim | Exercício vinculado à atividade. |
| `order_index` | `INTEGER` | Sim | Ordem de execução no treino. |
| `sets` | `INTEGER` | Sim | Quantidade de séries planejadas. |
| `rep_range_min` | `INTEGER` | Não | Faixa mínima de repetições. |
| `rep_range_max` | `INTEGER` | Não | Faixa máxima de repetições. |
| `target_load_kg` | `NUMERIC(5,2)` | Não | Carga sugerida em quilogramas. |
| `target_load_text` | `VARCHAR` | Não | Descrição textual alternativa da carga. |
| `duration_minutes` | `INTEGER` | Não | Duração prevista da atividade. |
| `distance_km` | `NUMERIC(5,2)` | Não | Distância prevista, quando aplicável. |
| `intensity_text` | `VARCHAR` | Não | Descrição qualitativa de intensidade. |
| `rest_seconds` | `INTEGER` | Não | Descanso entre séries ou blocos. |
| `notes` | `TEXT` | Não | Observações adicionais para execução. |

## Relacionamentos

Os relacionamentos do modelo seguem a composição abaixo:

- `users` 1:N `user_body_metrics`
- `users` 1:N `workout_cycles`
- `training_goal` 1:N `workout_cycles`
- `workout_cycles` 1:N `workout_days`
- `workout_days` 1:N `workout_activities`
- `exercises` 1:N `workout_activities`
- `muscle_group` 1:N `exercises`
- `equipment_type` 1:N `exercises`
- `activity_type` 1:N `exercises`

## Enums e valores controlados

### `WorkoutStatus`

- `NOT_STARTED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

### `WorkoutOrigin`

- `MANUAL`
- `AGENT`

### `WeekDay`

- `MONDAY`
- `TUESDAY`
- `WEDNESDAY`
- `THURSDAY`
- `FRIDAY`
- `SATURDAY`
- `SUNDAY`

## Fluxo lógico do domínio

De forma resumida, o fluxo do modelo funciona assim:

1. um `user` pode possuir várias medições corporais em `user_body_metrics`;
2. o mesmo `user` pode possuir vários `workout_cycles`;
3. cada `workout_cycle` é criado com base em um `training_goal`;
4. cada ciclo é dividido em vários `workout_days`;
5. cada dia contém várias `workout_activities`;
6. cada atividade aponta para um item do catálogo `exercises`;
7. cada exercício é classificado por grupamento muscular, tipo de equipamento e tipo de atividade.

## Observações de modelagem

- As tabelas de domínio (`muscle_group`, `equipment_type`, `activity_type` e `training_goal`) centralizam valores controlados e evitam duplicidade semântica.
- O detalhamento em `workout_activities` permite acomodar treinos de força, cardio e atividades híbridas com diferentes parâmetros de execução.
- O histórico de medições em `user_body_metrics` favorece rastreabilidade e análise evolutiva do usuário ao longo do tempo.
- A organização visual do diagrama foi pensada para facilitar a leitura humana; ela não indica ordem de processamento, dependência operacional prioritária ou relevância maior de uma tabela sobre outra.
- A imagem deste diagrama foi criada com o [dbdiagram.io](https://dbdiagram.io/).

<p align="right"><a href="../../../README.md">🔄 Voltar para a documentação completa</a></p>
