# v0.3.0 - User body metrics funcional

## Resumo

`v0.3.0` prepara a documentação de release para o fechamento funcional do módulo de user body metrics.

Esta referência está associada ao milestone GitHub `v6.0.0`, cujo foco é entregar o módulo de métricas corporais em estado production-ready dentro do escopo atual do projeto: regras de domínio consolidadas, persistência, endpoints REST autenticados, acompanhamento de progresso, testes automatizados e documentação atualizada.

## Escopo Entregue

- Domínio de `UserBodyMetrics`.
- Value objects para peso, altura, circunferências, BMI, percentual de gordura, massa gorda e massa magra.
- Cálculo obrigatório de BMI.
- Cálculo opcional de percentual de gordura corporal.
- Cálculo opcional de massa gorda e massa magra.
- Persistência relacional em `user_body_metrics`.
- Repository contract no domínio e adapters JPA em infrastructure.
- Use cases de criação, atualização, exclusão, consulta por id, consulta do registro mais recente, listagem paginada e progresso.
- Endpoints REST autenticados em `/api/users/me/body-metrics`.
- Endpoints de progresso para composição corporal, circunferências, gordura corporal e resumo de mudanças.
- Audit log para criação, atualização e exclusão de métricas corporais.
- Testes automatizados cobrindo domínio, aplicação, persistência e REST.
- Documentação técnica atualizada do módulo.

## Decisões Funcionais

- Apenas `weightKg` e `heightCm` são obrigatórios nos requests de criação e atualização.
- Circunferências são opcionais e permitem medições parciais.
- `measuredAt` é definido pelo backend no momento da criação usando `Clock`.
- O cliente não informa `measuredAt`.
- BMI é calculado sempre que peso e altura são válidos.
- Percentual de gordura só é calculado quando houver dados suficientes para a fórmula Navy.
- Massa gorda e massa magra só são calculadas quando houver percentual de gordura.
- Campos opcionais ausentes retornam `null` nos resultados correspondentes.
- Os fluxos operam sempre no escopo do usuário autenticado.

## Endpoints

Base path:

```http
/api/users/me/body-metrics
```

Endpoints principais:

- `POST /api/users/me/body-metrics`
- `PUT /api/users/me/body-metrics/{id}`
- `DELETE /api/users/me/body-metrics/{id}`
- `GET /api/users/me/body-metrics`
- `GET /api/users/me/body-metrics/{id}`
- `GET /api/users/me/body-metrics/latest`
- `GET /api/users/me/body-metrics/progress/body-composition`
- `GET /api/users/me/body-metrics/progress/circumferences`
- `GET /api/users/me/body-metrics/progress/body-fat`
- `GET /api/users/me/body-metrics/progress/changes`

## Documentação

Documentação técnica principal:

- `docs/user-body-metrics/README.md`

Documentações relacionadas:

- `docs/architecture/README.md`
- `docs/database/README.md`
- `docs/project-structure/README.md`
- `docs/testing/README.md`
- `docs/releases/README.md`

## Validação

Validação local desta atualização documental:

```bash
./mvnw test --batch-mode
```

Resultado medido em `2026-06-29`:

```text
Tests run: 292, Failures: 0, Errors: 0, Skipped: 0
```

<p align="right"><a href="../README.md">Voltar para releases</a></p>
