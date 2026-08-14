package com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "week_day", nullable = false, length = 20)
    private WeekDay weekDay;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
