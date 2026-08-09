package com.ironcore.domain.workoutplanning.workoutday.model;

import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.exception.InvalidWorkoutDayException;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WorkoutDay {

    private final WorkoutDayId id;
    private final WorkoutCycleId workoutCycleId;
    private WeekDay weekDay;
    private String title;
    private Integer sortOrder;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private WorkoutDay(WorkoutDayId id,  WorkoutCycleId workoutCycleId, WeekDay weekDay, String title,
            Integer sortOrder, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.workoutCycleId = requireNonNull(workoutCycleId, "Ciclo de treino não pode ser nulo.");
        this.weekDay = requireNonNull(weekDay, "Dia da semana não pode ser nulo.");
        this.title = requireNonBlank(title, "Título não pode ser nulo ou vazio.");
        this.sortOrder = requirePositive(sortOrder, "Ordem de exibição deve ser maior que zero.");
        this.createdAt = requireNonNull(createdAt, "Data de criação é obrigatória.");
        this.updatedAt = updatedAt;
    }

    public static WorkoutDay register(
            WorkoutCycleId workoutCycleId,
            WeekDay weekDay,
            String title,
            Integer sortOrder,
            LocalDateTime createdAt
    ) {
        return new WorkoutDay(
                null,
                workoutCycleId,
                weekDay,
                title,
                sortOrder,
                createdAt,
                null
        );
    }

    public static WorkoutDay restore(
            WorkoutDayId id,
            WorkoutCycleId workoutCycleId,
            WeekDay weekDay,
            String title,
            Integer sortOrder,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new WorkoutDay(
                id,
                workoutCycleId,
                weekDay,
                title,
                sortOrder,
                createdAt,
                updatedAt
        );
    }

    public void updateDay(
            WeekDay weekDay,
            String title,
            Integer sortOrder,
            LocalDateTime updatedAt
    ) {
        WeekDay validatedWeekDay = requireNonNull(weekDay, "Dia da semana não pode ser nulo.");
        String validatedTitle = requireNonBlank(title, "Título não pode ser nulo ou vazio.");
        Integer validatedSortOrder = requirePositive(sortOrder, "Ordem de exibição deve ser maior que zero.");
        LocalDateTime validatedUpdatedAt = requireNonNull(updatedAt, "Data de atualização é obrigatória.");

        this.weekDay = validatedWeekDay;
        this.title = validatedTitle;
        this.sortOrder = validatedSortOrder;
        markUpdatedAt(validatedUpdatedAt);
    }

    private void markUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = requireNonNull(updatedAt, "Data de atualização é obrigatória.");
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidWorkoutDayException(message);
        }

        return value.trim();
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidWorkoutDayException(message);
        }

        return value;
    }

    private Integer requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new InvalidWorkoutDayException(message);
        }

        return value;
    }
}
