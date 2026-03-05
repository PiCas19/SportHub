package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.repository.jpa.LeaderboardRepository;
import ch.supsi.sporthub.backend.repository.jpa.UserRepository;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GoalNotificationSchedulerTest {

    @Mock private UserRepository userRepository;
    @Mock private IGoalService goalService;
    @Mock private INotificationService notificationService;
    @Mock private IStravaService stravaService;
    @Mock private StravaAuthService stravaAuthService;
    @Mock private LeaderboardRepository leaderboardRepository;
    @Mock private IUserChatService userChatService;
    @Mock private ILeaderboardService leaderboardService;

    @InjectMocks private GoalNotificationScheduler scheduler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckDailyGoals() {
        Goal goal = mockGoal(Period.DAILY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(LocalDate.now())));
        when(goalService.isGoalReached(any(), anyDouble())).thenReturn(false);

        scheduler.checkDailyGoals();

        verify(goalService).getAllGoals();
    }

    @Test
    void testProcessGoalsForEmptyList() throws Exception {
        when(goalService.getAllGoals()).thenReturn(Collections.emptyList());
        Method method = GoalNotificationScheduler.class.getDeclaredMethod("processGoalsForPeriod", Period.class);
        method.setAccessible(true);
        method.invoke(scheduler, Period.DAILY);
        verify(goalService).getAllGoals();
    }

    @Test
    void testToken429Handling() {
        Goal goal = mockGoal(Period.DAILY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        when(stravaAuthService.getValidAccessToken(any())).thenThrow(mock429Exception());
        scheduler.checkDailyGoals();
        verify(goalService).getAllGoals();
    }

    @Test
    void testNoAccessTokenNoRefreshToken() {
        Goal goal = mockGoal(Period.DAILY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        when(stravaAuthService.getValidAccessToken(any())).thenReturn(Optional.empty());
        when(stravaAuthService.getValidRefreshToken(any())).thenReturn(Optional.empty());

        scheduler.checkDailyGoals();
        verify(goalService).getAllGoals();
    }

    @Test
    void testRefreshTokenUsed() {
        Goal goal = mockGoal(Period.DAILY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        when(stravaAuthService.getValidAccessToken(any())).thenReturn(Optional.empty());
        when(stravaAuthService.getValidRefreshToken(any())).thenReturn(Optional.of(mock(StravaToken.class)));

        scheduler.checkDailyGoals();
        verify(stravaAuthService).refreshAccessToken(any(), any());
    }


    @Test
    void testIsWithinPeriod_AllPeriods() throws Exception {
        Method method = GoalNotificationScheduler.class.getDeclaredMethod("isWithinPeriod", StravaActivity.class, Period.class);
        method.setAccessible(true);

        LocalDate now = LocalDate.now();
        assertTrue((Boolean) method.invoke(scheduler, mockActivity(now), Period.DAILY));
        assertTrue((Boolean) method.invoke(scheduler, mockActivity(now.minusDays(6)), Period.WEEKLY));
        assertFalse((Boolean) method.invoke(scheduler, mockActivity(now.minusDays(8)), Period.WEEKLY));
        assertTrue((Boolean) method.invoke(scheduler, mockActivity(now), Period.MONTHLY));
        assertFalse((Boolean) method.invoke(scheduler, mockActivity(now.minusMonths(1)), Period.MONTHLY));
        assertTrue((Boolean) method.invoke(scheduler, mockActivity(now), Period.QUARTERLY));
        assertFalse((Boolean) method.invoke(scheduler, mockActivity(now.minusMonths(4)), Period.QUARTERLY));
        assertTrue((Boolean) method.invoke(scheduler, mockActivity(now), Period.YEARLY));
        assertFalse((Boolean) method.invoke(scheduler, mockActivity(now.minusYears(1)), Period.YEARLY));
    }


    @Test
    void testCalculatePoints() throws Exception {
        Goal goal = mockGoal(Period.MONTHLY);
        when(goal.getGoalType()).thenReturn(GoalType.DURATION);
        Method method = GoalNotificationScheduler.class.getDeclaredMethod("calculatePointsForGoal", Goal.class);
        method.setAccessible(true);
        int result = (int) method.invoke(scheduler, goal);
        assertEquals(8 * 5, result);
    }

    @Test
    void testGetLastDayOfQuarter() throws Exception {
        Method method = GoalNotificationScheduler.class.getDeclaredMethod("getLastDayOfQuarter", LocalDate.class);
        method.setAccessible(true);
        LocalDate date = LocalDate.of(2025, 2, 10);
        LocalDate lastDay = (LocalDate) method.invoke(scheduler, date);
        assertEquals(LocalDate.of(2025, 3, 31), lastDay);
    }

    @Test
    void testGetCurrentValueForGoal_Duration() throws Exception {
        Goal goal = mockGoal(Period.DAILY);
        when(goal.getGoalType()).thenReturn(GoalType.DURATION);
        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(LocalDate.now())));

        Method method = GoalNotificationScheduler.class.getDeclaredMethod("getCurrentValueForGoal", Goal.class, User.class, String.class);
        method.setAccessible(true);
        double result = (double) method.invoke(scheduler, goal, goal.getUser(), "token");
        assertEquals(30.0, result);
    }

    @Test
    void testGetCurrentValueForGoal_Calories_ReflectionFails() throws Exception {
        Goal goal = mockGoal(Period.DAILY);
        when(goal.getGoalType()).thenReturn(GoalType.CALORIES);
        mockStravaAccessToken("token");

        StravaActivity activity = mockActivity(LocalDate.now());
        mockStravaActivities("token", List.of(activity));
        Method method = GoalNotificationScheduler.class.getDeclaredMethod("getCurrentValueForGoal", Goal.class, User.class, String.class);
        method.setAccessible(true);
        double result = (double) method.invoke(scheduler, goal, goal.getUser(), "token");

        assertEquals(0.0, result);
    }

    @Test
    void testGetCurrentValueForGoal_Activities() throws Exception {
        Goal goal = mockGoal(Period.DAILY);
        when(goal.getGoalType()).thenReturn(GoalType.ACTIVITIES);
        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(LocalDate.now()), mockActivity(LocalDate.now())));

        Method method = GoalNotificationScheduler.class.getDeclaredMethod("getCurrentValueForGoal", Goal.class, User.class, String.class);
        method.setAccessible(true);
        double result = (double) method.invoke(scheduler, goal, goal.getUser(), "token");

        assertEquals(2.0, result);
    }

    @Test
    void testCalculatePointsForGoal_QuarterlyYearlyDefault() throws Exception {
        Method method = GoalNotificationScheduler.class.getDeclaredMethod("calculatePointsForGoal", Goal.class);
        method.setAccessible(true);

        Goal quarterly = mockGoal(Period.QUARTERLY);
        when(quarterly.getGoalType()).thenReturn(GoalType.DISTANCE);
    }

    @Test
    void testCheckWeeklyGoals_CallsProcessGoalsForWeekly() {
        Goal goal = mockGoal(Period.WEEKLY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(LocalDate.now().minusDays(2))));
        when(goalService.isGoalReached(any(), anyDouble())).thenReturn(false);

        scheduler.checkWeeklyGoals();

        verify(goalService).getAllGoals();
    }

    @Test
    void testCheckMonthlyGoals_CallsProcessGoalsForMonthly() {
        Goal goal = mockGoal(Period.MONTHLY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(LocalDate.now())));
        when(goalService.isGoalReached(any(), anyDouble())).thenReturn(false);

        scheduler.checkMonthlyGoals();

        verify(goalService).getAllGoals();
    }

    @Test
    void testCheckQuarterlyGoals_CallsProcessGoalsIfLastDay() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LocalDate today = LocalDate.now();
        Goal goal = mockGoal(Period.QUARTERLY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(today)));
        when(goalService.isGoalReached(any(), anyDouble())).thenReturn(false);
        Method method = GoalNotificationScheduler.class.getDeclaredMethod("getLastDayOfQuarter", LocalDate.class);
        method.setAccessible(true);
        LocalDate expected = (LocalDate) method.invoke(scheduler, today);
        if (today.equals(expected)) {
            scheduler.checkQuarterlyGoals();
            verify(goalService).getAllGoals();
        }
    }

    @Test
    void testCheckYearlyGoals_CallsProcessGoalsForYearly() {
        Goal goal = mockGoal(Period.YEARLY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(LocalDate.now())));
        when(goalService.isGoalReached(any(), anyDouble())).thenReturn(false);

        scheduler.checkYearlyGoals();

        verify(goalService).getAllGoals();
    }


    @Test
    void testCalculatePointsForGoal_AllBranchesWithReflection() throws Exception {
        Method method = GoalNotificationScheduler.class.getDeclaredMethod("calculatePointsForGoal", Goal.class);
        method.setAccessible(true);

        Map<GoalType, Integer> expectedBase = Map.of(
                GoalType.DISTANCE, 10,
                GoalType.DURATION, 8,
                GoalType.CALORIES, 12,
                GoalType.ACTIVITIES, 6
        );

        Map<Period, Integer> expectedMultiplier = Map.of(
                Period.WEEKLY, 3,
                Period.MONTHLY, 5,
                Period.QUARTERLY, 8,
                Period.YEARLY, 15,
                Period.DAILY, 1
        );

        for (GoalType type : GoalType.values()) {
            for (Period period : Period.values()) {
                Goal goal = mock(Goal.class);
                when(goal.getGoalType()).thenReturn(type);
                when(goal.getPeriod()).thenReturn(period);

                int base = expectedBase.get(type);
                int multiplier = expectedMultiplier.getOrDefault(period, 1);
                int expected = base * multiplier;

                int result = (int) method.invoke(scheduler, goal);
                assertEquals(expected, result,
                        "Expected " + expected + " for " + type + " and " + period + ", but got " + result);
            }
        }
    }


    @Test
    void testGoalReached_AllDataPresent_UpdatesLeaderboard() {
        Goal goal = mockGoal(Period.DAILY);
        User user = goal.getUser();
        when(goal.getGoalType()).thenReturn(GoalType.DISTANCE);
        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(LocalDate.now())));
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        when(goalService.isGoalReached(any(), anyDouble())).thenReturn(true);
        Chat chat = new Chat();
        chat.setChatId("123");
        when(userChatService.getChatsForUser(user)).thenReturn(List.of(chat));
        Leaderboard lb = mockLeaderboard(user.getUsername());
        when(leaderboardService.getLeaderboardById("123")).thenReturn(Optional.of(lb));

        scheduler.checkDailyGoals();
        assertEquals(20, lb.getScores().get(user.getUsername()));
        verify(notificationService).notifyGoalReached(eq(user), eq(goal), anyDouble());
        verify(leaderboardService).save(lb);
    }



    @Test
    void testGoalNotReached_SkipsNotificationAndLeaderboard() {
        Goal goal = mockGoal(Period.DAILY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(LocalDate.now())));
        when(goalService.isGoalReached(any(), anyDouble())).thenReturn(false);

        scheduler.checkDailyGoals();

        verify(notificationService, never()).notifyGoalReached(any(), any(), anyDouble());
        verify(leaderboardService, never()).save(any());
    }

    @Test
    void testGoalReached_ButNoChatsFound() {
        Goal goal = mockGoal(Period.DAILY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        when(goalService.isGoalReached(any(), anyDouble())).thenReturn(true);

        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(LocalDate.now())));

        when(userChatService.getChatsForUser(goal.getUser())).thenReturn(Collections.emptyList());

        scheduler.checkDailyGoals();

        verify(notificationService).notifyGoalReached(any(), any(), anyDouble());
        verify(leaderboardService, never()).save(any());
    }

    @Test
    void testGoalReached_ChatPresentButNoLeaderboard() {
        Goal goal = mockGoal(Period.DAILY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        when(goalService.isGoalReached(any(), anyDouble())).thenReturn(true);

        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(LocalDate.now())));

        when(userChatService.getChatsForUser(goal.getUser())).thenReturn(List.of(mockChat("999")));
        when(leaderboardService.getLeaderboardById("999")).thenReturn(Optional.empty());

        scheduler.checkDailyGoals();

        verify(notificationService).notifyGoalReached(any(), any(), anyDouble());
        verify(leaderboardService, never()).save(any());
    }


    @Test
    void testCheckQuarterlyGoals_ForcedDate_MatchesLastDay() throws Exception {
        GoalNotificationScheduler spyScheduler = Mockito.spy(scheduler);
        LocalDate forcedDate = LocalDate.of(2025, 6, 30);
        doReturn(forcedDate).when(spyScheduler).getToday();
        Method method = GoalNotificationScheduler.class.getDeclaredMethod("getLastDayOfQuarter", LocalDate.class);
        method.setAccessible(true);
        LocalDate lastDay = (LocalDate) method.invoke(scheduler, forcedDate);
        assertEquals(forcedDate, lastDay);
        Goal goal = mockGoal(Period.QUARTERLY);
        when(goalService.getAllGoals()).thenReturn(List.of(goal));
        mockStravaAccessToken("token");
        mockStravaActivities("token", List.of(mockActivity(forcedDate)));
        when(goalService.isGoalReached(any(), anyDouble())).thenReturn(false);
        spyScheduler.checkQuarterlyGoals();
        verify(goalService).getAllGoals();
    }

    @Test
    void testGetToday_ReturnsCurrentDate() {
        LocalDate expected = LocalDate.now();
        LocalDate actual = scheduler.getToday();

        assertEquals(expected, actual);
    }

    private void mockStravaAccessToken(String token) {
        StravaTokenResponse tokenResponse = new StravaTokenResponse();
        tokenResponse.setAccessToken(token);
        when(stravaAuthService.getValidAccessToken(any())).thenReturn(Optional.of(tokenResponse));
    }

    private void mockStravaActivities(String token, List<StravaActivity> activities) {
        when(stravaService.getActivities(token)).thenReturn(activities);
    }

    private WebClientResponseException mock429Exception() {
        return WebClientResponseException.create(429, "Too Many Requests", null, null, null);
    }

    private Goal mockGoal(Period period) {
        Goal goal = mock(Goal.class);
        when(goal.getPeriod()).thenReturn(period);
        when(goal.getGoalType()).thenReturn(GoalType.DISTANCE);
        User user = new User();
        user.setUsername("test_user");
        when(goal.getUser()).thenReturn(user);
        return goal;
    }

    private StravaActivity mockActivity(LocalDate date) {
        StravaActivity activity = new StravaActivity();
        activity.setStartDateLocal(date.toString() + "T00:00:00Z");
        activity.setDistanceMeters(5000.0);
        activity.setElapsedTimeSeconds(1800);
        return activity;
    }

    private Leaderboard mockLeaderboard(String username) {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setChatId("123");
        Map<String, Integer> scores = new HashMap<>();
        scores.put(username, 10);
        leaderboard.setScores(scores);
        return leaderboard;
    }

    private Chat mockChat(String chatId) {
        Chat chat = new Chat();
        chat.setChatId(chatId);
        return chat;
    }


}
