package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;

/**
 * Service interface for sending notifications to users based on various events
 * such as activity completion, goal achievements, and competition updates.
 */
public interface INotificationService {

    /**
     * Notifies the user that they have completed a Strava activity.
     *
     * @param user the user who completed the activity
     * @param activity the {@link StravaActivity} that was completed
     */
    void notifyActivityCompleted(User user, StravaActivity activity);

    /**
     * Notifies the user that they have reached a goal.
     *
     * @param user the user who reached the goal
     * @param goal the {@link Goal} that was achieved
     * @param currentValue the current value that met or exceeded the goal
     */
    void notifyGoalReached(User user, Goal goal, double currentValue);

    /**
     * Notifies the user during a simulation that they would have reached a goal.
     *
     * @param user the user being simulated
     * @param goal the simulated goal
     * @param currentProgress the simulated progress toward the goal
     * @param chatId the chat identifier for sending the notification
     */
    void notifyGoalReachedSimulation(User user, Goal goal, double currentProgress, String chatId);

    /**
     * Notifies all relevant users that a competition has ended.
     *
     * @param competition the {@link Competition} that has finished
     */
    void notifyCompetitionFinished(Competition competition);

    /**
     * Notifies users about the creation of a new competition.
     *
     * @param competition the newly created {@link Competition}
     * @param goal the goal associated with the competition
     */
    void notifyCompetitionCreated(Competition competition, CompetitionGoal goal);

    /**
     * Notifies that a milestone within a competition has been reached.
     *
     * @param username the username of the user who reached the milestone
     * @param competition the related {@link Competition}
     * @param progressPercent the percentage progress achieved (e.g., 100.0 for full completion)
     */
    void notifyMilestoneReached(String username, Competition competition, double progressPercent);

}