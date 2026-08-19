package com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.converter.WeekDayConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "workout_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutDayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workout_cycle_id", nullable = false)
    private WorkoutCycleEntity workoutCycle;

    @Convert(converter = WeekDayConverter.class)
    @Column(name = "week_day", nullable = false)
    private WeekDay weekDay;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
