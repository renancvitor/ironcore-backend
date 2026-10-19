package com.ironcore.interfaces.rest.auth.api;

import com.ironcore.interfaces.rest.openapi.InternalServerErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;

@Tag(name = "Autenticação")
public interface CsrfApi {

    @Operation(
            summary = "Obter token CSRF",
            description = "Retorna o token CSRF mascarado para proteção BREACH, sem exigir autenticação. "
                    + "Envie o valor de `token` no cabeçalho indicado por `headerName` nas operações de escrita, "
                    + "junto aos cookies da API. `parameterName` indica o nome do parâmetro CSRF. "
                    + "O cookie CSRF é HttpOnly; utilize o token retornado no corpo, não o valor bruto do cookie. "
                    + "Obtenha um novo token após login e logout. A resposta utiliza Cache-Control: no-store."
    )
    @SecurityRequirements
    @ApiResponse(
            responseCode = "200",
            description = "Token CSRF disponível para envio nas operações de escrita.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CsrfToken.class)
            )
    )
    @InternalServerErrorResponse
    ResponseEntity<CsrfToken> csrf(@Parameter(hidden = true) CsrfToken token);
}
