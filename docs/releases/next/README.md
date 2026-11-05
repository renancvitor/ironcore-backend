# Próximo Release — Workout Session e Autenticação Persistida

## Status e Referência

**Em preparação; não publicado.** A numeração candidata é `v1.3.0`, seguindo a sequência minor das releases oficiais v1.0.0, v1.1.0 e v1.2.0. A aprovação da versão permanece pendente: a remoção de tokens do JSON de login e a exigência de sessão persistida são mudanças de contrato que precisam ser consideradas antes de confirmar a numeração.

Base: última [release oficial v1.2.0](https://github.com/renancvitor/ironcore-backend/releases/tag/v1.2.0), publicada em 24/10/2026. Comparação: [`v1.2.0...main`](https://github.com/renancvitor/ironcore-backend/compare/v1.2.0...main), revisada no commit `b5b7a152f5023399aa375b85f20ae758746f8c07` em 04/11/2026. A Issue [#317](https://github.com/renancvitor/ironcore-backend/issues/317) organiza esta documentação; o milestone de referência `v12.0.0` não define a versão semântica nem substitui o histórico Git.

## Resumo

O delta entrega execução real de treinos com snapshots históricos, registros parciais, lifecycle, histórico paginado e indicadores de força/cardio. Também entrega sessões persistidas de autenticação, refresh token com rotação, logout no servidor, invalidação após troca de senha, limites de tentativas e rotinas de limpeza.

## Escopo Entregue Desde v1.2.0

- Aggregate `WorkoutSession -> WorkoutSessionActivity -> WorkoutSessionSet`, ownership por `PersonId` e snapshots independentes do planejamento.
- Início a partir de ciclo em andamento, recuperação e detalhe; no máximo uma sessão em andamento por pessoa.
- Encerramento manual e automático às 03h do dia seguinte à data local do início, em São Paulo, preservando execução parcial.
- Relatos opcionais, duração, distância, séries, repetições e modos `TOTAL`/`PER_SIDE`, sem assumir prescrição como execução.
- Correções após encerramento sem reabertura ou mudança dos snapshots e datas do lifecycle.
- Histórico de sessões encerradas com filtros, paginação, busca por nomes capturados e título do dia nos retornos.
- Indicadores por exercício de carga normalizada, volume, distância, duração, pace e velocidade média, com ausência explícita das métricas sem dados suficientes.
- Sessões de autenticação persistidas com `sid`, revogação no logout e após troca de senha, refresh de uso único por rotação e controle transacional de concorrência.
- Janela de 30 dias renovada somente por refresh válido, limitada a 90 dias desde a criação; transição conservadora para registros antigos.
- Limites de tentativas persistidos, proteção de credenciais, configuração restrita de proxy em produção e limpeza periódica de registros vencidos.
- Migrations V24–V31, testes de domínio, aplicação, REST, persistência, segurança e integração PostgreSQL dos fluxos novos e afetados.

O [inventário por commit](inventory.md) relaciona todas as alterações da comparação aos documentos e às evidências de código.

## Contratos e Compatibilidade

Rotas e exemplos: [Workout Session](../../workout-session/README.md) e [Users e Auth](../../users/README.md). Novos endpoints não alteram a prescrição dos ciclos; `weekStartsOn` já foi publicado em v1.2.0.

Login passa a retornar apenas `expiresAt`, `userId`, `email`, `nickname` e `mustChangePassword`; JWT e refresh são enviados em cookies seguros HttpOnly. Clientes que consumiam `accessToken`/`tokenType` do JSON precisam adaptar-se. Refresh requer cookie `refresh_token` e CSRF, sem exigir access token válido; clientes devem coordenar renovação concorrente. A troca normal de senha mantém `204`, mas emite nova sessão e revoga todas as anteriores.

JWT sem `sid` não é aceito: é necessário novo login após transição. A V29 permite hashes nulos em registros antigos. A V31 preenche o limite absoluto com a expiração previamente existente, sem conceder prazo novo. Sessões novas seguem 30 dias de inatividade/90 dias absolutos. A expiração do access token (padrão 120 minutos) e a retenção do registro não equivalem à validade da sessão.

## Banco e Operação

V24–V27 adicionam execução e registros parciais; V28–V31 adicionam autenticação persistida, refresh, contadores e expiração absoluta. Índice e constraints: [Banco de Dados](../../database/README.md).

Schedulers: expiração de treinos a cada 60 segundos; limpeza de sessões de autenticação diariamente às 03h de São Paulo, apenas quando `expires_at < agora - 15 dias`; limpeza de tentativas a cada cinco minutos. Revogação não antecipa a remoção física. O segredo JWT exige pelo menos 32 bytes UTF-8. Bind e confiança de proxy em produção passam a ser restritos a loopback.

O rollback do JAR não desfaz migrations nem dados persistidos. Não houve publicação de release, criação de tag ou alteração de release existente nesta preparação.

## Validação e Pendências

A revisão documental compara Controllers, APIs, DTOs, domínio, adapters, migrations e testes ao inventário completo. As verificações desta entrega são registradas no PR; resultados históricos das releases v1.1.0/v1.2.0 não são resultados do código atual.

Validação local desta preparação em 04/11/2026, sobre o código de `b5b7a152`:

- `mvnw.cmd clean verify --batch-mode`: **BUILD SUCCESS**, 1522 testes, zero falhas, erros ou ignorados, incluindo integrações PostgreSQL/Testcontainers.
- Conferência de 40 documentos Markdown e 247 referências relativas (links, imagens e âncoras), sem destinos ausentes; índice com as 31 migrations reais.
- `git diff --check` sem problemas e nenhuma alteração em Java, migrations, testes funcionais ou configuração de produção.

Não houve validação manual em navegador nem publicação em produção nesta tarefa. Os diagramas Mermaid foram revisados no fonte; a renderização no GitHub deve ser conferida na revisão do PR.

Antes da publicação:

- Confirmar versão semântica considerando as mudanças de autenticação e fechar o escopo sobre o HEAD definitivo da main.
- Verificar o resultado do CI e dos testes com Docker/Testcontainers disponível.
- Revisar a integração dos clientes com cookies, refresh coordenado, CSRF e novo JSON de login.

Geração por IA, persistência documental funcional, cadastro público, recuperação de senha e roles não são entregas deste delta. Não se declara nesta preparação que os novos fluxos foram publicados ou validados em produção.

<p align="right"><a href="../README.md">Voltar para releases</a></p>
