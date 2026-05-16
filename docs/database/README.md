# Banco de Dados e Migrations

## Status Atual

**Status:** atual para a `v0.1.0`.

A camada de persistência funcional desta release é relacional e baseada em PostgreSQL, JPA/Hibernate e Flyway.

MongoDB está presente como dependência e serviço local no Docker, mas não há repositories MongoDB, modelos documentais ou fluxos funcionais de domínio usando MongoDB na `v0.1.0`.

## Banco Relacional

- Banco: PostgreSQL.
- Ferramenta de migration: Flyway.
- Caminho das migrations: `src/main/resources/db/migration`.
- Local padrão do Flyway: `classpath:db/migration`.

A configuração base da aplicação habilita o Flyway e usa `hibernate.ddl-auto=validate`.

## Migrations Atuais

| Arquivo | Tabela | Finalidade |
|---|---|---|
| `V1__create_table_users.sql` | `users` | Armazena dados iniciais de usuário: nome, email, hash de senha, sexo, flags de ciclo de vida e timestamps. |
| `V2__create_table_user_body_metrics.sql` | `user_body_metrics` | Armazena medições corporais do usuário, valores calculados de composição corporal e observações. |
| `V3__create_table_audit_log.sql` | `audit_logs` | Armazena registros de auditoria para ações relevantes, incluindo actor, action, target e snapshots opcionais. |
| `V4__create_table_error_log.sql` | `error_logs` | Armazena registros técnicos de erro com error code, message, exception class, contexto HTTP, user id opcional e correlation id. |

## Observações Sobre os Diagramas

Alguns diagramas descrevem um modelo de dados planejado mais amplo, incluindo exercícios, ciclos de treino, dias de treino e atividades de treino. Essas tabelas são blueprint/escopo futuro e não são criadas pelas migrations atuais.

Para a `v0.1.0`, somente as quatro tabelas listadas acima devem ser tratadas como estado de banco implementado.

## Serviços Locais

`docker-compose.dev.yml` starts:

- PostgreSQL 17
- MongoDB 7

PostgreSQL é usado pela persistência funcional atual. MongoDB está preparado para trabalho futuro e ainda não deve ser descrito como módulo funcional.

## Observação Sobre o Perfil de Produção

`application-prod.yml` atualmente desabilita o Flyway. Isso deve ser revisado antes de um deploy real em produção. Para a release técnica de fundação `v0.1.0`, isso é uma limitação conhecida de configuração, não uma decisão validada de produção.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
