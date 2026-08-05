# v0.6.0 - Exercise Catalog

## Resumo

`v0.6.0` consolida o catálogo de exercises do IronCore Backend.

Esta release está associada ao milestone GitHub `v9.0.0`, cujo foco foi entregar um catálogo completo de exercises com catálogos auxiliares, associação de músculos-alvo, busca combinada, filtros, paginação, detalhe por id, testes automatizados, cobertura OpenAPI e documentação.

## Escopo Entregue

- Domínio de `ActivityType`.
- Domínio de `EquipmentType`.
- Domínio de `MuscleGroup`.
- Domínio de `MuscleSubgroup`.
- Domínio de `Exercise`.
- Domínio de `ExerciseMuscleTarget`.
- Persistência JPA dos catálogos auxiliares.
- Persistência JPA de exercises.
- Persistência JPA de exercise muscle targets.
- Migrations Flyway para tabelas do catálogo.
- Seeds iniciais para catálogos auxiliares.
- Seed inicial com 341 exercises.
- Seed inicial com 2766 associações entre exercises e subgrupos musculares.
- Consultas dos catálogos auxiliares ativos.
- Listagem paginada de exercises ativos.
- Filtros combinados por nome, tipo de atividade, tipo de equipamento, grupo muscular, subgrupo muscular e papel do músculo alvo.
- Consulta detalhada de exercise ativo por id.
- Endpoints REST de consulta do exercise catalog.
- Documentação OpenAPI dos contratos REST do catálogo.
- Testes automatizados de domínio, aplicação, persistência, REST e integração.
- Preparação de tipos de auditoria para alvos do catálogo.

## Decisões Funcionais e Arquiteturais

- O catálogo é global e controlado pelo sistema.
- Não há CRUD público para catálogos auxiliares, exercises ou exercise muscle targets.
- A API atual expõe apenas endpoints de consulta.
- Registros expostos pelas listagens são filtrados por `active = true`.
- Catálogos auxiliares usam `code` estável e `displayName` para apresentação.
- Catálogos auxiliares usam `sortOrder` para ordenação previsível.
- `Exercise` não possui código natural nesta release; a seed de targets resolve exercícios por `name + equipment_type.code + activity_type.code`.
- `ExerciseMuscleTarget` associa um exercise a um subgrupo muscular com papel `PRIMARY`, `SECONDARY` ou `STABILIZER`.
- A listagem usa `Specification` para filtros combinados.
- Consultas simples usam derived queries do Spring Data JPA.
- Consultas de progresso de body metrics continuam usando JPQL com projection própria.
- A auditoria operacional de catálogo fica preparada para fluxos futuros de manutenção, pois a API atual não possui operações de escrita.

## Endpoints

Base path:

```http
/api/exercise-catalog
```

Catálogos auxiliares:

- `GET /api/exercise-catalog/activity-types`
- `GET /api/exercise-catalog/equipment-types`
- `GET /api/exercise-catalog/muscle-groups`
- `GET /api/exercise-catalog/muscle-subgroups`
- `GET /api/exercise-catalog/muscle-subgroups?muscleGroupId={id}`

Exercises:

- `GET /api/exercise-catalog/exercises`
- `GET /api/exercise-catalog/exercises/{id}`

## Filtros e Paginação

`GET /api/exercise-catalog/exercises` aceita:

- `page`
- `size`
- `name`
- `activityTypeId`
- `equipmentTypeId`
- `muscleGroupId`
- `muscleSubgroupId`
- `targetRole`

`targetRole` aceita:

- `PRIMARY`
- `SECONDARY`
- `STABILIZER`

A ordenação atual da listagem é:

```text
name ASC, id ASC
```

## Banco de Dados

Migrations relacionadas:

- `V6__create_tabela_activity_types.sql`
- `V7__create_table_equipment_types.sql`
- `V8__create_table_muscle_groups.sql`
- `V9__create_table_muscle_subgroups.sql`
- `V10__create_table_exercises.sql`
- `V11__create_table_exercise_muscle_targets.sql`
- `V12__seed_activity_types.sql`
- `V13__seed_equipment_types.sql`
- `V14__seed_muscle_groups.sql`
- `V15__seed_muscle_subgroups.sql`
- `V16__seed_exercises.sql`
- `V17__seed_exercise_muscle_targets.sql`

Modelo central:

```text
activity_types -> exercises
equipment_types -> exercises
muscle_groups -> muscle_subgroups -> exercise_muscle_targets -> exercises
```

## Documentação

Documentação técnica principal:

- [Catálogo de Exercises](../../exercises/README.md)
- [Estratégia de Filtragem](../../filtering/README.md)

Documentações relacionadas:

- [Banco de Dados e Migrations](../../database/README.md)
- [Swagger/OpenAPI](../../swagger/README.md)
- [Arquitetura](../../architecture/README.md)
- [Testes Automatizados](../../testing/README.md)
- [Releases](../README.md)

## Validação

Validações locais recomendadas para esta release:

```bash
./mvnw test --batch-mode
./mvnw clean verify --batch-mode
```

Validações manuais esperadas:

- Swagger UI exibindo tags de exercise catalog.
- `/v3/api-docs` contendo schemas e operações do catálogo.
- Catálogos auxiliares retornando apenas registros ativos.
- Listagem de exercises respeitando paginação, ordenação e filtros combinados.
- Consulta por id retornando detalhe com `muscleTargets`.
- Banco limpo aplicando migrations e seeds de catálogo sem erro.

<p align="right"><a href="../README.md">Voltar para releases</a></p>
