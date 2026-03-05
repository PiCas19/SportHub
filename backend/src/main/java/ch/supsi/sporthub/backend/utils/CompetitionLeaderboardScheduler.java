package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.*;

/**
 * This class is responsible for managing and updating the competition leaderboards on a scheduled basis.
 * It periodically updates the scores for ongoing competitions, closes expired competitions, sends monthly leaderboard summaries,
 * and cleans up old competitions.
 */
@Slf4j
@Component
public class CompetitionLeaderboardScheduler {

    private final ICompetitionService competitionService;
    private final ILeaderboardService leaderboardService;
    private final INotificationService notificationService;
    private final ITelegramService telegramService;
    private final StravaAuthService stravaAuthService;
    private final IStravaService stravaService;
    private final IUserChatService userChatService;
    private final Map<String, Map<String, Set<Double>>> userMilestoneTracker = new HashMap<>();

    /**
     * Constructor to initialize the CompetitionLeaderboardScheduler with required service dependencies.
     *
     * @param competitionService            The service for handling competition operations.
     * @param leaderboardService            The service for handling leaderboard operations.
     * @param notificationService           The service for sending notifications.
     * @param telegramService               The service for interacting with Telegram.
     * @param stravaAuthService             The service for managing Strava authorization.
     * @param stravaService                 The service for interacting with Strava data.
     * @param userChatService               The service for handling user chat information.
     */
    public CompetitionLeaderboardScheduler(ICompetitionService competitionService,
                                           ILeaderboardService leaderboardService,
                                           INotificationService notificationService,
                                           ITelegramService telegramService,
                                           StravaAuthService stravaAuthService,
                                           IStravaService stravaService,
                                           IUserChatService userChatService) {
        this.competitionService = competitionService;
        this.leaderboardService = leaderboardService;
        this.notificationService = notificationService;
        this.telegramService = telegramService;
        this.stravaAuthService = stravaAuthService;
        this.stravaService = stravaService;
        this.userChatService = userChatService;
    }

    /**
     * Scheduled task to close expired competitions at midnight every day.
     * This method updates the leaderboard scores, notifies users that the competition has ended,
     * and marks the competition as finished.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void closeExpiredCompetitions() {
        LocalDate today = LocalDate.now();

        competitionService.getAllCompetitions().stream()
                .filter(c -> !c.isFinished() && c.isStarted() && !c.getDeadline().isAfter(today))
                .forEach(competition -> {
                    updateCompetitionScoresForSingleCompetition(competition);
                    notificationService.notifyCompetitionFinished(competition);
                    leaderboardService.getLeaderboardById(competition.getChatId())
                            .ifPresent(leaderboard -> {
                                List<LeaderboardEntryResponse> leaderboardEntries = competitionService.getLeaderboardEntriesForCompetition(competition);
                                leaderboardEntries.forEach(entry -> {
                                    if (entry.getScore() != null && entry.getScore() > 0) {
                                        leaderboard.getScores().merge(entry.getSporthubUsername(), entry.getScore().intValue(), Integer::sum);
                                    }
                                });
                                leaderboardService.save(leaderboard);
                            });
                    competition.setFinished(true);
                    competitionService.saveCompetition(competition);
                });
    }


    /**
     * Scheduled task to update competition scores twice a day, at midnight and noon.
     * This method recalculates the scores for all ongoing competitions and updates the leaderboard.
     */
    @Scheduled(cron = "0 0 0,12 * * *")
    @Transactional
    public void updateCompetitionScores() {
        competitionService.getAllCompetitions().stream()
                .filter(c -> c.isStarted() && !c.isFinished())
                .forEach(this::updateCompetitionScoresForSingleCompetition);
    }

    /**
     * Updates the scores for a single competition.
     * This method calculates the total score for each participant based on their activities and updates the leaderboard.
     *
     * @param competition The competition whose scores need to be updated.
     */
    private void updateCompetitionScoresForSingleCompetition(Competition competition) {
        CompetitionGoal goal = competitionService.getGoalForCompetition(competition);
        List<LeaderboardEntryResponse> leaderboardEntries = competitionService.getLeaderboardEntriesForCompetition(competition);

        for (LeaderboardEntryResponse entry : leaderboardEntries) {
            String username = entry.getSporthubUsername();

            Optional<StravaTokenResponse> tokenOpt = getTokenForUsername(username);
            if (tokenOpt.isEmpty()) continue;

            List<StravaActivity> activities = getActivitiesForUser(tokenOpt.get().getAccessToken());
            double total = calculateTotalForGoal(activities, goal, competition.getEndDate());
            double cappedTotal = Math.min(total, goal.getTargetValue());
            entry.setScore(cappedTotal);
            competitionService.updateLeaderboardEntryScore(competition, username, cappedTotal);

            CompetitionUtils.checkAndNotifyMilestones(
                    cappedTotal, competition, username, competition.getChatId(), goal.getTargetValue(),
                    notificationService, userMilestoneTracker
            );
        }

        competitionService.saveCompetition(competition);

        if (!CompetitionUtils.isPrivateChat(competition.getChatId())) {
            CompetitionUtils.sendLeaderboardToGroup(competition, leaderboardEntries, telegramService);
        }
    }

