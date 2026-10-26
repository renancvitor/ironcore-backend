package com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.entity;

import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionset.entity.WorkoutSessionSetEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "workout_session_activities")
@Getter
@Setter
@NoArgsConstructor
public class WorkoutSessionActivityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", nullable = false, updatable = false)
    private WorkoutSessionEntity session;

    @Column(name = "workout_activity_id", updatable = false)
    private Long workoutActivityId;

    @Column(name = "exercise_id", updatable = false)
    private Long exerciseId;

    @Column(name = "exercise_name_snapshot", nullable = false, length = 150, updatable = false)
    private String exerciseNameSnapshot;

    @Column(name = "activity_type_snapshot", nullable = false, length = 50, updatable = false)
    private String activityTypeSnapshot;

    @Column(name = "order_index_snapshot", nullable = false, updatable = false)
    private Integer orderIndexSnapshot;

    @Column(name = "planned_sets_snapshot", updatable = false)
    private Integer plannedSetsSnapshot;

    @Column(name = "planned_rep_range_min_snapshot", updatable = false)
    private Integer plannedRepRangeMinSnapshot;

    @Column(name = "planned_rep_range_max_snapshot", updatable = false)
    private Integer plannedRepRangeMaxSnapshot;

    @Column(name = "planned_target_load_kg_snapshot", precision = 8, scale = 2, updatable = false)
    private BigDecimal plannedTargetLoadKgSnapshot;

    @Column(name = "planned_target_load_text_snapshot", length = 100, updatable = false)
    private String plannedTargetLoadTextSnapshot;

    @Column(name = "planned_duration_minutes_snapshot", updatable = false)
    private Integer plannedDurationMinutesSnapshot;

    @Column(name = "planned_distance_km_snapshot", precision = 8, scale = 2, updatable = false)
    private BigDecimal plannedDistanceKmSnapshot;

    @Column(name = "planned_intensity_text_snapshot", length = 100, updatable = false)
    private String plannedIntensityTextSnapshot;

    @Column(name = "planned_rest_seconds_snapshot", updatable = false)
    private Integer plannedRestSecondsSnapshot;

    @Column(name = "planned_notes_snapshot", columnDefinition = "TEXT", updatable = false)
    private String plannedNotesSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    @Column(name = "actual_distance_km", precision = 8, scale = 2)
    private BigDecimal actualDistanceKm;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    private List<WorkoutSessionSetEntity> sets = new ArrayList<>();
}
