package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.service.api.INotificationService;
import ch.supsi.sporthub.backend.service.api.ITelegramService;

import java.util.*;

/**
 * Utility class for handling various competition-related operations such as sending leaderboards,
 * checking chat types, and notifying users about milestone achievements during a competition.
 */
public class CompetitionUtils {

    /**
     * Sends the leaderboard to a Telegram group.
     * This method sorts the leaderboard entries by score in descending order and sends them as a formatted message.
     *
     * @param competition        The competition whose leaderboard is being sent.
     * @param leaderboardEntries The list of leaderboard entries to be sent.
     * @param telegramService    The service used to send messages to Telegram.
     */
    public static void sendLeaderboardToGroup(Competition competition, List<LeaderboardEntryResponse> leaderboardEntries, ITelegramService telegramService) {
        StringBuilder leaderboardMessage = new StringBuilder();
        leaderboardMessage.append("\uD83D\uDCCA <b>Leaderboard of the competition:</b>\n\n");

        leaderboardEntries.stream()
                .sorted((e1, e2) -> Double.compare(e2.getScore(), e1.getScore()))
                .forEach(entry -> {
                    leaderboardMessage.append("• <b>")
                            .append(entry.getSporthubUsername())
                            .append("</b> (")
                            .append(entry.getTelegramUsername() != null ? "@" + entry.getTelegramUsername() : "no telegram")
                            .append("): ")
                            .append(entry.getScore())
                            .append(" points\n");
                });

        telegramService.sendMessage(competition.getChatId(), leaderboardMessage.toString());
    }


    /**
     * Checks if a given chat ID represents a private chat.
     * In this implementation, a positive chat ID (numerical) is considered a private chat.
     *
     * @param chatId The chat ID to check.
     * @return true if the chat ID is valid and represents a private chat, false otherwise.
     */
    public static boolean isPrivateChat(String chatId) {
        try {
            long chatIdLong = Long.parseLong(chatId);
            return chatIdLong > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the user has reached any milestones in the competition and sends notifications.
     * Milestones are set at 25%, 50%, 75%, and 100% of the target value.
     *
     * @param currentProgress       The current progress towards the competition's goal.
     * @param competition           The competition in which the user is participating.
     * @param username              The username of the participant.
     * @param chatId               The chat ID where the notification should be sent.
     * @param targetValue          The target value for the competition (e.g., 100 km).
     * @param notificationService  The service used to send notifications.
     * @param userMilestoneTracker A map tracking which milestones have already been notified for each user.
     */
    public static void checkAndNotifyMilestones(
            double currentProgress,
            Competition competition,
            String username,
            String chatId,
            double targetValue,
            INotificationService notificationService,
            Map<String, Map<String, Set<Double>>> userMilestoneTracker
    ) {
        double[] milestones = {0.25, 0.5, 0.75, 1.0};
        Map<String, Set<Double>> userTracker = userMilestoneTracker.computeIfAbsent(username, k -> new HashMap<>());
        Set<Double> notifiedMilestones = userTracker.computeIfAbsent(chatId, k -> new HashSet<>());

        for (double milestone : milestones) {
            double milestoneValue = targetValue * milestone;
            if (currentProgress >= milestoneValue && !notifiedMilestones.contains(milestone)) {
                notificationService.notifyMilestoneReached(username, competition, milestone * 100);
                notifiedMilestones.add(milestone);
            }
        }
    }
}