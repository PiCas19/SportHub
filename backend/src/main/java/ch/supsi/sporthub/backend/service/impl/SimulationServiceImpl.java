package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.request.SetGoalRequest;
import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.utils.CompetitionUtils;
import ch.supsi.sporthub.backend.utils.StravaActivityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Implementation of the ISimulationService interface that simulates competitions and goal progress for users.
 * This service handles the simulation of competition progress, including calculating milestones, updating leaderboards,
 * and sending notifications to users via Telegram.
 */
@Service
public class SimulationServiceImpl implements ISimulationService {

    private final ICompetitionService competitionService;
    private final IGoalService goalService;
    private final INotificationService notificationService;
    private final ILeaderboardService leaderboardService;
    private final ITelegramService telegramService;
    private final Map<String, Map<String, Set<Double>>> userMilestoneTracker = new HashMap<>();

    /**
     * Constructor for the SimulationServiceImpl class.
     * Initializes the service with dependencies such as competition, goal, notification, leaderboard, and Telegram services.
     *
     * @param competitionService The service for managing competitions.
     * @param goalService        The service for managing user goals.
     * @param notificationService The service for sending notifications.
     * @param leaderboardService The service for managing leaderboards.
     * @param telegramService    The service for sending Telegram messages.
     */
    public SimulationServiceImpl(ICompetitionService competitionService,
                                 IGoalService goalService,
                                 INotificationService notificationService,
                                 ILeaderboardService leaderboardService,
                                 ITelegramService telegramService) {
        this.competitionService = competitionService;
        this.goalService = goalService;
        this.notificationService = notificationService;
        this.leaderboardService = leaderboardService;
        this.telegramService = telegramService;
    }

    /**
     * Simulates a competition for a user, including calculating progress, checking milestones, updating the leaderboard,
     * and finishing the competition if the goal is reached.
     *
     * @param user    The user participating in the competition.
     * @param chatId  The chat ID for sending notifications.
     */
    @Override
    public void simulateCompetition(User user, String chatId) {
        Competition competition = createCompetition(user, chatId);
        CompetitionGoal competitionGoal = createCompetitionGoal(competition);

        if (isCompetitionActive(competition)) {
            double currentProgress = calculateProgress(competitionGoal);

            checkAndNotifyMilestones(currentProgress, competitionGoal, user.getUsername(), chatId);

            updateLeaderboardDuringSimulation(currentProgress, competitionGoal, user.getUsername(), chatId);

            if (currentProgress >= competitionGoal.getTargetValue()) {
                finishCompetition(competition, user.getUsername(), chatId, competitionGoal);
            }
        }
    }

    /**
     * Simulates the progress of goals for a user and sends notifications if the goal is reached.
     *
     * @param user   The user for whom goals are simulated.
     * @param chatId The chat ID for sending notifications.
     */
    @Override
    public void simulateGoals(User user, String chatId) {
        Goal goal = new Goal();
        goal.setGoalType(GoalType.DISTANCE);
        goal.setSportType(SportType.RUN);
        goal.setTargetValue(100.0);
        goal.setPeriod(Period.DAILY);
        goal.setUser(user);

        SetGoalRequest goalRequest = new SetGoalRequest();
        goalRequest.setGoalType(goal.getGoalType().name());
        goalRequest.setSportType(goal.getSportType().name());
        goalRequest.setTargetValue(goal.getTargetValue());
        goalRequest.setPeriod(goal.getPeriod());

        goalService.createGoal(goalRequest, user);

        List<StravaActivity> activities = StravaActivityUtils.createDemoActivities();

        double currentProgress = activities.stream()
                .filter(a -> a.getSportType().equalsIgnoreCase("run"))
                .mapToDouble(a -> a.getDistanceMeters() / 1000.0)
                .sum();

        if (currentProgress >= goal.getTargetValue()) {
            notificationService.notifyGoalReachedSimulation(user, goal, currentProgress, chatId);
            goalService.deleteGoalForUser(goal.getId(), user);
        }
    }

    /**
     * Creates a new competition for a user with the specified chat ID.
     *
     * @param user   The user participating in the competition.
     * @param chatId The chat ID for the competition.
     * @return The created competition.
     */
    private Competition createCompetition(User user, String chatId) {
        Competition competition = new Competition();
        competition.setName("Marathon Challenge");
        competition.setMaxParticipants(1);
        competition.setStarted(true);
        competition.setFinished(false);
        competition.setDeadline(LocalDate.now());
        competition.setEndDate(LocalDate.now().plusDays(7));
        competition.setChatId(chatId);

        competitionService.saveCompetition(competition);
        competitionService.joinCompetition(competition.getId(), user);
        return competition;
    }

    /**
     * Creates a competition goal with the specified target value and sport type.
     *
     * @param competition The competition to associate the goal with.
     * @return The created competition goal.
     */
    private CompetitionGoal createCompetitionGoal(Competition competition) {
        CompetitionGoal competitionGoal = new CompetitionGoal();
        competitionGoal.setGoalType(GoalType.DISTANCE);
        competitionGoal.setSportType(SportType.RUN);
        competitionGoal.setTargetValue(100.0);
        competitionGoal.setCompetition(competition);

        competitionService.saveCompetitionGoal(competitionGoal);
        return competitionGoal;
    }


    /**
     * Calculates the progress towards the competition goal based on demo activities.
     *
     * @param competitionGoal The competition goal.
     * @return The current progress towards the goal.
     */
    private double calculateProgress(CompetitionGoal competitionGoal) {
        List<StravaActivity> activities = StravaActivityUtils.createDemoActivities();
        return activities.stream()
                .filter(a -> a.getSportType().equalsIgnoreCase(competitionGoal.getSportType().name()))
                .mapToDouble(a -> a.getDistanceMeters() / 1000.0)
                .sum();
    }


