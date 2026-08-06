# Catálogo de Exercises

## Visão Geral

O módulo de exercise catalog representa o catálogo global de exercícios do IronCore Backend.

Esta área é controlada pelo sistema e serve como base para consultas, filtros combinados e montagem futura de workouts. O usuário da API não cria, altera ou remove registros do catálogo pelos endpoints atuais.

## Escopo Implementado

- Catálogos auxiliares globais de `ActivityType`, `EquipmentType`, `MuscleGroup` e `MuscleSubgroup`.
- Domínio de `Exercise`.
- Domínio de `ExerciseMuscleTarget`.
- Persistência relacional das tabelas do catálogo.
- Seeds iniciais versionados por migrations Flyway.
- Consultas de catálogos auxiliares ativos.
- Listagem paginada de exercises ativos.
- Filtros combinados por nome, tipo de atividade, tipo de equipamento, grupo muscular, subgrupo muscular e papel do músculo alvo.
- Consulta detalhada de exercise ativo por id.
- Contratos OpenAPI para os endpoints do catálogo.
- Testes automatizados de domínio, aplicação, persistência, REST e integração.

## Modelo Funcional

```text
ActivityType
EquipmentType
MuscleGroup -> MuscleSubgroup

Exercise
  -> ActivityType
  -> EquipmentType
  -> ExerciseMuscleTarget -> MuscleSubgroup -> MuscleGroup
```

### Catálogos Auxiliares

Os catálogos auxiliares usam `code` como identificador estável de sistema, `displayName` para apresentação, `active` para disponibilidade e `sortOrder` para ordenação quando aplicável.

Seeds iniciais:

- `ActivityType`: `STRENGTH`, `CARDIO`, `CORE`, `MOBILITY`, `RECOVERY`, `CONDITIONING`.
- `EquipmentType`: `BODYWEIGHT`, `DUMBBELL`, `BARBELL`, `MACHINE`, `CABLE`, `KETTLEBELL`, `BAND`.
- `MuscleGroup`: `CHEST`, `BACK`, `SHOULDERS`, `BICEPS`, `FOREARMS`, `TRICEPS`, `QUADRICEPS`, `HAMSTRINGS`, `ADDUCTORS`, `HIP_FLEXORS`, `GLUTES`, `LOWER_LEGS`, `ABS`.
- `MuscleSubgroup`: subgrupos musculares vinculados a `MuscleGroup`.

### Exercise

`Exercise` representa uma variação exercitável do catálogo.

Campos centrais:

- `id`
- `name`
- `equipmentTypeId`
- `activityTypeId`
- `unilateral`
- `compound`
- `suggestedRestSeconds`
- `active`
- `createdAt`
- `updatedAt`

Regras atuais:

- `name` é obrigatório.
- `equipmentTypeId` é obrigatório.
- `activityTypeId` é obrigatório.
- `unilateral`, `compound` e `active` são obrigatórios.
- `suggestedRestSeconds` é opcional, mas precisa ser positivo quando informado.
- Apenas exercises ativos são expostos nas consultas REST atuais.

### ExerciseMuscleTarget

`ExerciseMuscleTarget` representa a associação entre um exercise e um subgrupo muscular alvo.

Campos centrais:

- `id`
- `exerciseId`
- `muscleSubgroupId`
- `targetRole`
- `active`
- `createdAt`
- `updatedAt`

`targetRole` usa os valores:

- `PRIMARY`
- `SECONDARY`
- `STABILIZER`

A tabela possui restrição única para a associação `exercise_id + muscle_subgroup_id`.

## Endpoints REST

Base path:

```http
/api/exercise-catalog
```

Os endpoints abaixo seguem a configuração global de segurança do backend e exigem autenticação por cookie JWT `access_token`. Eles são endpoints de consulta do catálogo, não endpoints públicos anônimos.

Catálogos auxiliares:

- `GET /api/exercise-catalog/activity-types`
- `GET /api/exercise-catalog/equipment-types`
- `GET /api/exercise-catalog/muscle-groups`
- `GET /api/exercise-catalog/muscle-subgroups`
- `GET /api/exercise-catalog/muscle-subgroups?muscleGroupId={id}`

Exercises:

- `GET /api/exercise-catalog/exercises`
- `GET /api/exercise-catalog/exercises/{id}`

## Listagem de Exercises

Endpoint:

```http
GET /api/exercise-catalog/exercises
```

Paginação:

