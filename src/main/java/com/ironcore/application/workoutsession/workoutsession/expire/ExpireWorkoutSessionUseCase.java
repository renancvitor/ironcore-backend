package com.ironcore.application.workoutsession.workoutsession.expire;

import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpireWorkoutSessionUseCase {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Optional<ExpireWorkoutSessionResult> execute(ExpireWorkoutSessionCommand command) {
        Optional<WorkoutSession> session = workoutSessionRepository.findInProgressByPersonId(command.personId());
        if (session.isEmpty() || !session.get().expireIfDue(clock.instant())) {
            return Optional.empty();
        }

        WorkoutSession savedSession = workoutSessionRepository.save(session.get());
        return Optional.of(new ExpireWorkoutSessionResult(
                savedSession.getId(),
                savedSession.getFinishedAt(),
                savedSession.getUpdatedAt()
        ));
    }
}
