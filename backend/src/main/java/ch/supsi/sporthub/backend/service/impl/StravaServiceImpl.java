package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.config.StravaProperties;
import ch.supsi.sporthub.backend.dto.request.strava.ActivityFilterRequest;
import ch.supsi.sporthub.backend.dto.request.strava.AddActivityRequest;
import ch.supsi.sporthub.backend.model.SportType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import ch.supsi.sporthub.backend.service.api.IStravaService;
import ch.supsi.sporthub.backend.service.domain.strava.Athlete;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the IStravaService interface that integrates with the Strava API to retrieve, create, and filter activities.
 * This service handles operations such as fetching user activities, calculating performance summaries, and saving new activities.
 */
@Service
public class StravaServiceImpl implements IStravaService {
    private final StravaProperties stravaConfig;
    private final IBaseApiService baseApiService;

    /**
     * Constructor for the StravaServiceImpl class.
     * Initializes the service with the base API service and Strava configuration properties.
     *
     * @param baseApiService The service for making API requests.
     * @param stravaConfig   The Strava configuration properties (e.g., base URL, API keys).
     */
    public StravaServiceImpl(IBaseApiService baseApiService, StravaProperties stravaConfig) {
        this.baseApiService = baseApiService;
        this.stravaConfig = stravaConfig;
    }

    /**
     * Parses a string to a SportType enum. Defaults to SportType.ALL if input is invalid.
     *
     * @param input The string representing the sport type.
     * @return The corresponding SportType enum.
     */
    private SportType parseSportType(String input) {
        if (input == null || input.isBlank()) return SportType.ALL;
        try {
            return SportType.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return SportType.OTHER;
        }
    }

    /**
     * Filters a list of activities by a specific sport type.
     *
     * @param activities The list of Strava activities.
     * @param sportType The sport type to filter by.
     * @return A list of filtered activities matching the specified sport type.
     */
    private List<StravaActivity> filterActivitiesBySportType(List<StravaActivity> activities, SportType sportType) {
        if (sportType == null) {
            throw new RuntimeException("Type of sport not specified.");
        }

        if (sportType == SportType.ALL) {
            return activities;
        }

        if (sportType == SportType.OTHER) {
            Set<String> knownTypes = Stream.of(SportType.RUN, SportType.RIDE, SportType.WALK, SportType.SWIM).map(Enum::name).collect(Collectors.toSet());

            return activities.stream()
                    .filter(a -> a.getSportType() != null && !knownTypes.contains(a.getSportType().toUpperCase()))
                    .toList();
        }

        return activities.stream()
                .filter(a -> a.getSportType() != null && a.getSportType().equalsIgnoreCase(sportType.name()))
                .toList();
    }

    /**
     * Calculates the calories burned during a Strava activity based on the user's weight, height, and the sport type.
     *
     * @param activity The Strava activity.
     * @param athlete  The user (athlete) who completed the activity.
     * @return The estimated number of calories burned during the activity.
     */
    private double calculateCalories(StravaActivity activity, User athlete) {
        double distanceKm = activity.getDistanceMeters() / 1000.0;
        double weight = athlete.getWeight();
        double height = athlete.getHeight();
        SportType type = parseSportType(activity.getSportType());

        double constant = switch (type) {
            case RUN -> 1.036;
            case RIDE -> 0.8;
            case SWIM -> 1.2;
            case WALK -> 1.0;
            default -> 0.0;
        };

        return distanceKm * weight * (height / 170.0) * constant;
    }

    /**
     * Fetches a list of activities for a user from the Strava API.
     *
     * @param accessToken The Strava access token.
     * @return A list of Strava activities.
     */
    @Override
    public List<StravaActivity> getActivities(String accessToken) {
        ResponseEntity<StravaActivity[]> response = baseApiService.get(
                stravaConfig.getBaseUrl(), "/athlete/activities", accessToken, StravaActivity[].class);
        return List.of(Objects.requireNonNull(response.getBody()));
    }

