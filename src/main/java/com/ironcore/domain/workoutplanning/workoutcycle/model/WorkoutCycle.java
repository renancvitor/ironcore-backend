package com.ironcore.domain.workoutplanning.workoutcycle.model;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.exception.InvalidWorkoutCycleException;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
public class WorkoutCycle {

    private final WorkoutCycleId id;
    private final PersonId personId;
    private String name;
    private TrainingGoalId trainingGoalId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer desiredDurationMonths;
    private WorkoutStatus workoutStatus;
    private final WorkoutOrigin  workoutOrigin;
    private String notes;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private WorkoutCycle(WorkoutCycleId id, PersonId personId, String name, TrainingGoalId trainingGoalId,
                         LocalDate startDate, LocalDate endDate, Integer desiredDurationMonths, WorkoutStatus workoutStatus,
                         WorkoutOrigin workoutOrigin, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.personId = requireNonNull(personId, "Pessoa não pode ser nula.");
        this.name = requireNonBlank(name, "Nome não pode ser nulo ou vazio.");
        this.trainingGoalId = requireNonNull(trainingGoalId, "Objetivo de treino é obrigatório.");
        this.startDate = startDate;
        this.endDate = endDate;
        this.desiredDurationMonths = requirePositiveIfPresent(desiredDurationMonths,
                "Duração desejada deve ser positiva.");
        this.workoutStatus = requireNonNull(workoutStatus, "Status do treino é obrigatório.");
        this.workoutOrigin = requireNonNull(workoutOrigin, "Origem do treino é obrigatório.");
        this.notes = notes;
        this.createdAt = requireNonNull(createdAt, "Data de criação é obrigatória.");
        this.updatedAt = updatedAt;
    }

    public static WorkoutCycle register(
            PersonId personId,
            String name,
            TrainingGoalId trainingGoalId,
            Integer desiredDurationMonths,
            WorkoutOrigin workoutOrigin,
            String notes,
            LocalDateTime createdAt
    ) {
        return new WorkoutCycle(
                null,
                personId,
                name,
                trainingGoalId,
                null,
                null,
                desiredDurationMonths,
                WorkoutStatus.NOT_STARTED,
                workoutOrigin,
                notes,
                createdAt,
                null
        );
    }

    public static WorkoutCycle restore(
            WorkoutCycleId id,
            PersonId personId,
            String name,
            TrainingGoalId trainingGoalId,
            LocalDate startDate,
            LocalDate endDate,
            Integer desiredDurationMonths,
            WorkoutStatus workoutStatus,
            WorkoutOrigin workoutOrigin,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new WorkoutCycle(
                id,
                personId,
                name,
                trainingGoalId,
                startDate,
                endDate,
                desiredDurationMonths,
                workoutStatus,
                workoutOrigin,
                notes,
                createdAt,
                updatedAt
        );
    }

    public void updateCycle(
            String name,
            TrainingGoalId trainingGoalId,
            Integer desiredDurationMonths,
            String notes,
            LocalDateTime updatedAt
    ) {
        String validatedName = requireNonBlank(name, "Nome não pode ser nulo ou vazio.");
        TrainingGoalId validatedTrainingGoalId = requireNonNull(trainingGoalId,
                "Objetivo de treino é obrigatório.");
        Integer validatedDesiredDurationMonths = requirePositiveIfPresent(desiredDurationMonths,
                "Duração desejada deve ser positiva.");
        LocalDateTime validatedUpdatedAt = requireNonNull(updatedAt, "Data de atualização é obrigatória.");

        this.name = validatedName;
        this.trainingGoalId = validatedTrainingGoalId;
        this.desiredDurationMonths = validatedDesiredDurationMonths;
        this.notes = notes;
        markUpdatedAt(validatedUpdatedAt);
    }

    public void startCycle(LocalDate startDate) {
        if (workoutStatus !=  WorkoutStatus.NOT_STARTED) {
            throw new InvalidWorkoutCycleException("Somente um ciclo não iniciado pode ser iniciado.");
        }

        LocalDate validatedStartDate = requireNonNull(startDate, "Data de início é obrigatória.");

        this.workoutStatus = WorkoutStatus.IN_PROGRESS;
        this.startDate = validatedStartDate;
    }

    public void endCycle(LocalDate endDate) {
        if (workoutStatus != WorkoutStatus.IN_PROGRESS) {
            throw new InvalidWorkoutCycleException("Somente um ciclo em andamento pode ser concluído.");
        }

        LocalDate validatedEndDate = requireNonNull(endDate, "Data de conclusão é obrigatória.");

        if (validatedEndDate.isBefore(startDate)) {
            throw new InvalidWorkoutCycleException("Data de conclusão não pode ser anterior à data de início.");
        }

        this.workoutStatus = WorkoutStatus.COMPLETED;
        this.endDate = validatedEndDate;
    }

    public void cancelCycle() {
        if (workoutStatus == WorkoutStatus.COMPLETED || workoutStatus == WorkoutStatus.CANCELLED) {
            throw new InvalidWorkoutCycleException("Um ciclo concluído ou cancelado não pode ser cancelado.");
        }

        workoutStatus = WorkoutStatus.CANCELLED;
    }

    public Integer calculateProgress(LocalDate referenceDate) {
        if (desiredDurationMonths == null || startDate == null) {
            return null;
        }

        LocalDate expectedEndDate = startDate.plusMonths(desiredDurationMonths);

        long totalDays = ChronoUnit.DAYS.between(startDate, expectedEndDate);
        long elapsedDays = ChronoUnit.DAYS.between(startDate, referenceDate);

        if (elapsedDays <= 0) {
            return 0;
        }

        if (elapsedDays >= totalDays) {
            return 100;
        }

        return (int) ((elapsedDays * 100) / totalDays);
    }

    private void markUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = requireNonNull(updatedAt, "Data de atualização é obrigatória.");
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidWorkoutCycleException(message);
        }

        return value.trim();
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidWorkoutCycleException(message);
        }

        return value;
    }

    private Integer requirePositiveIfPresent(Integer value, String message) {
        if (value != null && value <= 0) {
            throw new InvalidWorkoutCycleException(message);
        }

        return value;
    }
}
