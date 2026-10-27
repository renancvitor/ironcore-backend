package com.ironcore.application.workoutsession.workoutsession.expire;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpireWorkoutSessionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExpireWorkoutSessionsUseCase.class);
    private static final int BATCH_SIZE = 100;

    private final WorkoutSessionRepository workoutSessionRepository;
    private final ExpireWorkoutSessionUseCase expireWorkoutSessionUseCase;

    public void execute() {
        PersonId cursor = null;
        while (true) {
            List<PersonId> candidates = workoutSessionRepository.findInProgressPersonIdsAfter(cursor, BATCH_SIZE);
            if (candidates.isEmpty()) {
                return;
            }

            for (PersonId personId : candidates) {
                try {
                    expireWorkoutSessionUseCase.execute(new ExpireWorkoutSessionCommand(personId))
                            .ifPresent(result -> log.info(
                                    "Sessão expirada: sessionId={}, personId={}, finishedAt={}, updatedAt={}",
                                    result.id().value(), personId.value(), result.finishedAt(), result.updatedAt()));
                } catch (RuntimeException exception) {
                    log.error("Falha ao expirar sessão: personId={}", personId.value(), exception);
                }
            }
            cursor = candidates.getLast();
        }
    }
}
