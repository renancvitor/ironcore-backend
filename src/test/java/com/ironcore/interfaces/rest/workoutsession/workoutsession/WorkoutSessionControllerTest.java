package com.ironcore.interfaces.rest.workoutsession.workoutsession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.workoutsession.workoutsession.list.*;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.error.port.ErrorLogPublisher;
import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionResult;
import com.ironcore.application.workoutsession.workoutsession.close.CloseWorkoutSessionUseCase;
import com.ironcore.application.workoutsession.workoutsession.detail.GetWorkoutSessionDetailCommand;
import com.ironcore.application.workoutsession.workoutsession.detail.GetWorkoutSessionDetailUseCase;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionActivityDetailResult;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionDetailResult;
import com.ironcore.application.workoutsession.workoutsession.detail.WorkoutSessionSetDetailResult;
import com.ironcore.application.workoutsession.workoutsession.inprogress.GetInProgressWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.inprogress.GetInProgressWorkoutSessionUseCase;
import com.ironcore.application.workoutsession.workoutsession.notes.UpdateWorkoutSessionNotesCommand;
import com.ironcore.application.workoutsession.workoutsession.notes.UpdateWorkoutSessionNotesUseCase;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionCommand;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionResult;
import com.ironcore.application.workoutsession.workoutsession.start.StartWorkoutSessionUseCase;
import com.ironcore.application.workoutsession.workoutsessionactivity.execution.UpdateWorkoutSessionActivityExecutionCommand;
import com.ironcore.application.workoutsession.workoutsessionactivity.execution.UpdateWorkoutSessionActivityExecutionUseCase;
import com.ironcore.application.workoutsession.workoutsessionactivity.loadmode.ChangeWorkoutSessionActivityLoadModeCommand;
import com.ironcore.application.workoutsession.workoutsessionactivity.loadmode.ChangeWorkoutSessionActivityLoadModeUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.create.CreateWorkoutSessionSetCommand;
import com.ironcore.application.workoutsession.workoutsessionset.create.CreateWorkoutSessionSetUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.delete.DeleteWorkoutSessionSetCommand;
import com.ironcore.application.workoutsession.workoutsessionset.delete.DeleteWorkoutSessionSetUseCase;
import com.ironcore.application.workoutsession.workoutsessionset.update.UpdateWorkoutSessionSetCommand;
import com.ironcore.application.workoutsession.workoutsessionset.update.UpdateWorkoutSessionSetUseCase;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.exception.InvalidWorkoutSessionSetException;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.security.jwt.JwtAccessTokenValidator;
import com.ironcore.interfaces.rest.workoutsession.WorkoutSessionController;
import com.ironcore.interfaces.rest.workoutsession.dto.start.StartWorkoutSessionRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static com.ironcore.interfaces.rest.support.security.AuthenticatedUserRequestPostProcessorFactory.authenticatedUser;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkoutSessionControllerTest {

    @MockitoBean private ListWorkoutSessionsUseCase listWorkoutSessionsUseCase;

    @Nested
    class History {
        @Test
        void shouldUseDefaultsAndAuthenticatedUserIgnoringExternalOwnership() throws Exception {
            when(listWorkoutSessionsUseCase.execute(any())).thenReturn(
                    new ListWorkoutSessionsResult(new PageResult<>(List.of(), 0, 20, 0, 0, true)));
            mockMvc.perform(get(ENDPOINT).param("personId", "999").with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessions.content").isEmpty())
                    .andExpect(jsonPath("$.sessions.page").value(0))
                    .andExpect(jsonPath("$.sessions.size").value(20))
                    .andExpect(jsonPath("$.sessions.totalElements").value(0))
                    .andExpect(jsonPath("$.sessions.totalPages").value(0))
                    .andExpect(jsonPath("$.sessions.last").value(true));
            verify(listWorkoutSessionsUseCase).execute(new ListWorkoutSessionsCommand(
                    new UserId(1L), null, null, null, null, 0, 20));
        }

        @Test
        void shouldForwardAllFiltersAndReturnHistoricalSummary() throws Exception {
            var date = LocalDate.of(2026, 10, 26);
            var item = new ListWorkoutSessionsItemResult(new WorkoutSessionId(100L), null, null,
                    "Ciclo histórico", "Treino original", WeekDay.MONDAY, date, START, START.plusSeconds(60),
                    WorkoutSessionStatus.CLOSED, WorkoutSessionCloseReason.AUTO_EXPIRED);
            when(listWorkoutSessionsUseCase.execute(any())).thenReturn(
                    new ListWorkoutSessionsResult(new PageResult<>(List.of(item), 1, 2, 3, 2, true)));
            mockMvc.perform(get(ENDPOINT).with(authenticatedUser())
                            .param("workoutCycleId", "10").param("workoutDayId", "20")
                            .param("startDate", "2026-10-26").param("endDate", "2026-10-26")
                            .param("page", "1").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessions.totalElements").value(3))
                    .andExpect(jsonPath("$.sessions.totalPages").value(2))
                    .andExpect(jsonPath("$.sessions.page").value(1))
                    .andExpect(jsonPath("$.sessions.size").value(2))
                    .andExpect(jsonPath("$.sessions.last").value(true))
                    .andExpect(jsonPath("$.sessions.content[0].id").value(100))
                    .andExpect(jsonPath("$.sessions.content[0].status").value("CLOSED"))
                    .andExpect(jsonPath("$.sessions.content[0].closeReason").value("AUTO_EXPIRED"))
                    .andExpect(jsonPath("$.sessions.content[0].workoutCycleId").value(nullValue()))
                    .andExpect(jsonPath("$.sessions.content[0].workoutDayId").value(nullValue()))
                    .andExpect(jsonPath("$.sessions.content[0].workoutCycleNameSnapshot").value("Ciclo histórico"))
                    .andExpect(jsonPath("$.sessions.content[0].workoutDayTitleSnapshot").value("Treino original"))
                    .andExpect(jsonPath("$.sessions.content[0].weekDaySnapshot").value("MONDAY"))
                    .andExpect(jsonPath("$.sessions.content[0].performedOn").value("2026-10-26"))
                    .andExpect(jsonPath("$.sessions.content[0].startedAt").value(START.toString()))
                    .andExpect(jsonPath("$.sessions.content[0].finishedAt").value(START.plusSeconds(60).toString()))
                    .andExpect(jsonPath("$.sessions.content[0].activities").doesNotExist());
            verify(listWorkoutSessionsUseCase).execute(new ListWorkoutSessionsCommand(new UserId(1L),
                    new WorkoutCycleId(10L), new WorkoutDayId(20L), date, date, 1, 2));
        }

        @ParameterizedTest
        @CsvSource({"page,-1", "page,abc", "page,2147483648", "size,0", "size,101",
                "workoutCycleId,0", "workoutCycleId,abc", "workoutDayId,-1", "workoutDayId,abc",
                "startDate,2026-02-30", "endDate,2026-13-01", "startDate,invalid", "endDate,26/10/2026"})
        void shouldRejectInvalidParameters(String parameter, String value) throws Exception {
            mockMvc.perform(get(ENDPOINT).with(authenticatedUser()).param(parameter, value))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(listWorkoutSessionsUseCase);
        }

        @Test
        void shouldKeepReversedIntervalAsUnprocessableEntity() throws Exception {
            when(listWorkoutSessionsUseCase.execute(any())).thenThrow(
                    new OperationNotAllowedException("Data inicial não pode ser posterior à data final."));
            mockMvc.perform(get(ENDPOINT).with(authenticatedUser())
                            .param("startDate", "2026-10-27").param("endDate", "2026-10-26"))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void shouldAcceptMaximumPageSize() throws Exception {
            when(listWorkoutSessionsUseCase.execute(any())).thenReturn(
                    new ListWorkoutSessionsResult(new PageResult<>(List.of(), 0, 100, 0, 0, true)));
            mockMvc.perform(get(ENDPOINT).with(authenticatedUser()).param("size", "100"))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest
        @ValueSource(ints = {403, 404, 500})
        void shouldReuseExistingErrorResponses(int code) throws Exception {
            RuntimeException failure = switch (code) {
                case 403 -> new UserInactiveException("Usuário inativo.");
                case 404 -> new ResourceNotFoundException("Usuário não encontrado.");
                default -> new PersistenceException("offline");
            };
            when(listWorkoutSessionsUseCase.execute(any())).thenThrow(failure);
            mockMvc.perform(get(ENDPOINT).with(authenticatedUser())).andExpect(status().is(code));
        }
    }

    @MockitoBean private UpdateWorkoutSessionNotesUseCase updateWorkoutSessionNotesUseCase;
    @MockitoBean private UpdateWorkoutSessionActivityExecutionUseCase updateWorkoutSessionActivityExecutionUseCase;
    @MockitoBean private CreateWorkoutSessionSetUseCase createWorkoutSessionSetUseCase;
    @MockitoBean private UpdateWorkoutSessionSetUseCase updateWorkoutSessionSetUseCase;
    @MockitoBean private DeleteWorkoutSessionSetUseCase deleteWorkoutSessionSetUseCase;
    @MockitoBean private ChangeWorkoutSessionActivityLoadModeUseCase changeWorkoutSessionActivityLoadModeUseCase;

    private static final String ENDPOINT = "/api/users/me/workout-sessions";
    private static final Instant START = Instant.parse("2026-10-27T02:30:00Z");
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private StartWorkoutSessionUseCase startWorkoutSessionUseCase;
    @MockitoBean private GetWorkoutSessionDetailUseCase getWorkoutSessionDetailUseCase;
    @MockitoBean private GetInProgressWorkoutSessionUseCase getInProgressWorkoutSessionUseCase;
    @MockitoBean private ErrorLogPublisher errorLogPublisher;
    @MockitoBean private JwtAccessTokenValidator jwtAccessTokenValidator;
    @MockitoBean private UserRepository userRepository;

    @MockitoBean
    private CloseWorkoutSessionUseCase closeWorkoutSessionUseCase;

    @Nested
    class Close {

        @ParameterizedTest
        @ValueSource(strings = {"MANUAL", "AUTO_EXPIRED"})
        void shouldCloseUsingAuthenticatedUserAndReturnPersistedLifecycle(String reason) throws Exception {
            CloseWorkoutSessionCommand command = new CloseWorkoutSessionCommand(new UserId(77L), new WorkoutSessionId(100L));
            boolean expired = reason.equals("AUTO_EXPIRED");
            Instant finishedAt = expired ? Instant.parse("2026-10-27T06:00:00Z") : START.plusSeconds(60);
            Instant updatedAt = expired ? finishedAt.plusSeconds(172800) : finishedAt;
            when(closeWorkoutSessionUseCase.execute(command)).thenReturn(new CloseWorkoutSessionResult(
                    command.id(), LocalDate.of(2026, 10, 26), START, finishedAt,
                    WorkoutSessionStatus.CLOSED, WorkoutSessionCloseReason.valueOf(reason), updatedAt));

            mockMvc.perform(patch(ENDPOINT + "/100/close").with(authenticatedUser(77L, "actor@example.com", false)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.status").value("CLOSED"))
                    .andExpect(jsonPath("$.closeReason").value(reason))
                    .andExpect(jsonPath("$.performedOn").value("2026-10-26"))
                    .andExpect(jsonPath("$.startedAt").value(START.toString()))
                    .andExpect(jsonPath("$.finishedAt").value(finishedAt.toString()))
                    .andExpect(jsonPath("$.updatedAt").value(updatedAt.toString()));
            verify(closeWorkoutSessionUseCase).execute(command);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1", "invalid"})
        void shouldRejectInvalidSessionId(String id) throws Exception {
            mockMvc.perform(patch(ENDPOINT + "/" + id + "/close").with(authenticatedUser()))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(closeWorkoutSessionUseCase);
        }

        @Test
        void shouldRejectClosedSessionWithExistingDomainErrorContract() throws Exception {
            when(closeWorkoutSessionUseCase.execute(any()))
                    .thenThrow(new InvalidWorkoutSessionException("Sessão já encerrada."));
            mockMvc.perform(patch(ENDPOINT + "/100/close").with(authenticatedUser()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Sessão já encerrada."));
        }

        @Test
        void shouldReturnNotFoundForMissingOrForeignSession() throws Exception {
            when(closeWorkoutSessionUseCase.execute(any()))
                    .thenThrow(new ResourceNotFoundException("Sessão de treino não encontrada."));
            mockMvc.perform(patch(ENDPOINT + "/100/close").with(authenticatedUser()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnForbiddenForInactiveUser() throws Exception {
            when(closeWorkoutSessionUseCase.execute(any())).thenThrow(new UserInactiveException("Usuário inativo."));
            mockMvc.perform(patch(ENDPOINT + "/100/close").with(authenticatedUser()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnConflictForConcurrentClosure() throws Exception {
            when(closeWorkoutSessionUseCase.execute(any())).thenThrow(new WorkoutSessionConflictException("Conflito"));
            mockMvc.perform(patch(ENDPOINT + "/100/close").with(authenticatedUser()))
                    .andExpect(status().isConflict());
        }

        @Test
        void shouldReturnInternalServerErrorForUnexpectedPersistenceFailure() throws Exception {
            when(closeWorkoutSessionUseCase.execute(any())).thenThrow(new PersistenceException("Falha"));
            mockMvc.perform(patch(ENDPOINT + "/100/close").with(authenticatedUser()))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class Start {

        @Test
        void shouldStartSessionUsingAuthenticatedUserAndReturnCreated() throws Exception {
            StartWorkoutSessionCommand command = new StartWorkoutSessionCommand(
                    new UserId(77L), new WorkoutCycleId(10L), new WorkoutDayId(20L));
            when(startWorkoutSessionUseCase.execute(command)).thenReturn(startResult());

            mockMvc.perform(post(ENDPOINT).with(authenticatedUser(77L, "actor@example.com", false))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new StartWorkoutSessionRequest(10L, 20L))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.workoutCycleId").value(10))
                    .andExpect(jsonPath("$.workoutDayId").value(20))
                    .andExpect(jsonPath("$.performedOn").value("2026-10-26"))
                    .andExpect(jsonPath("$.startedAt").value(START.toString()))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.createdAt").value(START.toString()));
            verify(startWorkoutSessionUseCase).execute(command);
        }

        @Test
        void shouldNeverTakeOwnershipOrExecutionFromAdditionalClientFields() throws Exception {
            StartWorkoutSessionCommand command = startCommand();
            when(startWorkoutSessionUseCase.execute(command)).thenReturn(startResult());

            mockMvc.perform(post(ENDPOINT).with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON).content("""
                                    {"workoutCycleId":10,"workoutDayId":20,"personId":999,
                                     "actorUserId":999,"actualLoadKg":200,"actualReps":20,
                                     "workoutCycleNameSnapshot":"Cliente","sets":[{"setNumber":1}]}
                                    """))
                    .andExpect(status().isCreated());
            verify(startWorkoutSessionUseCase).execute(command);
        }

        @ParameterizedTest
        @CsvSource({",20", "10,", ",", "0,20", "-1,20", "10,0", "10,-1"})
        void shouldRejectMissingOrNonPositivePlanningIds(Long cycleId, Long dayId) throws Exception {
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new StartWorkoutSessionRequest(cycleId, dayId))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Erro de validação nos campos de requisição"))
                    .andExpect(jsonPath("$.fields").isNotEmpty());
            verifyNoInteractions(startWorkoutSessionUseCase);
        }

        @Test
        void shouldRejectMalformedJson() throws Exception {
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"workoutCycleId\":"))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(startWorkoutSessionUseCase);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Somente um ciclo em andamento permite iniciar sessão.",
                "Planejamento de origem não pertence à pessoa ou ao ciclo.",
                "O dia de treino deve possuir pelo menos uma atividade planejada."
        })
        void shouldKeepDomainValidationAsBadRequest(String message) throws Exception {
            when(startWorkoutSessionUseCase.execute(startCommand())).thenThrow(new InvalidWorkoutSessionException(message));
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new StartWorkoutSessionRequest(10L, 20L))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(message));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Usuário não encontrado.", "Pessoa não encontrada.",
                "Ciclo de treino não encontrado.", "Dia de treino não encontrado."})
        void shouldReturnNotFoundForMissingOrForeignResource(String message) throws Exception {
            when(startWorkoutSessionUseCase.execute(startCommand())).thenThrow(new ResourceNotFoundException(message));
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new StartWorkoutSessionRequest(10L, 20L))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(message));
        }

        @Test
        void shouldReturnForbiddenForInactiveUser() throws Exception {
            when(startWorkoutSessionUseCase.execute(startCommand())).thenThrow(new UserInactiveException("Usuário inativo."));
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new StartWorkoutSessionRequest(10L, 20L))))
                    .andExpect(status().isForbidden());
        }

        @ParameterizedTest
        @ValueSource(strings = {"Já existe uma sessão em andamento para a pessoa.",
                "Prescrição foi alterada. Recarregue a ficha."})
        void shouldReturnConflictForOpenSessionOrConcurrentChange(String message) throws Exception {
            when(startWorkoutSessionUseCase.execute(startCommand())).thenThrow(new WorkoutSessionConflictException(message));
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new StartWorkoutSessionRequest(10L, 20L))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.path").value(ENDPOINT));
        }

        @Test
        void shouldKeepUnexpectedPersistenceFailureAsInternalServerError() throws Exception {
            when(startWorkoutSessionUseCase.execute(startCommand())).thenThrow(new PersistenceException("Falha de persistência."));
            mockMvc.perform(post(ENDPOINT).with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new StartWorkoutSessionRequest(10L, 20L))))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class Detail {

        @Test
        void shouldRecoverExpiredSessionWithHistoricalData() throws Exception {
            var original = detailResult(true, true);
            Instant deadline = Instant.parse("2026-10-27T06:00:00Z");
            Instant processing = deadline.plusSeconds(172800);
            var expired = new WorkoutSessionDetailResult(
                    original.id(), original.personId(), original.workoutCycleId(), original.workoutDayId(),
                    original.workoutCycleNameSnapshot(), original.workoutDayTitleSnapshot(), original.weekDaySnapshot(),
                    original.performedOn(), original.startedAt(), deadline, WorkoutSessionStatus.CLOSED,
                    WorkoutSessionCloseReason.AUTO_EXPIRED, original.notes(), original.createdAt(), processing,
                    original.activities());
            when(getWorkoutSessionDetailUseCase.execute(any())).thenReturn(expired);

            mockMvc.perform(get(ENDPOINT + "/100").with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CLOSED"))
                    .andExpect(jsonPath("$.closeReason").value("AUTO_EXPIRED"))
                    .andExpect(jsonPath("$.finishedAt").value(deadline.toString()))
                    .andExpect(jsonPath("$.updatedAt").value(processing.toString()))
                    .andExpect(jsonPath("$.workoutDayId").value(nullValue()))
                    .andExpect(jsonPath("$.activities[0].exerciseNameSnapshot").value("Exercício histórico"))
                    .andExpect(jsonPath("$.activities[0].sets.length()").value(2));
        }

        @Test
        void shouldReturnCompleteSnapshotAndRegisteredExecution() throws Exception {
            GetWorkoutSessionDetailCommand command = new GetWorkoutSessionDetailCommand(new UserId(1L), new WorkoutSessionId(100L));
            when(getWorkoutSessionDetailUseCase.execute(command)).thenReturn(detailResult(false, true));

            mockMvc.perform(get(ENDPOINT + "/100").with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.personId").value(3))
                    .andExpect(jsonPath("$.workoutCycleId").value(10))
                    .andExpect(jsonPath("$.workoutDayId").value(20))
                    .andExpect(jsonPath("$.workoutCycleNameSnapshot").value("Ciclo histórico"))
                    .andExpect(jsonPath("$.workoutDayTitleSnapshot").value("Treino A histórico"))
                    .andExpect(jsonPath("$.weekDaySnapshot").value("MONDAY"))
                    .andExpect(jsonPath("$.performedOn").value("2026-10-26"))
                    .andExpect(jsonPath("$.startedAt").value(START.toString()))
                    .andExpect(jsonPath("$.finishedAt").value(nullValue()))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.closeReason").value(nullValue()))
                    .andExpect(jsonPath("$.notes").value("Relato da sessão"))
                    .andExpect(jsonPath("$.createdAt").value(START.toString()))
                    .andExpect(jsonPath("$.updatedAt").value(START.plusSeconds(5).toString()))
                    .andExpect(jsonPath("$.activities.length()").value(1))
                    .andExpect(jsonPath("$.activities[0].id").value(30))
                    .andExpect(jsonPath("$.activities[0].workoutActivityId").value(40))
                    .andExpect(jsonPath("$.activities[0].exerciseId").value(50))
                    .andExpect(jsonPath("$.activities[0].exerciseNameSnapshot").value("Exercício histórico"))
                    .andExpect(jsonPath("$.activities[0].activityTypeSnapshot").value("STRENGTH"))
                    .andExpect(jsonPath("$.activities[0].orderIndexSnapshot").value(1))
                    .andExpect(jsonPath("$.activities[0].plannedSetsSnapshot").value(4))
                    .andExpect(jsonPath("$.activities[0].plannedRepRangeMinSnapshot").value(8))
                    .andExpect(jsonPath("$.activities[0].plannedRepRangeMaxSnapshot").value(12))
                    .andExpect(jsonPath("$.activities[0].plannedTargetLoadKgSnapshot").value(90.50))
                    .andExpect(jsonPath("$.activities[0].plannedTargetLoadTextSnapshot").value("Cada lado"))
                    .andExpect(jsonPath("$.activities[0].plannedDurationMinutesSnapshot").value(40))
                    .andExpect(jsonPath("$.activities[0].plannedDistanceKmSnapshot").value(5.50))
                    .andExpect(jsonPath("$.activities[0].plannedIntensityTextSnapshot").value("Moderada"))
                    .andExpect(jsonPath("$.activities[0].plannedRestSecondsSnapshot").value(90))
                    .andExpect(jsonPath("$.activities[0].plannedNotesSnapshot").value("Prescrição histórica"))
                    .andExpect(jsonPath("$.activities[0].actualDurationMinutes").value(20))
                    .andExpect(jsonPath("$.activities[0].actualDistanceKm").value(2.25))
                    .andExpect(jsonPath("$.activities[0].notes").value("Execução parcial"))
                    .andExpect(jsonPath("$.activities[0].createdAt").value(START.toString()))
                    .andExpect(jsonPath("$.activities[0].updatedAt").value(START.plusSeconds(4).toString()))
                    .andExpect(jsonPath("$.activities[0].sets.length()").value(2))
                    .andExpect(jsonPath("$.activities[0].sets[0].id").value(60))
                    .andExpect(jsonPath("$.activities[0].sets[0].setNumber").value(1))
                    .andExpect(jsonPath("$.activities[0].sets[0].actualLoadKg").value(42.50))
                    .andExpect(jsonPath("$.activities[0].sets[0].actualLoadMode").value("PER_SIDE"))
                    .andExpect(jsonPath("$.activities[0].actualLoadMode").value("PER_SIDE"))
                    .andExpect(jsonPath("$.activities[0].sets[0].normalizedLoadKg").value(85.0))
                    .andExpect(jsonPath("$.activities[0].sets[0].actualReps").value(9))
                    .andExpect(jsonPath("$.activities[0].sets[0].notes").value("Série registrada"))
                    .andExpect(jsonPath("$.activities[0].sets[0].createdAt").value(START.plusSeconds(1).toString()))
                    .andExpect(jsonPath("$.activities[0].sets[0].updatedAt").value(START.plusSeconds(2).toString()))
                    .andExpect(jsonPath("$.activities[0].sets[1].actualLoadKg").value(nullValue()))
                    .andExpect(jsonPath("$.activities[0].sets[1].actualLoadMode").value(nullValue()))
                    .andExpect(jsonPath("$.activities[0].sets[1].normalizedLoadKg").value(nullValue()))
                    .andExpect(jsonPath("$.activities[0].sets[1].actualReps").value(nullValue()))
                    .andExpect(jsonPath("$.activities[0].sets[1].notes").value("Somente relato"));
            verify(getWorkoutSessionDetailUseCase).execute(command);
        }

        @Test
        void shouldReturnSnapshotWhenOriginReferencesAreNull() throws Exception {
            when(getWorkoutSessionDetailUseCase.execute(any())).thenReturn(detailResult(true, false));
            mockMvc.perform(get(ENDPOINT + "/100").with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.workoutCycleId").value(nullValue()))
                    .andExpect(jsonPath("$.workoutDayId").value(nullValue()))
                    .andExpect(jsonPath("$.activities[0].workoutActivityId").value(nullValue()))
                    .andExpect(jsonPath("$.activities[0].exerciseId").value(nullValue()))
                    .andExpect(jsonPath("$.activities[0].exerciseNameSnapshot").value("Exercício histórico"))
                    .andExpect(jsonPath("$.activities[0].sets").isEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1", "invalid"})
        void shouldRejectInvalidSessionId(String id) throws Exception {
            mockMvc.perform(get(ENDPOINT + "/" + id).with(authenticatedUser())).andExpect(status().isBadRequest());
            verifyNoInteractions(getWorkoutSessionDetailUseCase);
        }

        @Test
        void shouldReturnNotFoundForMissingOrForeignSession() throws Exception {
            when(getWorkoutSessionDetailUseCase.execute(any()))
                    .thenThrow(new ResourceNotFoundException("Sessão de treino não encontrada."));
            mockMvc.perform(get(ENDPOINT + "/100").with(authenticatedUser()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Sessão de treino não encontrada."));
        }

        @Test
        void shouldReturnForbiddenForInactiveUser() throws Exception {
            when(getWorkoutSessionDetailUseCase.execute(any())).thenThrow(new UserInactiveException("Usuário inativo."));
            mockMvc.perform(get(ENDPOINT + "/100").with(authenticatedUser())).andExpect(status().isForbidden());
        }
    }

    @Nested
    class InProgress {

        @Test
        void shouldResolveLiteralRouteAndReturnExecutionToResume() throws Exception {
            GetInProgressWorkoutSessionCommand command = new GetInProgressWorkoutSessionCommand(new UserId(77L));
            when(getInProgressWorkoutSessionUseCase.execute(command)).thenReturn(detailResult(false, true));
            mockMvc.perform(get(ENDPOINT + "/in-progress").with(authenticatedUser(77L, "actor@example.com", false)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.activities[0].id").value(30))
                    .andExpect(jsonPath("$.activities[0].plannedSetsSnapshot").value(4))
                    .andExpect(jsonPath("$.activities[0].sets[0].actualLoadKg").value(42.50))
                    .andExpect(jsonPath("$.activities[0].sets[0].actualReps").value(9));
            verify(getInProgressWorkoutSessionUseCase).execute(command);
            verifyNoInteractions(getWorkoutSessionDetailUseCase);
        }

        @Test
        void shouldReturnEmptySetsWithoutFillingExecutionFromPrescription() throws Exception {
            when(getInProgressWorkoutSessionUseCase.execute(any())).thenReturn(detailResult(false, false));
            mockMvc.perform(get(ENDPOINT + "/in-progress").with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.activities[0].plannedSetsSnapshot").value(4))
                    .andExpect(jsonPath("$.activities[0].plannedTargetLoadKgSnapshot").value(90.50))
                    .andExpect(jsonPath("$.activities[0].actualDurationMinutes").value(nullValue()))
                    .andExpect(jsonPath("$.activities[0].actualDistanceKm").value(nullValue()))
                    .andExpect(jsonPath("$.activities[0].notes").value(nullValue()))
                    .andExpect(jsonPath("$.activities[0].sets").isEmpty());
        }

        @Test
        void shouldReturnNotFoundWhenNoSessionIsOpen() throws Exception {
            when(getInProgressWorkoutSessionUseCase.execute(any()))
                    .thenThrow(new ResourceNotFoundException("Sessão de treino em andamento não encontrada."));
            mockMvc.perform(get(ENDPOINT + "/in-progress").with(authenticatedUser()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Sessão de treino em andamento não encontrada."));
            verifyNoInteractions(getWorkoutSessionDetailUseCase);
        }

        @Test
        void shouldReturnForbiddenForInactiveUser() throws Exception {
            when(getInProgressWorkoutSessionUseCase.execute(any())).thenThrow(new UserInactiveException("Usuário inativo."));
            mockMvc.perform(get(ENDPOINT + "/in-progress").with(authenticatedUser())).andExpect(status().isForbidden());
        }
    }

    @Nested
    class Execution {
        private static final String ACTIVITY = "/100/activities/30";
        private static final String SET = ACTIVITY + "/sets/60";

        @Test
        void shouldClearSessionNotesWithExplicitNull() throws Exception {
            when(updateWorkoutSessionNotesUseCase.execute(any())).thenReturn(detailResult(false, false));
            mockMvc.perform(patch(ENDPOINT + "/100/notes").with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"notes\":null}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.notes").value(nullValue()));
            verify(updateWorkoutSessionNotesUseCase).execute(new UpdateWorkoutSessionNotesCommand(
                    new UserId(1L), new WorkoutSessionId(100L), null, true));
        }

        @Test
        void shouldPreserveActivityFieldPresenceThroughRestMapper() throws Exception {
            when(updateWorkoutSessionActivityExecutionUseCase.execute(any())).thenReturn(detailResult(false, true).activities().getFirst());
            mockMvc.perform(patch(ENDPOINT + ACTIVITY + "/execution").with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"actualDurationMinutes\":null,\"notes\":\"Relato\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.actualLoadMode").value("PER_SIDE"))
                    .andExpect(jsonPath("$.sets[0].normalizedLoadKg").value(85.0));
            verify(updateWorkoutSessionActivityExecutionUseCase).execute(new UpdateWorkoutSessionActivityExecutionCommand(
                    new UserId(1L), new WorkoutSessionId(100L), new WorkoutSessionActivityId(30L),
                    null, true, null, false, "Relato", true));
        }

        @ParameterizedTest
        @ValueSource(strings = {"TOTAL", "PER_SIDE"})
        void shouldCreateLoadedSetWithoutReps(String mode) throws Exception {
            ActualLoadMode loadMode = ActualLoadMode.valueOf(mode);
            var domain = com.ironcore.domain.workoutsession.workoutsessionset.model.WorkoutSessionSet.restore(
                    new WorkoutSessionSetId(60L), 4, new BigDecimal("20"), loadMode, null, null, START, null);
            when(createWorkoutSessionSetUseCase.execute(any())).thenReturn(WorkoutSessionSetDetailResult.from(domain));
            mockMvc.perform(post(ENDPOINT + ACTIVITY + "/sets").with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"setNumber\":4,\"actualLoadKg\":20,\"actualLoadMode\":\"" + mode + "\"}"))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(60))
                    .andExpect(jsonPath("$.actualReps").value(nullValue()))
                    .andExpect(jsonPath("$.normalizedLoadKg").value(mode.equals("TOTAL") ? 20 : 40));
            verify(createWorkoutSessionSetUseCase).execute(new CreateWorkoutSessionSetCommand(
                    new UserId(1L), new WorkoutSessionId(100L), new WorkoutSessionActivityId(30L),
                    4, new BigDecimal("20"), loadMode, null, null));
        }

        @Test
        void shouldCreateSetWithRepsAndNoExternalLoad() throws Exception {
            var domain = com.ironcore.domain.workoutsession.workoutsessionset.model.WorkoutSessionSet.restore(
                    new WorkoutSessionSetId(60L), 1, null, null, 12, null, START, null);
            when(createWorkoutSessionSetUseCase.execute(any())).thenReturn(WorkoutSessionSetDetailResult.from(domain));
            mockMvc.perform(post(ENDPOINT + ACTIVITY + "/sets").with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"setNumber\":1,\"actualReps\":12}"))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.actualLoadKg").value(nullValue()))
                    .andExpect(jsonPath("$.normalizedLoadKg").value(nullValue()));
        }

        @Test
        void shouldPreserveSetFieldPresenceThroughRestMapper() throws Exception {
            when(updateWorkoutSessionSetUseCase.execute(any())).thenReturn(detailResult(false, true).activities().getFirst().sets().getFirst());
            mockMvc.perform(patch(ENDPOINT + SET).with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"actualLoadKg\":null,\"actualLoadMode\":null}"))
                    .andExpect(status().isOk());
            verify(updateWorkoutSessionSetUseCase).execute(new UpdateWorkoutSessionSetCommand(
                    new UserId(1L), new WorkoutSessionId(100L), new WorkoutSessionActivityId(30L), new WorkoutSessionSetId(60L),
                    null, true, null, true, null, false, null, false));
        }

        @Test
        void shouldDeleteSetWithoutResponseBody() throws Exception {
            mockMvc.perform(delete(ENDPOINT + SET).with(authenticatedUser())).andExpect(status().isNoContent());
            verify(deleteWorkoutSessionSetUseCase).execute(new DeleteWorkoutSessionSetCommand(
                    new UserId(1L), new WorkoutSessionId(100L), new WorkoutSessionActivityId(30L), new WorkoutSessionSetId(60L)));
        }

        @Test
        void shouldChangeLoadModeForActivity() throws Exception {
            when(changeWorkoutSessionActivityLoadModeUseCase.execute(any())).thenReturn(detailResult(false, true).activities().getFirst());
            mockMvc.perform(patch(ENDPOINT + ACTIVITY + "/load-mode").with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"actualLoadMode\":\"PER_SIDE\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.actualLoadMode").value("PER_SIDE"));
            verify(changeWorkoutSessionActivityLoadModeUseCase).execute(new ChangeWorkoutSessionActivityLoadModeCommand(
                    new UserId(1L), new WorkoutSessionId(100L), new WorkoutSessionActivityId(30L), ActualLoadMode.PER_SIDE));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/100/notes", "/100/activities/30/execution", "/100/activities/30/sets/60"})
        void shouldRejectPatchWithoutRecognizedFields(String path) throws Exception {
            mockMvc.perform(patch(ENDPOINT + path).with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
            verifyNoInteractions(updateWorkoutSessionNotesUseCase, updateWorkoutSessionActivityExecutionUseCase, updateWorkoutSessionSetUseCase);
        }

        @ParameterizedTest
        @ValueSource(strings = {"{}", "{\"actualLoadMode\":null}", "{\"actualLoadMode\":\"UNKNOWN\"}"})
        void shouldRejectMissingNullOrUnknownLoadMode(String body) throws Exception {
            mockMvc.perform(patch(ENDPOINT + ACTIVITY + "/load-mode").with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
            verifyNoInteractions(changeWorkoutSessionActivityLoadModeUseCase);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1", "invalid"})
        void shouldRejectInvalidChildIdentifiers(String id) throws Exception {
            mockMvc.perform(patch(ENDPOINT + "/100/activities/" + id + "/execution").with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"notes\":null}")).andExpect(status().isBadRequest());
            mockMvc.perform(delete(ENDPOINT + ACTIVITY + "/sets/" + id).with(authenticatedUser())).andExpect(status().isBadRequest());
            verifyNoInteractions(updateWorkoutSessionActivityExecutionUseCase, deleteWorkoutSessionSetUseCase);
        }

        @ParameterizedTest
        @CsvSource({"NOTES,404", "ACTIVITY,404", "CREATE,404", "UPDATE,404", "DELETE,404", "MODE,404",
                "NOTES,409", "ACTIVITY,409", "CREATE,409", "UPDATE,409", "DELETE,409", "MODE,409"})
        void shouldMapOwnershipAndConcurrencyErrors(String operation, int status) throws Exception {
            RuntimeException exception = status == 404 ? new ResourceNotFoundException("Registro não encontrado.")
                    : new WorkoutSessionConflictException("Conflito");
            var request = switch (operation) {
                case "NOTES" -> {
                    doThrow(exception).when(updateWorkoutSessionNotesUseCase).execute(any());
                    yield patch(ENDPOINT + "/100/notes").content("{\"notes\":null}");
                }
                case "ACTIVITY" -> {
                    doThrow(exception).when(updateWorkoutSessionActivityExecutionUseCase).execute(any());
                    yield patch(ENDPOINT + ACTIVITY + "/execution").content("{\"notes\":null}");
                }
                case "CREATE" -> {
                    doThrow(exception).when(createWorkoutSessionSetUseCase).execute(any());
                    yield post(ENDPOINT + ACTIVITY + "/sets").content("{\"setNumber\":1,\"actualReps\":8}");
                }
                case "UPDATE" -> {
                    doThrow(exception).when(updateWorkoutSessionSetUseCase).execute(any());
                    yield patch(ENDPOINT + SET).content("{\"notes\":null}");
                }
                case "DELETE" -> {
                    doThrow(exception).when(deleteWorkoutSessionSetUseCase).execute(any());
                    yield delete(ENDPOINT + SET);
                }
                default -> {
                    doThrow(exception).when(changeWorkoutSessionActivityLoadModeUseCase).execute(any());
                    yield patch(ENDPOINT + ACTIVITY + "/load-mode").content("{\"actualLoadMode\":\"TOTAL\"}");
                }
            };
            mockMvc.perform(request.with(authenticatedUser()).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(status)).andExpect(jsonPath("$.status").value(status));
        }

        @ParameterizedTest
        @ValueSource(ints = {400, 403, 409, 500})
        void shouldReuseExistingExceptionMapping(int code) throws Exception {
            RuntimeException exception = switch (code) {
                case 400 -> new InvalidWorkoutSessionSetException("Série vazia.");
                case 403 -> new UserInactiveException("Usuário inativo.");
                case 409 -> new com.ironcore.application.exception.ResourceAlreadyExistsException("Número repetido.");
                default -> new PersistenceException("Indisponível.");
            };
            when(createWorkoutSessionSetUseCase.execute(any())).thenThrow(exception);
            mockMvc.perform(post(ENDPOINT + ACTIVITY + "/sets").with(authenticatedUser())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"setNumber\":1,\"actualReps\":8}"))
                    .andExpect(status().is(code)).andExpect(jsonPath("$.message").value(
                            code == 500 ? "Erro interno de persistência." : exception.getMessage()));
        }
    }

    private StartWorkoutSessionCommand startCommand() {
        return new StartWorkoutSessionCommand(new UserId(1L), new WorkoutCycleId(10L), new WorkoutDayId(20L));
    }

    private StartWorkoutSessionResult startResult() {
        return new StartWorkoutSessionResult(new WorkoutSessionId(100L), new WorkoutCycleId(10L), new WorkoutDayId(20L),
                LocalDate.of(2026, 10, 26), START, WorkoutSessionStatus.IN_PROGRESS, START);
    }

    private WorkoutSessionDetailResult detailResult(boolean withoutOrigins, boolean withExecution) {
        List<WorkoutSessionSetDetailResult> sets = withExecution ? List.of(
                new WorkoutSessionSetDetailResult(new WorkoutSessionSetId(60L), 1, new BigDecimal("42.50"),
                        ActualLoadMode.PER_SIDE, 9, "Série registrada", START.plusSeconds(1), START.plusSeconds(2), new BigDecimal("85.00")),
                new WorkoutSessionSetDetailResult(new WorkoutSessionSetId(61L), 2, null, null, null,
                        "Somente relato", START.plusSeconds(3), null, null)) : List.of();
        WorkoutSessionActivityDetailResult activity = new WorkoutSessionActivityDetailResult(
                new WorkoutSessionActivityId(30L), withoutOrigins ? null : new WorkoutActivityId(40L),
                withoutOrigins ? null : new ExerciseId(50L), "Exercício histórico", "STRENGTH", 1,
                4, 8, 12, new BigDecimal("90.50"), "Cada lado", 40, new BigDecimal("5.50"), "Moderada", 90,
                "Prescrição histórica", withExecution ? 20 : null, withExecution ? new BigDecimal("2.25") : null,
                withExecution ? "Execução parcial" : null, START, withExecution ? START.plusSeconds(4) : null, sets,
                withExecution ? ActualLoadMode.PER_SIDE : null);
        return new WorkoutSessionDetailResult(new WorkoutSessionId(100L), new PersonId(3L),
                withoutOrigins ? null : new WorkoutCycleId(10L), withoutOrigins ? null : new WorkoutDayId(20L),
                "Ciclo histórico", "Treino A histórico", WeekDay.MONDAY, LocalDate.of(2026, 10, 26), START, null,
                WorkoutSessionStatus.IN_PROGRESS, null, withExecution ? "Relato da sessão" : null,
                START, withExecution ? START.plusSeconds(5) : null, List.of(activity));
    }
}
