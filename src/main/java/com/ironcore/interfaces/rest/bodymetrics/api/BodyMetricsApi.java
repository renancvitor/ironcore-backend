package com.ironcore.interfaces.rest.bodymetrics.api;

import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.interfaces.rest.bodymetrics.dto.create.CreateBodyMetricsRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.create.CreateBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.get.GetBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.latest.GetLatestBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.list.ListBodyMetricsResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.progress.BodyMetricsProgressChangesRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.progress.BodyMetricsProgressChangesResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.progress.BodyMetricsProgressChartRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.progress.BodyMetricsProgressChartResponse;
import com.ironcore.interfaces.rest.bodymetrics.dto.update.UpdateBodyMetricsRequest;
import com.ironcore.interfaces.rest.bodymetrics.dto.update.UpdateBodyMetricsResponse;
import com.ironcore.interfaces.rest.openapi.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Medidas corporais")
public interface BodyMetricsApi {

    @Operation(
            summary = "Cadastrar medidas corporais",
            description = "Permite o usuário cadastrar suas medidas corporais."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Cadastro realizado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateBodyMetricsResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<CreateBodyMetricsResponse> create(
            AuthenticatedUser authenticatedUser,
            @Valid CreateBodyMetricsRequest request
    );

    @Operation(
            summary = "Alterar medidas corporais",
            description = "Atualiza parcialmente as medidas corporais vinculadas ao usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Medidas corporais alteradas com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpdateBodyMetricsResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<UpdateBodyMetricsResponse> update(
            AuthenticatedUser authenticatedUser,
            Long id,
            @Valid UpdateBodyMetricsRequest request
    );

    @Operation(
            summary = "Deletar medidas corporais",
            description = "Deleta o registro de medidas corporais do usuário autenticado."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Dados deletados com sucesso."
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<Void> delete(
            AuthenticatedUser authenticatedUser,
            Long id
    );

    @Operation(
            summary = "Listagem de medidas corporais",
            description = "Lista as medidas corporais do usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listagem concluída.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ListBodyMetricsResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<ListBodyMetricsResponse> list(
            AuthenticatedUser authenticatedUser,
            @Min(0) int page,
            @Min(1) @Max(100) int size
    );

    @Operation(
            summary = "Busca medidas corporais",
            description = "Busca medidas corporais por ID da medida corporal do usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Busca concluída.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetBodyMetricsResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<GetBodyMetricsResponse> getById(
            AuthenticatedUser authenticatedUser,
            Long id
    );

    @Operation(
            summary = "Busca a última das medidas corporais",
            description = "Busca a última das medidas corporais do usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Busca concluída.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetLatestBodyMetricsResponse.class)
            )
    )
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<GetLatestBodyMetricsResponse> getLatest(AuthenticatedUser authenticatedUser);

    @Operation(
            summary = "Gráfico de composições corporais em quilograma (kg)",
            description = "Retorna gráfico de período com as composições corporais em kg referente às medidas " +
                    "corporais do usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Sucesso ao retornar dados.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BodyMetricsProgressChartResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<BodyMetricsProgressChartResponse> getBodyComposition(
            AuthenticatedUser authenticatedUser,
            @Valid @ParameterObject BodyMetricsProgressChartRequest request
    );

    @Operation(
            summary = "Gráfico de circunferências corporais em centímetros (cm)",
            description = "Retorna gráfico de período com as circunferências corporais em cm referente às medidas " +
                    "corporais do usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Sucesso ao retornar dados.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BodyMetricsProgressChartResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<BodyMetricsProgressChartResponse> getCircumferences(
            AuthenticatedUser authenticatedUser,
            @Valid @ParameterObject BodyMetricsProgressChartRequest request
    );

    @Operation(
            summary = "Gráfico de percentual de gordura corporal",
            description = "Retorna gráfico de período com percentual de gordura corporal referente às medidas " +
                    "corporais do usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Sucesso ao retornar dados.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BodyMetricsProgressChartResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<BodyMetricsProgressChartResponse> getBodyFat(
            AuthenticatedUser authenticatedUser,
            @Valid @ParameterObject BodyMetricsProgressChartRequest request
    );

    @Operation(
            summary = "Tabela das diferenças entre medidas corporais",
            description = "Retorna tabela de período com as diferenças entre as medidas corporais " +
                    "do usuário autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Sucesso ao retornar dados.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BodyMetricsProgressChangesResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnprocessableEntityResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<BodyMetricsProgressChangesResponse> getChanges(
            AuthenticatedUser authenticatedUser,
            @Valid @ParameterObject BodyMetricsProgressChangesRequest request
    );
}
