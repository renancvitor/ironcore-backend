package com.ironcore.interfaces.rest.openapi;

import com.ironcore.interfaces.rest.exception.model.ApiErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;

import java.lang.annotation.*;

@Documented
@Target({
        ElementType.METHOD,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "406",
        description = "Formato de resposta não suportado",
        content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ApiErrorResponse.class)
        )
)
public @interface NotAcceptableResponse {
}