    /**
     * Checks and notifies the user if any milestones are reached during the competition.
     *
     * @param currentProgress The current progress towards the goal.
     * @param competitionGoal The competition goal.
     * @param username        The username of the user.
     * @param chatId          The chat ID for sending notifications.
     */
    private void checkAndNotifyMilestones(double currentProgress, CompetitionGoal competitionGoal, String username, String chatId) {
        CompetitionUtils.checkAndNotifyMilestones(
                currentProgress,
                competitionGoal.getCompetition(),
                username,
                chatId,
                competitionGoal.getTargetValue(),
                notificationService,
                userMilestoneTracker
        );
    }

    /**
     * Updates the leaderboard during the competition simulation.
     *
     * @param currentProgress The current progress towards the competition goal.
     * @param competitionGoal The competition goal.
     * @param username        The username of the user.
     * @param chatId          The chat ID for sending notifications.
     */
    private void updateLeaderboardDuringSimulation(double currentProgress, CompetitionGoal competitionGoal, String username, String chatId) {
        if (isPrivateChat(chatId)) return;

        List<LeaderboardEntryResponse> leaderboardEntries = competitionService.getLeaderboardEntriesForCompetition(competitionGoal.getCompetition());

        leaderboardEntries.stream()
                .filter(entry -> entry.getSporthubUsername().equals(username))
                .findFirst()
                .ifPresent(entry -> {
                    double cappedProgress = Math.min(currentProgress, competitionGoal.getTargetValue());
                    entry.setScore(cappedProgress);
                    competitionService.updateLeaderboardEntryScore(competitionGoal.getCompetition(), username, cappedProgress);
                });

        competitionService.saveCompetition(competitionGoal.getCompetition());
    }

    /**
     * Marks the competition as finished, updates the leaderboard, and sends notifications.
     *
     * @param competition     The competition that is finished.
     * @param username        The username of the user.
     * @param chatId          The chat ID for sending notifications.
     * @param competitionGoal The competition goal.
     */
    private void finishCompetition(Competition competition, String username, String chatId, CompetitionGoal competitionGoal) {
        competition.setFinished(true);
        competitionService.saveCompetition(competition);

        double userScoreInCompetition = 0.0;

        List<LeaderboardEntryResponse> leaderboardEntries = competitionService.getLeaderboardEntriesForCompetition(competition);
        for (LeaderboardEntryResponse entry : leaderboardEntries) {
            if (entry.getSporthubUsername().equals(username)) {
                userScoreInCompetition = entry.getScore() != null ? entry.getScore() : 0.0;
                break;
            }
        }

        final int pointsToAdd = (int) Math.round(userScoreInCompetition);

        leaderboardService.getLeaderboardById(chatId).ifPresent(leaderboard -> {
            leaderboard.getScores().merge(
                    username,
                    pointsToAdd,
                    Integer::sum
            );
            leaderboardService.save(leaderboard);
        });


        notificationService.notifyCompetitionFinished(competition);

        sendMonthlyLeaderboard(chatId);

        leaderboardService.getLeaderboardById(chatId).ifPresent(leaderboard -> {
            leaderboard.getScores().merge(
                    username,
                    -pointsToAdd,
                    Integer::sum
            );
            leaderboardService.save(leaderboard);
        });

        competitionService.deleteCompetition(competition.getId());
    }

    /**
     * Sends the monthly leaderboard to the chat.
     *
     * @param chatId The chat ID for sending the monthly leaderboard.
     */
    private void sendMonthlyLeaderboard(String chatId) {
        leaderboardService.getLeaderboardById(chatId).ifPresent(leaderboard -> {
            if (leaderboard.getScores().isEmpty()) return;

            StringBuilder message = new StringBuilder();
            message.append("\uD83D\uDCCA <b>Monthly Leaderboard:</b>\n\n")
                    .append("\uD83C\uDFC6 <b>").append(escapeHtml(leaderboard.getName())).append("</b>\n\n");

            List<Map.Entry<String, Integer>> sorted = leaderboard.getScores().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .toList();

            int rank = 1;
            for (Map.Entry<String, Integer> entry : sorted) {
                message.append(switch (rank) {
                    case 1 -> "🥇 ";
                    case 2 -> "🥈 ";
                    case 3 -> "🥉 ";
                    default -> rank + ". ";
                });
                message.append(escapeHtml(entry.getKey()))
                        .append(" - ").append(entry.getValue()).append(" Points\n");
                rank++;
            }

            sorted.stream().findFirst().ifPresent(winner -> {
                message.append("\n\uD83C\uDFC5 <b>Winner of the month:</b> <code>")
                        .append(escapeHtml(winner.getKey()))
                        .append("</code> \uD83C\uDF89");
            });

            telegramService.sendMessage(chatId, message.toString());
        });
    }

    /**
     * Checks if the competition is still active based on the current date.
     *
     * @param competition The competition to check.
     * @return true if the competition is active, false otherwise.
     */
    private boolean isCompetitionActive(Competition competition) {
        LocalDate today = LocalDate.now();
        return !today.isAfter(competition.getEndDate());
    }

    /**
     * Checks if the chat is a private chat.
     *
     * @param chatId The chat ID to check.
     * @return true if the chat is private, false otherwise.
     */
    private boolean isPrivateChat(String chatId) {
        try {
            long chatIdLong = Long.parseLong(chatId);
            return chatIdLong > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Escapes HTML characters to prevent XSS vulnerabilities in messages.
     *
     * @param text The text to escape.
     * @return The escaped text.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}