package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.config.StravaProperties;
import ch.supsi.sporthub.backend.dto.request.strava.ActivityFilterRequest;
import ch.supsi.sporthub.backend.dto.request.strava.AddActivityRequest;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import ch.supsi.sporthub.backend.service.domain.strava.Athlete;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class StravaServiceImplTest {

    @Mock
    private IBaseApiService baseApiService;

    @Mock
    private StravaProperties stravaProperties;

    @InjectMocks
    private StravaServiceImpl stravaService;

    private User user;
    private List<StravaActivity> activityList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setWeight(70.0);
        user.setHeight(175.0);

        StravaActivity activity = new StravaActivity();
        activity.setDistanceMeters(5000);
        activity.setSportType("run");
        activity.setStartDateLocal(LocalDate.now().toString());
        activity.setAverageSpeed(3.0);
        activity.setTotalElevationGain(100);
        activity.setName("Morning Run");
        activity.setDescription("Chill pace");

        activityList = List.of(activity);
    }

    @Test
    void testParseSportType() throws Exception {
        Method method = stravaService.getClass().getDeclaredMethod("parseSportType", String.class);
        method.setAccessible(true);
        assertEquals(SportType.RUN, method.invoke(stravaService, "run"));
        assertEquals(SportType.OTHER, method.invoke(stravaService, "paragliding"));
        assertEquals(SportType.ALL, method.invoke(stravaService, (Object) null));
        assertEquals(SportType.ALL, method.invoke(stravaService, "   "));
    }

    @Test
    void testCalculateKm() {
        List<StravaActivity> activities = new ArrayList<>(activityList);

        StravaActivity lastMonthActivity = new StravaActivity();
        lastMonthActivity.setSportType("run");
        lastMonthActivity.setDistanceMeters(3000);
        lastMonthActivity.setStartDateLocal(LocalDate.now().minusMonths(1).withDayOfMonth(1).toString());
        activities.add(lastMonthActivity);

        StravaActivity thisYearActivity = new StravaActivity();
        thisYearActivity.setSportType("run");
        thisYearActivity.setDistanceMeters(2000);
        thisYearActivity.setStartDateLocal(LocalDate.now().withDayOfMonth(2).toString());
        activities.add(thisYearActivity);

        Map<String, String> km = stravaService.calculateKm(activities, "run");
        assertTrue(km.containsKey("total_km_last_seven_day"));
        assertTrue(km.containsKey("total_km_last_month"));
        assertTrue(km.containsKey("total_km_year"));
    }

    @Test
    void testFilterActivities() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setMinDistance(1.0);
        filter.setMaxDistance(10.0);
        filter.setKeywords("morning");
        filter.setFromDate(LocalDate.now().minusDays(2).toString());
        filter.setToDate(LocalDate.now().plusDays(2).toString());

        List<StravaActivity> filtered = stravaService.filterActivities(activityList, filter);
        assertEquals(1, filtered.size());

        filter.setSportType("ride");
        filtered = stravaService.filterActivities(activityList, filter);
        assertEquals(0, filtered.size());
    }

    @Test
    void testCalculateSportTypeDistribution() {
        Map<String, Integer> result = stravaService.calculateSportTypeDistribution(activityList);
        assertEquals(1, result.get("run"));

        StravaActivity unknown = new StravaActivity();
        unknown.setSportType(null);
        List<StravaActivity> extended = new ArrayList<>(activityList);
        extended.add(unknown);
        Map<String, Integer> result2 = stravaService.calculateSportTypeDistribution(extended);
        assertEquals(1, result2.get("unknown"));
    }

    @Test
    void testGetActivities() {
        when(stravaProperties.getBaseUrl()).thenReturn("https://api.strava.com");
        when(baseApiService.get(anyString(), eq("/athlete/activities"), anyString(), eq(StravaActivity[].class)))
                .thenReturn(new ResponseEntity<>(activityList.toArray(new StravaActivity[0]), HttpStatus.OK));

        List<StravaActivity> result = stravaService.getActivities("token");
        assertEquals(1, result.size());
    }

    @Test
    void testGetActivityById() {
        when(stravaProperties.getBaseUrl()).thenReturn("https://api.strava.com");
        when(baseApiService.get(anyString(), contains("/activities/"), anyString(), eq(StravaActivity.class)))
                .thenReturn(new ResponseEntity<>(activityList.getFirst(), HttpStatus.OK));

        StravaActivity result = stravaService.getActivityById("token", "123");
        assertNotNull(result);
    }

    @Test
    void testSaveActivity() {
        AddActivityRequest request = new AddActivityRequest();
        request.setName("Test Activity");
        request.setElapsedTime(3600);
        request.setStartDateLocal("2024-03-01T08:00:00Z");

        when(stravaProperties.getBaseUrl()).thenReturn("https://api.strava.com");
        when(baseApiService.post(anyString(), anyString(), any(), any(HttpHeaders.class), eq(StravaActivity.class)))
                .thenReturn(new ResponseEntity<>(new StravaActivity(), HttpStatus.CREATED));

        StravaActivity result = stravaService.saveActivity(request, "token");
        assertNotNull(result);
    }

    @Test
    void testGetAthlete() {
        Athlete athlete = new Athlete();
        when(stravaProperties.getBaseUrl()).thenReturn("https://api.strava.com");
        when(baseApiService.get(anyString(), eq("/athlete"), anyString(), eq(Athlete.class)))
                .thenReturn(new ResponseEntity<>(athlete, HttpStatus.OK));

        Athlete result = stravaService.getAthlete("token");
        assertNotNull(result);
    }

    @Test
    void testCalculatePerformanceSummary() {
        Map<String, Object> result = stravaService.calculatePerformanceSummary(user, activityList, "run");
        assertEquals("run", result.get("sport_type"));
        assertTrue((Double) result.get("total_distance_km") > 0);
    }

    @Test
    void testCalculateByTimePeriodMonth() throws Exception {
        var method = StravaServiceImpl.class.getDeclaredMethod("calculateByTimePeriod", User.class, List.class, int.class, String.class);
        method.setAccessible(true);
        Map result = (Map) method.invoke(stravaService, user, activityList, 1, "month");
        assertNotNull(result);
    }

    @Test
    void testCalculateByTimePeriodDay() throws Exception {
        var method = StravaServiceImpl.class.getDeclaredMethod("calculateByTimePeriod", User.class, List.class, int.class, String.class);
        method.setAccessible(true);
        Map result = (Map) method.invoke(stravaService, user, activityList, 1, "day");
        assertNotNull(result);
    }

    @Test
    void testCalculateByTimePeriodYear() throws Exception {
        var method = StravaServiceImpl.class.getDeclaredMethod("calculateByTimePeriod", User.class, List.class, int.class, String.class);
        method.setAccessible(true);
        Map result = (Map) method.invoke(stravaService, user, activityList, 1, "year");
        assertNotNull(result);
    }

    @Test
    void testCalculateByTimePeriodInvalid() throws Exception {
        var method = StravaServiceImpl.class.getDeclaredMethod("calculateByTimePeriod", User.class, List.class, int.class, String.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(stravaService, user, activityList, 1, "week"));
    }

    @Test
    void testAllPerformanceMethods() {
        Map<String, Map<String, Double>> yearly = stravaService.calculateYearlyPerformance(user, activityList, "run");
        Map<String, Map<String, Double>> sixMonths = stravaService.calculateLastSixMonthsPerformance(user, activityList, "run");
        Map<String, Map<String, Double>> monthly = stravaService.calculateMonthlyPerformance(user, activityList, "run");
        Map<String, Map<String, Double>> daily = stravaService.calculateDailyPerformance(user, activityList, "run");
        Map<String, Map<String, Double>> weekly = stravaService.calculateWeeklyPerformance(user, activityList, "run");

        assertNotNull(yearly);
        assertNotNull(sixMonths);
        assertNotNull(monthly);
        assertNotNull(daily);
        assertNotNull(weekly);
    }

    @Test
    void testCaloriesConstantSwitch() throws Exception {
        Method method = StravaServiceImpl.class.getDeclaredMethod("calculateCalories", StravaActivity.class, User.class);
        method.setAccessible(true);

        for (SportType type : SportType.values()) {
            StravaActivity activity = new StravaActivity();
            activity.setDistanceMeters(1000);
            activity.setSportType(type.name());
            Object result = method.invoke(stravaService, activity, user);
            assertTrue(result instanceof Double);
        }
    }

    @Test
    void testFilterActivitiesBySportTypeReflection() throws Exception {
        Method method = StravaServiceImpl.class.getDeclaredMethod("filterActivitiesBySportType", List.class, SportType.class);
        method.setAccessible(true);
        Exception exception = assertThrows(Exception.class, () -> method.invoke(stravaService, activityList, (Object) null));
        assertTrue(exception.getCause().getMessage().contains("Type of sport not specified."));
        List<StravaActivity> resultAll = (List<StravaActivity>) method.invoke(stravaService, activityList, SportType.ALL);
        assertEquals(activityList.size(), resultAll.size());
        List<StravaActivity> resultRun = (List<StravaActivity>) method.invoke(stravaService, activityList, SportType.RUN);
        assertTrue(resultRun.stream().allMatch(a -> SportType.RUN.name().equalsIgnoreCase(a.getSportType())));
        StravaActivity unknown = new StravaActivity();
        unknown.setSportType("fencing");
        List<StravaActivity> mixed = new ArrayList<>(activityList);
        mixed.add(unknown);
        List<StravaActivity> resultOther = (List<StravaActivity>) method.invoke(stravaService, mixed, SportType.OTHER);
        assertEquals(1, resultOther.size());
        assertEquals("fencing", resultOther.getFirst().getSportType());
    }

    @Test
    void testSaveActivityFieldsAndError() {
        AddActivityRequest request = new AddActivityRequest();
        request.setName("Test Ride");
        request.setType("Ride");
        request.setStartDateLocal("2024-04-01T08:00:00Z");
        request.setElapsedTime(3600);
        request.setSportType("Ride");
        request.setDescription("Nice route");
        request.setDistance(20000);
        request.setTrainer(1);
        request.setCommute(0);

        when(stravaProperties.getBaseUrl()).thenReturn("https://api.strava.com");

        when(baseApiService.post(anyString(), anyString(), any(), any(HttpHeaders.class), eq(StravaActivity.class)))
                .thenReturn(new ResponseEntity<>(new StravaActivity(), HttpStatus.CREATED));

        StravaActivity created = stravaService.saveActivity(request, "token");
        assertNotNull(created);
        when(baseApiService.post(anyString(), anyString(), any(), any(HttpHeaders.class), eq(StravaActivity.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> stravaService.saveActivity(request, "token"));
        assertEquals("Error creating activity on Strava.", ex.getMessage());
    }

    @Test
    void testCalculateKm_YearBranch() {
        List<StravaActivity> activities = new ArrayList<>();

        StravaActivity a = new StravaActivity();
        a.setDistanceMeters(3000);
        a.setSportType("run");
        a.setStartDateLocal(LocalDate.now().withMonth(1).withDayOfMonth(1).toString());
        activities.add(a);

        Map<String, String> result = stravaService.calculateKm(activities, "run");

        assertTrue(Double.parseDouble(result.get("total_km_year")) > 0);
        assertTrue(result.containsKey("total_km_last_month"));
        assertTrue(result.containsKey("total_km_last_seven_day"));
    }
    @Test
    void testFilterActivitiesWithAllBranches() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setMinDistance(2.0);
        filter.setMaxDistance(10.0);
        filter.setKeywords("morning");
        filter.setFromDate(LocalDate.now().minusDays(1).toString());
        filter.setToDate(LocalDate.now().plusDays(1).toString());

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertEquals(1, result.size());

        filter.setSportType("ride");
        assertTrue(stravaService.filterActivities(activityList, filter).isEmpty());

        filter.setSportType("run");
        filter.setMinDistance(10.0);
        assertTrue(stravaService.filterActivities(activityList, filter).isEmpty());

        filter.setMinDistance(1.0);
        filter.setMaxDistance(2.0);
        assertTrue(stravaService.filterActivities(activityList, filter).isEmpty());

        filter.setMaxDistance(10.0);
        filter.setKeywords("evening");
        assertTrue(stravaService.filterActivities(activityList, filter).isEmpty());

        filter.setKeywords("morning");
        filter.setFromDate(LocalDate.now().plusDays(1).toString());
        filter.setToDate(LocalDate.now().plusDays(2).toString());
        assertTrue(stravaService.filterActivities(activityList, filter).isEmpty());
    }

    @Test
    void testFilterActivitiesBySportType_SportTypeNullButFilterRun() throws Exception {
        Method method = StravaServiceImpl.class.getDeclaredMethod("filterActivitiesBySportType", List.class, SportType.class);
        method.setAccessible(true);

        StravaActivity nullSport = new StravaActivity();
        nullSport.setSportType(null);
        List<StravaActivity> input = List.of(nullSport);

        List<?> result = (List<?>) method.invoke(stravaService, input, SportType.RUN);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCalculateCalories_WithNullSportType() throws Exception {
        Method method = StravaServiceImpl.class.getDeclaredMethod("calculateCalories", StravaActivity.class, User.class);
        method.setAccessible(true);

        StravaActivity activity = new StravaActivity();
        activity.setDistanceMeters(1000);
        activity.setSportType(null);

        double calories = (double) method.invoke(stravaService, activity, user);
        assertEquals(0.0, calories);
    }

    @Test
    void testFilterActivitiesBySportType_OtherIncludesNullCheck() throws Exception {
        Method method = StravaServiceImpl.class.getDeclaredMethod("filterActivitiesBySportType", List.class, SportType.class);
        method.setAccessible(true);

        StravaActivity nullSport = new StravaActivity();
        nullSport.setSportType(null);

        StravaActivity known = new StravaActivity();
        known.setSportType("RIDE");

        StravaActivity unknown = new StravaActivity();
        unknown.setSportType("FENCING");

        List<StravaActivity> input = List.of(nullSport, known, unknown);

        @SuppressWarnings("unchecked")
        List<StravaActivity> result = (List<StravaActivity>) method.invoke(stravaService, input, SportType.OTHER);
        assertEquals(1, result.size());
        assertEquals("FENCING", result.getFirst().getSportType());
    }

    @Test
    void testCalculateByTimePeriod_keyNotPresentInData() throws Exception {
        Method method = StravaServiceImpl.class.getDeclaredMethod("calculateByTimePeriod", User.class, List.class, int.class, String.class);
        method.setAccessible(true);

        StravaActivity oldActivity = new StravaActivity();
        oldActivity.setDistanceMeters(5000);
        oldActivity.setAverageSpeed(2.5);
        oldActivity.setTotalElevationGain(50);
        oldActivity.setStartDateLocal("2000-01-01T00:00:00Z");
        oldActivity.setSportType("run");

        List<StravaActivity> input = List.of(oldActivity);

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Double>> result = (Map<String, Map<String, Double>>) method.invoke(stravaService, user, input, 1, "month");
        for (Map<String, Double> stats : result.values()) {
            assertTrue(stats.isEmpty());
        }
    }


    @Test
    void testCalculateKm_ActivityOutsideCurrentYear() {
        List<StravaActivity> activities = new ArrayList<>();
        StravaActivity thisYear = new StravaActivity();
        thisYear.setSportType("run");
        thisYear.setDistanceMeters(4000);
        thisYear.setStartDateLocal(LocalDate.now().withDayOfMonth(1).toString());
        activities.add(thisYear);

        StravaActivity oldYear = new StravaActivity();
        oldYear.setSportType("run");
        oldYear.setDistanceMeters(10000);
        oldYear.setStartDateLocal("2020-06-15T00:00:00Z");
        activities.add(oldYear);

        Map<String, String> result = stravaService.calculateKm(activities, "run");

        double yearKm = Double.parseDouble(result.get("total_km_year"));
        assertEquals(4.00, yearKm, 0.01);
    }

    @Test
    void testFilterActivities_SportTypeNull_ShouldNotFilterByType() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType(null);

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertEquals(1, result.size());
    }

    @Test
    void testFilterActivities_SportTypeAll_ShouldNotFilterByType() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("ALL");

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertEquals(1, result.size());
    }

    @Test
    void testFilterActivities_KeywordsNull_ShouldSkipKeywordFilter() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setKeywords(null);

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertEquals(1, result.size());
    }

    @Test
    void testFilterActivities_KeywordsBlank_ShouldSkipKeywordFilter() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setKeywords("   ");

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertEquals(1, result.size());
    }

    @Test
    void testFilterActivities_NoMatchInTitleOrDescription_ShouldBeFilteredOut() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setKeywords("xyz");

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterActivities_MatchInTitleOnly_ShouldPass() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setKeywords("morning");

        StravaActivity a = new StravaActivity();
        a.setSportType("run");
        a.setDistanceMeters(1000);
        a.setStartDateLocal(LocalDate.now().toString());
        a.setName("Morning Session");
        a.setDescription(null);

        List<StravaActivity> result = stravaService.filterActivities(List.of(a), filter);
        assertEquals(1, result.size());
    }


    @Test
    void testFilterActivities_MatchInDescriptionOnly_ShouldPass() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setKeywords("chill");

        StravaActivity a = new StravaActivity();
        a.setSportType("run");
        a.setDistanceMeters(1000);
        a.setStartDateLocal(LocalDate.now().toString());
        a.setName(null);
        a.setDescription("Chill pace");

        List<StravaActivity> result = stravaService.filterActivities(List.of(a), filter);
        assertEquals(1, result.size());
    }

    @Test
    void testFilterActivities_DateFilter_NullFromAndTo_ShouldSkipDateCheck() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setKeywords("morning");
        filter.setFromDate(null);
        filter.setToDate(null);

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertEquals(1, result.size());
    }


    @Test
    void testFilterActivities_DateFilter_FromDateOnly() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setKeywords("morning");
        filter.setFromDate(LocalDate.now().minusDays(2).toString());
        filter.setToDate(null);

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertEquals(1, result.size());
    }

    @Test
    void testFilterActivities_DateFilter_ToDateOnly() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setKeywords("morning");
        filter.setFromDate(null);
        filter.setToDate(LocalDate.now().plusDays(2).toString());

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertEquals(1, result.size());
    }

    @Test
    void testFilterActivities_ToDateNull_ShouldNotCheckIsAfter() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setToDate(null);

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertEquals(1, result.size());
    }

    @Test
    void testFilterActivities_ToDatePresent_NotAfter_ShouldPass() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setToDate(LocalDate.now().plusDays(1).toString());

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertEquals(1, result.size());
    }

    @Test
    void testFilterActivities_ToDatePresent_ActivityAfter_ShouldBeFilteredOut() {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("run");
        filter.setToDate(LocalDate.now().minusDays(1).toString());

        List<StravaActivity> result = stravaService.filterActivities(activityList, filter);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserActivities_ShouldFilterByDateAndSportType() {

        String accessToken = "test-token";
        LocalDateTime fromDate = LocalDateTime.now().minusDays(7);
        SportType sportType = SportType.RUN;
        StravaActivity[] mockActivitiesArray = new StravaActivity[3];
        StravaActivity matchingActivity = new StravaActivity();
        matchingActivity.setStartDateLocal(LocalDate.now().minusDays(3).toString() + "T10:00:00Z");
        matchingActivity.setSportType("run");
        mockActivitiesArray[0] = matchingActivity;
        StravaActivity oldActivity = new StravaActivity();
        oldActivity.setStartDateLocal(LocalDate.now().minusDays(8).toString() + "T10:00:00Z");
        oldActivity.setSportType("run");
        mockActivitiesArray[1] = oldActivity;
        StravaActivity wrongSportActivity = new StravaActivity();
        wrongSportActivity.setStartDateLocal(LocalDate.now().minusDays(3).toString() + "T10:00:00Z");
        wrongSportActivity.setSportType("ride");
        mockActivitiesArray[2] = wrongSportActivity;
        when(stravaProperties.getBaseUrl()).thenReturn("https://api.strava.com");
        when(baseApiService.get(
                eq("https://api.strava.com"),
                eq("/athlete/activities"),
                eq(accessToken),
                eq(StravaActivity[].class)
        )).thenReturn(ResponseEntity.ok(mockActivitiesArray));
        List<StravaActivity> result = stravaService.getUserActivities(accessToken, fromDate, sportType);
        assertEquals(1, result.size());
        assertEquals("run", result.getFirst().getSportType());
        assertTrue(LocalDate.parse(result.getFirst().getStartDateLocal().substring(0, 10))
                .atStartOfDay().isAfter(fromDate));
        List<StravaActivity> allSportsResult = stravaService.getUserActivities(accessToken, fromDate, SportType.ALL);
        assertEquals(2, allSportsResult.size());
    }
}
