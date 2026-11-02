package com.ironcore.interfaces.rest.openapi;

import com.ironcore.interfaces.rest.exception.model.ApiErrorResponse;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "429",
        description = "Limite de tentativas atingido.",
        headers = @Header(name = "Retry-After", description = "Segundos até uma nova tentativa.", schema = @Schema(type = "integer", format = "int64")),
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))
)
public @interface TooManyRequestsResponse {
}
