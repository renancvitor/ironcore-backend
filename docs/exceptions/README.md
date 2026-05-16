# Exceptions e Tratamento de Erros REST

## Propósito

O projeto separa exceptions por camada para manter explícitas as regras de domínio, decisões de aplicação e falhas de infraestrutura.

A release atual inclui classes de exception e um handler REST global, mas ainda não há controllers REST de negócio. Portanto, o handler está implementado como infraestrutura para endpoints futuros e já consegue tratar erros REST em nível de framework.

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

Use exceptions de application para decisões em nível de use case, como recursos ausentes, recursos duplicados ou regras de operação que coordenam domínio e persistência.

## Exceptions de Infrastructure

Tipo base:

- `InfrastructureException`

Exceptions de infrastructure atuais:

- `PersistenceException`
- `DataMappingException`
- `ExternalServiceException`
- `JsonSerializationException`

Use exceptions de infrastructure para falhas técnicas relacionadas a persistência, mapping, serialização ou serviços externos.

## Tratamento REST

`GlobalExceptionHandler` está implementado com `@RestControllerAdvice`.

Ele mapeia categorias conhecidas de exceptions para respostas HTTP e publica error logs por meio de `ErrorLogPublisher`.

Models de resposta atuais:

- `ApiErrorResponse`
- `FieldErrorResponse`

Factories atuais:

- `ApiErrorResponseFactory`
- `FieldErrorResponseFactory`

O handler cobre exceptions de domínio, exceptions de application, erros de validação, métodos HTTP/media types não suportados, rotas ausentes, falhas de persistência, falhas de infraestrutura e exceptions inesperadas.

## Limitações Atuais

- Não há controllers REST de negócio na `v0.1.0`.
- O handler ainda não é exercitado por endpoints públicos de negócio.
- O error logging está implementado, mas depende de uma requisição chegar ao REST exception handler ou de publicação explícita de error log.
- O tratamento de erros de autenticação e autorização precisará ser revisado quando os fluxos de JWT/security forem implementados.

<p align="right"><a href="../README.md">Voltar para a documentação técnica</a></p>
