# Estratégia de Filtragem nas Listagens

## Visão Geral

O IronCore usa três estratégias principais para consultas e listagens, escolhidas conforme a complexidade do caso.

- Derived queries do Spring Data JPA para consultas simples e legíveis.
- JPQL com projection para consultas estáveis que precisam controlar campos retornados.
- Specifications para filtros combinados e dinâmicos.

## Derived Queries

Derived queries são usadas quando o filtro é simples, direto e expressivo pelo nome do método.

Exemplos atuais:

- `findByIdAndPerson_Id`
- `findFirstByPerson_IdOrderByMeasuredAtDesc`
- `findByPerson_IdOrderByMeasuredAtDescIdDesc`
- `findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc`
- `findAllByMuscleGroup_IdAndActiveTrueOrderBySortOrderAscDisplayNameAsc`
- `findByIdAndActiveTrue`

Esse estilo é adequado quando a combinação de campos não cresce demais e o método continua fácil de ler.

## JPQL

JPQL é usado quando a consulta precisa de seleção explícita de dados ou projection própria.

Exemplo atual:

- `BodyMetricsProgressJpaRepository.findProgressData`

Esse método projeta os campos necessários para gráficos e cálculos de progresso, evitando carregar dados além do necessário.

## Specifications

Specifications são usadas quando uma listagem precisa combinar filtros opcionais sem criar uma explosão de métodos no repository.

Exemplo atual:

- `ExerciseSpecifications.filter`
- `WorkoutCycleSpecification.filter`

A listagem de exercises combina:

- `name`
- `activityTypeId`
- `equipmentTypeId`
- `muscleGroupId`
- `muscleSubgroupId`
- `targetRole`

O adapter monta a `Specification`, aplica paginação e ordena por `name` e `id`.

### Workout cycles

`WorkoutCycleSpecification.filter` sempre restringe a consulta à `PersonId` resolvida do usuário autenticado. Ela combina opcionalmente:

- `workoutStatus`
- `trainingGoalId`
- `startDate`
- `endDate`
- `name`

O período usa sobreposição entre as datas do ciclo e a janela solicitada; ciclos sem `endDate` são considerados em aberto. `name` aplica busca parcial case-insensitive com escape de caracteres de `LIKE`. O adapter aplica `page` e `size` e ordena por `name` ascendente e `id` ascendente.

## Critério de Escolha

Use derived query quando:

- A consulta tem poucos critérios.
- A intenção fica clara no nome do método.
- Não há composição dinâmica relevante.

Use JPQL quando:

- A consulta possui projection específica.
- O formato de retorno precisa ser controlado.
- A query é estável e vale ser declarada explicitamente.

Use Specification quando:

- Há vários filtros opcionais.
- Os filtros podem ser combinados livremente.
- A consulta precisa atravessar relacionamentos ou usar subqueries.
- Criar derived queries para cada combinação deixaria o repository frágil.

## Estado Atual

- Body metrics usa derived queries para CRUD/listagem simples e JPQL para progresso.
- Exercise catalog usa derived queries para catálogos auxiliares e detalhe por id.
- Exercise catalog usa Specifications para listagem com filtros combinados.
- Workout planning usa `WorkoutCycleSpecification` para listagem de ciclos com ownership, filtros combináveis, paginação e ordenação.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
