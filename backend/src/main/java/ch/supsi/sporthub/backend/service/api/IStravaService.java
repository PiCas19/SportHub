package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.dto.request.strava.ActivityFilterRequest;
import ch.supsi.sporthub.backend.dto.request.strava.AddActivityRequest;
import ch.supsi.sporthub.backend.model.SportType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.domain.strava.Athlete;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface for interacting with Strava services.
 * Provides methods to retrieve, filter, and analyze Strava activities,
 * as well as to manage athlete data and statistics.
 */
public interface IStravaService {

    /**
     * Retrieves the list of all Strava activities for the user.
     *
     * @param accessToken the Strava API access token
     * @return list of Strava activities
     */
    List<StravaActivity> getActivities(String accessToken);

    /**
     * Retrieves the athlete profile associated with the access token.
     *
     * @param accessToken the Strava API access token
     * @return the athlete profile
     */
    Athlete getAthlete(String accessToken);

    /**
     * Retrieves a specific activity by its ID.
     *
     * @param accessToken the Strava API access token
     * @param activityId the ID of the activity
     * @return the activity details
     */
    StravaActivity getActivityById(String accessToken, String activityId);

    /**
     * Saves a new activity to Strava.
     *
     * @param request the activity data to add
     * @param accessToken the Strava API access token
     * @return the created activity
     */
    StravaActivity saveActivity(AddActivityRequest request, String accessToken);

    /**
     * Calculates a summary of the user's performance.
     *
     * @param user the user whose performance is being evaluated
     * @param activities list of activities
     * @param sportType the sport type to filter by
     * @return map with performance metrics
     */
    Map<String, Object> calculatePerformanceSummary(User user, List<StravaActivity> activities, String sportType);

    /**
     * Calculates the user's monthly performance for the specified sport.
     *
     * @param user the user
     * @param activities list of activities
     * @param sportType the sport type
     * @return map with monthly performance data
     */
    Map<String, Map<String, Double>> calculateMonthlyPerformance(User user, List<StravaActivity> activities, String sportType);

    /**
     * Calculates the distribution of activities across sport types.
     *
     * @param activities list of activities
     * @return map with sport type and their respective count
     */
    Map<String, Integer> calculateSportTypeDistribution(List<StravaActivity> activities);

    /**
     * Calculates daily performance data for a user.
     *
     * @param user the user
     * @param activities list of activities
     * @param sportType the sport type
     * @return map with daily performance values
     */
    Map<String, Map<String, Double>> calculateDailyPerformance(User user, List<StravaActivity> activities, String sportType);

    /**
     * Calculates weekly performance statistics.
     *
     * @param user the user
     * @param activities list of activities
     * @param sportType the sport type
     * @return map with weekly metrics
     */
    Map<String, Map<String, Double>> calculateWeeklyPerformance(User user, List<StravaActivity> activities, String sportType);

    /**
     * Calculates performance metrics aggregated by year.
     *
     * @param user the user
     * @param activities list of activities
     * @param sportType the sport type
     * @return yearly performance statistics
     */
    Map<String, Map<String, Double>> calculateYearlyPerformance(User user, List<StravaActivity> activities, String sportType);

    /**
     * Calculates statistics for the last six months.
     *
     * @param user the user
     * @param activities list of activities
     * @param sportType the sport type
     * @return performance data for the last six months
     */
    Map<String, Map<String, Double>> calculateLastSixMonthsPerformance(User user, List<StravaActivity> activities, String sportType);

    /**
     * Calculates total kilometers per sport type.
     *
     * @param activities list of activities
     * @param sportType the sport type
     * @return map with sport type and total kilometers
     */
    Map<String, String> calculateKm(List<StravaActivity> activities, String sportType);

    /**
     * Filters activities based on the provided filter criteria.
     *
     * @param activities list of activities to filter
     * @param filter the filter criteria
     * @return filtered list of activities
     */
    List<StravaActivity> filterActivities(List<StravaActivity> activities, ActivityFilterRequest filter);

    /**
     * Retrieves user activities from a given start date and for a specific sport.
     *
     * @param accessToken the access token
     * @param from the start date
     * @param sportType the sport type to filter
     * @return list of filtered activities
     */
    List<StravaActivity> getUserActivities(String accessToken, LocalDateTime from, SportType sportType);
}
