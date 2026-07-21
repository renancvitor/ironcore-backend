package com.ironcore.infrastructure.persistence.exercise.entity;

import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;
import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "equipment_type_id", nullable = false)
    private EquipmentTypeEntity equipmentType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "activity_type_id", nullable = false)
    private ActivityTypeEntity activityType;

    @Column(nullable = false)
    private Boolean unilateral;

    @Column(nullable = false)
    private Boolean compound;

    @Column(name = "suggested_rest_seconds")
    private Integer suggestedRestSeconds;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
