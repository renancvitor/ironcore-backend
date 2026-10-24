package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository;

import org.flywaydb.core.Flyway;
import java.sql.DriverManager;
import java.util.UUID;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.workoutplanning.workoutcycle.list.ListWorkoutCyclesItemResult;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.entity.TrainingGoalEntity;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.repository.TrainingGoalJpaRepository;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class WorkoutCycleSearchAdapterIntegrationTest {

    @Autowired
    private WorkoutCycleJpaRepository workoutCycleJpaRepository;

    @Autowired
    private PersonJpaRepository personJpaRepository;

    @Autowired
    private TrainingGoalJpaRepository trainingGoalJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WorkoutCycleDetailJpaRepository detailRepository;

    private WorkoutCycleSearchAdapter adapter;
    private PersonEntity person;
    private TrainingGoalEntity hypertrophy;

    @BeforeEach
    void setUp() {
        adapter = new WorkoutCycleSearchAdapter(workoutCycleJpaRepository);
        person = savePerson("Renan");
        PersonEntity anotherPerson = savePerson("Outra pessoa");
        hypertrophy = saveTrainingGoal("HYPERTROPHY", "Hipertrofia");
        TrainingGoalEntity endurance = saveTrainingGoal("ENDURANCE", "Resistência");

        saveWorkoutCycle(person, hypertrophy, "Ciclo Hipertrofia Atual", WorkoutStatus.IN_PROGRESS,
                LocalDate.of(2026, 1, 10), null);
        saveWorkoutCycle(person, hypertrophy, "Ciclo Hipertrofia Encerrado", WorkoutStatus.COMPLETED,
                LocalDate.of(2025, 12, 1), LocalDate.of(2026, 2, 15));
        saveWorkoutCycle(person, endurance, "Ciclo Resistência Atual", WorkoutStatus.IN_PROGRESS,
                LocalDate.of(2026, 1, 10), null);
        saveWorkoutCycle(anotherPerson, hypertrophy, "Ciclo Hipertrofia de Outra Pessoa", WorkoutStatus.IN_PROGRESS,
                LocalDate.of(2026, 1, 10), null);
    }

    @Test
    void shouldFilterByPersonStatusTrainingGoalNameAndApplyDefaultOrdering() {
        PageResult<ListWorkoutCyclesItemResult> result = adapter.findWorkoutCycles(
                new PersonId(person.getId()),
                WorkoutStatus.IN_PROGRESS,
                new TrainingGoalId(hypertrophy.getId()),
                null,
                null,
                "hipertrofia",
                new PageQuery(0, 10)
        );

        assertThat(result.content())
                .extracting(ListWorkoutCyclesItemResult::name)
                .containsExactly("Ciclo Hipertrofia Atual");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnCyclesThatOverlapPeriodIncludingOngoingCycles() {
        PageResult<ListWorkoutCyclesItemResult> result = adapter.findWorkoutCycles(
                new PersonId(person.getId()),
                null,
                new TrainingGoalId(hypertrophy.getId()),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31),
                null,
                new PageQuery(0, 10)
        );

        assertThat(result.content())
                .extracting(ListWorkoutCyclesItemResult::name)
                .containsExactly(
                        "Ciclo Hipertrofia Atual",
                        "Ciclo Hipertrofia Encerrado"
                );
    }

    @ParameterizedTest
    @EnumSource(WeekDay.class)
    void shouldPersistAndProjectEveryWeekStart(WeekDay weekDay) {
        var cycle = workoutCycleJpaRepository.findAll().stream()
                .filter(item -> item.getPerson().getId().equals(person.getId())).findFirst().orElseThrow();
        cycle.setWeekStartsOn(weekDay);
        workoutCycleJpaRepository.saveAndFlush(cycle);
        Long cycleId = cycle.getId();
        entityManager.clear();

        assertThat(workoutCycleJpaRepository.findById(cycleId).orElseThrow().getWeekStartsOn())
                .isEqualTo(weekDay);
        assertThat(detailRepository.findDetail(cycleId, person.getId())).singleElement()
                .satisfies(row -> assertThat(row.weekStartsOn()).isEqualTo(weekDay));
        assertThat(entityManager.createNativeQuery("select week_starts_on from workout_cycles where id = :id")
                .setParameter("id", cycleId).getSingleResult()).isEqualTo(weekDay.getValue());
    }

    @Test
    void shouldBackfillExistingCyclesAndDefaultNewRowsToSunday() throws Exception {
        String url = "jdbc:h2:mem:week_start_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE workout_cycles (id BIGINT PRIMARY KEY)");
            statement.execute("INSERT INTO workout_cycles (id) VALUES (1)");

            Flyway.configure().dataSource(url, "sa", "")
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true).baselineVersion("22").target("23")
                    .load().migrate();

            statement.execute("INSERT INTO workout_cycles (id) VALUES (2)");
            try (var rows = statement.executeQuery("SELECT week_starts_on FROM workout_cycles ORDER BY id")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getInt(1)).isEqualTo(1);
                assertThat(rows.next()).isTrue();
                assertThat(rows.getInt(1)).isEqualTo(1);
                assertThat(rows.next()).isFalse();
            }
            assertThatThrownBy(() -> statement.execute(
                    "INSERT INTO workout_cycles (id, week_starts_on) VALUES (3, NULL)"))
                    .isInstanceOf(java.sql.SQLException.class);
        }
    }

    private PersonEntity savePerson(String name) {
        return personJpaRepository.save(new PersonEntity(
                null,
                name,
                SexType.MALE,
                LocalDate.of(1994, 4, 9),
                LocalDateTime.of(2026, 1, 1, 10, 0),
                null
        ));
    }

    private TrainingGoalEntity saveTrainingGoal(String code, String displayName) {
        return trainingGoalJpaRepository.save(new TrainingGoalEntity(
                null,
                code,
                displayName,
                true,
                1
        ));
    }

    private void saveWorkoutCycle(
            PersonEntity owner,
            TrainingGoalEntity trainingGoal,
            String name,
            WorkoutStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        workoutCycleJpaRepository.save(new WorkoutCycleEntity(
                null,
                owner,
                name,
                trainingGoal,
                startDate,
                endDate,
                3,
                WeekDay.SUNDAY,
                status,
                WorkoutOrigin.MANUAL,
                null,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                null
        ));
    }
}
