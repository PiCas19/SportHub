package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.request.SetGoalRequest;
import ch.supsi.sporthub.backend.dto.response.GoalItemResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.repository.jpa.GoalRepository;
import ch.supsi.sporthub.backend.service.api.IGoalService;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the IGoalService interface that provides functionality for creating, retrieving,
 * updating, and deleting goals for users. This service also calculates the progress of goals based on
 * the user's activities and provides goal-related data.
 */
@Service
public class GoalServiceImpl implements IGoalService {

    private final GoalRepository goalRepository;
    private final StravaServiceImpl stravaService;

    /**
     * Constructor for the GoalServiceImpl class.
     * Initializes the service with the goal repository and Strava service for activity tracking.
     *
     * @param goalRepository The repository for managing goal data.
     * @param stravaService  The service for fetching Strava activity data.
     */
    public GoalServiceImpl(GoalRepository goalRepository, StravaServiceImpl stravaService) {
        this.goalRepository = goalRepository;
        this.stravaService = stravaService;
    }

    /**
     * Creates a new goal for the specified user based on the provided goal data.
     *
     * @param goal The request object containing the goal details.
     * @param user The user for whom the goal is being created.
     * @return The created goal.
     * @throws IllegalArgumentException If the goal type or sport type is invalid.
     */
    @Override
    public Goal createGoal(SetGoalRequest goal, User user) {
        Goal goalCreated = new Goal();
        goalCreated.setTargetValue(goal.getTargetValue());
        goalCreated.setGoalType(parseGoalType(goal.getGoalType()));
        goalCreated.setPeriod(goal.getPeriod());
        goalCreated.setSportType(parseSportType(goal.getSportType()));
        goalCreated.setUser(user);
        return goalRepository.save(goalCreated);
    }

    /**
     * Parses the goal type from a string to the corresponding GoalType enum.
     *
     * @param input The goal type as a string.
     * @return The corresponding GoalType enum.
     * @throws IllegalArgumentException If the goal type is invalid.
     */
    private GoalType parseGoalType(String input) {
        if (input == null || input.isBlank()) throw new IllegalArgumentException("GoalType not specified");
        try {
            return GoalType.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Goal Type: " + input);
        }
    }

    /**
     * Parses the sport type from a string to the corresponding SportType enum.
     *
     * @param input The sport type as a string.
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
     * Retrieves all goals stored in the repository.
     *
     * @return A list of all goals.
     */
    @Override
    public List<Goal> getAllGoals() {
        return goalRepository.findAllWithUser();
    }

    /**
     * Retrieves all goals associated with a specific user.
     *
     * @param user The user whose goals are to be retrieved.
     * @return A list of goals for the user.
     */
    @Override
    public List<Goal> getGoalsForUser(User user) {
        return goalRepository.findByUser(user);
    }

    /**
     * Retrieves a specific goal for a user based on the goal's ID.
     *
     * @param id   The ID of the goal to retrieve.
     * @param user The user associated with the goal.
     * @return An Optional containing the goal if found, or empty if not.
     */
    @Override
    public Optional<Goal> getGoalForUser(Long id, User user) {
        return goalRepository.findByIdAndUser(id, user);
    }

    /**
     * Deletes a goal for a user based on the goal's ID.
     *
     * @param id   The ID of the goal to delete.
     * @param user The user who owns the goal.
     * @return true if the goal was deleted, false if no such goal was found.
     */
    @Override
    public boolean deleteGoalForUser(Long id, User user) {
        Optional<Goal> goalOpt = goalRepository.findByIdAndUser(id, user);
        if (goalOpt.isPresent()) {
            goalRepository.delete(goalOpt.get());
            return true;
        }
        return false;
    }

    /**
     * Retrieves goal progress data for a user, including the current value, remaining value, and percentage completion.
     *
     * @param user        The user for whom goal progress is retrieved.
     * @param accessToken The access token to fetch Strava activity data.
     * @return A list of GoalItemResponse objects representing the progress of the user's goals.
     */
    @Override
    public List<GoalItemResponse> getGoalItemResponses(User user, String accessToken) {
        List<Goal> goals = goalRepository.findByUser(user);
        return goals.stream()
                .map(goal -> toItemResponse(goal, accessToken))
                .toList();
    }

    /**
     * Converts a Goal object to a GoalItemResponse, calculating progress towards the goal.
     *
     * @param goal        The goal for which progress is calculated.
     * @param accessToken The access token for retrieving activity data.
     * @return The GoalItemResponse representing the goal progress.
     */
    private GoalItemResponse toItemResponse(Goal goal, String accessToken) {
        double current = calculateCurrentProgress(goal, accessToken);
        double target = goal.getTargetValue();
        double remaining = Math.max(target - current, 0.0);

        double percentCompleted = Math.min((current / target) * 100.0, 100.0);

        GoalItemResponse dto = new GoalItemResponse();
        dto.setMessage("Goal progress retrieved successfully");
        dto.setId(goal.getId());
        dto.setName(goal.getGoalType() + " - " + goal.getSportType());
        dto.setGoalType(goal.getGoalType());
        dto.setSportType(goal.getSportType());
        dto.setCurrent(current);
        dto.setRemaining(remaining);
        dto.setTarget(target);
        dto.setUnit(getUnitForGoalType(goal.getGoalType()));
        dto.setPercentage(percentCompleted);
        dto.setPeriod(goal.getPeriod());
        return dto;
    }

    /**
     * Returns the unit of measurement for a specific goal type.
     *
     * @param type The type of the goal.
     * @return The unit of measurement (e.g., "km" for distance, "min" for duration).
     */
    private String getUnitForGoalType(GoalType type) {
        return switch (type) {
            case DISTANCE -> "km";
            case  DURATION -> "min";
            case CALORIES -> "kcal";
            case ACTIVITIES -> "sessioni";
        };
    }

    /**
     * Calculates the current progress of a goal based on the user's activities.
     *
     * @param goal        The goal for which progress is calculated.
     * @param accessToken The access token for retrieving Strava activity data.
     * @return The current progress towards the goal.
     */
    private double calculateCurrentProgress(Goal goal, String accessToken) {
        LocalDateTime fromDate = getStartDateFromPeriod(goal.getPeriod());

        List<StravaActivity> activities = stravaService.getUserActivities(
                accessToken, fromDate, goal.getSportType()
        );

        return switch (goal.getGoalType()) {
            case DISTANCE -> activities.stream().mapToDouble(a -> a.getDistanceMeters() / 1000.0).sum();
            case DURATION -> activities.stream().mapToDouble(a -> a.getMovingTimeSeconds() / 60.0).sum();
            case CALORIES -> activities.stream().mapToDouble(StravaActivity::getCalories).sum();
            case ACTIVITIES -> activities.size();
        };
    }

    /**
     * Returns the start date for a goal period.
     *
     * @param period The period for the goal (e.g., DAILY, WEEKLY).
     * @return The start date based on the goal's period.
     */
    private LocalDateTime getStartDateFromPeriod(Period period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case DAILY -> now.minusDays(1);
            case WEEKLY -> now.minusWeeks(1);
            case MONTHLY -> now.minusMonths(1);
            case QUARTERLY -> now.minusMonths(3);
            case YEARLY -> now.minusYears(1);
        };
    }

    /**
     * Checks if the specified goal has been reached based on the current value.
     *
     * @param goal        The goal to check.
     * @param currentValue The current value to compare against the goal's target.
     * @return true if the goal has been reached, false otherwise.
     */
    @Override
    public boolean isGoalReached(Goal goal, double currentValue) {
        return currentValue >= goal.getTargetValue();
    }
}