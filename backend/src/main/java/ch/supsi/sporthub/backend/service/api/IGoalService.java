package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.dto.request.SetGoalRequest;
import ch.supsi.sporthub.backend.dto.response.GoalItemResponse;
import ch.supsi.sporthub.backend.model.Goal;
import ch.supsi.sporthub.backend.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing user fitness goals.
 * Includes operations for creating, retrieving, checking, and deleting goals.
 */
public interface IGoalService {

    /**
     * Creates a new goal for a given user.
     *
     * @param goal the request object containing goal details
     * @param user the user for whom the goal is being created
     * @return the created Goal entity
     */
    Goal createGoal(SetGoalRequest goal, User user);

    /**
     * Retrieves all goals associated with the specified user.
     *
     * @param user the user whose goals are to be retrieved
     * @return a list of Goal objects
     */
    List<Goal> getGoalsForUser(User user);

    /**
     * Retrieves a specific goal by ID for the given user.
     *
     * @param id   the ID of the goal
     * @param user the user who owns the goal
     * @return an Optional containing the goal if found, or empty otherwise
     */
    Optional<Goal> getGoalForUser(Long id, User user);

    /**
     * Deletes a goal for the specified user.
     *
     * @param id   the ID of the goal to delete
     * @param user the user who owns the goal
     * @return true if the goal was deleted, false if not found or not owned by the user
     */
    boolean deleteGoalForUser(Long id, User user);

    /**
     * Checks whether the specified goal has been reached based on the current progress.
     *
     * @param goal         the goal to evaluate
     * @param currentValue the user's current value (e.g., distance, calories, etc.)
     * @return true if the goal is reached, false otherwise
     */
    boolean isGoalReached(Goal goal, double currentValue);

    /**
     * Retrieves all goals in the system (typically for admin or aggregation purposes).
     *
     * @return a list of all Goal entities
     */
    List<Goal> getAllGoals();

    /**
     * Retrieves a list of goal item responses for the user,
     * optionally enriched with external data using the access token.
     *
     * @param user        the user whose goals are requested
     * @param accessToken a token to access external services, such as Strava
     * @return a list of goal summaries tailored for display
     */
    List<GoalItemResponse> getGoalItemResponses(User user, String accessToken);
}