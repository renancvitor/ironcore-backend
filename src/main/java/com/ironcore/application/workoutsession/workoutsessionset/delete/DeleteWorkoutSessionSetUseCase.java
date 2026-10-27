package com.ironcore.application.workoutsession.workoutsessionset.delete;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutsession.workoutsession.WorkoutSessionAuditData;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsessionactivity.model.WorkoutSessionActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DeleteWorkoutSessionSetUseCase {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final Clock clock;
    private final AuditLogPublisher publisher;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void execute(DeleteWorkoutSessionSetCommand command) {
        User user = userRepository.findById(command.actorUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        Person person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));
        WorkoutSession session = workoutSessionRepository.findByIdAndPersonId(command.id(), person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sessão de treino não encontrada."));

        WorkoutSessionActivity activity = session.getActivities().stream()
                .filter(value -> value.getId().equals(command.activityId())).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Atividade da sessão não encontrada."));

        if (activity.getSets().stream().noneMatch(value -> value.getId().equals(command.setId()))) {
            throw new ResourceNotFoundException("Série da atividade não encontrada.");
        }

        WorkoutSessionAuditData beforeState = WorkoutSessionAuditData.from(session);
        Instant now = clock.instant();
        session.expireIfDue(now);
        session.removeSet(person.getId(), command.activityId(), command.setId(), now);
        WorkoutSession savedSession = workoutSessionRepository.save(session);

        publisher.publish(
                AuditActionType.UPDATE,
                user.getId().value(),
                user.getEmail().value(),
                AuditTargetType.WORKOUT_SESSION,
                savedSession.getId().value(),
                beforeState,
                WorkoutSessionAuditData.from(savedSession)
        );
    }
}
