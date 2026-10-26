package com.ironcore.infrastructure.persistence.workoutsession.workoutsessionset.entity;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.entity.WorkoutSessionActivityEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "workout_session_sets")
@Getter
@Setter
@NoArgsConstructor
public class WorkoutSessionSetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_activity_id", nullable = false, updatable = false)
    private WorkoutSessionActivityEntity activity;

    @Column(name = "set_number", nullable = false, updatable = false)
    private Integer setNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "actual_load_kg", precision = 8, scale = 2)
    private BigDecimal actualLoadKg;

    @Column(name = "actual_reps")
    private Integer actualReps;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "actual_load_mode", length = 20)
    private ActualLoadMode actualLoadMode;
}
