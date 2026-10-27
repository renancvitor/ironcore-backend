package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.converter.SessionWeekDayConverter;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.entity.WorkoutSessionActivityEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_sessions")
@Getter
@Setter
@NoArgsConstructor
public class WorkoutSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false, updatable = false)
    private PersonEntity person;

    @Column(name = "workout_cycle_id", updatable = false)
    private Long workoutCycleId;

    @Column(name = "workout_day_id", updatable = false)
    private Long workoutDayId;

    @Column(name = "workout_cycle_name_snapshot", nullable = false, length = 150, updatable = false)
    private String workoutCycleNameSnapshot;

    @Column(name = "workout_day_title_snapshot", nullable = false, length = 100, updatable = false)
    private String workoutDayTitleSnapshot;

    @Convert(converter = SessionWeekDayConverter.class)
    @Column(name = "week_day_snapshot", nullable = false, columnDefinition = "SMALLINT", updatable = false)
    private WeekDay weekDaySnapshot;

    @Column(name = "performed_on", nullable = false, updatable = false)
    private LocalDate performedOn;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkoutSessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "close_reason", length = 30)
    private WorkoutSessionCloseReason closeReason;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @OrderBy("orderIndexSnapshot ASC")
    private List<WorkoutSessionActivityEntity> activities = new ArrayList<>();
}
