package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.strava.*;
import ch.supsi.sporthub.backend.dto.response.Response;
import ch.supsi.sporthub.backend.dto.response.strava.StravaUrlResponse;
import ch.supsi.sporthub.backend.exception.MissingAuthorizationCodeException;
import ch.supsi.sporthub.backend.exception.StravaTokenUnavailableException;
import ch.supsi.sporthub.backend.model.StravaToken;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.UserService;
import ch.supsi.sporthub.backend.service.api.INotificationService;
import ch.supsi.sporthub.backend.service.api.IStravaService;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * StravaController handles the Strava-related operations, such as user authentication,
 * managing Strava tokens, retrieving activities, calculating performance, and more.
 * It provides the necessary API endpoints to interact with Strava data.
 */
@RestController
@RequestMapping("/api/strava")
public class StravaController extends BaseController {

    private final StravaAuthService stravaAuthService;
    private final IStravaService stravaService;
    private final INotificationService notificationService;

    /**
     * Constructs an instance of StravaController with the necessary services.
     *
     * @param stravaAuthService The service for handling Strava authentication and token management.
     * @param stravaService     The service for retrieving and managing Strava activities.
     * @param jwtTokenService   The service for handling JWT tokens.
     * @param userService       The service for user management.
     * @param notificationService The service for sending notifications.
     */
    public StravaController(StravaAuthService stravaAuthService,
                            IStravaService stravaService,
                            JwtTokenService jwtTokenService,
                            UserService userService,
                            INotificationService notificationService) {
        super(jwtTokenService, userService);
        this.stravaAuthService = stravaAuthService;
        this.stravaService = stravaService;
        this.notificationService = notificationService;
    }

    /**
     * Initiates the Strava login process by checking if the user is already authenticated.
     * If not authenticated, it returns a URL for Strava authorization.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A response entity containing the Strava authorization URL or a message if already authenticated.
     */
    @GetMapping("/login")
    public ResponseEntity<Response> login(@RequestHeader("Authorization") String authHeader) {
        String username = jwtTokenService.extractUsername(extractToken(authHeader));

        if (stravaAuthService.refreshAccessTokenIfNeeded(username).isPresent()) {
            return ResponseEntity.ok(new Response("Access token refreshed successfully."));
        }

        if (stravaAuthService.getValidAccessToken(username).isPresent()) {
            return ResponseEntity.ok(new Response("User already authenticated with Strava."));
        }

        return ResponseEntity.ok(new StravaUrlResponse("Strava Authorization Required", stravaAuthService.getAuthorizationUrl()));
    }

    /**
     * Retrieves the access token using the authorization code received from Strava.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @param request The request body containing the Strava authorization code.
     * @return A response entity containing the success message.
     * @throws MissingAuthorizationCodeException If the authorization code is missing or invalid.
     */
    @PostMapping("/token")
    public ResponseEntity<Response> getAccessToken(@RequestHeader("Authorization") String authHeader,
                                                   @RequestBody AuthorizationStravaCodeRequest request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new MissingAuthorizationCodeException("Missing authorization code");
        }

