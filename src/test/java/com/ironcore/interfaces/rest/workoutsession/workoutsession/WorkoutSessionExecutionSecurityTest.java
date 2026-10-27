package com.ironcore.interfaces.rest.workoutsession.workoutsession;

import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionUseCase;
import com.ironcore.application.workoutsession.workoutsession.detail.GetWorkoutSessionDetailUseCase;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionActivityDetailResult;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionDetailResult;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionSetDetailResult;
import com.ironcore.application.workoutsession.workoutsession.inprogress.GetInProgressWorkoutSessionUseCase;
import com.ironcore.application.workoutsession.workoutsession.notes.UpdateWorkoutSessionNotesUseCase;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionUseCase;
import com.ironcore.application.workoutsession.workoutsessionactivity.execution.UpdateWorkoutSessionActivityExecutionUseCase;
import com.ironcore.application.workoutsession.workoutsessionactivity.loadmode.ChangeWorkoutSessionActivityLoadModeUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.create.CreateWorkoutSessionSetUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.delete.DeleteWorkoutSessionSetUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.update.UpdateWorkoutSessionSetUseCase;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.security.auth.AuthenticatedUser;
import com.ironcore.infrastructure.security.config.SecurityConfig;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkoutSessionController.class)
@Import(SecurityConfig.class)
class WorkoutSessionExecutionSecurityTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private UpdateWorkoutSessionNotesUseCase updateWorkoutSessionNotesUseCase;
    @MockitoBean private UpdateWorkoutSessionActivityExecutionUseCase updateWorkoutSessionActivityExecutionUseCase;
    @MockitoBean private CreateWorkoutSessionSetUseCase createWorkoutSessionSetUseCase;
    @MockitoBean private UpdateWorkoutSessionSetUseCase updateWorkoutSessionSetUseCase;
    @MockitoBean private DeleteWorkoutSessionSetUseCase deleteWorkoutSessionSetUseCase;
    @MockitoBean private ChangeWorkoutSessionActivityLoadModeUseCase changeWorkoutSessionActivityLoadModeUseCase;
    @MockitoBean private StartWorkoutSessionUseCase startWorkoutSessionUseCase;
    @MockitoBean private CloseWorkoutSessionUseCase closeWorkoutSessionUseCase;
    @MockitoBean private GetWorkoutSessionDetailUseCase getWorkoutSessionDetailUseCase;
    @MockitoBean private GetInProgressWorkoutSessionUseCase getInProgressWorkoutSessionUseCase;
    @MockitoBean private ErrorLogPublisher errorLogPublisher;
    @MockitoBean private JwtAccessTokenValidator jwtAccessTokenValidator;
    @MockitoBean private UserRepository userRepository;

    @ParameterizedTest
    @ValueSource(strings = {"NOTES", "ACTIVITY", "CREATE", "UPDATE", "DELETE", "MODE"})
    void shouldRequireAuthenticationForEveryExecutionWrite(String operation) throws Exception {
        mvc.perform(request(operation).with(csrf())).andExpect(status().isUnauthorized());
        verifyNoWrites();
    }

    @ParameterizedTest
    @ValueSource(strings = {"NOTES", "ACTIVITY", "CREATE", "UPDATE", "DELETE", "MODE"})
    void shouldRequireCsrfForEveryAuthenticatedExecutionWrite(String operation) throws Exception {
        var principal = new AuthenticatedUser(new UserId(77L), new Email("actor@example.com"), false);
        mvc.perform(request(operation).with(authentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()))))
                .andExpect(status().isForbidden());
        verifyNoWrites();
    }

    @ParameterizedTest
    @ValueSource(strings = {"NOTES", "ACTIVITY", "CREATE", "UPDATE", "DELETE", "MODE"})
    void shouldAllowAuthenticatedCsrfProtectedExecutionWrites(String operation) throws Exception {
        var session = session();
        session.addSet(OWNER, ACTIVITY_ID, 1, null, null, 8, null, START);
        session = restoredSession(session);
        var activity = session.getActivities().getFirst();
        when(updateWorkoutSessionNotesUseCase.execute(any())).thenReturn(WorkoutSessionDetailResult.from(session));
        when(updateWorkoutSessionActivityExecutionUseCase.execute(any())).thenReturn(WorkoutSessionActivityDetailResult.from(activity));
        when(createWorkoutSessionSetUseCase.execute(any())).thenReturn(WorkoutSessionSetDetailResult.from(activity.getSets().getFirst()));
        when(updateWorkoutSessionSetUseCase.execute(any())).thenReturn(WorkoutSessionSetDetailResult.from(activity.getSets().getFirst()));
        when(changeWorkoutSessionActivityLoadModeUseCase.execute(any())).thenReturn(WorkoutSessionActivityDetailResult.from(activity));
        var principal = new AuthenticatedUser(new UserId(77L), new Email("actor@example.com"), false);
        mvc.perform(request(operation).with(csrf()).with(authentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()))))
                .andExpect(status().is(operation.equals("CREATE") ? 201 : operation.equals("DELETE") ? 204 : 200));
        switch (operation) {
            case "NOTES" -> verify(updateWorkoutSessionNotesUseCase).execute(argThat(command -> command.actorUserId().equals(principal.userId())));
            case "ACTIVITY" -> verify(updateWorkoutSessionActivityExecutionUseCase).execute(argThat(command -> command.actorUserId().equals(principal.userId())));
            case "CREATE" -> verify(createWorkoutSessionSetUseCase).execute(argThat(command -> command.actorUserId().equals(principal.userId())));
            case "UPDATE" -> verify(updateWorkoutSessionSetUseCase).execute(argThat(command -> command.actorUserId().equals(principal.userId())));
            case "DELETE" -> verify(deleteWorkoutSessionSetUseCase).execute(argThat(command -> command.actorUserId().equals(principal.userId())));
            case "MODE" -> verify(changeWorkoutSessionActivityLoadModeUseCase).execute(argThat(command -> command.actorUserId().equals(principal.userId())));
        }
    }

    private MockHttpServletRequestBuilder request(String operation) {
        String base = "/api/users/me/workout-sessions/1";
        String activity = base + "/activities/10";
        return switch (operation) {
            case "NOTES" -> patch(base + "/notes").contentType(MediaType.APPLICATION_JSON).content("{\"notes\":null}");
            case "ACTIVITY" -> patch(activity + "/execution").contentType(MediaType.APPLICATION_JSON).content("{\"notes\":null}");
            case "CREATE" -> post(activity + "/sets").contentType(MediaType.APPLICATION_JSON).content("{\"setNumber\":1,\"actualReps\":8}");
            case "UPDATE" -> patch(activity + "/sets/100").contentType(MediaType.APPLICATION_JSON).content("{\"actualReps\":8}");
            case "DELETE" -> delete(activity + "/sets/100");
            case "MODE" -> patch(activity + "/load-mode").contentType(MediaType.APPLICATION_JSON).content("{\"actualLoadMode\":\"TOTAL\"}");
            default -> throw new IllegalArgumentException(operation);
        };
    }

    private void verifyNoWrites() {
        verifyNoInteractions(updateWorkoutSessionNotesUseCase, updateWorkoutSessionActivityExecutionUseCase,
                createWorkoutSessionSetUseCase, updateWorkoutSessionSetUseCase, deleteWorkoutSessionSetUseCase,
                changeWorkoutSessionActivityLoadModeUseCase);
    }
}
