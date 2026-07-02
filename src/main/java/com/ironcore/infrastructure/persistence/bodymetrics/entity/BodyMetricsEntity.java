package com.ironcore.infrastructure.persistence.bodymetrics.entity;

import java.time.LocalDateTime;

import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "body_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BodyMetricsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;

    @Column(name = "height_cm", nullable = false)
    private Double heightCm;

    @Column(name = "neck_cm")
    private Double neckCm;

    @Column(name = "chest_cm")
    private Double chestCm;

    @Column(name = "shoulder_cm")
    private Double shoulderCm;

    @Column(name = "arm_cm")
    private Double armCm;

    @Column(name = "forearm_cm")
    private Double forearmCm;

    @Column(name = "waist_cm")
    private Double waistCm;

    @Column(name = "hip_cm")
    private Double hipCm;

    @Column(name = "thigh_cm")
    private Double thighCm;

    @Column(name = "calf_cm")
    private Double calfCm;

    @Column(name = "bmi")
    private Double bmi;

    @Column(name = "body_fat_percentage")
    private Double bodyFatPercentage;

    @Column(name = "fat_mass_kg")
    private Double fatMassKg;

    @Column(name = "lean_mass_kg")
    private Double leanMassKg;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
