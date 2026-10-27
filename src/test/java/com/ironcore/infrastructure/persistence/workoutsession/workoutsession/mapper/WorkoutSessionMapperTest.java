package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.mapper;

import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionSnapshot;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsession.entity.WorkoutSessionEntity;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionactivity.mapper.WorkoutSessionActivityMapper;
import com.ironcore.infrastructure.persistence.workoutsession.workoutsessionset.mapper.WorkoutSessionSetMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.ACTIVITY_ID;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.OWNER;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.START;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.session;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.source;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkoutSessionMapperTest {
    @Test
    void shouldRoundTripAllSnapshotsExecutionAndRelationships() {
        WorkoutSession source = session();
        source.addSet(OWNER, ACTIVITY_ID, 1, new BigDecimal("45.00"), ActualLoadMode.PER_SIDE, 10, "Série", START);
        source.addSet(OWNER, ACTIVITY_ID, 2, null, null, null, "Somente relato", START);
        source.updateActivityExecution(OWNER, ACTIVITY_ID, 20, new BigDecimal("2.50"), "Atividade", START);
        source.closeManually(OWNER, START.plusSeconds(30));
        source.updateNotes(OWNER, "Relato geral", START.plusSeconds(60));
        WorkoutSessionEntity entity = persisted(source);
        assertThat(entity.getActivities().getFirst().getSession()).isSameAs(entity);
        assertThat(entity.getActivities().getFirst().getSets().getFirst().getActivity())
                .isSameAs(entity.getActivities().getFirst());
        WorkoutSession restored = WorkoutSessionMapper.toDomain(entity);
        assertThat(restored).usingRecursiveComparison()
                .ignoringFields("id", "version", "activities.id", "activities.sets.id").isEqualTo(source);
    }

    @Test
    void shouldRestoreNullableOriginsWithAllHistoricalData() {
        WorkoutSession source = session();
        WorkoutSessionEntity entity = persisted(source);
        entity.setWorkoutCycleId(null);
        entity.setWorkoutDayId(null);
        entity.getActivities().getFirst().setWorkoutActivityId(null);
        entity.getActivities().getFirst().setExerciseId(null);
        WorkoutSession restored = WorkoutSessionMapper.toDomain(entity);
        assertThat(restored.getWorkoutCycleId()).isNull();
        assertThat(restored.getWorkoutDayId()).isNull();
        assertThat(restored.getActivities().getFirst().getWorkoutActivityId()).isNull();
        assertThat(restored.getActivities().getFirst().getExerciseId()).isNull();
        assertThat(restored.getSnapshot()).isEqualTo(source.getSnapshot());
        assertThat(restored.getActivities().getFirst().getSnapshot()).isEqualTo(source.getActivities().getFirst().getSnapshot());
    }

    @Test
    void shouldAcceptEquivalentDecimalScalesInPersistedSnapshots() {
        WorkoutSessionEntity entity = persisted(session());
        WorkoutSession source = WorkoutSessionMapper.toDomain(entity);
        var activity = entity.getActivities().getFirst();
        activity.setPlannedTargetLoadKgSnapshot(new BigDecimal("90.000"));
        activity.setPlannedDistanceKmSnapshot(new BigDecimal("6.0"));
        source.updateNotes(OWNER, "Relato", START.plusSeconds(1));
        WorkoutSessionMapper.applyExecution(source, entity);
        assertThat(WorkoutSessionMapper.toDomain(entity).getNotes()).isEqualTo("Relato");
        activity.setPlannedTargetLoadKgSnapshot(new BigDecimal("90.01"));
        assertThatThrownBy(() -> WorkoutSessionMapper.applyExecution(source, entity))
                .isInstanceOf(DataMappingException.class);
        activity.setPlannedTargetLoadKgSnapshot(new BigDecimal("90.00"));
        activity.setPlannedDistanceKmSnapshot(new BigDecimal("6.01"));
        assertThatThrownBy(() -> WorkoutSessionMapper.applyExecution(source, entity))
                .isInstanceOf(DataMappingException.class);
    }

    @Test
    void shouldRejectSnapshotChangesAndReopening() {
        WorkoutSessionEntity entity = persisted(session());
        WorkoutSession original = WorkoutSessionMapper.toDomain(entity);
        WorkoutSession tampered = WorkoutSession.restore(original.getId(), OWNER, original.getWorkoutCycleId(),
                original.getWorkoutDayId(), new WorkoutSessionSnapshot("Alterado", "Alterado", original.getSnapshot().weekDay()),
                original.getPerformedOn(), START, null, original.getStatus(), null, null, START, null, 0L,
                original.getActivities());
        assertThatThrownBy(() -> WorkoutSessionMapper.applyExecution(tampered, entity))
                .isInstanceOf(DataMappingException.class);
        original.closeManually(OWNER, START.plusSeconds(30));
        WorkoutSessionMapper.applyExecution(original, entity);
        WorkoutSession reopened = WorkoutSessionMapper.toDomain(persisted(session()));
        assertThatThrownBy(() -> WorkoutSessionMapper.applyExecution(reopened, entity))
                .isInstanceOf(DataMappingException.class);
    }

    @Test
    void shouldRejectForeignActivityAndInvalidMapperInput() {
        WorkoutSessionEntity entity = persisted(session());
        WorkoutSession source = WorkoutSessionMapper.toDomain(entity);
        entity.getActivities().getFirst().setId(99L);
        assertThatThrownBy(() -> WorkoutSessionMapper.applyExecution(source, entity))
                .isInstanceOf(DataMappingException.class);
        assertThatThrownBy(() -> WorkoutSessionMapper.toDomain(null)).isInstanceOf(DataMappingException.class);
        assertThatThrownBy(() -> WorkoutSessionActivityMapper.toDomain(null)).isInstanceOf(DataMappingException.class);
        assertThatThrownBy(() -> WorkoutSessionSetMapper.toDomain(null)).isInstanceOf(DataMappingException.class);
        PersonEntity other = new PersonEntity();
        other.setId(2L);
        assertThatThrownBy(() -> WorkoutSessionMapper.toEntity(session(), other)).isInstanceOf(DataMappingException.class);
    }

    private WorkoutSessionEntity persisted(WorkoutSession source) {
        PersonEntity owner = new PersonEntity();
        owner.setId(OWNER.value());
        WorkoutSessionEntity entity = WorkoutSessionMapper.toEntity(source, owner);
        entity.setId(1L);
        entity.setVersion(0L);
        long activityId = 1L;
        long setId = 1L;
        for (var activity : entity.getActivities()) {
            activity.setId(activityId++);
            for (var set : activity.getSets()) {
                set.setId(setId++);
            }
        }
        return entity;
    }
}