- `page`: padrão `0`, mínimo `0`.
- `size`: padrão `20`, mínimo `1`, máximo `100`.

Filtros:

- `name`
- `activityTypeId`
- `equipmentTypeId`
- `muscleGroupId`
- `muscleSubgroupId`
- `targetRole`

A ordenação atual é por `name` ascendente e `id` ascendente.

Exemplo:

```http
GET /api/exercise-catalog/exercises?name=supino&activityTypeId=1&equipmentTypeId=3&page=0&size=20
```

## Estratégia de Filtragem

A listagem de exercises usa Spring Data JPA `Specification` para montar filtros combinados dinamicamente.

Regras atuais:

- A busca sempre parte de exercises ativos.
- `name` aplica busca parcial case-insensitive com escape de caracteres especiais de `LIKE`.
- `activityTypeId` filtra pela relação com `activity_types`.
- `equipmentTypeId` filtra pela relação com `equipment_types`.
- `muscleGroupId`, `muscleSubgroupId` e `targetRole` usam subquery com `exists` sobre `exercise_muscle_targets`.
- Quando `muscleSubgroupId` é informado junto com `muscleGroupId`, o filtro por `targetRole` é aplicado no nível do subgrupo.
- Quando apenas `muscleGroupId` é informado, `targetRole` pode ser aplicado no nível do grupo.
- Quando não há grupo nem subgrupo, `targetRole` filtra qualquer alvo muscular ativo com aquele papel.

Consultas simples dos catálogos auxiliares e detalhe por id usam derived queries do Spring Data JPA, por exemplo:

- `findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc`
- `findAllByMuscleGroup_IdAndActiveTrueOrderBySortOrderAscDisplayNameAsc`
- `findByIdAndActiveTrue`
- `findAllByExercise_IdAndActiveTrueOrderByTargetRoleAscMuscleSubgroup_DisplayNameAsc`

Consultas de progresso de body metrics continuam usando JPQL com projection própria.

Referência transversal: [Estratégia de Filtragem](../filtering/README.md).

## Ausência de CRUD Público

Não há endpoint público para criar, atualizar, inativar, reativar ou excluir exercises e catálogos auxiliares.

Essa decisão preserva o catálogo como dado global de sistema. A gestão do conteúdo do catálogo é feita por migrations e seeds versionados, não por operação de usuário.

## Seeds e Carga de Dados

As seeds atuais são versionadas por Flyway:

- `V12__seed_activity_types.sql`
- `V13__seed_equipment_types.sql`
- `V14__seed_muscle_groups.sql`
- `V15__seed_muscle_subgroups.sql`
- `V16__seed_exercises.sql`
- `V17__seed_exercise_muscle_targets.sql`

O seed atual inclui:

- 341 exercises.
- 2766 associações entre exercises e subgrupos musculares.

Como `exercises` ainda não possui um código natural estável, o seed de `exercise_muscle_targets` resolve cada exercise pela combinação exata de `name`, `equipment_type.code` e `activity_type.code`.

A migration `V17` possui validações estruturais para impedir target role inválido, associações duplicadas, referências não resolvidas e exercises sem pelo menos um alvo `PRIMARY`.

Durante o planejamento do módulo, a carga temporária por `INSERT` manual era uma alternativa aceitável para desenvolvimento local. No estado final desta release, essa estratégia foi substituída por migrations de seed versionadas, mantendo a carga inicial reprodutível em banco limpo e rastreável pelo histórico do Flyway.

## Auditoria

O catálogo possui tipos de alvo preparados em `AuditTargetType`, como `EXERCISE`, `MUSCLE_GROUP` e `EQUIPMENT_TYPE`.

Como a API atual expõe apenas consultas, não há evento de auditoria operacional gerado pelos endpoints do catálogo. A auditoria fica preparada para fluxos futuros de manutenção controlada do catálogo.

## Banco de Dados

Tabelas:

- `activity_types`
- `equipment_types`
- `muscle_groups`
- `muscle_subgroups`
- `exercises`
- `exercise_muscle_targets`

Documentação relacionada: [Banco de Dados e Migrations](../database/README.md).

## Documentação Relacionada

- [Swagger/OpenAPI](../swagger/README.md)
- [Estratégia de Filtragem](../filtering/README.md)
- [Arquitetura](../architecture/README.md)
- [Testes Automatizados](../testing/README.md)
- [Release v0.6.0](../releases/v0.6.0/README.md)

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
