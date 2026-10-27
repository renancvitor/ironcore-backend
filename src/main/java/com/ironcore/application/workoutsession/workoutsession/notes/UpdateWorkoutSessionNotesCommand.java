package com.ironcore.application.workoutsession.workoutsession.notes;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

public record UpdateWorkoutSessionNotesCommand(
        UserId actorUserId,
        WorkoutSessionId id,
        String notes, boolean notesPresent
) {
}
