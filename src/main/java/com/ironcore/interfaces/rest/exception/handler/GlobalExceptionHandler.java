package com.ironcore.interfaces.rest.exception.handler;

import com.ironcore.application.exception.*;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.domain.exception.DomainException;
import com.ironcore.domain.logging.error.enums.ErrorCodeType;
import com.ironcore.infrastructure.exception.*;
import com.ironcore.infrastructure.security.jwt.exception.JwtTokenException;
import com.ironcore.interfaces.rest.exception.factory.ApiErrorResponseFactory;
import com.ironcore.interfaces.rest.exception.factory.FieldErrorResponseFactory;
import com.ironcore.interfaces.rest.exception.model.ApiErrorResponse;
import com.ironcore.interfaces.rest.exception.model.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorLogPublisher publisher;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(
            DomainException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                exception.getMessage(),
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        publishErrorLog(
                ErrorCodeType.RESOURCE_NOT_FOUND,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                exception.getMessage(),
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                exception.getMessage(),
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Credenciais incorretas.",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler({
            BusinessRuleViolationException.class,
            OperationNotAllowedException.class})
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleException(
            ApplicationException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

        publishErrorLog(
                ErrorCodeType.BUSINESS_RULE_VIOLATION,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                exception.getMessage(),
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(InitialPasswordChangeRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handleInitialPasswordChangeRequiredException(
            InitialPasswordChangeRequiredException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        publishErrorLog(
                ErrorCodeType.AUTHENTICATION_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                exception.getMessage(),
                request
        );

        return  ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiErrorResponse> handleApplicationException(
            ApplicationException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

        publishErrorLog(
                ErrorCodeType.BUSINESS_RULE_VIOLATION,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                exception.getMessage(),
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        List<FieldErrorResponse> fields = FieldErrorResponseFactory.from(
                exception.getBindingResult().getFieldErrors()
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Erro de validação nos campos de requisição",
                request,
                fields
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                exception.getMessage(),
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        List<FieldErrorResponse> fields = exception.getConstraintViolations().stream()
                .map(violation -> new FieldErrorResponse(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Erro de validação nos parâmetros da requisição",
                request,
                fields
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Corpo da requisição inválido ou mal formatado",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Parâmetro da requisição possui tipo inválido: " + exception.getName(),
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Parâmetro obrigatório ausente: " + exception.getParameterName(),
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Método HTTP não suportado para este recurso",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Tipo de conteúdo da requisição não suportado",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotAcceptableException(
            HttpMediaTypeNotAcceptableException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_ACCEPTABLE;

        publishErrorLog(
                ErrorCodeType.VALIDATION_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Tipo de resposta solicitado não é suportado",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleRouteNotFoundException(
            Exception exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        publishErrorLog(
                ErrorCodeType.RESOURCE_NOT_FOUND,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Recurso não encontrado",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ApiErrorResponse> handlePersistenceException(
            PersistenceException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        publishErrorLog(
                ErrorCodeType.DATABASE_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Erro interno de persistência.",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalServiceException(
            ExternalServiceException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;

        publishErrorLog(
                ErrorCodeType.EXTERNAL_SERVICE_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Erro ao comunicar com serviço externo.",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler({
            DataMappingException.class,
            JsonSerializationException.class})
    public ResponseEntity<ApiErrorResponse> handleInternalInfrastructureException(
            InfrastructureException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        publishErrorLog(
                ErrorCodeType.INTERNAL_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Erro interno ao processar dados.",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(JwtTokenException.class)
    public ResponseEntity<ApiErrorResponse> handlerJwtTokenException(
            JwtTokenException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        publishErrorLog(
                ErrorCodeType.INTERNAL_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Erro interno ao processar autenticação.",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ApiErrorResponse> handleInfrastructureException(
            InfrastructureException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        publishErrorLog(
                ErrorCodeType.INTERNAL_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Erro interno de infraestrutura.",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        publishErrorLog(
                ErrorCodeType.INTERNAL_ERROR,
                exception,
                request
        );

        ApiErrorResponse response = ApiErrorResponseFactory.create(
                status,
                "Erro interno inesperado",
                request
        );

        return ResponseEntity.status(status).body(response);
    }

    private void publishErrorLog(
            ErrorCodeType errorCode,
            Exception exception,
            HttpServletRequest request
    ) {
        publisher.publish(
                errorCode,
                getExceptionMessage(exception),
                exception.getClass().getName(),
                request.getRequestURI(),
                request.getMethod(),
                null,
                getOrCreateCorrelationId(request)
        );
    }

    private String getOrCreateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-Id");

        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return correlationId;
    }

    private String getExceptionMessage(Exception exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return message;
    }
}
