package com.ironcore.infrastructure.persistence.exercisemuscletarget.entity;

import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.entity.MuscleSubgroupEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_muscle_targets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseMuscleTargetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exercise_id", nullable = false)
    private ExerciseEntity exercise;

    @ManyToOne
    @JoinColumn(name = "muscle_subgroup_id", nullable = false)
    private MuscleSubgroupEntity muscleSubgroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", nullable = false, length = 30)
    private TargetRoleType targetRole;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
