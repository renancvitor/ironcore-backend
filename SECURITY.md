# Política de Segurança

## Estado do projeto

O IronCore Backend está em desenvolvimento ativo e ainda não representa uma versão estável destinada a uso em produção.

A segurança continua sendo tratada como parte da arquitetura do projeto, especialmente nos fluxos relacionados a:

- autenticação;
- autorização;
- JWT;
- cookies;
- senhas;
- ownership por `PersonId`;
- métricas corporais;
- persistência;
- logs;
- contratos REST;
- dependências externas.

## Versões suportadas

Correções de segurança são aplicadas somente ao estado mais recente da branch `main`.

| Versão | Suporte |
|---|---|
| `main` | Suportada |
| Releases históricas | Não suportadas |
| Branches de desenvolvimento | Não suportadas |
| Forks externos | Não suportados |

Como o projeto ainda está em evolução, versões históricas podem não receber backports de correções.

## Reportando uma vulnerabilidade

Não abra uma Issue pública contendo detalhes de uma vulnerabilidade explorável.

Utilize o recurso privado de reporte de vulnerabilidades do GitHub:

https://github.com/renancvitor/ironcore-backend/security/advisories/new

O relatório deve conter, sempre que possível:

- descrição da vulnerabilidade;
- componente, endpoint ou fluxo afetado;
- branch, release ou commit analisado;
- pré-condições necessárias;
- passos para reprodução;
- impacto esperado;
- prova de conceito segura;
- logs ou respostas relevantes;
- sugestão de correção, caso exista.

Remova do relatório:

- credenciais reais;
- tokens válidos;
- cookies de autenticação;
- segredos;
- dados pessoais;
- informações pertencentes a terceiros.

Caso o reporte privado do GitHub não esteja disponível, entre em contato com o responsável pelo repositório por um canal privado indicado no perfil GitHub de [@renancvitor](https://github.com/renancvitor).

Não publique detalhes técnicos da vulnerabilidade enquanto ela estiver em análise.

## O que pode ser considerado uma vulnerabilidade

Exemplos relevantes para o projeto:

- bypass de autenticação;
- bypass de autorização;
- acesso indevido a dados pertencentes a outra `Person`;
- validação incorreta de ownership por `PersonId`;
- falsificação, reutilização ou validação incorreta de JWT;
- exposição de cookie ou token de autenticação;
- armazenamento ou exposição de senha em texto puro;
- exposição de hash de senha;
- injeção SQL;
- injeção NoSQL;
- mass assignment;
- falhas de validação que permitam quebrar invariantes de domínio;
- exposição de dados pessoais ou métricas corporais;
- vazamento de segredos em código, configuração, logs ou respostas;
- configuração insegura de CORS, CSRF ou cookies;
- acesso não autorizado ao Swagger/OpenAPI em cenários onde deveria existir restrição;
- logs contendo tokens, senhas ou dados sensíveis;
- dependência com vulnerabilidade conhecida e impacto demonstrável no projeto;
- endpoint que permita executar uma operação fora do escopo do usuário autenticado.

## Modelo de segurança atual

O IronCore utiliza atualmente um baseline de autenticação single-user.

Fazem parte desse modelo:

- bootstrap controlado de pessoa e usuário;
- autenticação com JWT;
- utilização de cookie `access_token`;
- troca obrigatória da senha inicial;
- troca normal de senha;
- acesso autenticado aos dados da pessoa;
- ownership de métricas corporais por `PersonId`.

Algumas funcionalidades comuns em aplicações multiusuário estão fora do escopo atual, incluindo:

- cadastro público;
- recuperação de senha;
- refresh token;
- blacklist de JWT;
- roles;
- permissões administrativas;
- gerenciamento público de usuários.

A ausência dessas funcionalidades, por si só, não representa uma vulnerabilidade. Um relatório deve diferenciar limitações intencionais do escopo atual de falhas em funcionalidades efetivamente implementadas.

## Fora de escopo

Geralmente não são considerados vulnerabilidades:

- ausência de funcionalidade ainda documentada como planejada;
- problemas exclusivos de branches não integradas;
- vulnerabilidades sem impacto demonstrável;
- recomendações genéricas sem relação com o código;
- alertas automáticos sem confirmação de explorabilidade;
- ataques que exijam acesso prévio total à máquina ou ao banco de dados;
- engenharia social;
- negação de serviço causada apenas por volume extremo de requisições, sem falha específica demonstrável;
- informações já públicas no próprio repositório;
- uso inseguro resultante exclusivamente de alteração deliberada das configurações recomendadas.

Esses casos ainda podem ser discutidos como melhoria técnica por meio de uma Issue comum, desde que nenhum detalhe sensível seja exposto.

## Processo de análise

Após o recebimento de um relatório:

1. O conteúdo será analisado para confirmação e classificação.
2. Poderão ser solicitadas informações adicionais.
3. Caso confirmado, será preparada uma correção.
4. Testes serão adicionados ou atualizados para prevenir regressão.
5. A documentação afetada será revisada.
6. A divulgação pública ocorrerá apenas quando for considerada segura.

Não existe atualmente um SLA formal de resposta ou correção.

O projeto é mantido individualmente e em caráter de desenvolvimento e aprendizado. Relatórios responsáveis serão analisados conforme a disponibilidade do mantenedor.

## Divulgação responsável

Espera-se que o responsável pelo relatório:

- mantenha os detalhes em sigilo durante a análise;
- não explore a vulnerabilidade além do necessário para comprová-la;
- não acesse, altere ou remova dados de terceiros;
- não interrompa serviços;
- não publique provas de conceito antes da correção;
- conceda tempo razoável para investigação.

Relatórios realizados de boa-fé e respeitando estas orientações serão tratados de forma colaborativa.

## Créditos

Quando apropriado e autorizado pelo responsável pelo relatório, a contribuição poderá ser reconhecida na correção, no advisory ou nas notas da release.
