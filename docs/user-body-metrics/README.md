# Documentação de User Body Metrics

## Propósito

O módulo `userbodymetrics` registra medições corporais do usuário autenticado e calcula indicadores de composição corporal.

O módulo possui fluxo funcional com domínio, use cases, persistência relacional, endpoints REST autenticados, consultas paginadas, consulta do registro mais recente e endpoints de progresso.

## Escopo Atual

Implementado:

- `UserBodyMetrics` domain model.
- Value objects de medições corporais e valores calculados.
- Calculadoras de BMI, percentual de gordura, massa gorda e massa magra.
- `UserBodyMetricsRepository` como contrato de domínio.
- Adapters JPA para persistência e consultas específicas.
- Use cases de criação, atualização, exclusão, consulta por id, consulta do último registro, listagem paginada e progresso.
- Controller REST autenticado em `/api/users/me/body-metrics`.
- Auditoria para criação, atualização e exclusão.
- Testes automatizados de domínio, aplicação, persistência e REST.

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

Services de domínio:

- `BMICalculator`
- `NavyBodyFatCalculator`
- `FatMassCalculator`
- `LeanMassCalculator`

Componente de aplicação:

- `BodyFatPercentageCalculator`

## Campos

### Obrigatórios

| Campo | Request | Domínio | Observação |
|---|---|---|---|
| Peso | `weightKg` | `BodyWeightKg` | Deve ser maior que zero. |
| Altura | `heightCm` | `BodyHeightCm` | Deve ser maior que zero. |

### Opcionais

| Campo | Request | Observação |
|---|---|---|
| Circunferências | `circumferences` | Objeto opcional. Quando informado, cada medida deve ser positiva. |
| Pescoço | `circumferences.neckCm` | Usado no cálculo opcional de percentual de gordura. |
| Peitoral | `circumferences.chestCm` | Medida histórica e de progresso. |
| Ombro | `circumferences.shoulderCm` | Medida histórica e de progresso. |
| Braço | `circumferences.armCm` | Medida histórica e de progresso. |
| Antebraço | `circumferences.forearmCm` | Medida histórica e de progresso. |
| Cintura | `circumferences.waistCm` | Usada no cálculo opcional de percentual de gordura. |
| Quadril | `circumferences.hipCm` | Obrigatório para cálculo de percentual de gordura feminino. |
| Coxa | `circumferences.thighCm` | Medida histórica e de progresso. |
| Panturrilha | `circumferences.calfCm` | Medida histórica e de progresso. |
| Observações | `notes` | Texto livre opcional. |

`measuredAt` é definido pelo backend no momento da criação usando `Clock`. O cliente não envia essa data no request.

## Cálculos

### BMI

O BMI é calculado sempre que há peso e altura válidos:

```text
bmi = weightKg / heightMeters^2
```

Como peso e altura são obrigatórios nos requests de criação e atualização, o BMI é parte do resultado funcional do módulo.

### Body Fat Percentage

O percentual de gordura corporal é opcional.

O cálculo usa a fórmula Navy e depende de dados suficientes:

- usuários masculinos: altura, pescoço e cintura;
- usuários femininos: altura, pescoço, cintura e quadril.

Quando os dados necessários não forem informados, `bodyFatPercentage` fica `null`. Isso é comportamento esperado, pois medições parciais são permitidas.

### Fat Mass e Lean Mass

Massa gorda e massa magra também são opcionais.

- `fatMassKg` é calculada quando existe `bodyFatPercentage`.
- `leanMassKg` é calculada quando existe `fatMassKg`.

Quando o percentual de gordura não puder ser calculado, `fatMassKg` e `leanMassKg` ficam `null`.

## Persistência

Tabela:

- `user_body_metrics`

Migration:

- `V2__create_table_user_body_metrics.sql`

Componentes principais:

- `UserBodyMetricsEntity`
- `UserBodyMetricsMapper`
- `UserBodyMetricsJpaRepository`
- `UserBodyMetricsRepositoryAdapter`
- `ListUserBodyMetricsQueryAdapter`
- `BodyMetricsProgressQueryAdapter`

O domínio depende do contrato `UserBodyMetricsRepository`. As implementações JPA permanecem em `infrastructure`.

## Endpoints REST

Todos os endpoints abaixo exigem usuário autenticado por cookie `access_token`.

Base path:

```http
/api/users/me/body-metrics
```

### Criar Medição

```http
POST /api/users/me/body-metrics
```

Request:

```json
{
  "weightKg": 82.4,
  "heightCm": 178.0,
  "circumferences": {
    "neckCm": 39.0,
    "chestCm": 104.0,
    "shoulderCm": 118.0,
    "armCm": 36.0,
    "forearmCm": 29.0,
    "waistCm": 86.0,
    "hipCm": 98.0,
    "thighCm": 61.0,
    "calfCm": 39.0
  },
  "notes": "Medição em jejum."
}
```

Resposta:

- `201 Created`

```json
{
  "id": 1,
  "userId": 1,
  "measuredAt": "2026-06-29T08:30:00",
  "weightKg": 82.4,
  "heightCm": 178.0,
  "circumferences": {
    "neckCm": 39.0,
    "chestCm": 104.0,
    "shoulderCm": 118.0,
    "armCm": 36.0,
    "forearmCm": 29.0,
    "waistCm": 86.0,
    "hipCm": 98.0,
    "thighCm": 61.0,
    "calfCm": 39.0
  },
  "bmi": 26.01,
  "bodyFatPercentage": 19.2,
  "fatMass": 15.82,
  "leanMass": 66.58,
  "notes": "Medição em jejum."
}
```

