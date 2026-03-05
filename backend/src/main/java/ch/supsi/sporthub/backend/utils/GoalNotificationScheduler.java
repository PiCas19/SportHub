package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Scheduler component that handles goal notifications for users based on their progress.
 * This class periodically checks users' progress for their goals and sends notifications if a goal is reached.
 * It operates on different periods (daily, weekly, monthly, quarterly, and yearly).
 */
@Component
public class GoalNotificationScheduler {

    private final IGoalService goalService;
    private final INotificationService notificationService;
    private final IStravaService stravaService;
    private final StravaAuthService stravaAuthService;
    private final ILeaderboardService leaderboardService;
    private final IUserChatService userChatService;

    /**
     * Constructor for initializing the necessary services.
     *
     * @param goalService         Service for managing goals.
     * @param notificationService Service for sending notifications.
     * @param stravaService       Service for interacting with Strava activities.
     * @param stravaAuthService   Service for handling Strava authentication and tokens.
     * @param leaderboardService  Service for managing leaderboard entries.
     * @param userChatService     Service for handling user chats.
     */
    public GoalNotificationScheduler(IGoalService goalService,
                                     INotificationService notificationService,
                                     IStravaService stravaService,
                                     StravaAuthService stravaAuthService,
                                     ILeaderboardService leaderboardService,
                                     IUserChatService userChatService) {
        this.goalService = goalService;
        this.notificationService = notificationService;
        this.stravaService = stravaService;
        this.stravaAuthService = stravaAuthService;
        this.leaderboardService = leaderboardService;
        this.userChatService = userChatService;
    }

    /**
     * Scheduled task that checks and processes daily goals at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void checkDailyGoals() {
        processGoalsForPeriod(Period.DAILY);
    }

    /**
     * Scheduled task that checks and processes weekly goals every Sunday at 11:55 PM.
     */
    @Scheduled(cron = "0 55 23 * * SUN")
    public void checkWeeklyGoals() {
        processGoalsForPeriod(Period.WEEKLY);
    }


    /**
     * Scheduled task that checks and processes monthly goals at 11:55 PM on the last day of each month.
     */
    @Scheduled(cron = "0 55 23 L * ?")
    public void checkMonthlyGoals() {
        processGoalsForPeriod(Period.MONTHLY);
    }

    /**
     * Scheduled task that checks and processes monthly goals at 11:55 PM on the last day of each month.
     */
    @Scheduled(cron = "0 55 23 * * *")
    public void checkQuarterlyGoals() {
        LocalDate today = getToday();
        if (today.equals(getLastDayOfQuarter(today))) {
            processGoalsForPeriod(Period.QUARTERLY);
        }
    }

    /**
     * Scheduled task that checks and processes quarterly goals at 11:55 PM on the last day of each quarter.
     */
    @Scheduled(cron = "0 55 23 31 12 ?")
    public void checkYearlyGoals() {
        processGoalsForPeriod(Period.YEARLY);
    }

    /**
     * Processes all goals for a specific period and checks if they have been reached.
     * If a goal is reached, a notification is sent and points are added to the leaderboard.
     *
     * @param period The period for which goals should be checked (daily, weekly, monthly, quarterly, or yearly).
     */
    private void processGoalsForPeriod(Period period) {
        List<Goal> periodGoals = goalService.getAllGoals().stream()
                .filter(goal -> goal.getPeriod() == period)
                .toList();

        for (Goal goal : periodGoals) {
            User user = goal.getUser();
            Optional<StravaTokenResponse> tokenOpt;
            try {
                tokenOpt = stravaAuthService.getValidAccessToken(user.getUsername());
            } catch (WebClientResponseException e) {
                if (e.getStatusCode().value() == 429) continue;
                throw e;
            }

            if (tokenOpt.isEmpty()) {
                Optional<StravaToken> refreshTokenOpt = stravaAuthService.getValidRefreshToken(user.getUsername());
                if (refreshTokenOpt.isEmpty()) continue;
                stravaAuthService.refreshAccessToken(refreshTokenOpt.get().getRefreshToken(), user.getUsername());
                continue;
            }

            String accessToken = tokenOpt.get().getAccessToken();
            double currentValue = getCurrentValueForGoal(goal, user, accessToken);
            if (goalService.isGoalReached(goal, currentValue)) {
                notificationService.notifyGoalReached(user, goal, currentValue);
                int points = calculatePointsForGoal(goal);

                userChatService.getChatsForUser(user).stream().findFirst()
                        .flatMap(chat -> leaderboardService.getLeaderboardById(chat.getChatId()))
                        .ifPresent(leaderboard -> {
                            leaderboard.getScores().merge(user.getUsername(), points, Integer::sum);
                            leaderboardService.save(leaderboard);
                        });
            }

            goalService.deleteGoalForUser(goal.getId(), user);
        }
    }

