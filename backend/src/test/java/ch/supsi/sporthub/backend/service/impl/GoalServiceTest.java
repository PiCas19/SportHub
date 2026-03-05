package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.request.SetGoalRequest;
import ch.supsi.sporthub.backend.dto.response.GoalItemResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.repository.jpa.GoalRepository;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private GoalServiceImpl goalService;

    @Mock
    private StravaServiceImpl stravaService;


    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
    }

    @Test
    void testCreateGoal_Success() {
        SetGoalRequest request = new SetGoalRequest();
        request.setTargetValue(100.0);
        request.setGoalType("DISTANCE");
        request.setSportType("RUN");
        request.setPeriod(Period.valueOf("WEEKLY"));

        Goal savedGoal = new Goal();
        savedGoal.setId(1L);

        when(goalRepository.save(any(Goal.class))).thenReturn(savedGoal);

        Goal result = goalService.createGoal(request, user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void testParseGoalType_Invalid() {
        SetGoalRequest request = new SetGoalRequest();
        request.setGoalType("UNKNOWN");
        request.setSportType("RUN");
        request.setTargetValue(50.0);
        request.setPeriod(Period.valueOf("MONTHLY"));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> goalService.createGoal(request, user));
        assertEquals("Invalid Goal Type: UNKNOWN", ex.getMessage());
    }

    @Test
    void testParseGoalType_Missing() {
        SetGoalRequest request = new SetGoalRequest();
        request.setGoalType(" ");
        request.setSportType("RUN");
        request.setTargetValue(50.0);
        request.setPeriod(Period.valueOf("MONTHLY"));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> goalService.createGoal(request, user));
        assertEquals("GoalType not specified", ex.getMessage());
    }

    @Test
    void testParseSportType_InvalidDefaultsToOther() {
        SetGoalRequest req = new SetGoalRequest();
        req.setGoalType("DISTANCE");
        req.setSportType("XYZ");
        req.setTargetValue(100.0);
        req.setPeriod(Period.valueOf("DAILY"));

        when(goalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Goal goal = goalService.createGoal(req, user);
        assertEquals(SportType.OTHER, goal.getSportType());
    }

    @Test
    void testParseSportType_NullDefaultsToAll() {
        SetGoalRequest req = new SetGoalRequest();
        req.setGoalType("DISTANCE");
        req.setSportType(null);
        req.setTargetValue(100.0);
        req.setPeriod(Period.valueOf("DAILY"));

        when(goalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Goal goal = goalService.createGoal(req, user);
        assertEquals(SportType.ALL, goal.getSportType());
    }

    @Test
    void testGetAllGoals() {
        List<Goal> goals = List.of(new Goal(), new Goal());
        when(goalRepository.findAllWithUser()).thenReturn(goals);

        List<Goal> result = goalService.getAllGoals();
        assertEquals(2, result.size());
    }

    @Test
    void testGetGoalsForUser() {
        when(goalRepository.findByUser(user)).thenReturn(List.of(new Goal()));
        List<Goal> result = goalService.getGoalsForUser(user);
        assertEquals(1, result.size());
    }

    @Test
    void testGetGoalForUser() {
        Goal goal = new Goal();
        goal.setId(5L);
        when(goalRepository.findByIdAndUser(5L, user)).thenReturn(Optional.of(goal));

        Optional<Goal> result = goalService.getGoalForUser(5L, user);
        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getId());
    }

    @Test
    void testDeleteGoalForUser_Success() {
        Goal goal = new Goal();
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));

        boolean deleted = goalService.deleteGoalForUser(1L, user);

        assertTrue(deleted);
        verify(goalRepository).delete(goal);
    }

    @Test
    void testDeleteGoalForUser_NotFound() {
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        boolean deleted = goalService.deleteGoalForUser(1L, user);

        assertFalse(deleted);
        verify(goalRepository, never()).delete(any());
    }

    @Test
    void testIsGoalReached() {
        Goal goal = new Goal();
        goal.setTargetValue(100.0);

        assertTrue(goalService.isGoalReached(goal, 150));
        assertTrue(goalService.isGoalReached(goal, 100));
        assertFalse(goalService.isGoalReached(goal, 99.9));
    }

    @Test
    void testParseGoalType_Null_ThrowsException() {
        SetGoalRequest request = new SetGoalRequest();
        request.setGoalType(null);
        request.setSportType("RUN");
        request.setTargetValue(100.0);
        request.setPeriod(Period.valueOf("DAILY"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goalService.createGoal(request, user));
        assertEquals("GoalType not specified", ex.getMessage());
    }

    @Test
    void testParseGoalType_Blank_ThrowsException() {
        SetGoalRequest request = new SetGoalRequest();
        request.setGoalType("   ");
        request.setSportType("RUN");
        request.setTargetValue(100.0);
        request.setPeriod(Period.valueOf("DAILY"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goalService.createGoal(request, user));
        assertEquals("GoalType not specified", ex.getMessage());
    }

    @Test
    void testParseSportType_Null_ReturnsAll() {
        SetGoalRequest request = new SetGoalRequest();
        request.setGoalType("DISTANCE");
        request.setSportType(null);
        request.setTargetValue(100.0);
        request.setPeriod(Period.valueOf("DAILY"));

        when(goalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Goal result = goalService.createGoal(request, user);

        assertEquals(SportType.ALL, result.getSportType());
    }

    @Test
    void testParseSportType_Blank_ReturnsAll() {
        SetGoalRequest request = new SetGoalRequest();
        request.setGoalType("DISTANCE");
        request.setSportType("   ");
        request.setTargetValue(100.0);
        request.setPeriod(Period.valueOf("DAILY"));

        when(goalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Goal result = goalService.createGoal(request, user);

        assertEquals(SportType.ALL, result.getSportType());
    }


    @Test
    void testGetGoalItemResponses() {
        Goal goal = new Goal();
        goal.setId(1L);
        goal.setUser(user);
        goal.setGoalType(GoalType.DISTANCE);
        goal.setSportType(SportType.RUN);
        goal.setTargetValue(100.0);
        goal.setPeriod(Period.WEEKLY);

        when(goalRepository.findByUser(user)).thenReturn(List.of(goal));
        when(stravaService.getUserActivities(anyString(), any(), any()))
                .thenReturn(List.of(mockStravaActivity(50_000.0, 0, 0)));

        List<GoalItemResponse> responses = goalService.getGoalItemResponses(user, "token");

        assertEquals(1, responses.size());
        GoalItemResponse response = responses.get(0);
        assertEquals(1L, response.getId());
        assertEquals("DISTANCE - RUN", response.getName());
        assertEquals(GoalType.DISTANCE, response.getGoalType());
        assertEquals(SportType.RUN, response.getSportType());
        assertEquals(50.0, response.getCurrent(), 0.01);
        assertEquals(50.0, response.getRemaining(), 0.01);
        assertEquals(100.0, response.getTarget(), 0.01);
        assertEquals(50.0, response.getPercentage(), 0.01);
        assertEquals("km", response.getUnit());
        assertEquals(Period.WEEKLY, response.getPeriod());
        assertEquals("Goal progress retrieved successfully", response.getMessage());
    }


    @Test
    void testGetUnitForGoalType_AllBranches() throws Exception {
        Method method = GoalServiceImpl.class.getDeclaredMethod("getUnitForGoalType", GoalType.class);
        method.setAccessible(true);

        assertEquals("km", method.invoke(goalService, GoalType.DISTANCE));
        assertEquals("min", method.invoke(goalService, GoalType.DURATION));
        assertEquals("kcal", method.invoke(goalService, GoalType.CALORIES));
        assertEquals("sessioni", method.invoke(goalService, GoalType.ACTIVITIES));
    }

    @Test
    void testCalculateCurrentProgress_AllGoalTypes() throws Exception {
        Goal baseGoal = new Goal();
        baseGoal.setSportType(SportType.RUN);
        baseGoal.setUser(user);
        baseGoal.setPeriod(Period.WEEKLY);

        Method method = GoalServiceImpl.class.getDeclaredMethod("calculateCurrentProgress", Goal.class, String.class);
        method.setAccessible(true);

        StravaActivity activity = new StravaActivity();
        activity.setDistanceMeters(5000);
        activity.setMovingTimeSeconds(1800);
        activity.setCalories(300);
        activity.setStartDateLocal(LocalDateTime.now().minusDays(1).toString());
        activity.setSportType("RUN");

        when(stravaService.getUserActivities(anyString(), any(), any())).thenReturn(List.of(activity));

        baseGoal.setGoalType(GoalType.DISTANCE);
        assertEquals(5.0, (double) method.invoke(goalService, baseGoal, "token"), 0.01);

        baseGoal.setGoalType(GoalType.DURATION);
        assertEquals(30.0, (double) method.invoke(goalService, baseGoal, "token"), 0.01);

        baseGoal.setGoalType(GoalType.CALORIES);
        assertEquals(300.0, (double) method.invoke(goalService, baseGoal, "token"), 0.01);

        baseGoal.setGoalType(GoalType.ACTIVITIES);
        assertEquals(1.0, (double) method.invoke(goalService, baseGoal, "token"), 0.01);
    }

    @Test
    void testGetStartDateFromPeriod_AllBranches() throws Exception {
        Method method = GoalServiceImpl.class.getDeclaredMethod("getStartDateFromPeriod", Period.class);
        method.setAccessible(true);

        LocalDateTime now = LocalDateTime.now();

        assertTrue(((LocalDateTime) method.invoke(goalService, Period.DAILY)).isBefore(now.minusHours(23)));
        assertTrue(((LocalDateTime) method.invoke(goalService, Period.WEEKLY)).isBefore(now.minusDays(6)));
        assertTrue(((LocalDateTime) method.invoke(goalService, Period.MONTHLY)).isBefore(now.minusDays(27)));
        assertTrue(((LocalDateTime) method.invoke(goalService, Period.QUARTERLY)).isBefore(now.minusMonths(2)));
        assertTrue(((LocalDateTime) method.invoke(goalService, Period.YEARLY)).isBefore(now.minusMonths(11)));
    }

    private StravaActivity mockStravaActivity(double meters, int durationSeconds, double calories) {
        StravaActivity activity = new StravaActivity();
        activity.setDistanceMeters(meters);
        activity.setMovingTimeSeconds(durationSeconds);
        activity.setCalories(calories);
        activity.setSportType("RUN");
        activity.setStartDateLocal(LocalDateTime.now().minusDays(1).toString());
        return activity;
    }




}
