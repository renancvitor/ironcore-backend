# Inventário Técnico Desde v1.2.0

## Base da Revisão

Inventário levantado antes das alterações documentais da Issue #317. HEAD local, `main` e `origin/main` conferidos em `b5b7a152f5023399aa375b85f20ae758746f8c07`, após atualização das referências remotas. A branch existente `317-docs-atualizar-documentação-do-workout-session` partia desse mesmo commit e não possuía alterações locais.

Comandos de referência:

```bash
git fetch origin --tags
git rev-parse HEAD main origin/main
git tag --sort=version:refname
git log --format="%h %cs %s" v1.2.0..main
git diff --stat v1.2.0...main
git diff --name-status v1.2.0...main
```

O delta contém **13 commits e 279 arquivos alterados**. Foram conferidos commits integrados (incluindo PRs com squash), arquivos de produção/testes/documentação e migrations. O milestone `v12.0.0` é referência de planejamento; autenticação posterior integra o escopo por estar no Git, independentemente do milestone. A cronologia inclui um ajuste documental no mesmo dia da release, mas posterior à tag.

## Cobertura por Commit

| Commit | Entrega integrada | Documentação correspondente |
|---|---|---|
| `d66a2d3` (#309) | Índice da V23 já existente na tag | Banco, estrutura e reconciliação da v1.2.0; não atribuir V23 ao próximo release |
| `df0cdb0` (#318) | Domínio, persistência, snapshots e V24–V26 de sessões/atividades/séries | Workout Session, arquitetura, estrutura, banco e ER |
| `a90bae7` (#319) | Início, detalhe, recuperação, ownership e concorrência com planejamento | Workout Session e Swagger |
| `37e4e9a` (#320) | Fechamento, expiração, scheduler e lifecycle | Workout Session, diagrama de sequência e deploy |
| `b1b8ab9` (#321) | Execução, relatos, séries, carga/modo, PATCH parcial e V27 | Workout Session, banco, erros e auditoria |
| `50e2839` (#322) | Histórico encerrado, filtros e paginação | Workout Session e filtragem |
| `872d509` (#323) | Evolução de força e cardio, normalização, volume e ausência de métricas | Workout Session e testes |
| `5a20acd` | Busca do histórico por nome de ciclo/dia capturado | Workout Session, filtragem e relação com planning |
| `783b097` | Título capturado do dia em detalhe/retornos de evolução | Workout Session e relação com planning |
| `d256a29` (#324) | Integração REST/PostgreSQL do fluxo completo de sessão | Workout Session e testes |
| `7ebf7b9` (#326, Issue #325) | Sessões persistidas, revogação, refresh/rotação, concorrência, rate limiting, credenciais e proxy; V28–V30 | Users/Auth, SECURITY, arquitetura, banco, deploy, Swagger, erros e testes |
| `91034fb` (#328, Issue #327) | Limpeza de sessões vencidas com retenção | Users/Auth, deploy e testes |
| `b5b7a15` (#330, Issue #329) | Inatividade de 30 dias, renovação por refresh e limite absoluto de 90 dias; V31 e transição | Users/Auth, banco, deploy e testes |

## Autoridades de Código Inspecionadas

- `domain/workoutsession`: aggregate, snapshots, invariantes das atividades/séries, `ExecutedLoadNormalizer` e `ExecutionMetricsCalculator`.
- `application/workoutsession`: início, consultas, fechamento, expiração, mutações, histórico e analytics; `WorkoutSessionAuditData`.
- `infrastructure/persistence/workoutsession`: entidades/mappers, locks e captura consistente do planejamento, índice de sessão em andamento, persistência do aggregate, projeções de histórico/analytics e escape da busca por nome.
- `interfaces/rest/workoutsession`: Controllers, APIs e DTOs, presença de campos em PATCH e serialização de métricas ausentes.
- `domain/auth`, `application/auth`, casos de uso de senha/nickname: emissão, rotação, locks/releituras, revogação e rollback.
- Segurança: validação de JWT/`sid`, filtro de autenticação, factories dos cookies, geração/hash de refresh, rate limiting, CSRF/CORS e configuração `prod` de proxy.
- Schedulers de treino/autenticação e queries de limpeza: expiração funcional e remoção física são operações distintas.
- Flyway V24–V31: nomes reais, constraints, índices, cascades, nulabilidade e backfill da expiração absoluta.
- Testes novos e adaptados: integração do aggregate, histórico/analytics em PostgreSQL, segurança REST, PATCH, expiração, limpeza, migração e concorrência de autenticação. Alterações em testes dos módulos existentes adaptam a identidade autenticada às sessões persistidas; não representam novos endpoints nesses módulos.

## Mapa Documental

Criados: módulo `docs/workout-session/`, histórico verificável de `v1.1.0`/`v1.2.0` e preparação `docs/releases/next/`.

Atualizados: README principal e índice técnico, arquitetura, estrutura, banco/migrations, users/auth, workout planning, Swagger/UI/api-docs, testes, deploy, filtragem, exceptions, auditoria, SECURITY e diagramas ER/sequência/índice. Informações extensas de regras e contratos ficam nos módulos; índices remetem a essas fontes.

Revisados sem mudança funcional nova identificada: Persons, Body Metrics, catálogo de exercícios, tecnologias/dependências, logging geral e blueprints de IA. Nenhuma release anterior existente foi reescrita; novos históricos reproduzem as respectivas notas oficiais, com identificação da fonte.

## Diferenças e Limites

- O índice local parava em v1.0.0, embora v1.1.0 (22/10/2026) e v1.2.0 (24/10/2026) já estivessem publicadas. Foram reconciliadas usando as releases oficiais, sem alterar GitHub Releases.
- `weekStartsOn` é conteúdo da v1.2.0, apesar de seu índice ter sido corrigido depois da tag.
- O título do commit `5a20acd` menciona planning, mas seus efeitos documentados são a busca do histórico de sessões; não se inferiu novo contrato de busca de ciclos.
- Documentos correntes ainda tratavam sessão, refresh e revogação como futuros; agora refletem o código. IA e integrações preparadas permanecem identificadas como futuras.
- A candidata v1.3.0 não é confirmada pelo nome do milestone; mudanças de contrato de autenticação devem ser avaliadas pelo mantenedor.
- Este inventário é fixado ao SHA acima. Novos commits na main exigem nova comparação antes de fechar o release.

<p align="right"><a href="README.md">Voltar para o próximo release</a></p>
