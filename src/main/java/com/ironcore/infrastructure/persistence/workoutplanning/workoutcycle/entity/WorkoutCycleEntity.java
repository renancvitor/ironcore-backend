package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.entity.TrainingGoalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "workout_cycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutCycleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne
    @JoinColumn(name = "training_goal_id", nullable = false)
    private TrainingGoalEntity trainingGoal;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "desired_duration_months")
    private Integer desiredDurationMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "workout_status", nullable = false, length = 50)
    private WorkoutStatus workoutStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "workout_origin", nullable = false, length = 50)
    private WorkoutOrigin workoutOrigin;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
