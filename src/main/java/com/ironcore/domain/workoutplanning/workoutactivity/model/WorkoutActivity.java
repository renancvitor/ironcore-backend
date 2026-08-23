package com.ironcore.domain.workoutplanning.workoutactivity.model;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutplanning.workoutactivity.exception.InvalidWorkoutActivityException;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class WorkoutActivity {

    private final WorkoutActivityId id;
    private final WorkoutDayId workoutDayId;
    private ExerciseId exerciseId;
    private Integer orderIndex;
    private Integer sets;
    private Integer repRangeMin;
    private Integer repRangeMax;
    private BigDecimal targetLoadKg;
    private String targetLoadText;
    private Integer durationMinutes;
    private BigDecimal distanceKm;
    private String intensityText;
    private Integer restSeconds;
    private String notes;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private WorkoutActivity(WorkoutActivityId id, WorkoutDayId workoutDayId, ExerciseId exerciseId, Integer orderIndex,
                            Integer sets, Integer repRangeMin, Integer repRangeMax, BigDecimal targetLoadKg,
                            String targetLoadText, Integer durationMinutes, BigDecimal distanceKm, String intensityText,
                            Integer restSeconds, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.workoutDayId = requireNonNull(workoutDayId, "Dia de treino não pode ser nulo.");
        this.exerciseId = requireNonNull(exerciseId, "Exercício não pode ser nulo.");
        this.orderIndex = requirePositive(orderIndex, "Ordem deve ser maior que zero.");
        this.sets = requirePositiveIfPresent(sets, "Quantidade de séries deve ser maior que zero.");
        this.repRangeMin = requirePositiveIfPresent(repRangeMin,
                "Mínimo de repetições desejadas deve ser maior do que zero.");
        this.repRangeMax = requirePositiveIfPresent(repRangeMax,
                "Máximo de repetições desejadas deve ser maior do que zero.");

        validateRepRange(this.repRangeMin, this.repRangeMax);

        this.targetLoadKg = requirePositiveIfPresent(targetLoadKg,
                "Carga alvo desejada deve ser maior do que zero.");
        this.targetLoadText = requireNonBlankIfPresent(targetLoadText, "Alvo desejado não pode ser vazio.");
        this.durationMinutes = requirePositiveIfPresent(durationMinutes,
                "Duração em minutos deve ser maior que zero.");
        this.distanceKm = requirePositiveIfPresent(distanceKm,
                "Distância em quilômetros deve ser maior que zero.");
        this.intensityText = requireNonBlankIfPresent(intensityText,
                "Intensidade não pode ser vazia.");
        this.restSeconds = requirePositiveIfPresent(restSeconds,
                "Segundos de descanso deve ser maior do que zero.");
        this.notes = requireNonBlankIfPresent(notes,
                "Anotações não podem ser vazias.");
        this.createdAt = requireNonNull(createdAt, "Data de criação é obrigatória.");
        this.updatedAt = updatedAt;
    }

    public static WorkoutActivity register(
            WorkoutDayId workoutDayId,
            ExerciseId exerciseId,
            Integer orderIndex,
            Integer sets,
            Integer repRangeMin,
            Integer repRangeMax,
            BigDecimal targetLoadKg,
            String targetLoadText,
            Integer durationMinutes,
            BigDecimal distanceKm,
            String intensityText,
            Integer restSeconds,
            String notes,
            LocalDateTime createdAt
    ) {
        return new WorkoutActivity(
                null,
                workoutDayId,
                exerciseId,
                orderIndex,
                sets,
                repRangeMin,
                repRangeMax,
                targetLoadKg,
                targetLoadText,
                durationMinutes,
                distanceKm,
                intensityText,
                restSeconds,
                notes,
                createdAt,
                null
        );
    }

    public static WorkoutActivity restore(
            WorkoutActivityId id,
            WorkoutDayId workoutDayId,
            ExerciseId exerciseId,
            Integer orderIndex,
            Integer sets,
            Integer repRangeMin,
            Integer repRangeMax,
            BigDecimal targetLoadKg,
            String targetLoadText,
            Integer durationMinutes,
            BigDecimal distanceKm,
            String intensityText,
            Integer restSeconds,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new WorkoutActivity(
                id,
                workoutDayId,
                exerciseId,
                orderIndex,
                sets,
                repRangeMin,
                repRangeMax,
                targetLoadKg,
                targetLoadText,
                durationMinutes,
                distanceKm,
                intensityText,
                restSeconds,
                notes,
                createdAt,
                updatedAt
        );
    }

    public void updateActivity(
            ExerciseId exerciseId,
            Integer sets,
            Integer repRangeMin,
            Integer repRangeMax,
            BigDecimal targetLoadKg,
            String targetLoadText,
            Integer durationMinutes,
            BigDecimal distanceKm,
            String intensityText,
            Integer restSeconds,
            String notes,
            LocalDateTime updatedAt
    ) {
        ExerciseId validatedExerciseId = requireNonNull(exerciseId, "Exercício não pode ser nulo.");
        Integer validatedSets = requirePositiveIfPresent(sets, "Quantidade de séries deve ser maior que zero.");
        Integer validatedRepRangeMin = requirePositiveIfPresent(repRangeMin,
                "Mínimo de repetições desejadas deve ser maior do que zero.");
        Integer validatedRepRangeMax = requirePositiveIfPresent(repRangeMax,
                "Máximo de repetições desejadas deve ser maior do que zero.");

        validateRepRange(validatedRepRangeMin, validatedRepRangeMax);

        BigDecimal validatedTargetLoadKg = requirePositiveIfPresent(targetLoadKg,
                "Carga alvo desejada deve ser maior do que zero.");
        String validatedTargetLoadText = requireNonBlankIfPresent(targetLoadText,
                "Alvo desejado não pode ser vazio.");
        Integer validatedDurationMinutes = requirePositiveIfPresent(durationMinutes,
                "Duração em minutos deve ser maior que zero.");
        BigDecimal validatedDistanceKm = requirePositiveIfPresent(distanceKm,
                "Distância em quilômetros deve ser maior que zero.");
        String validatedIntensityText = requireNonBlankIfPresent(intensityText,
                "Intensidade não pode ser vazia.");
        Integer validatedRestSeconds = requirePositiveIfPresent(restSeconds,
                "Segundos de descanso deve ser maior do que zero.");
        String validatedNotes = requireNonBlankIfPresent(notes,
                "Anotações não podem ser vazias.");
        LocalDateTime validatedUpdatedAt = requireNonNull(updatedAt, "Data de atualização é obrigatória.");

        this.exerciseId = validatedExerciseId;
        this.sets = validatedSets;
        this.repRangeMin = validatedRepRangeMin;
        this.repRangeMax = validatedRepRangeMax;
        this.targetLoadKg = validatedTargetLoadKg;
        this.targetLoadText = validatedTargetLoadText;
        this.durationMinutes = validatedDurationMinutes;
        this.distanceKm = validatedDistanceKm;
        this.intensityText = validatedIntensityText;
        this.restSeconds = validatedRestSeconds;
        this.notes = validatedNotes;
        markUpdatedAt(validatedUpdatedAt);
    }

    public void reorder(Integer orderIndex, LocalDateTime updatedAt) {
        Integer validatedOrderIndex = requirePositive(orderIndex, "Ordem deve ser maior que zero.");
        LocalDateTime validatedUpdatedAt = requireNonNull(updatedAt, "Data de atualização é obrigatória.");

        this.orderIndex = validatedOrderIndex;
        markUpdatedAt(validatedUpdatedAt);
    }

    private void validateRepRange(Integer min, Integer max) {
        if (min != null && max != null && min > max) {
            throw new InvalidWorkoutActivityException("Mínimo de repetições não pode ser maior que o máximo.");
        }
    }

    private void markUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = requireNonNull(updatedAt, "Data de atualização é obrigatória.");
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidWorkoutActivityException(message);
        }

        return value;
    }

    private String requireNonBlankIfPresent(String value, String message) {
        if (value == null) {
            return null;
        }

        if (value.isBlank()) {
            throw new InvalidWorkoutActivityException(message);
        }

        return value.trim();
    }

    private Integer requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new InvalidWorkoutActivityException(message);
        }

        return value;
    }

    private BigDecimal requirePositiveIfPresent(BigDecimal value, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidWorkoutActivityException(message);
        }

        return value;
    }

    private Integer requirePositiveIfPresent(Integer value, String message) {
        if (value != null && value <= 0) {
            throw new InvalidWorkoutActivityException(message);
        }

        return value;
    }
}
