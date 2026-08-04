package com.ironcore.interfaces.rest.exercise.catalog.api;

import com.ironcore.interfaces.rest.exercise.catalog.dto.ActivityTypeItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.dto.EquipmentTypeItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.dto.MuscleGroupItemResponse;
import com.ironcore.interfaces.rest.exercise.catalog.dto.MuscleSubgroupItemResponse;
import com.ironcore.interfaces.rest.openapi.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Catálogo de exercícios")
public interface ExerciseCatalogApi {

    @Operation(
            summary = "Listar tipos de atividade",
            description = "Busca e retorna tipos de atividade controlados pelo sistema."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Tipos de atividade encontrados com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ActivityTypeItemResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<List<ActivityTypeItemResponse>> getActivityTypes();

    @Operation(
            summary = "Listar tipos de equipamento",
            description = "Busca e retorna tipos de equipamentos controlados pelo sistema."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Tipos de equipamentos encontrados com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EquipmentTypeItemResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<List<EquipmentTypeItemResponse>> getEquipmentTypes();

    @Operation(
            summary = "Listar grupos musculares",
            description = "Busca e retorna grupos musculares controlados pelo sistema."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Grupos musculares encontrados com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MuscleGroupItemResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<List<MuscleGroupItemResponse>> getMuscleGroups();

    @Operation(
            summary = "Listar subgrupos musculares",
            description = "Busca e retorna subgrupos musculares controlados pelo sistema."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Subgrupos musculares encontrados com sucesso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MuscleSubgroupItemResponse.class)
            )
    )
    @BadRequestResponse
    @NotFoundResponse
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<List<MuscleSubgroupItemResponse>> getMuscleSubgroups(Long muscleGroupId);
}
