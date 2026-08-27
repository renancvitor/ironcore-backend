package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutCycleDetailAssemblerTest {

    private final WorkoutCycleDetailAssembler assembler = new WorkoutCycleDetailAssembler();

    @Test
    void shouldAssembleDaysActivitiesAndMuscleGroupsWithoutDuplicates() {
        List<WorkoutCycleDetailProjection> projections = List.of(
                projection(1L, WeekDay.MONDAY, 1, 10L, 1, 100L, "Supino reto", 1L, "Peitoral"),
                projection(1L, WeekDay.MONDAY, 1, 10L, 1, 100L, "Supino reto", 2L, "Tríceps"),
                projection(1L, WeekDay.MONDAY, 1, 11L, 2, 101L, "Crucifixo", 1L, "Peitoral"),
                projection(2L, WeekDay.WEDNESDAY, 1, 20L, 1, 200L, "Remada", 3L, "Costas")
        );

        WorkoutCycleDetailResult result = assembler.toResult(projections);

        assertThat(result.id().value()).isEqualTo(10L);
        assertThat(result.trainingGoal().name()).isEqualTo("Hipertrofia");
        assertThat(result.days()).hasSize(2);

        WorkoutDayDetailResult monday = result.days().getFirst();
        assertThat(monday.id().value()).isEqualTo(1L);
        assertThat(monday.activities()).hasSize(2);
        assertThat(monday.activities().getFirst().id().value()).isEqualTo(10L);
        assertThat(monday.activities().getFirst().exercise().name()).isEqualTo("Supino reto");
        assertThat(monday.activities().getFirst().exercise().muscleGroups())
                .extracting(MuscleGroupDetailResult::name)
                .containsExactly("Peitoral", "Tríceps");
        assertThat(monday.activities().get(1).id().value()).isEqualTo(11L);

        WorkoutDayDetailResult wednesday = result.days().get(1);
        assertThat(wednesday.id().value()).isEqualTo(2L);
        assertThat(wednesday.activities()).singleElement()
                .extracting(activity -> activity.exercise().name())
                .isEqualTo("Remada");
    }

    @Test
    void shouldKeepDayWithoutActivities() {
        WorkoutCycleDetailResult result = assembler.toResult(List.of(
                projection(1L, WeekDay.MONDAY, 1, null, null, null, null, null, null)
        ));

        assertThat(result.days()).singleElement()
                .satisfies(day -> {
                    assertThat(day.id().value()).isEqualTo(1L);
                    assertThat(day.activities()).isEmpty();
                });
    }

    @Test
    void shouldKeepCycleWithoutDays() {
        WorkoutCycleDetailResult result = assembler.toResult(List.of(
                projection(null, null, null, null, null, null, null, null, null)
        ));

        assertThat(result.days()).isEmpty();
    }

    private static WorkoutCycleDetailProjection projection(
            Long dayId,
            WeekDay weekDay,
            Integer sortOrder,
            Long activityId,
            Integer orderIndex,
            Long exerciseId,
            String exerciseName,
            Long muscleGroupId,
            String muscleGroupName
    ) {
        return new WorkoutCycleDetailProjection(
                10L,
                "Ficha A",
                WorkoutStatus.IN_PROGRESS,
                LocalDate.of(2026, 8, 1),
                null,
                3,
                "Foco em hipertrofia",
                20L,
                "Hipertrofia",
                dayId,
                weekDay,
                dayId == null ? null : "Treino " + dayId,
                sortOrder,
                activityId,
                orderIndex,
                3,
                8,
                12,
                new BigDecimal("60.00"),
                null,
                null,
                null,
                null,
                90,
                null,
                exerciseId,
                exerciseName,
                muscleGroupId,
                muscleGroupName
        );
    }
}
