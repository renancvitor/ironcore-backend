# Exceptions e Tratamento de Erros REST

## Propósito

O projeto separa exceptions por camada para manter explícitas as regras de domínio, decisões de aplicação e falhas de infraestrutura.

O repositório inclui classes de exception, um handler REST global e factories de resposta para padronizar falhas expostas pela API. Esta documentação descreve a política transversal de erros; os documentos de cada módulo devem detalhar cenários específicos quando necessário.

## Exceptions de Domain

Tipo base:

- `DomainException`

Exceptions de domínio atuais:

- `InvalidUserException`
- `InvalidEmailException`
- `InvalidPasswordException`
- `InvalidBodyMetricException`
- `InvalidAuditLogException`
- `InvalidErrorLogException`

Use exceptions de domínio para estado inválido de domínio, value objects inválidos e invariantes de negócio que pertencem ao modelo de domínio. Exceptions de domínio não devem depender de Spring ou HTTP.

## Exceptions de Application

Tipo base:

- `ApplicationException`

Exceptions de application atuais:

- `ResourceAlreadyExistsException`
- `ResourceNotFoundException`
- `OperationNotAllowedException`
- `BusinessRuleViolationException`
- `InvalidCredentialsException`
- `UserInactiveException`
- `InitialPasswordChangeRequiredException`

Use exceptions de application para decisões em nível de use case, como recursos ausentes, recursos duplicados ou regras de operação que coordenam domínio e persistência.

## Exceptions de Infrastructure

Tipo base:

- `InfrastructureException`

Exceptions de infrastructure atuais:

- `PersistenceException`
- `DataMappingException`
- `ExternalServiceException`
- `JsonSerializationException`
- `JwtTokenException`
- `JwtTokenConfigurationException`
- `JwtTokenGenerationException`
- `JwtTokenValidationException`

Use exceptions de infrastructure para falhas técnicas relacionadas a persistência, mapping, serialização, segurança/JWT ou serviços externos.

## Tratamento REST

`GlobalExceptionHandler` está implementado com `@RestControllerAdvice`.

Ele mapeia categorias conhecidas de exceptions para respostas HTTP e publica error logs por meio de `ErrorLogPublisher`.

Models de resposta atuais:

- `ApiErrorResponse`
- `FieldErrorResponse`

Factories atuais:

- `ApiErrorResponseFactory`
- `FieldErrorResponseFactory`

O handler cobre exceptions de domínio, exceptions de application, erros de validação, métodos HTTP/media types não suportados, rotas ausentes, falhas de persistência, falhas de JWT, falhas de infraestrutura e exceptions inesperadas.

## Mapeamentos Relevantes

| Exception | Status | Mensagem exposta |
|---|---|---|
| `InvalidCredentialsException` | `401 Unauthorized` | `Credenciais incorretas.` |
| `InitialPasswordChangeRequiredException` | `401 Unauthorized` | Mensagem da exception. |
| `JwtTokenValidationException` | `401 Unauthorized` | `Token de autenticação inválido ou expirado.` |
| `UserInactiveException` | `403 Forbidden` | Mensagem da exception. |
| `ResourceNotFoundException` | `404 Not Found` | Mensagem da exception. |
| `BusinessRuleViolationException` | `422 Unprocessable Entity` | Mensagem da exception. |
| `OperationNotAllowedException` | `422 Unprocessable Entity` | Mensagem da exception. |
| `JwtTokenConfigurationException` | `500 Internal Server Error` | `Erro interno ao processar autenticação.` |
| `JwtTokenGenerationException` | `500 Internal Server Error` | `Erro interno ao processar autenticação.` |

## Uso por Módulos

Cada módulo deve reaproveitar as categorias existentes sempre que a semântica for suficiente:

- `ResourceNotFoundException` para recurso inexistente no escopo permitido.
- `DomainException` ou `IllegalArgumentException` para value objects inválidos, conforme o padrão já existente.
- `BusinessRuleViolationException` ou `OperationNotAllowedException` somente quando houver regra de aplicação que não seja simples validação de valor.
- `PersistenceException` para falhas técnicas de persistência.

Crie uma exception nova apenas quando houver uma semântica recorrente que não esteja bem representada pelas categorias atuais.

## Limitações Atuais

- O error logging está implementado, mas depende de uma requisição chegar ao REST exception handler ou de publicação explícita de error log.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
