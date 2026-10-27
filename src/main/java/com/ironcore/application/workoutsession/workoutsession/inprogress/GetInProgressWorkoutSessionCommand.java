package com.ironcore.application.workoutsession.workoutsession.inprogress;

import com.ironcore.domain.user.valueobject.UserId;

public record GetInProgressWorkoutSessionCommand(UserId actorUserId) {
}
