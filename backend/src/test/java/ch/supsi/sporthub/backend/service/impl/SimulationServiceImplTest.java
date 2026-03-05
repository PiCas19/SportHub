package ch.supsi.sporthub.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.supsi.sporthub.backend.dto.request.SetGoalRequest;
import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.utils.StravaActivityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class SimulationServiceImplTest {

    @Mock private ICompetitionService competitionService;
    @Mock private IGoalService goalService;
    @Mock private INotificationService notificationService;
    @Mock private ILeaderboardService leaderboardService;
    @Mock private ITelegramService telegramService;

    @InjectMocks
    private SimulationServiceImpl simulationService;

    private User testUser;
    private String testChatId;
    private Competition testCompetition;
    private CompetitionGoal testCompetitionGoal;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testChatId = "12345";

        testCompetition = new Competition();
        testCompetition.setId(1L);
        testCompetition.setName("Test Competition");
        testCompetition.setMaxParticipants(10);
        testCompetition.setStarted(true);
        testCompetition.setFinished(false);
        testCompetition.setDeadline(LocalDate.now());
        testCompetition.setEndDate(LocalDate.now().plusDays(7));
        testCompetition.setChatId(testChatId);

        testCompetitionGoal = new CompetitionGoal();
        testCompetitionGoal.setId(1L);
        testCompetitionGoal.setGoalType(GoalType.DISTANCE);
        testCompetitionGoal.setSportType(SportType.RUN);
        testCompetitionGoal.setTargetValue(100.0);
        testCompetitionGoal.setCompetition(testCompetition);
    }

    @Test
    void simulateCompetition_ShouldCreateCompetitionAndGoal() {
        when(competitionService.getLeaderboardEntriesForCompetition(any())).thenReturn(List.of());
        simulationService.simulateCompetition(testUser, testChatId);
        verify(competitionService, atLeastOnce()).saveCompetition(any());
        verify(competitionService).saveCompetitionGoal(any());
    }

    @Test
    void simulateCompetition_ShouldNotifyMilestones() {
        when(competitionService.getLeaderboardEntriesForCompetition(any())).thenReturn(List.of());
        simulationService.simulateCompetition(testUser, testChatId);
        verify(notificationService, atLeastOnce()).notifyMilestoneReached(any(), any(), anyDouble());
    }

    @Test
    void simulateCompetition_ShouldFinishWhenTargetReached() {
        doAnswer(invocation -> {
            Competition comp = invocation.getArgument(0);
            comp.setId(1L);
            return null;
        }).when(competitionService).saveCompetition(any());

        when(competitionService.getLeaderboardEntriesForCompetition(any())).thenReturn(List.of(
                new LeaderboardEntryResponse(100.0, "testuser", null)
        ));
        when(leaderboardService.getLeaderboardById(any())).thenReturn(Optional.of(new Leaderboard()));

        simulationService.simulateCompetition(testUser, testChatId);

        verify(notificationService, atLeastOnce()).notifyCompetitionFinished(any());
        verify(competitionService).deleteCompetition(eq(1L));
    }

    @Test
    void simulateGoals_ShouldCreateAndCompleteGoal() {
        doAnswer(invocation -> {
            Goal goal = new Goal();
            goal.setId(1L);
            return null;
        }).when(goalService).createGoal(any(), eq(testUser));

        simulationService.simulateGoals(testUser, testChatId);

        verify(goalService).createGoal(any(), eq(testUser));
        verify(notificationService).notifyGoalReachedSimulation(eq(testUser), any(), anyDouble(), eq(testChatId));
        verify(goalService).deleteGoalForUser(eq(null), eq(testUser));
    }


    @Test
    void createCompetition_ShouldReturnCompetition() {
        Competition result = ReflectionTestUtils.invokeMethod(
                simulationService, "createCompetition", testUser, testChatId
        );
        assertNotNull(result);
        assertEquals("Marathon Challenge", result.getName());
    }

    @Test
    void createCompetitionGoal_ShouldReturnGoal() {
        CompetitionGoal result = ReflectionTestUtils.invokeMethod(
                simulationService, "createCompetitionGoal", testCompetition
        );
        assertNotNull(result);
        assertEquals(GoalType.DISTANCE, result.getGoalType());
    }

    @Test
    void calculateProgress_ShouldReturnCorrectValue() {
        double result = ReflectionTestUtils.invokeMethod(
                simulationService, "calculateProgress", testCompetitionGoal
        );
        assertTrue(result >= 0);
    }

    @Test
    void checkAndNotifyMilestones_ShouldNotifyAppropriately() {
        ReflectionTestUtils.invokeMethod(
                simulationService, "checkAndNotifyMilestones", 50.0, testCompetitionGoal, "testuser", testChatId
        );
        verify(notificationService, atLeastOnce()).notifyMilestoneReached(any(), any(), anyDouble());
    }

    @Test
    void updateLeaderboardDuringSimulation_ShouldUpdateCorrectly() {
        when(competitionService.getLeaderboardEntriesForCompetition(any())).thenReturn(List.of(
                new LeaderboardEntryResponse(50.0, "testuser", "telegramuser")
        ));
        ReflectionTestUtils.invokeMethod(
                simulationService, "updateLeaderboardDuringSimulation", 75.0, testCompetitionGoal, "testuser", "-10012345"
        );
        verify(competitionService).saveCompetition(any());
    }

    @Test
    void finishCompetition_ShouldCompleteAllSteps() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setScores(new HashMap<>());

        when(competitionService.getLeaderboardEntriesForCompetition(any())).thenReturn(List.of(
                new LeaderboardEntryResponse(100.0, "testuser", null)
        ));
        when(leaderboardService.getLeaderboardById(anyString())).thenReturn(Optional.of(leaderboard));

        ReflectionTestUtils.invokeMethod(
                simulationService, "finishCompetition", testCompetition, "testuser", testChatId, testCompetitionGoal
        );

        verify(notificationService, atLeastOnce()).notifyCompetitionFinished(any());
        verify(leaderboardService, atLeastOnce()).save(any());
    }

    @Test
    void isCompetitionActive_ShouldReturnCorrectStatus() {
        testCompetition.setEndDate(LocalDate.now().plusDays(1));
        boolean active = Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(
                simulationService, "isCompetitionActive", testCompetition
        ));
        assertTrue(active);

        testCompetition.setEndDate(LocalDate.now().minusDays(1));
        boolean expired = Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(
                simulationService, "isCompetitionActive", testCompetition
        ));
        assertFalse(expired);
    }

    @Test
    void showLeaderboardBeforeClosing_ShouldSendToGroup() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setName("Test Leaderboard");
        leaderboard.setChatId("12345");
        leaderboard.setScores(Map.of("testuser", 100));

        when(leaderboardService.getLeaderboardById(anyString())).thenReturn(Optional.of(leaderboard));

        ReflectionTestUtils.invokeMethod(simulationService, "sendMonthlyLeaderboard", "12345");

        verify(telegramService).sendMessage(eq("12345"), contains("Monthly Leaderboard"));
    }



    @Test
    void isPrivateChat_ShouldReturnFalseWhenChatIdInvalid() {
        boolean result = Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(
                simulationService, "isPrivateChat", "invalidChatId"
        ));
        assertFalse(result);
    }


    @Test
    void simulateGoals_ShouldNotNotifyGoalReached_WhenProgressLowerThanTarget() {
        User user = new User();
        user.setUsername("testuser");

        Goal goal = new Goal();
        goal.setGoalType(GoalType.DISTANCE);
        goal.setSportType(SportType.RUN);
        goal.setTargetValue(10.0);

        SetGoalRequest goalRequest = new SetGoalRequest();
        goalRequest.setGoalType(goal.getGoalType().name());
        goalRequest.setSportType(goal.getSportType().name());
        goalRequest.setTargetValue(goal.getTargetValue());
        goalRequest.setPeriod(Period.DAILY);

        when(goalService.createGoal(any(), any())).thenReturn(goal);
        StravaActivity activity = new StravaActivity();
        activity.setSportType("RUN");
        activity.setDistanceMeters(5000.0);

        List<StravaActivity> activities = List.of(activity);
        try (MockedStatic<StravaActivityUtils> utilities = mockStatic(StravaActivityUtils.class)) {
            utilities.when(StravaActivityUtils::createDemoActivities).thenReturn(activities);

            simulationService.simulateGoals(user, "12345");

            verify(notificationService, never()).notifyGoalReachedSimulation(any(), any(), anyDouble(), anyString());
            verify(goalService, never()).deleteGoalForUser(any(), any());
        }
    }

    private void invokeSendMonthlyLeaderboard(String chatId) throws Exception {
        Method method = SimulationServiceImpl.class.getDeclaredMethod("sendMonthlyLeaderboard", String.class);
        method.setAccessible(true);
        method.invoke(simulationService, chatId);
    }

    @Test
    void sendMonthlyLeaderboard_shouldHandleAllRanks() throws Exception {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setName("Test Leaderboard");
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("firstUser", 100);
        scores.put("secondUser", 90);
        scores.put("thirdUser", 80);
        scores.put("fourthUser", 70);
        leaderboard.setScores(scores);

        when(leaderboardService.getLeaderboardById("12345"))
                .thenReturn(Optional.of(leaderboard));

        invokeSendMonthlyLeaderboard("12345");
        verify(telegramService).sendMessage(eq("12345"), argThat(message ->
                message.contains("🥇") &&
                        message.contains("🥈") &&
                        message.contains("🥉") &&
                        message.contains("4. ") &&
                        message.contains("Monthly Leaderboard") &&
                        message.contains("Winner of the month")
        ));
    }

    @Test
    void sendMonthlyLeaderboard_shouldDoNothing_whenLeaderboardIsEmpty() throws Exception {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setName("Empty Leaderboard");
        leaderboard.setScores(new HashMap<>());

        when(leaderboardService.getLeaderboardById("chatId"))
                .thenReturn(Optional.of(leaderboard));

        invokeSendMonthlyLeaderboard("chatId");

        verify(telegramService, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void sendMonthlyLeaderboard_shouldDoNothing_whenLeaderboardNotFound() throws Exception {
        when(leaderboardService.getLeaderboardById("chatId"))
                .thenReturn(Optional.empty());

        invokeSendMonthlyLeaderboard("chatId");

        verify(telegramService, never()).sendMessage(anyString(), anyString());
    }

    private void invokeFinishCompetition(Competition competition, String username, String chatId) throws Exception {
        Method method = SimulationServiceImpl.class.getDeclaredMethod("finishCompetition", Competition.class, String.class, String.class, CompetitionGoal.class);
        method.setAccessible(true);
        method.invoke(simulationService, competition, username, chatId, null);
    }

    @Test
    void finishCompetition_shouldHandleUserFoundWithScore() throws Exception {
        Competition competition = new Competition();
        competition.setId(1L);

        LeaderboardEntryResponse entry = new LeaderboardEntryResponse(100.0, "testuser", "test_telegram");

        when(competitionService.getLeaderboardEntriesForCompetition(competition))
                .thenReturn(List.of(entry));

        invokeFinishCompetition(competition, "testuser", "chatId");

        verify(competitionService).deleteCompetition(competition.getId());
    }

    @Test
    void finishCompetition_shouldHandleUserFoundWithNullScore() throws Exception {
        Competition competition = new Competition();
        competition.setId(2L);

        LeaderboardEntryResponse entry = new LeaderboardEntryResponse(null, "testuser", "test_telegram");

        when(competitionService.getLeaderboardEntriesForCompetition(competition))
                .thenReturn(List.of(entry));

        invokeFinishCompetition(competition, "testuser", "chatId");

        verify(competitionService).deleteCompetition(competition.getId());
    }

    @Test
    void finishCompetition_shouldHandleUserNotFound() throws Exception {
        Competition competition = new Competition();
        competition.setId(3L);

        LeaderboardEntryResponse entry = new LeaderboardEntryResponse(50.0, "otheruser", "other_telegram");

        when(competitionService.getLeaderboardEntriesForCompetition(competition))
                .thenReturn(List.of(entry));

        invokeFinishCompetition(competition, "testuser", "chatId");

        verify(competitionService).deleteCompetition(competition.getId());
    }

    @Test
    void simulateCompetition_shouldSkipWhenCompetitionIsInactive() {
        User user = new User();
        user.setUsername("testuser");
        doAnswer(invocation -> {
            Competition competition = invocation.getArgument(0);
            competition.setEndDate(LocalDate.now().minusDays(1));
            return null;
        }).when(competitionService).saveCompetition(any(Competition.class));

        doNothing().when(competitionService).joinCompetition(any(), any());
        doNothing().when(competitionService).saveCompetitionGoal(any());
        simulationService.simulateCompetition(user, "12345");
        verify(notificationService, never()).notifyCompetitionFinished(any());
        verify(competitionService, never()).updateLeaderboardEntryScore(any(), any(), anyDouble());
    }

    @Test
    void shouldFinishCompetition_WhenProgressIsEnough() {
        User user = new User();
        user.setUsername("testuser");

        doAnswer(invocation -> {
            var competition = invocation.getArgument(0);
            ((ch.supsi.sporthub.backend.model.Competition) competition)
                    .setEndDate(LocalDate.now().plusDays(1));
            return null;
        }).when(competitionService).saveCompetition(any());

        doNothing().when(competitionService).joinCompetition(any(), any());
        doNothing().when(competitionService).saveCompetitionGoal(any());

        try (MockedStatic<StravaActivityUtils> utilities = mockStatic(StravaActivityUtils.class)) {
            StravaActivity activity = new StravaActivity();
            activity.setSportType("RUN");
            activity.setDistanceMeters(120000.0);

            utilities.when(StravaActivityUtils::createDemoActivities)
                    .thenReturn(List.of(activity));

            simulationService.simulateCompetition(user, "12345");

            verify(notificationService).notifyCompetitionFinished(any());
        }
    }

    @Test
    void shouldNotFinishCompetition_WhenProgressIsInsufficient() {
        User user = new User();
        user.setUsername("testuser");

        doAnswer(invocation -> {
            var competition = invocation.getArgument(0);
            ((ch.supsi.sporthub.backend.model.Competition) competition)
                    .setEndDate(LocalDate.now().plusDays(1));
            return null;
        }).when(competitionService).saveCompetition(any());

        doNothing().when(competitionService).joinCompetition(any(), any());
        doNothing().when(competitionService).saveCompetitionGoal(any());

        try (MockedStatic<StravaActivityUtils> utilities = mockStatic(StravaActivityUtils.class)) {
            StravaActivity activity = new StravaActivity();
            activity.setSportType("RUN");
            activity.setDistanceMeters(30000.0);

            utilities.when(StravaActivityUtils::createDemoActivities)
                    .thenReturn(List.of(activity));

            simulationService.simulateCompetition(user, "12345");

            verify(notificationService, never()).notifyCompetitionFinished(any());
        }
    }

}