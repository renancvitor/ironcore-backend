package com.ironcore.interfaces.rest.exercise.api;

import com.ironcore.interfaces.rest.exercise.dto.GetExerciseByIdResponse;
import com.ironcore.interfaces.rest.exercise.dto.ListExercisesRequest;
import com.ironcore.interfaces.rest.exercise.dto.ListExercisesResponse;
import com.ironcore.interfaces.rest.openapi.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Exercício")
public interface ExerciseApi {

    @Operation(
            summary = "Obter exercício pelo id",
            description = "Busca e retorna o exercício."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Exercício encontrado com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetExerciseByIdResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<GetExerciseByIdResponse> getExerciseById(Long id);

    @Operation(
            summary = "Listar catálogo de exercícios",
            description = "Lista exercícios com paginação e filtros combinados do catálogo controlado pelo sistema."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Exercícios encontrados com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ListExercisesResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<ListExercisesResponse> list(
            @Min(0) int page,
            @Min(1) @Max(100) int size,
            @ParameterObject ListExercisesRequest request
    );
}