        String username = jwtTokenService.extractUsername(extractToken(authHeader));
        stravaAuthService.getAccessToken(username, request.getCode());
        return ResponseEntity.ok(new Response("Authentication successful!"));
    }

    /**
     * Refreshes the Strava access token using the refresh token.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A response entity containing the success message.
     * @throws StravaTokenUnavailableException If the Strava refresh token is missing or expired.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<Response> refreshAccessToken(@RequestHeader("Authorization") String authHeader) {
        String username = jwtTokenService.extractUsername(extractToken(authHeader));
        Optional<StravaToken> refreshOpt = stravaAuthService.getValidRefreshToken(username);

        if (refreshOpt.isEmpty() || refreshOpt.get().getRefreshToken() == null) {
            throw new StravaTokenUnavailableException("Strava refresh token is missing or expired.");
        }

        stravaAuthService.refreshAccessToken(refreshOpt.get().getRefreshToken(), username);
        return ResponseEntity.ok(new Response("Token refreshed successfully!"));
    }

    /**
     * Validates if the user's Strava token is valid.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A response entity indicating whether the token is valid or expired.
     */
    @GetMapping("/validate-token")
    public ResponseEntity<Response> validateStravaToken(@RequestHeader("Authorization") String authHeader) {
        String username = jwtTokenService.extractUsername(extractToken(authHeader));
        boolean isValid = stravaAuthService.isUserTokenValid(username);
        return ResponseEntity.ok(new Response(isValid ? "Token is valid" : "Token is expired"));
    }

    /**
     * Retrieves the activities for the authenticated user from Strava.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A response entity containing the list of activities or a message if no activities are found.
     */
    @GetMapping("/activities")
    public ResponseEntity<?> getActivities(@RequestHeader("Authorization") String authHeader) {
        String accessToken = getValidAccessTokenOrThrow(authHeader);
        List<StravaActivity> activities = stravaService.getActivities(accessToken);
        return ResponseEntity.ok(activities.isEmpty() ? Map.of("message", "No activities found") : activities);
    }

    /**
     * Retrieves a specific Strava activity by its ID.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @param id The ID of the Strava activity.
     * @return A response entity containing the activity data.
     */
    @GetMapping("/activities/{id}")
    public ResponseEntity<StravaActivity> getActivityById(@RequestHeader("Authorization") String authHeader,
                                                          @PathVariable String id) {
        return ResponseEntity.ok(stravaService.getActivityById(getValidAccessTokenOrThrow(authHeader), id));
    }

    /**
     * Retrieves the Strava athlete's profile information.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A response entity containing the athlete's profile data.
     */
    @GetMapping("/athlete")
    public ResponseEntity<?> getAthlete(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(stravaService.getAthlete(getValidAccessTokenOrThrow(authHeader)));
    }

    /**
     * Adds a new activity for the authenticated user.
     *
     * @param request The request body containing the activity details.
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A response entity containing the newly added activity.
     */
    @PostMapping("/activities")
    public ResponseEntity<StravaActivity> addActivity(@RequestBody AddActivityRequest request,
                                                      @RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        StravaActivity activity = stravaService.saveActivity(request, getValidAccessTokenOrThrow(authHeader));
        notificationService.notifyActivityCompleted(user, activity);
        return ResponseEntity.ok(activity);
    }

    /**
     * Filters activities based on the provided criteria.
     *
     * @param filter The filter criteria for the activities.
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A response entity containing the filtered list of activities.
     */
    @PostMapping("/activities/filter")
    public ResponseEntity<List<StravaActivity>> filterActivities(@RequestBody ActivityFilterRequest filter,
                                                                 @RequestHeader("Authorization") String authHeader) {
        String accessToken = getValidAccessTokenOrThrow(authHeader);
        return ResponseEntity.ok(stravaService.filterActivities(stravaService.getActivities(accessToken), filter));
    }

    /**
     * Retrieves the performance summary for the authenticated user based on their Strava activities.
     * The performance summary includes various statistics such as total distance, total time, etc.
     * The summary can be filtered by sport type.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @param sportType  The type of sport to filter the performance data (default is "all").
     * @return A response entity containing the performance summary.
     */
    @GetMapping("/performance/summary")
    public ResponseEntity<?> getSummary(@RequestHeader("Authorization") String authHeader,
                                        @RequestParam(defaultValue = "all") String sportType) {
        return ResponseEntity.ok(stravaService.calculatePerformanceSummary(
                getUserFromHeader(authHeader),
                stravaService.getActivities(getValidAccessTokenOrThrow(authHeader)),
                sportType));
    }

    /**
     * Retrieves the monthly performance data for the authenticated user.
     * This includes statistics such as total distance and time for each month.
     * The data can be filtered by sport type.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @param sportType  The type of sport to filter the performance data (default is "all").
     * @return A response entity containing the monthly performance data.
     */
    @GetMapping("/performance/monthly")
    public ResponseEntity<?> getMonthly(@RequestHeader("Authorization") String authHeader,
                                        @RequestParam(defaultValue = "all") String sportType) {
        return ResponseEntity.ok(stravaService.calculateMonthlyPerformance(
                getUserFromHeader(authHeader),
                stravaService.getActivities(getValidAccessTokenOrThrow(authHeader)),
                sportType));
    }

    /**
     * Retrieves the weekly performance data for the authenticated user.
     * This includes statistics such as total distance and time for each week.
     * The data can be filtered by sport type.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @param sportType  The type of sport to filter the performance data (default is "all").
     * @return A response entity containing the weekly performance data.
     */
    @GetMapping("/performance/weekly")
    public ResponseEntity<?> getWeekly(@RequestHeader("Authorization") String authHeader,
                                       @RequestParam(defaultValue = "all") String sportType) {
        return ResponseEntity.ok(stravaService.calculateWeeklyPerformance(
                getUserFromHeader(authHeader),
                stravaService.getActivities(getValidAccessTokenOrThrow(authHeader)),
                sportType));
    }

    /**
     * Retrieves the daily performance data for the authenticated user.
     * This includes statistics such as total distance and time for each day.
     * The data can be filtered by sport type.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @param sportType  The type of sport to filter the performance data (default is "all").
     * @return A response entity containing the daily performance data.
     */
    @GetMapping("/performance/daily")
    public ResponseEntity<?> getDaily(@RequestHeader("Authorization") String authHeader,
                                      @RequestParam(defaultValue = "all") String sportType) {
        return ResponseEntity.ok(stravaService.calculateDailyPerformance(
                getUserFromHeader(authHeader),
                stravaService.getActivities(getValidAccessTokenOrThrow(authHeader)),
                sportType));
    }

    /**
     * Retrieves the yearly performance data for the authenticated user.
     * This includes statistics such as total distance and time for each year.
     * The data can be filtered by sport type.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @param sportType  The type of sport to filter the performance data (default is "all").
     * @return A response entity containing the yearly performance data.
     */
    @GetMapping("/performance/yearly")
    public ResponseEntity<?> getYearly(@RequestHeader("Authorization") String authHeader,
                                       @RequestParam(defaultValue = "all") String sportType) {
        return ResponseEntity.ok(stravaService.calculateYearlyPerformance(
                getUserFromHeader(authHeader),
                stravaService.getActivities(getValidAccessTokenOrThrow(authHeader)),
                sportType));
    }

    /**
     * Retrieves the performance data for the last 6 months for the authenticated user.
     * This includes statistics such as total distance and time for each of the last six months.
     * The data can be filtered by sport type.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @param sportType  The type of sport to filter the performance data (default is "all").
     * @return A response entity containing the last 6 months' performance data.
     */
    @GetMapping("/performance/last-6-months")
    public ResponseEntity<?> getLastSixMonths(@RequestHeader("Authorization") String authHeader,
                                              @RequestParam(defaultValue = "all") String sportType) {
        return ResponseEntity.ok(stravaService.calculateLastSixMonthsPerformance(
                getUserFromHeader(authHeader),
                stravaService.getActivities(getValidAccessTokenOrThrow(authHeader)),
                sportType));
    }

    /**
     * Retrieves the distribution of sport types for the authenticated user.
     * This shows how much time or distance was spent on each type of sport (e.g., cycling, running).
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A response entity containing the sport type distribution data.
     */
    @GetMapping("/performance/sport-distribution")
    public ResponseEntity<?> getSportDistribution(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(stravaService.calculateSportTypeDistribution(
                stravaService.getActivities(getValidAccessTokenOrThrow(authHeader))));
    }

    /**
     * Retrieves the total kilometers completed by the authenticated user for a specified sport type.
     * The total kilometers can be calculated over different time periods, filtered by sport type.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @param sportType  The type of sport to filter the performance data (default is "all").
     * @return A response entity containing the total kilometers completed by the user.
     */
    @GetMapping("/total-km")
    public ResponseEntity<?> getTotalKm(@RequestHeader("Authorization") String authHeader,
                                        @RequestParam(defaultValue = "all") String sportType) {
        return ResponseEntity.ok(stravaService.calculateKm(
                stravaService.getActivities(getValidAccessTokenOrThrow(authHeader)),
                sportType));
    }

    /**
     * Retrieves the valid access token for the authenticated user, or refreshes the token if needed.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A valid Strava access token.
     * @throws StravaTokenUnavailableException If no valid access token is available.
     */
    private String getValidAccessTokenOrThrow(String authHeader) {
        String username = jwtTokenService.extractUsername(extractToken(authHeader));

        return stravaAuthService.getValidAccessToken(username)
                .map(StravaTokenResponse::getAccessToken)
                .orElseGet(() -> {
                    Optional<StravaToken> refresh = stravaAuthService.getValidRefreshToken(username);
                    if (refresh.isEmpty()) {
                        throw new StravaTokenUnavailableException("Strava token is missing or expired.");
                    }
                    return stravaAuthService
                            .refreshAccessToken(refresh.get().getRefreshToken(), username)
                            .getAccessToken();
                });
    }
}