Request mínimo válido:

```json
{
  "weightKg": 82.4,
  "heightCm": 178.0
}
```

Nesse caso, a resposta inclui `bmi`, mas `bodyFatPercentage`, `fatMass` e `leanMass` podem retornar `null`.

### Atualizar Medição

```http
PUT /api/users/me/body-metrics/{id}
```

Request:

```json
{
  "weightKg": 81.9,
  "heightCm": 178.0,
  "circumferences": {
    "neckCm": 39.0,
    "waistCm": 85.0
  },
  "notes": "Correção da medição anterior."
}
```

Resposta:

- `200 OK`

```json
{
  "id": 1,
  "userId": 1,
  "measuredAt": "2026-06-29T08:30:00",
  "weightKg": 81.9,
  "heightCm": 178.0,
  "circumferences": {
    "neckCm": 39.0,
    "chestCm": null,
    "shoulderCm": null,
    "armCm": null,
    "forearmCm": null,
    "waistCm": 85.0,
    "hipCm": null,
    "thighCm": null,
    "calfCm": null
  },
  "bmi": 25.85,
  "bodyFatPercentage": 18.7,
  "fatMassKg": 15.32,
  "leanMassKg": 66.58,
  "notes": "Correção da medição anterior.",
  "updatedAt": "2026-06-29T09:10:00"
}
```

### Excluir Medição

```http
DELETE /api/users/me/body-metrics/{id}
```

Resposta:

- `204 No Content`

A exclusão respeita o escopo do usuário autenticado. Uma medição inexistente ou pertencente a outro usuário deve ser tratada como recurso não encontrado no fluxo de aplicação.

### Listar Medições

```http
GET /api/users/me/body-metrics?page=0&size=20
```

Parâmetros:

| Parâmetro | Obrigatório | Padrão | Regra |
|---|---:|---:|---|
| `page` | Não | `0` | Mínimo `0`. |
| `size` | Não | `20` | Mínimo `1`, máximo `100`. |

Resposta:

- `200 OK`

```json
{
  "metrics": {
    "content": [
      {
        "id": 1,
        "measuredAt": "2026-06-29T08:30:00",
        "weightKg": 82.4,
        "heightCm": 178.0,
        "notes": "Medição em jejum."
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

### Buscar Medição por Id

```http
GET /api/users/me/body-metrics/{id}
```

Resposta:

- `200 OK`

O payload segue o formato completo de consulta, com `id`, `userId`, `measuredAt`, peso, altura, circunferências, cálculos, observações e `updatedAt`.

### Buscar Última Medição

```http
GET /api/users/me/body-metrics/latest
```

Resposta:

- `200 OK`

Retorna a medição mais recente do usuário autenticado, ordenada por `measuredAt`.

### Progresso de Composição Corporal

```http
GET /api/users/me/body-metrics/progress/body-composition?startDate=2026-01-01&endDate=2026-06-29
```

Inclui séries de:

- `WEIGHT_KG`
- `FAT_MASS_KG`
- `LEAN_MASS_KG`

### Progresso de Circunferências

```http
GET /api/users/me/body-metrics/progress/circumferences?startDate=2026-01-01&endDate=2026-06-29
```

Inclui séries de circunferências corporais com valores disponíveis.

### Progresso de Gordura Corporal

```http
GET /api/users/me/body-metrics/progress/body-fat?startDate=2026-01-01&endDate=2026-06-29
```

Inclui série de:

- `BODY_FAT_PERCENTAGE`

### Resumo de Mudanças

```http
GET /api/users/me/body-metrics/progress/changes?startDate=2026-01-01&endDate=2026-06-29
```

Calcula primeira medição, última medição, variação absoluta e variação percentual para métricas com pelo menos dois pontos válidos.

Exemplo de resposta:

```json
{
  "startDate": "2026-01-01",
  "endDate": "2026-06-29",
  "changes": [
    {
      "metric": "WEIGHT_KG",
      "label": "Peso",
      "unit": "kg",
      "firstDate": "2026-01-10",
      "firstValue": 84.0,
      "lastDate": "2026-06-20",
      "lastValue": 80.5,
      "absoluteChange": -3.5,
      "percentageChange": -4.1666666667
    }
  ]
}
```

Regras comuns dos endpoints de progresso:

- `startDate` e `endDate` são obrigatórios.
- As datas não podem ser futuras.
- `startDate` não pode ser maior que `endDate`.
- O período máximo permitido é de 12 meses.
- Séries e mudanças ignoram valores `null` ou não positivos.
- Os gráficos agregam os dados por mês e usam o último ponto válido de cada mês.

## Regras de Aplicação

- A medição sempre pertence ao usuário autenticado.
- Usuário inexistente gera erro de recurso não encontrado.
- Usuário inativo não pode operar métricas corporais.
- Peso e altura são obrigatórios para criação e atualização.
- Medições parciais são permitidas.
- Composição corporal só é calculada quando houver dados suficientes.
- Criação, atualização e exclusão publicam audit log.

## Exceptions Relevantes

| Cenário | Categoria |
|---|---|
| Peso, altura ou circunferência inválida | `DomainException` / validação REST |
| Usuário inexistente | `ResourceNotFoundException` |
| Medição inexistente no escopo do usuário | `ResourceNotFoundException` |
| Usuário inativo | `UserInactiveException` |
| Período de progresso inválido | `BusinessRuleViolationException` ou `OperationNotAllowedException` |
| Falha técnica de persistência | `PersistenceException` |

O tratamento HTTP final é centralizado em `GlobalExceptionHandler`.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
