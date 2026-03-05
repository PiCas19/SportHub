package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompetitionLeaderboardSchedulerTest {

    @Mock private ICompetitionService competitionService;
    @Mock private ILeaderboardService leaderboardService;
    @Mock private INotificationService notificationService;
    @Mock private ITelegramService telegramService;
    @Mock private StravaAuthService stravaAuthService;
    @Mock private IStravaService stravaService;
    @Mock private IUserChatService userChatService;

    @InjectMocks private CompetitionLeaderboardScheduler scheduler;

    private Competition expiredCompetition;
    private Leaderboard leaderboard;
    private CompetitionGoal goal;
    private StravaTokenResponse tokenResponse;
    private List<StravaActivity> activities;

    @BeforeEach
    void setup() {
        expiredCompetition = new Competition();
        expiredCompetition.setId(1L);
        expiredCompetition.setChatId("123456");
        expiredCompetition.setStarted(true);
        expiredCompetition.setFinished(false);
        expiredCompetition.setDeadline(LocalDate.now().minusDays(1));
        expiredCompetition.setEndDate(LocalDate.now());

        leaderboard = new Leaderboard();
        leaderboard.setChatId("123456");
        leaderboard.setName("Leaderboard Test");
        leaderboard.setScores(new HashMap<>());

        goal = new CompetitionGoal();
        goal.setGoalType(GoalType.DISTANCE);
        goal.setSportType(SportType.RUN);
        goal.setTargetValue(50.0);

        tokenResponse = new StravaTokenResponse();
        tokenResponse.setAccessToken("valid_token");

        StravaActivity activity = new StravaActivity();
        activity.setSportType("RUN");
        activity.setStartDateLocal(LocalDate.now().toString() + "T12:00:00Z");
        activity.setDistanceMeters(5000.0);
        activity.setElapsedTimeSeconds(1800);

        activities = List.of(activity);
    }

    @Test
    void testCloseExpiredCompetitions() {
        when(competitionService.getAllCompetitions()).thenReturn(List.of(expiredCompetition));
        when(competitionService.getLeaderboardEntriesForCompetition(expiredCompetition)).thenReturn(
                List.of(
                        new LeaderboardEntryResponse(10.0, "user1", "tele1"),
                        new LeaderboardEntryResponse(5.0, "user2", "tele2")
                )
        );
        when(leaderboardService.getLeaderboardById("123456")).thenReturn(Optional.of(leaderboard));

        scheduler.closeExpiredCompetitions();

        verify(notificationService).notifyCompetitionFinished(expiredCompetition);
        verify(leaderboardService).save(any());
        verify(competitionService, times(2)).saveCompetition(expiredCompetition);
        assertTrue(expiredCompetition.isFinished());
    }


    @Test
    void testCloseExpiredCompetitions_NoLeaderboardFound() {
        when(competitionService.getAllCompetitions()).thenReturn(List.of(expiredCompetition));
        when(competitionService.getLeaderboardEntriesForCompetition(expiredCompetition)).thenReturn(List.of());
        when(leaderboardService.getLeaderboardById("123456")).thenReturn(Optional.empty());

        scheduler.closeExpiredCompetitions();

        verify(notificationService).notifyCompetitionFinished(expiredCompetition);
        verify(competitionService, times(2)).saveCompetition(expiredCompetition);
    }


    @Test
    void testUpdateCompetitionScores() {
        when(competitionService.getAllCompetitions()).thenReturn(List.of(expiredCompetition));
        when(competitionService.getGoalForCompetition(expiredCompetition)).thenReturn(goal);
        when(competitionService.getLeaderboardEntriesForCompetition(expiredCompetition)).thenReturn(
                List.of(new LeaderboardEntryResponse(0.0, "user1", "tele1"))
        );
        when(stravaAuthService.getValidAccessToken("user1")).thenReturn(Optional.of(tokenResponse));
        when(stravaService.getActivities("valid_token")).thenReturn(activities);

        scheduler.updateCompetitionScores();

        verify(competitionService).updateLeaderboardEntryScore(eq(expiredCompetition), eq("user1"), anyDouble());
        verify(competitionService).saveCompetition(expiredCompetition);
    }

    @Test
    void testNoLeaderboards() {
        when(leaderboardService.getAllLeaderboards()).thenReturn(Collections.emptyList());
        scheduler.sendMonthlyLeaderboardSummary();
        verifyNoInteractions(telegramService);
    }

    @Test
    void testEmptyScores() {
        when(leaderboardService.getAllLeaderboards()).thenReturn(List.of(leaderboard));
        scheduler.sendMonthlyLeaderboardSummary();
        verifyNoInteractions(telegramService);
    }

    @Test
    void testWithScoresAndNoTelegramUser() {
        leaderboard.getScores().put("alice", 30);
        leaderboard.getScores().put("bob", 50);
        when(leaderboardService.getAllLeaderboards()).thenReturn(List.of(leaderboard));

        when(userChatService.findUserByUsername(anyString())).thenReturn(Optional.empty());

        scheduler.sendMonthlyLeaderboardSummary();
        ArgumentCaptor<String> chatIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> msgCaptor    = ArgumentCaptor.forClass(String.class);
        verify(telegramService, times(1)).sendMessage(chatIdCaptor.capture(), msgCaptor.capture());

        assertEquals("123456", chatIdCaptor.getValue());

        String msg = msgCaptor.getValue();
        assertTrue(msg.contains("📊 <b>Monthly Leaderboard:</b>"));
        int idxBob = msg.indexOf("bob");
        int idxAlice = msg.indexOf("alice");
        assertTrue(idxBob < idxAlice, "bob deve apparire prima di alice");
        assertFalse(msg.contains("@"));
    }

    @Test
    void testWithScoresAndWithTelegramUser() {
        leaderboard.getScores().put("charlie", 75);
        when(leaderboardService.getAllLeaderboards()).thenReturn(List.of(leaderboard));

        User u = new User();
        u.setId(42L);
        u.setUsername("charlie");
        when(userChatService.findUserByUsername("charlie"))
                .thenReturn(Optional.of(u));
        when(userChatService.getTelegramUsernameForUserInChat("123456", u))
                .thenReturn("tg_char");
        scheduler.sendMonthlyLeaderboardSummary();

        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        verify(telegramService).sendMessage(eq("123456"), msgCaptor.capture());

        String msg = msgCaptor.getValue();
        assertTrue(msg.contains("charlie (@tg_char)"),
                "deve includere il telegram username tra parentesi");
        assertTrue(msg.contains("- 75 Points"),
                "deve mostrare il punteggio corretto");
    }


    @Test
    void testSendMonthlyLeaderboardSummary_NoScores() {
        when(leaderboardService.getAllLeaderboards()).thenReturn(List.of(leaderboard));

        scheduler.sendMonthlyLeaderboardSummary();

        verify(telegramService, never()).sendMessage(any(), any());
    }

    @Test
    void testCleanupOldCompetitions() {
        Competition finishedOldCompetition = new Competition();
        finishedOldCompetition.setId(2L);
        finishedOldCompetition.setFinished(true);
        finishedOldCompetition.setDeadline(LocalDate.now().minusMonths(2));
        finishedOldCompetition.setChatId("old_chat_id");

        when(competitionService.getAllCompetitions()).thenReturn(List.of(expiredCompetition, finishedOldCompetition));

        scheduler.cleanupOldCompetitions();

        verify(competitionService).deleteCompetition(finishedOldCompetition.getId());
        verify(competitionService, never()).deleteCompetition(expiredCompetition.getId());
    }

    @Test
    void testCloseExpiredCompetitions_ScoreNull() {
        when(competitionService.getAllCompetitions()).thenReturn(List.of(expiredCompetition));
        when(competitionService.getLeaderboardEntriesForCompetition(expiredCompetition)).thenReturn(
                List.of(
                        new LeaderboardEntryResponse(null, "user1", "tele1")
                )
        );
        when(leaderboardService.getLeaderboardById("123456")).thenReturn(Optional.of(leaderboard));

        scheduler.closeExpiredCompetitions();
        assertTrue(leaderboard.getScores().isEmpty());
        verify(notificationService).notifyCompetitionFinished(expiredCompetition);
        verify(competitionService, times(2)).saveCompetition(expiredCompetition);
    }

    @Test
    void testCloseExpiredCompetitions_ScoreZero() {
        when(competitionService.getAllCompetitions()).thenReturn(List.of(expiredCompetition));
        when(competitionService.getLeaderboardEntriesForCompetition(expiredCompetition)).thenReturn(
                List.of(
                        new LeaderboardEntryResponse(0.0, "user1", "tele1")
                )
        );
        when(leaderboardService.getLeaderboardById("123456")).thenReturn(Optional.of(leaderboard));

        scheduler.closeExpiredCompetitions();
        assertTrue(leaderboard.getScores().isEmpty());
        verify(notificationService).notifyCompetitionFinished(expiredCompetition);
        verify(competitionService, times(2)).saveCompetition(expiredCompetition);
    }

    @Test
    void testCloseExpiredCompetitions_ScorePositive() {
        when(competitionService.getAllCompetitions()).thenReturn(List.of(expiredCompetition));
        when(competitionService.getLeaderboardEntriesForCompetition(expiredCompetition)).thenReturn(
                List.of(
                        new LeaderboardEntryResponse(5.0, "user1", "tele1")
                )
        );
        when(leaderboardService.getLeaderboardById("123456")).thenReturn(Optional.of(leaderboard));

        scheduler.closeExpiredCompetitions();

        assertEquals(1, leaderboard.getScores().size());
        assertEquals(5, leaderboard.getScores().get("user1"));
        verify(notificationService).notifyCompetitionFinished(expiredCompetition);
        verify(competitionService, times(2)).saveCompetition(expiredCompetition);
    }

    @Test
    void testCloseExpiredCompetitions_FinishedCompetition_NotProcessed() {
        Competition finished = new Competition();
        finished.setId(10L);
        finished.setFinished(true);
        finished.setStarted(true);
        finished.setDeadline(LocalDate.now().minusDays(1));
        finished.setChatId("chat1");

        when(competitionService.getAllCompetitions()).thenReturn(List.of(finished));

        scheduler.closeExpiredCompetitions();

        verify(notificationService, never()).notifyCompetitionFinished(finished);
        verify(competitionService, never()).saveCompetition(finished);
    }

    @Test
    void testCloseExpiredCompetitions_NotStartedCompetition_NotProcessed() {
        Competition notStarted = new Competition();
        notStarted.setId(11L);
        notStarted.setFinished(false);
        notStarted.setStarted(false);
        notStarted.setDeadline(LocalDate.now().minusDays(1));
        notStarted.setChatId("chat2");

        when(competitionService.getAllCompetitions()).thenReturn(List.of(notStarted));

        scheduler.closeExpiredCompetitions();

        verify(notificationService, never()).notifyCompetitionFinished(notStarted);
        verify(competitionService, never()).saveCompetition(notStarted);
    }

    @Test
    void testCloseExpiredCompetitions_DeadlineFuture_NotProcessed() {
        Competition futureDeadline = new Competition();
        futureDeadline.setId(12L);
        futureDeadline.setFinished(false);
        futureDeadline.setStarted(true);
        futureDeadline.setDeadline(LocalDate.now().plusDays(5));
        futureDeadline.setChatId("chat3");

        when(competitionService.getAllCompetitions()).thenReturn(List.of(futureDeadline));

        scheduler.closeExpiredCompetitions();

        verify(notificationService, never()).notifyCompetitionFinished(futureDeadline);
        verify(competitionService, never()).saveCompetition(futureDeadline);
    }

    @Test
    void testCloseExpiredCompetitions_ValidCompetition_Processed() {
        when(competitionService.getAllCompetitions()).thenReturn(List.of(expiredCompetition));
        when(competitionService.getLeaderboardEntriesForCompetition(expiredCompetition)).thenReturn(List.of());
        when(leaderboardService.getLeaderboardById(expiredCompetition.getChatId())).thenReturn(Optional.empty());

        scheduler.closeExpiredCompetitions();

        verify(notificationService).notifyCompetitionFinished(expiredCompetition);
        verify(competitionService, times(2)).saveCompetition(expiredCompetition);
    }

    @Test
    void testUpdateCompetitionScores_NotStartedCompetition_NotProcessed() {
        Competition notStarted = new Competition();
        notStarted.setId(100L);
        notStarted.setStarted(false);
        notStarted.setFinished(false);
        notStarted.setDeadline(LocalDate.now().plusDays(10));
        notStarted.setChatId("chat_not_started");

        when(competitionService.getAllCompetitions()).thenReturn(List.of(notStarted));

        scheduler.updateCompetitionScores();

        verify(competitionService, never()).saveCompetition(notStarted);
    }

    @Test
    void testUpdateCompetitionScores_FinishedCompetition_NotProcessed() {
        Competition finished = new Competition();
        finished.setId(101L);
        finished.setStarted(true);
        finished.setFinished(true);
        finished.setDeadline(LocalDate.now().minusDays(1));
        finished.setChatId("chat_finished");

        when(competitionService.getAllCompetitions()).thenReturn(List.of(finished));

        scheduler.updateCompetitionScores();

        verify(competitionService, never()).saveCompetition(finished);
    }

    @Test
    void testUpdateCompetitionScores_ValidCompetition_Processed() {
        Competition valid = new Competition();
        valid.setId(102L);
        valid.setStarted(true);
        valid.setFinished(false);
        valid.setDeadline(LocalDate.now().plusDays(5));
        valid.setChatId("chat_valid");

        when(competitionService.getAllCompetitions()).thenReturn(List.of(valid));
        when(competitionService.getGoalForCompetition(valid)).thenReturn(new CompetitionGoal());
        when(competitionService.getLeaderboardEntriesForCompetition(valid)).thenReturn(List.of());

        scheduler.updateCompetitionScores();

        verify(competitionService).saveCompetition(valid);
    }

    @Test
    void testGetTokenForUsername_TooManyRequests_ReturnsEmpty() throws Exception {
        WebClientResponseException tooManyRequestsException = mock(WebClientResponseException.class);
        when(tooManyRequestsException.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS);

        when(stravaAuthService.getValidAccessToken("user1")).thenThrow(tooManyRequestsException);

        Optional<StravaTokenResponse> result = callPrivateGetTokenForUsername("user1");

        assertTrue(result.isEmpty(), "Deve ritornare Optional.empty() se status 429");
    }

    @Test
    void testGetTokenForUsername_OtherError_ThrowsException() {
        WebClientResponseException badRequestException = mock(WebClientResponseException.class);
        when(badRequestException.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        when(stravaAuthService.getValidAccessToken("user1")).thenThrow(badRequestException);

        assertThrows(WebClientResponseException.class, () -> callPrivateGetTokenForUsername("user1"));
    }


    @SuppressWarnings("unchecked")
    private Optional<StravaTokenResponse> callPrivateGetTokenForUsername(String username) {
        try {
            Method method = CompetitionLeaderboardScheduler.class.getDeclaredMethod("getTokenForUsername", String.class);
            method.setAccessible(true);
            return (Optional<StravaTokenResponse>) method.invoke(scheduler, username);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof WebClientResponseException) {
                throw (WebClientResponseException) e.getCause();
            }
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void testGetActivitiesForUser_Exception_ReturnsEmptyList() throws Exception {
        when(stravaService.getActivities("valid_token")).thenThrow(new RuntimeException("API failure"));

        List<StravaActivity> activities = callPrivateGetActivitiesForUser("valid_token");

        assertTrue(activities.isEmpty(), "Deve ritornare una lista vuota se c'è un errore");
    }

    @SuppressWarnings("unchecked")
    private List<StravaActivity> callPrivateGetActivitiesForUser(String accessToken) {
        try {
            Method method = CompetitionLeaderboardScheduler.class.getDeclaredMethod("getActivitiesForUser", String.class);
            method.setAccessible(true);
            return (List<StravaActivity>) method.invoke(scheduler, accessToken);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCalculateTotalForGoal_FilterSportType() throws Exception {
        CompetitionGoal goal = new CompetitionGoal();
        goal.setGoalType(GoalType.DISTANCE);
        goal.setSportType(SportType.RUN);

        StravaActivity nullSportTypeActivity = new StravaActivity();
        nullSportTypeActivity.setSportType(null);

        StravaActivity differentSportTypeActivity = new StravaActivity();
        differentSportTypeActivity.setSportType("RIDE");

        StravaActivity correctSportTypeActivity = new StravaActivity();
        correctSportTypeActivity.setSportType("run");
        correctSportTypeActivity.setDistanceMeters(10000.0);
        correctSportTypeActivity.setStartDateLocal(LocalDate.now() + "T12:00:00Z");
        correctSportTypeActivity.setElapsedTimeSeconds(3600);

        List<StravaActivity> activities = List.of(
                nullSportTypeActivity,
                differentSportTypeActivity,
                correctSportTypeActivity
        );

        Method method = CompetitionLeaderboardScheduler.class.getDeclaredMethod(
                "calculateTotalForGoal", List.class, CompetitionGoal.class, LocalDate.class);
        method.setAccessible(true);

        double total = (double) method.invoke(scheduler, activities, goal, LocalDate.now());
        assertEquals(10.0, total, 0.001, "Deve sommare solo l'attività con sportType giusto");
    }

    @Test
    void testCalculateTotalForGoal_DurationCaloriesActivitiesDistance() throws Exception {
        StravaActivity activity = new StravaActivity();
        activity.setSportType("RUN");
        activity.setStartDateLocal(LocalDate.now() + "T12:00:00Z");
        activity.setDistanceMeters(12000.0);
        activity.setElapsedTimeSeconds(3600);

        List<StravaActivity> activities = List.of(activity);

        CompetitionGoal distanceGoal = new CompetitionGoal();
        distanceGoal.setGoalType(GoalType.DISTANCE);
        distanceGoal.setSportType(SportType.RUN);

        Method method = CompetitionLeaderboardScheduler.class.getDeclaredMethod(
                "calculateTotalForGoal", List.class, CompetitionGoal.class, LocalDate.class);
        method.setAccessible(true);

        double totalDistance = (double) method.invoke(scheduler, activities, distanceGoal, LocalDate.now());
        assertEquals(12.0, totalDistance, 0.001, "DISTANCE deve convertire metri in km");

        CompetitionGoal durationGoal = new CompetitionGoal();
        durationGoal.setGoalType(GoalType.DURATION);
        durationGoal.setSportType(SportType.RUN);

        double totalDuration = (double) method.invoke(scheduler, activities, durationGoal, LocalDate.now());
        assertEquals(60.0, totalDuration, 0.001, "DURATION deve convertire secondi in minuti");

        CompetitionGoal caloriesGoal = new CompetitionGoal();
        caloriesGoal.setGoalType(GoalType.CALORIES);
        caloriesGoal.setSportType(SportType.RUN);

        double totalCalories = (double) method.invoke(scheduler, activities, caloriesGoal, LocalDate.now());
        assertEquals(1.0, totalCalories, 0.001, "CALORIES deve restituire sempre 1.0");

        CompetitionGoal activitiesGoal = new CompetitionGoal();
        activitiesGoal.setGoalType(GoalType.ACTIVITIES);
        activitiesGoal.setSportType(SportType.RUN);

        double totalActivities = (double) method.invoke(scheduler, activities, activitiesGoal, LocalDate.now());
        assertEquals(1.0, totalActivities, 0.001, "ACTIVITIES deve restituire sempre 1.0");
    }

    @Test
    void testEscapeHtml_TextWithSpecialCharacters() throws Exception {
        Method method = CompetitionLeaderboardScheduler.class.getDeclaredMethod(
                "escapeHtml", String.class);
        method.setAccessible(true);

        String input = "<div>Hello & \"world\"</div>";
        String expected = "&lt;div&gt;Hello &amp; &quot;world&quot;&lt;/div&gt;";

        String result = (String) method.invoke(scheduler, input);

        assertEquals(expected, result, "Deve convertire i caratteri HTML speciali");
    }

    @Test
    void testEscapeHtml_NullText_ReturnsEmptyString() throws Exception {
        Method method = CompetitionLeaderboardScheduler.class.getDeclaredMethod(
                "escapeHtml", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(scheduler, (String) null);

        assertEquals("", result, "Se il testo è null deve ritornare stringa vuota");
    }




}