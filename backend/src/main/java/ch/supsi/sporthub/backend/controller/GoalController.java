package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.SetGoalRequest;
import ch.supsi.sporthub.backend.dto.response.GoalItemResponse;
import ch.supsi.sporthub.backend.dto.response.Response;
import ch.supsi.sporthub.backend.exception.GoalNotFoundException;
import ch.supsi.sporthub.backend.exception.StravaTokenUnavailableException;
import ch.supsi.sporthub.backend.model.Goal;
import ch.supsi.sporthub.backend.model.StravaToken;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.UserService;
import ch.supsi.sporthub.backend.service.api.IGoalService;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


/**
 * GoalController provides API endpoints to manage user goals, including creating, retrieving, and deleting goals.
 * It interacts with the GoalService to handle goal-related operations and the StravaAuthService to manage Strava tokens.
 */
@RestController
@RequestMapping("/api/goals")
public class GoalController extends BaseController {

    private final IGoalService goalService;
    private final StravaAuthService stravaAuthService;

    /**
     * Constructs an instance of GoalController with the specified services.
     *
     * @param goalService      The service for handling goal-related operations.
     * @param userService      The service for managing user-related operations.
     * @param jwtTokenService  The service for generating and validating JWT tokens.
     * @param stravaAuthService The service for managing Strava authentication tokens.
     */
    public GoalController(IGoalService goalService,
                          UserService userService,
                          JwtTokenService jwtTokenService,
                          StravaAuthService stravaAuthService) {
        super(jwtTokenService, userService);
        this.goalService = goalService;
        this.stravaAuthService = stravaAuthService;
    }

    /**
     * Creates a new goal for the authenticated user.
     * This method requires a valid authorization token and goal details in the request body.
     *
     * @param setGoalRequest The request body containing the goal details.
     * @param authHeader     The authorization header containing the user's JWT token.
     * @return A response entity containing a success message and the ID of the created goal.
     */
    @PostMapping
    public ResponseEntity<Response> createGoal(@RequestBody SetGoalRequest setGoalRequest,
                                               @RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        Goal createdGoal = goalService.createGoal(setGoalRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response("Goal created " + createdGoal.getId()));
    }

    /**
     * Retrieves all goals for the authenticated user.
     * This method requires the user to be authenticated and retrieves goals based on the user's access token.
     *
     * @param authHeader The authorization header containing the user's JWT token.
     * @return A response entity containing a list of the user's goals.
     */
    @GetMapping
    public ResponseEntity<List<GoalItemResponse>> getGoals(@RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        String accessToken = getValidAccessTokenOrThrow(user.getUsername());
        List<GoalItemResponse> goals = goalService.getGoalItemResponses(user, accessToken);
        return ResponseEntity.ok(goals);
    }

    /**
     * Deletes a goal for the authenticated user.
     * This method requires the user to be authenticated and provides the ID of the goal to be deleted.
     *
     * @param id         The ID of the goal to delete.
     * @param authHeader The authorization header containing the user's JWT token.
     * @return A response entity confirming the deletion of the goal.
     * @throws GoalNotFoundException If the goal with the specified ID is not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteGoal(@PathVariable Long id,
                                               @RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        boolean deleted = goalService.deleteGoalForUser(id, user);
        if (!deleted) {
            throw new GoalNotFoundException("Goal not found for id: " + id);
        }
        return ResponseEntity.ok(new Response("Goal deleted successfully."));
    }

    /**
     * Retrieves a valid access token for the specified user, or attempts to refresh the Strava access token if needed.
     * If no valid access token or refresh token is found, it throws a StravaTokenUnavailableException.
     *
     * @param username The username of the user whose Strava token is being requested.
     * @return A valid access token for the user.
     * @throws StravaTokenUnavailableException If no valid Strava token is available for the user.
     */
    private String getValidAccessTokenOrThrow(String username) {
        return stravaAuthService.getValidAccessToken(username)
                .map(StravaTokenResponse::getAccessToken)
                .orElseGet(() -> {
                    Optional<StravaToken> refresh = stravaAuthService.getValidRefreshToken(username);
                    if (refresh.isEmpty()) {
                        throw new StravaTokenUnavailableException("Strava token not available");
                    }
                    return stravaAuthService
                            .refreshAccessToken(refresh.get().getRefreshToken(), username)
                            .getAccessToken();
                });
    }
}