    /**
     * Fetches a specific Strava activity by its ID.
     *
     * @param accessToken The Strava access token.
     * @param activityId  The ID of the Strava activity.
     * @return The requested Strava activity.
     */
    @Override
    public StravaActivity getActivityById(String accessToken, String activityId) {
        return Objects.requireNonNull(baseApiService.get(
                stravaConfig.getBaseUrl(), "/activities/" + activityId, accessToken, StravaActivity.class).getBody());
    }

    /**
     * Creates a new activity on Strava.
     *
     * @param request      The request containing the activity data.
     * @param accessToken  The Strava access token.
     * @return The created Strava activity.
     */
    @Override
    public StravaActivity saveActivity(AddActivityRequest request, String accessToken) {
        Map<String, Object> activityData = getStringObjectMap(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<StravaActivity> response = baseApiService.post(
                stravaConfig.getBaseUrl(), "/activities", activityData, headers, StravaActivity.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Error creating activity on Strava.");
        }
    }

    /**
     * Helper method to convert an AddActivityRequest into a map for the Strava API.
     *
     * @param request The request containing activity data.
     * @return A map of activity data to send to Strava.
     */
    private static Map<String, Object> getStringObjectMap(AddActivityRequest request) {
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("name", request.getName());
        activityData.put("type", request.getType());
        activityData.put("start_date_local", request.getStartDateLocal());
        activityData.put("elapsed_time", request.getElapsedTime());
        if (request.getSportType() != null) activityData.put("sport_type", request.getSportType());
        if (request.getDescription() != null) activityData.put("description", request.getDescription());
        if (request.getDistance() != 0) activityData.put("distance", request.getDistance());
        if (request.getTrainer() != null) activityData.put("trainer", request.getTrainer());
        if (request.getCommute() != null) activityData.put("commute", request.getCommute());
        return activityData;
    }

    /**
     * Fetches the athlete's profile information from the Strava API.
     *
     * @param accessToken The Strava access token.
     * @return The athlete's profile data.
     */
    @Override
    public Athlete getAthlete(String accessToken) {
        return Objects.requireNonNull(baseApiService.get(
                stravaConfig.getBaseUrl(), "/athlete", accessToken, Athlete.class).getBody());
    }

    /**
     * Calculates a performance summary based on a list of Strava activities for a given sport type.
     *
     * @param user       The user whose performance is being calculated.
     * @param activities The list of Strava activities.
     * @param sportType  The sport type to calculate performance for.
     * @return A map containing performance metrics like total distance, total calories, etc.
     */
    @Override
    public Map<String, Object> calculatePerformanceSummary(User user, List<StravaActivity> activities, String sportType) {
        List<StravaActivity> filtered = filterActivitiesBySportType(activities, parseSportType(sportType));
        return Map.of(
                "sport_type", sportType,
                "total_distance_km", filtered.stream().mapToDouble(a -> a.getDistanceMeters() / 1000).sum(),
                "total_calories", filtered.stream().mapToDouble(a -> calculateCalories(a, user)).sum(),
                "average_speed_mps", filtered.stream().mapToDouble(StravaActivity::getAverageSpeed).average().orElse(0),
                "total_elevation_gain", filtered.stream().mapToDouble(StravaActivity::getTotalElevationGain).sum(),
                "total_activities", filtered.size()
        );
    }

    /**
     * Calculates performance over the past year for a given sport type.
     *
     * @param user       The user whose performance is being calculated.
     * @param activities The list of Strava activities.
     * @param sportType  The sport type to calculate performance for.
     * @return A map containing the yearly performance summary.
     */
    @Override
    public Map<String, Map<String, Double>> calculateYearlyPerformance(User user, List<StravaActivity> activities, String sportType) {
        return calculateByTimePeriod(user, filterActivitiesBySportType(activities, parseSportType(sportType)), 12, "year");
    }

    /**
     * Calculates performance over the last 6 months for a given sport type.
     *
     * @param user       The user whose performance is being calculated.
     * @param activities The list of Strava activities.
     * @param sportType  The sport type to calculate performance for.
     * @return A map containing the 6-month performance summary.
     */
    @Override
    public Map<String, Map<String, Double>> calculateLastSixMonthsPerformance(User user, List<StravaActivity> activities, String sportType) {
        return calculateByTimePeriod(user, filterActivitiesBySportType(activities, parseSportType(sportType)), 6, "month");
    }

    /**
     * Calculates monthly performance for a given sport type.
     *
     * @param user       The user whose performance is being calculated.
     * @param activities The list of Strava activities.
     * @param sportType  The sport type to calculate performance for.
     * @return A map containing the monthly performance summary.
     */
    @Override
    public Map<String, Map<String, Double>> calculateMonthlyPerformance(User user, List<StravaActivity> activities, String sportType) {
        return calculateByTimePeriod(user, filterActivitiesBySportType(activities, parseSportType(sportType)), 1, "month");
    }

    /**
     * Calculates daily performance for the last 7 days for a given sport type.
     *
     * @param user       The user whose performance is being calculated.
     * @param activities The list of Strava activities.
     * @param sportType  The sport type to calculate performance for.
     * @return A map containing the daily performance summary.
     */
    @Override
    public Map<String, Map<String, Double>> calculateDailyPerformance(User user, List<StravaActivity> activities, String sportType) {
        return calculateByTimePeriod(user, filterActivitiesBySportType(activities, parseSportType(sportType)), 7, "day");
    }

    /**
     * Calculates weekly performance for the last 7 days for a given sport type.
     *
     * @param user       The user whose performance is being calculated.
     * @param activities The list of Strava activities.
     * @param sportType  The sport type to calculate performance for.
     * @return A map containing the weekly performance summary.
     */
    @Override
    public Map<String, Map<String, Double>> calculateWeeklyPerformance(User user, List<StravaActivity> activities, String sportType) {
        return calculateByTimePeriod(user, filterActivitiesBySportType(activities, parseSportType(sportType)), 7, "day");
    }

    /**
     * Helper method to calculate performance metrics over a specified time period (e.g., daily, monthly, yearly).
     *
     * @param user        The user whose performance is being calculated.
     * @param activities  The list of Strava activities.
     * @param length      The length of the time period.
     * @param unit        The unit of time (e.g., "day", "month", "year").
     * @return A map of performance data for each time period.
     */
    private Map<String, Map<String, Double>> calculateByTimePeriod(User user, List<StravaActivity> activities, int length, String unit) {
        Map<String, Map<String, Double>> data = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();

        List<String> keys = switch (unit) {
            case "month" -> Stream.iterate(YearMonth.from(now.minusMonths(length - 1)), ym -> ym.plusMonths(1))
                    .limit(length).map(YearMonth::toString).toList();
            case "day" -> Stream.iterate(now.minusDays(length - 1), d -> d.plusDays(1))
                    .limit(length).map(LocalDate::toString).toList();
            case "year" -> Stream.iterate(YearMonth.from(now).minusMonths(length - 1), ym -> ym.plusMonths(1))
                    .limit(length).map(YearMonth::toString).toList();
            default -> throw new RuntimeException("Time unit not supported");
        };

        keys.forEach(k -> data.put(k, new HashMap<>()));

        for (StravaActivity activity : activities) {
            String dateStr = activity.getStartDateLocal().substring(0, 10);
            String key = unit.equals("day") ? dateStr : YearMonth.from(LocalDate.parse(dateStr)).toString();
            if (data.containsKey(key)) {
                Map<String, Double> stats = data.get(key);
                stats.merge("total_distance", activity.getDistanceMeters() / 1000.0, Double::sum);
                stats.merge("total_calories", calculateCalories(activity, user), Double::sum);
                stats.merge("average_speed", activity.getAverageSpeed(), (a, b) -> (a + b) / 2);
                stats.merge("total_elevation_gain", activity.getTotalElevationGain(), Double::sum);
            }
        }

        return data;
    }

    /**
     * Calculates the total distance covered (in kilometers) in the last 7 days, last month, and this year for a given sport type.
     *
     * @param activities The list of Strava activities.
     * @param sportType  The sport type to calculate distance for.
     * @return A map containing the total distance for the last 7 days, last month, and this year.
     */
    @Override
    public Map<String, String> calculateKm(List<StravaActivity> activities, String sportType) {
        List<StravaActivity> filtered = filterActivitiesBySportType(activities, parseSportType(sportType));
        LocalDate now = LocalDate.now();

        double km7 = filtered.stream()
                .filter(a -> !LocalDate.parse(a.getStartDateLocal().substring(0, 10)).isBefore(now.minusDays(7)))
                .mapToDouble(a -> a.getDistanceMeters() / 1000.0).sum();

        double kmMonth = filtered.stream()
                .filter(a -> YearMonth.from(LocalDate.parse(a.getStartDateLocal().substring(0, 10))).equals(YearMonth.from(now).minusMonths(1)))
                .mapToDouble(a -> a.getDistanceMeters() / 1000.0).sum();

        double kmYear = filtered.stream()
                .filter(a -> LocalDate.parse(a.getStartDateLocal().substring(0, 10)).getYear() == now.getYear())
                .mapToDouble(a -> a.getDistanceMeters() / 1000.0).sum();

        return Map.of(
                "total_km_last_seven_day", String.format("%.2f", km7),
                "total_km_last_month", String.format("%.2f", kmMonth),
                "total_km_year", String.format("%.2f", kmYear)
        );
    }

    /**
     * Filters a list of Strava activities based on the provided filter criteria.
     *
     * @param activities The list of activities to filter.
     * @param filter     The filter criteria.
     * @return A filtered list of Strava activities.
     */
    @Override
    public List<StravaActivity> filterActivities(List<StravaActivity> activities, ActivityFilterRequest filter) {
        return activities.stream()
                .filter(activity -> {
                    if (filter.getSportType() != null && !filter.getSportType().equalsIgnoreCase("all")) {
                        if (!activity.getSportType().equalsIgnoreCase(filter.getSportType())) return false;
                    }
                    double distanceKm = activity.getDistanceMeters() / 1000.0;
                    if (filter.getMinDistance() != null && distanceKm < filter.getMinDistance()) return false;
                    if (filter.getMaxDistance() != null && distanceKm > filter.getMaxDistance()) return false;

                    if (filter.getKeywords() != null && !filter.getKeywords().isBlank()) {
                        String search = filter.getKeywords().toLowerCase();
                        boolean inTitle = activity.getName() != null && activity.getName().toLowerCase().contains(search);
                        boolean inDesc = activity.getDescription() != null && activity.getDescription().toLowerCase().contains(search);
                        if (!inTitle && !inDesc) return false;
                    }

                    if (filter.getFromDate() != null || filter.getToDate() != null) {
                        LocalDate activityDate = LocalDate.parse(activity.getStartDateLocal().substring(0, 10));
                        if (filter.getFromDate() != null && activityDate.isBefore(LocalDate.parse(filter.getFromDate()))) return false;
                        return filter.getToDate() == null || !activityDate.isAfter(LocalDate.parse(filter.getToDate()));
                    }

                    return true;
                })
                .toList();
    }

    /**
     * Calculates the distribution of sport types in a list of Strava activities.
     *
     * @param activities The list of Strava activities.
     * @return A map containing sport types and their respective count.
     */
    @Override
    public Map<String, Integer> calculateSportTypeDistribution(List<StravaActivity> activities) {
        Map<String, Integer> sportTypeCount = new HashMap<>();
        for (StravaActivity activity : activities) {
            String sport = activity.getSportType() != null ? activity.getSportType() : "unknown";
            sportTypeCount.put(sport, sportTypeCount.getOrDefault(sport, 0) + 1);
        }
        return sportTypeCount;
    }

    /**
     * Fetches the user's activities from Strava based on the provided access token, a start time, and sport type.
     *
     * @param accessToken The Strava access token.
     * @param from        The start time to filter activities from.
     * @param sportType   The sport type to filter activities by.
     * @return A list of filtered Strava activities.
     */
    @Override
    public List<StravaActivity> getUserActivities(String accessToken, LocalDateTime from, SportType sportType) {
        List<StravaActivity> activities = getActivities(accessToken);

        return activities.stream()
                .filter(a -> a.getStartDateLocal() != null &&
                        LocalDate.parse(a.getStartDateLocal().substring(0, 10)).atStartOfDay().isAfter(from))
                .filter(a -> sportType == SportType.ALL || a.getSportType().equalsIgnoreCase(sportType.name()))
                .toList();
    }

}