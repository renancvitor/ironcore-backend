# v1.2.0 - Início da Semana Configurável nos Ciclos de Treino

## Resumo

A `v1.2.0` evolui o Workout Planning do IronCore Backend, introduzindo a configuração individual do dia inicial da semana em cada `WorkoutCycle`.

A funcionalidade permite definir qualquer dia da semana como referência para a apresentação dos treinos, mantendo a ordem natural dos dias e preservando as regras existentes de `weekDay` e `sortOrder`.

A atualização é retrocompatível, inclui persistência, auditoria, contratos REST/OpenAPI e migração Flyway, e já foi integrada ao frontend Angular em produção.

## Principais Mudanças

### Workout Planning

- Inclusão de `weekStartsOn` no domínio `WorkoutCycle`.
- Suporte aos sete valores do enum `WeekDay`, de `SUNDAY` a `SATURDAY`.
- Configuração do início da semana durante a criação de ciclos.
- Possibilidade de alterar a configuração em ciclos `NOT_STARTED` e `IN_PROGRESS`.
- Preservação das restrições de edição para ciclos `COMPLETED` e `CANCELLED`.
- Inclusão do campo nos Commands, Results, projeções e mappers.
- Inclusão de `weekStartsOn` nos snapshots de auditoria.
- Preservação das regras existentes para dias, atividades e lifecycle.

### Contratos REST e OpenAPI

- Inclusão de `weekStartsOn` no request de criação de `WorkoutCycle`.
- Inclusão do campo no request de atualização.
- Exposição do valor nas respostas de criação, atualização e consulta detalhada.
- Atualização dos schemas e descrições OpenAPI.
- Preservação da compatibilidade com clientes que não enviam o novo campo.

## Decisões Funcionais e Arquiteturais

- `weekStartsOn` pertence ao ciclo de treino, permitindo configurações independentes entre ciclos.
- Na criação, o campo omitido ou nulo assume `SUNDAY`.
- Na atualização, o campo omitido ou nulo preserva o valor existente.
- O backend mantém a ordenação canônica dos dias por `weekDay` e `sortOrder`.
- A ordenação circular para apresentação é responsabilidade do frontend.
- A configuração não modifica o `weekDay` persistido de nenhuma sessão.
- `sortOrder` continua representando a posição entre sessões do mesmo dia da semana.
- Não foram introduzidas mudanças na estratégia de filtragem ou nas regras de composição de treinos.

## Banco de Dados

Nova migration Flyway:

`V23__add_column_week_starts_on_to_workout_cycles.sql`

A migration adiciona a coluna `week_starts_on` à tabela `workout_cycles`, utilizando `INTEGER NOT NULL DEFAULT 1`.

O valor `1` corresponde a `SUNDAY`, garantindo a compatibilidade dos ciclos existentes.

A persistência reutiliza o `WeekDayConverter`, mantendo a representação numérica do enum.

Não é necessário alterar ou regravar os registros de `WorkoutDay` para aplicar a nova configuração.

## Testes e Validação

Foram ampliados os testes automatizados para cobrir:

- Valor padrão na criação.
- Alteração e preservação de `weekStartsOn`.
- Regras de edição conforme o status do ciclo.
- Persistência e conversão do enum.
- Projeções de consulta detalhada.
- Contratos REST e mapeamentos.
- Auditoria.
- Compatibilidade com requisições anteriores.
- Preservação da ordenação canônica dos dias.

Validação automatizada:

- `./mvnw clean verify --batch-mode`
- **945 testes executados.**
- **0 falhas, 0 erros e 0 testes ignorados.**
- Build e CI aprovados.

A integração funcional com o frontend também foi validada em produção.

## Compatibilidade e Limitações

A atualização preserva o comportamento dos ciclos existentes e não exige alterações nos clientes que ainda não enviam `weekStartsOn`.

A configuração determina apenas o ponto inicial da semana para apresentação, não permitindo reordenação arbitrária dos dias.

A release não introduz execução de Workout Sessions, histórico de sessões realizadas ou geração de treinos com IA.

## Documentação

Documentação técnica relacionada:

- `README.md`
- `docs/workout-planning/README.md`
- `docs/database/README.md`
- `docs/project-structure/README.md`

O histórico das releases anteriores permanece preservado.

A `v1.2.0` amplia a flexibilidade do planejamento de treinos, mantendo a integridade dos dados, a compatibilidade dos contratos e a separação de responsabilidades entre backend e frontend.

## Fonte Histórica

Registro documental da [release oficial](https://github.com/renancvitor/ironcore-backend/releases/tag/v1.2.0), publicada em 2026-10-24. O conteúdo acima preserva as notas publicadas; resultados de validação ali citados pertencem àquela release e não são uma nova execução.

<p align="right"><a href="../README.md">Voltar para releases</a></p>