    /**
     * Calculates the points for a goal based on the goal type and period.
     *
     * @param goal The goal for which points are being calculated.
     * @return The calculated points.
     */
    private int calculatePointsForGoal(Goal goal) {
        int base = switch (goal.getGoalType()) {
            case DISTANCE -> 10;
            case DURATION -> 8;
            case CALORIES -> 12;
            case ACTIVITIES -> 6;
        };

        int multiplier = switch (goal.getPeriod()) {
            case WEEKLY -> 3;
            case MONTHLY -> 5;
            case QUARTERLY -> 8;
            case YEARLY -> 15;
            default -> 1;
        };

        return base * multiplier;
    }

    /**
     * Retrieves the last day of the quarter for a given date.
     *
     * @param date The date for which the last day of the quarter is to be calculated.
     * @return The last day of the quarter.
     */
    private LocalDate getLastDayOfQuarter(LocalDate date) {
        int month = ((date.getMonthValue() - 1) / 3 + 1) * 3;
        return LocalDate.of(date.getYear(), month, 1)
                .with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Retrieves the current value for a user's goal based on their Strava activities.
     *
     * @param goal         The goal to calculate progress for.
     * @param user         The user whose activities are being considered.
     * @param accessToken  The Strava access token for the user.
     * @return The current progress value for the goal.
     */

    private double getCurrentValueForGoal(Goal goal, User user, String accessToken) {
        List<StravaActivity> activities = stravaService.getActivities(accessToken).stream()
                .filter(activity -> isWithinPeriod(activity, goal.getPeriod()))
                .toList();

        return switch (goal.getGoalType()) {
            case DISTANCE -> activities.stream().mapToDouble(a -> a.getDistanceMeters() / 1000.0).sum();
            case DURATION -> activities.stream().mapToDouble(a -> a.getElapsedTimeSeconds() / 60.0).sum();
            case CALORIES -> activities.stream().mapToDouble(a -> {
                try {
                    Method method = stravaService.getClass().getDeclaredMethod("calculateCalories", StravaActivity.class, User.class);
                    method.setAccessible(true);
                    return (double) method.invoke(stravaService, a, user);
                } catch (Exception e) {
                    return 0.0;
                }
            }).sum();
            case ACTIVITIES -> activities.size();
        };
    }

    /**
     * Checks if a Strava activity is within the specified period.
     *
     * @param activity The activity to check.
     * @param period   The period to check (daily, weekly, monthly, quarterly, or yearly).
     * @return true if the activity is within the specified period, false otherwise.
     */
    private boolean isWithinPeriod(StravaActivity activity, Period period) {
        LocalDate activityDate = LocalDate.parse(activity.getStartDateLocal().substring(0, 10));
        LocalDate now = LocalDate.now();

        return switch (period) {
            case DAILY -> activityDate.equals(now);
            case WEEKLY -> activityDate.isAfter(now.minusDays(7)) && !activityDate.isAfter(now);
            case MONTHLY -> YearMonth.from(activityDate).equals(YearMonth.from(now));
            case QUARTERLY -> {
                int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
                int activityQuarter = (activityDate.getMonthValue() - 1) / 3 + 1;
                yield activityDate.getYear() == now.getYear() && currentQuarter == activityQuarter;
            }
            case YEARLY -> activityDate.getYear() == now.getYear();
        };
    }

    /**
     * Retrieves the current date.
     *
     * @return The current date.
     */
    protected LocalDate getToday() {
        return LocalDate.now();
    }
}