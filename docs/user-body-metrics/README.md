# Documentação de User Body Metrics

## Propósito

O módulo `userbodymetrics` representa medições físicas e cálculos de composição corporal de um usuário.

Na `v0.1.0`, o model de domínio, value objects, calculadoras e tabela relacional estão presentes. Ainda não há endpoints REST nem recursos visuais de histórico/dashboard.

## Escopo Atual

Implementado:

- `UserBodyMetrics` domain model.
- Value objects for body measurements and calculated values.
- BMI calculation.
- Navy body fat percentage calculation.
- Fat mass calculation.
- Lean mass calculation.
- Relational persistence table.

Não implementado:

- REST endpoints for creating or querying metrics.
- Dashboard or visual history.
- Integration with workout sessions.
- Repository adapter for `UserBodyMetrics` domain operations.

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

## Persistência

Tabela:

- `user_body_metrics`

Migration:

- `V2__create_table_user_body_metrics.sql`

Classe de persistência atual:

- `UserBodyMetricsEntity`

Ainda não há repository/adapter completo de domínio para este módulo na `v0.1.0`.

## Limitações Atuais

- Nenhum endpoint REST de negócio expõe operações de body metrics.
- Não há histórico ou dashboard voltado ao usuário.
- Os cálculos existem em services de domínio, mas ainda não há fluxo público de API usando esses cálculos.
- O módulo não está integrado a recursos de workout/session.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
