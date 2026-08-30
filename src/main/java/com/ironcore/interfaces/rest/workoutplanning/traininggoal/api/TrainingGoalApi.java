package com.ironcore.interfaces.rest.workoutplanning.traininggoal.api;

import com.ironcore.interfaces.rest.openapi.*;
import com.ironcore.interfaces.rest.workoutplanning.traininggoal.dto.TrainingGoalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Objetivos de treino")
public interface TrainingGoalApi {

    @Operation(
            summary = "Listagem de objetivos de treino",
            description = "Lista os objetivos de treino."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listagem concluída",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = TrainingGoalResponse.class)
                    )
            )
    )
    @ForbiddenResponse
    @UnauthorizedResponse
    @InternalServerErrorResponse
    ResponseEntity<List<TrainingGoalResponse>> listTrainingGoals();
}
