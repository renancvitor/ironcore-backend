# Documentação de User Body Metrics

## Propósito

O módulo `userbodymetrics` representa medições físicas e cálculos de composição corporal de um usuário.

No estado atual do repositório, este módulo possui base de domínio, value objects, calculadoras e tabela relacional. Ele ainda não possui fluxo REST funcional, use cases de aplicação ou repository/adapter completo para operações de métricas corporais.

## Escopo Atual

Base existente:

- `UserBodyMetrics` domain model.
- Value objects de medições corporais e valores calculados.
- Calculadoras de BMI, percentual de gordura, massa gorda e massa magra.
- Exception de domínio para métricas inválidas.
- Tabela relacional `user_body_metrics`.
- Entidade JPA `UserBodyMetricsEntity`.

## Conceitos de Domínio

Model principal:

- `UserBodyMetrics`

Value objects:

- `UserBodyMetricsId`
- `BodyWeightKg`
- `BodyHeightCm`
- `BodyCircumferenceCm`
- `BodyCircumferences`
- `BMI`
- `BodyFatPercentage`
- `FatMassKg`
- `LeanMassKg`

## Calculadoras

Services de domínio atuais:

- `BMICalculator`
- `NavyBodyFatCalculator`
- `FatMassCalculator`
- `LeanMassCalculator`

Cálculos implementados:

- BMI: `weightKg / heightMeters^2`.
- Navy body fat for male users: uses waist, neck and height converted to inches.
- Navy body fat for female users: uses waist, hip, neck and height converted to inches.
- Fat mass: `weightKg * bodyFatPercentage / 100`.
- Lean mass: `weightKg - fatMassKg`.

As calculadoras validam entradas obrigatórias e lançam `InvalidBodyMetricException` para valores inválidos ou ausentes.

## Exceptions

Exception de domínio atual:

- `InvalidBodyMetricException`

Ela representa valores inválidos, ausentes ou insuficientes para criação de medições e execução dos cálculos corporais.

## Persistência

Tabela:

- `user_body_metrics`

Migration:

- `V2__create_table_user_body_metrics.sql`

Classe de persistência atual:

- `UserBodyMetricsEntity`

Essa tabela existe no schema relacional, mas ainda não há repository/adapter completo de domínio para operações de criação, atualização, exclusão ou consulta de medições corporais.

## Limitações Atuais

- Nenhum endpoint REST de negócio expõe operações de body metrics.
- Não há use cases de aplicação para criar, atualizar, excluir ou consultar medições corporais.
- Não há repository/adapter completo de domínio para o módulo.
- Não há histórico ou dashboard voltado ao usuário.
- Os cálculos existem em services de domínio, mas ainda não há fluxo público de API usando esses cálculos.
- O módulo não está integrado a recursos de workout/session.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
