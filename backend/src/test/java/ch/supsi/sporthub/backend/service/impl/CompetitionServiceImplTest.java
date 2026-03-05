package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.request.CreateCompetitionRequest;
import ch.supsi.sporthub.backend.dto.response.CompetitionCreationResult;
import ch.supsi.sporthub.backend.dto.response.CompetitionSummaryResponse;
import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.api.ICompetitionReadService;
import ch.supsi.sporthub.backend.service.api.ICompetitionWriteService;
import ch.supsi.sporthub.backend.service.api.INotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompetitionServiceImplTest {

    @Mock
    private INotificationService notificationService;

    @Mock
    private ICompetitionReadService competitionReadService;

    @Mock
    private ICompetitionWriteService competitionWriteService;

    @InjectMocks
    private CompetitionServiceImpl competitionService;

    private User testUser;
    private Competition testCompetition;
    private CompetitionGoal testGoal;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testCompetition = new Competition();
        testCompetition.setId(1L);
        testCompetition.setName("Test Competition");
        testCompetition.setMaxParticipants(10);
        testCompetition.setDeadline(LocalDate.now().plusDays(10));
        testCompetition.setEndDate(LocalDate.now().plusDays(20));
        testCompetition.setChatId("group-123");

        testGoal = new CompetitionGoal();
        testGoal.setId(1L);
        testGoal.setCompetition(testCompetition);
        testGoal.setGoalType(GoalType.DISTANCE);
        testGoal.setSportType(SportType.RUN);
        testGoal.setTargetValue(100.0);
    }

    @Test
    void createCompetition_ValidRequest_ReturnsCompetitionSummaryResponseAndSendsNotification() {
        CreateCompetitionRequest request = new CreateCompetitionRequest();
        Competition testCompetition = new Competition();
        testCompetition.setId(1L);
        testCompetition.setName("Test Competition");

        CompetitionGoal testGoal = new CompetitionGoal();
        testGoal.setGoalType(GoalType.DISTANCE);
        testGoal.setSportType(SportType.RUN);

        CompetitionCreationResult creationResult = new CompetitionCreationResult(testCompetition, testGoal, "Success");
        when(competitionWriteService.createCompetition(request, testUser)).thenReturn(creationResult);
        CompetitionSummaryResponse result = competitionService.createCompetition(request, testUser);

        assertNotNull(result);
        assertEquals(testCompetition.getId(), result.getCompetitionId());
        assertEquals(testCompetition.getName(), result.getName());
        assertEquals(testCompetition.getMaxParticipants(), result.getMaxParticipants());
        assertEquals(testCompetition.isFinished(), result.isFinished());
        assertEquals(testCompetition.isStarted(), result.isStarted());
        assertEquals(testCompetition.getChatId(), result.getChatId());
        assertEquals(testGoal.getGoalType(), result.getGoalType());
        assertEquals(testGoal.getSportType(), result.getSportType());
        verify(notificationService).notifyCompetitionCreated(testCompetition, testGoal);
    }


    @Test
    void joinCompetition_DelegatesToWriteService() {
        competitionService.joinCompetition(1L, testUser);
        verify(competitionWriteService).joinCompetition(1L, testUser);
    }

    @Test
    void leaveCompetition_DelegatesToWriteService() {
        competitionService.leaveCompetition(1L, testUser);
        verify(competitionWriteService).leaveCompetition(1L, testUser);
    }

    @Test
    void saveCompetitionGoal_DelegatesToWriteService() {
        competitionService.saveCompetitionGoal(testGoal);
        verify(competitionWriteService).saveCompetitionGoal(testGoal);
    }

    @Test
    void saveCompetition_DelegatesToWriteService() {
        competitionService.saveCompetition(testCompetition);
        verify(competitionWriteService).saveCompetition(testCompetition);
    }

    @Test
    void deleteCompetition_DelegatesToWriteService() {
        competitionService.deleteCompetition(1L);
        verify(competitionWriteService).deleteCompetition(1L);
    }


    @Test
    void getLeaderboardEntriesForCompetition_DelegatesToReadService() {
        LeaderboardEntryResponse expectedEntry = new LeaderboardEntryResponse(
                100.5,
                "testUser",
                "testUserTG"
        );
        List<LeaderboardEntryResponse> expected = List.of(expectedEntry);

        when(competitionReadService.getLeaderboardEntriesForCompetition(testCompetition))
                .thenReturn(expected);

        List<LeaderboardEntryResponse> result = competitionService.getLeaderboardEntriesForCompetition(testCompetition);

        assertEquals(expected, result);
        assertEquals(1, result.size());
        assertEquals(100.5, result.getFirst().getScore());
        assertEquals("testUser", result.getFirst().getSporthubUsername());
        assertEquals("testUserTG", result.getFirst().getTelegramUsername());
    }

    @Test
    void getCompetitionById_DelegatesToReadService() {
        when(competitionReadService.getCompetitionById(1L)).thenReturn(testCompetition);

        Competition result = competitionService.getCompetitionById(1L);

        assertEquals(testCompetition, result);
    }

    @Test
    void getGoalForCompetition_Found_ReturnsGoal() {
        when(competitionReadService.getGoalForCompetition(testCompetition)).thenReturn(Optional.of(testGoal));

        CompetitionGoal result = competitionService.getGoalForCompetition(testCompetition);

        assertEquals(testGoal, result);
    }

    @Test
    void getGoalForCompetition_NotFound_ThrowsException() {
        when(competitionReadService.getGoalForCompetition(testCompetition)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                competitionService.getGoalForCompetition(testCompetition));
    }

    @Test
    void getAllCompetitions_DelegatesToReadService() {
        List<Competition> expected = List.of(testCompetition);
        when(competitionReadService.getAllCompetitions()).thenReturn(expected);

        List<Competition> result = competitionService.getAllCompetitions();

        assertEquals(expected, result);
    }

    @Test
    void updateLeaderboardEntryScore_DelegatesToWriteService() {
        competitionService.updateLeaderboardEntryScore(testCompetition, "username", 100.0);
        verify(competitionWriteService).updateLeaderboardEntryScore(testCompetition, "username", 100.0);
    }

    @Test
    void getCompetitionsForUser_ReturnsCompetitionSummaries() {
        User testUser = new User();
        testUser.setId(1L);

        CompetitionSummaryResponse expectedResponse = new CompetitionSummaryResponse();
        expectedResponse.setCompetitionId(1L);
        expectedResponse.setName("Test Competition");
        expectedResponse.setMaxParticipants(10);
        expectedResponse.setCurrentParticipants(5);
        expectedResponse.setUserRegistered(true);
        expectedResponse.setGoalType(GoalType.DISTANCE);
        expectedResponse.setSportType(SportType.RUN);
        expectedResponse.setFinished(false);
        expectedResponse.setStarted(true);
        expectedResponse.setChatId("group-123");
        expectedResponse.setDeadline(LocalDate.now().plusDays(5));
        expectedResponse.setEndDate(LocalDate.now().plusDays(10));

        when(competitionReadService.getCompetitionsForUser(testUser))
                .thenReturn(List.of(expectedResponse));

        List<CompetitionSummaryResponse> result =
                competitionService.getCompetitionsForUser(testUser);

        assertEquals(1, result.size());
        CompetitionSummaryResponse actualResponse = result.get(0);

        assertEquals(1L, actualResponse.getCompetitionId());
        assertEquals("Test Competition", actualResponse.getName());
        assertEquals(10, actualResponse.getMaxParticipants());
        assertEquals(5, actualResponse.getCurrentParticipants());
        assertTrue(actualResponse.isUserRegistered());
        assertEquals(GoalType.DISTANCE, actualResponse.getGoalType());
        assertEquals(SportType.RUN, actualResponse.getSportType());
        assertFalse(actualResponse.isFinished());
        assertTrue(actualResponse.isStarted());
        assertEquals("group-123", actualResponse.getChatId());
        assertEquals(LocalDate.now().plusDays(5), actualResponse.getDeadline());
        assertEquals(LocalDate.now().plusDays(10), actualResponse.getEndDate());

        verify(competitionReadService).getCompetitionsForUser(testUser);
    }

    @Test
    void getCompetitionsForUser_EmptyList_ReturnsEmptyList() {
        User testUser = new User();

        when(competitionReadService.getCompetitionsForUser(testUser))
                .thenReturn(Collections.emptyList());

        List<CompetitionSummaryResponse> result =
                competitionService.getCompetitionsForUser(testUser);

        assertTrue(result.isEmpty());
        verify(competitionReadService).getCompetitionsForUser(testUser);
    }

}