    /**
     * Scheduled task to send a monthly leaderboard summary at 12:00 AM on the first day of every month.
     * This method formats and sends the leaderboard of the previous month to the Telegram group.
     */
    @Scheduled(cron = "0 0 0 1 * *", zone = "Europe/Zurich")
    @Transactional
    public void sendMonthlyLeaderboardSummary() {
        leaderboardService.getAllLeaderboards().forEach(leaderboard -> {
            Map<String, Integer> scores = leaderboard.getScores();
            if (scores.isEmpty()) return;

            List<LeaderboardEntryResponse> entries = scores.entrySet().stream()
                    .map(entry -> {
                        String sporthubUsername = entry.getKey();
                        double score = entry.getValue().doubleValue();
                        String telegramUsername = userChatService
                                .findUserByUsername(sporthubUsername)
                                .map(user -> userChatService.getTelegramUsernameForUserInChat(
                                        leaderboard.getChatId(), user))
                                .orElse(null);

                        return new LeaderboardEntryResponse(
                                score,
                                sporthubUsername,
                                telegramUsername
                        );
                    })
                    .toList();

            String header = "\uD83D\uDCCA <b>Monthly Leaderboard:</b>\n\n" +
                    "\uD83C\uDFC6 <b>" +
                    escapeHtml(leaderboard.getName()) +
                    "</b>\n\n";

            String body = ResponseUtils.formatLeaderboardResponse(entries);
            telegramService.sendMessage(leaderboard.getChatId(), header + body);
        });
    }

    /**
     * Scheduled task to clean up old competitions (finished and older than a month).
     * This method deletes competitions that are marked as finished and whose deadline is older than one month.
     */
    @Scheduled(cron = "0 0 3 1 * *")
    @Transactional
    public void cleanupOldCompetitions() {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);

        competitionService.getAllCompetitions().stream()
                .filter(c -> c.isFinished() && c.getDeadline().isBefore(oneMonthAgo))
                .forEach(c -> competitionService.deleteCompetition(c.getId()));
    }

    /**
     * Retrieves the Strava token for a specific user by their username.
     *
     * @param username The username of the user to retrieve the token for.
     * @return An Optional containing the Strava token if available, or an empty Optional if not.
     */
    private Optional<StravaTokenResponse> getTokenForUsername(String username) {
        try {
            return stravaAuthService.getValidAccessToken(username);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 429) return Optional.empty();
            throw e;
        }
    }

    /**
     * Retrieves the Strava activities for a specific user using their access token.
     *
     * @param accessToken The access token of the user.
     * @return A list of Strava activities for the user.
     */
    private List<StravaActivity> getActivitiesForUser(String accessToken) {
        try {
            return stravaService.getActivities(accessToken);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Calculates the total progress towards a competition goal for a list of activities.
     *
     * @param activities The list of Strava activities.
     * @param goal       The goal for the competition.
     * @param endDate    The end date of the competition.
     * @return The total progress towards the goal.
     */
    private double calculateTotalForGoal(List<StravaActivity> activities, CompetitionGoal goal, LocalDate endDate) {
        return activities.stream()
                .filter(a -> a.getSportType() != null && a.getSportType().equalsIgnoreCase(goal.getSportType().name()))
                .filter(a -> isWithinCompetitionPeriod(a, endDate))
                .mapToDouble(a -> switch (goal.getGoalType()) {
                    case DISTANCE -> a.getDistanceMeters() / 1000.0;
                    case DURATION -> a.getElapsedTimeSeconds() / 60.0;
                    case CALORIES, ACTIVITIES -> 1.0;
                })
                .sum();
    }

    /**
     * Checks if a Strava activity occurred within the competition's time period.
     *
     * @param activity The Strava activity.
     * @param endDate  The end date of the competition.
     * @return true if the activity is within the competition period, false otherwise.
     */
    private boolean isWithinCompetitionPeriod(StravaActivity activity, LocalDate endDate) {
        LocalDate activityDate = LocalDate.parse(activity.getStartDateLocal().substring(0, 10));
        return !activityDate.isAfter(endDate);
    }

    /**
     * Escapes HTML characters in a string to prevent HTML injection.
     *
     * @param text The string to escape.
     * @return The escaped string.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}