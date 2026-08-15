package com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.entity;

import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity.WorkoutDayEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "workout_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workout_day_id", nullable = false)
    private WorkoutDayEntity workoutDay;

    @ManyToOne
    @JoinColumn(name = "exercise_id", nullable = false)
    private ExerciseEntity exercise;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private Integer sets;

    @Column(name = "rep_range_min")
    private Integer repRangeMin;

    @Column(name = "rep_range_max")
    private Integer repRangeMax;

    @Column(name = "target_load_kg", precision = 5, scale = 2)
    private BigDecimal targetLoadKg;

    @Column(name = "target_load_text", length = 100)
    private String targetLoadText;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "distance_km", precision = 5, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "intensity_text", length = 100)
    private String intensityText;

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
