package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of the INotificationService interface that handles sending notifications related to user activities,
 * goal achievements, competition updates, and milestone completions via Telegram.
 */
@Service
public class NotificationServiceImpl implements INotificationService {

    private final ICompetitionReadService competitionReadService;
    private final IUserChatService userChatService;
    private final ITelegramService telegramService;

    /**
     * Constructor for the NotificationServiceImpl class.
     * Initializes the service with the provided dependencies: competition reading, user chat services, and Telegram service.
     *
     * @param competitionReadService The service to read competition data.
     * @param userChatService        The service to manage user chats.
     * @param telegramService        The service for sending Telegram messages.
     */
    public NotificationServiceImpl(
            ICompetitionReadService competitionReadService,
            IUserChatService userChatService,
            ITelegramService telegramService
    ) {
        this.competitionReadService = competitionReadService;
        this.userChatService = userChatService;
        this.telegramService = telegramService;
    }

    /**
     * Sends a notification to the user about the completion of a Strava activity.
     *
     * @param user     The user who completed the activity.
     * @param activity The Strava activity the user completed.
     */
    @Override
    public void notifyActivityCompleted(User user, StravaActivity activity) {
        List<Chat> userChats = userChatService.getChatsForUser(user);
        if (userChats.isEmpty()) return;

        String message = generateActivityMessage(user, activity);
        userChats.forEach(chat -> telegramService.sendMessage(chat.getChatId(), message));
    }

    /**
     * Sends a notification to the user about achieving a goal.
     *
     * @param user        The user who reached the goal.
     * @param goal        The goal that was reached.
     * @param currentValue The current progress value towards the goal.
     */
    @Override
    public void notifyGoalReached(User user, Goal goal, double currentValue) {
        List<Chat> userChats = userChatService.getChatsForUser(user);
        if (userChats.isEmpty()) return;

        String message = generateGoalMessage(user, goal, currentValue);
        userChats.forEach(chat -> telegramService.sendMessage(chat.getChatId(), message));
    }

    /**
     * Simulates a notification when a user reaches their goal, sending it to a specific chat.
     *
     * @param user        The user who reached the goal.
     * @param goal        The goal that was reached.
     * @param currentValue The current progress value towards the goal.
     * @param chatId      The chat ID to send the notification to.
     */
    @Override
    public void notifyGoalReachedSimulation(User user, Goal goal, double currentValue, String chatId) {
        String message = generateGoalMessage(user, goal, currentValue);
        telegramService.sendMessage(chatId, message);
    }

    /**
     * Sends a notification when a new competition is created, including details such as goal, deadline, and sport type.
     *
     * @param competition The competition that was created.
     * @param goal        The goal associated with the competition.
     */
    @Override
    public void notifyCompetitionCreated(Competition competition, CompetitionGoal goal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String message = """
        📣 <b>New competition created!</b>
        
        🏁 <b>%s</b>
        🎯 Goal: %.2f %s
        🕓 End of competition: %s
        🥇 Sport type: %s
        👥 Max participants: %d
        📅 Deadline: %s
        """.formatted(
                escapeHtml(competition.getName()),
                goal.getTargetValue(),
                escapeHtml(goal.getGoalType().name()),
                escapeHtml(competition.getEndDate().format(formatter)),
                escapeHtml(goal.getSportType().name()),
                competition.getMaxParticipants(),
                escapeHtml(competition.getDeadline().format(formatter))
        );

        telegramService.sendMessage(competition.getChatId(), message);
    }

    /**
     * Sends a notification when a competition has finished, including the leaderboard and winner information.
     *
     * @param competition The competition that has ended.
     */
    @Override
    @Transactional
    public void notifyCompetitionFinished(Competition competition) {
        Competition full = competitionReadService.getCompetitionById(competition.getId());

        String title = String.format("🏁 <b>The competition has ended:</b> <code>%s</code>\n\n",
                escapeHtml(full.getName()));
        String leaderboardTitle = "📊 <b>Leaderboard</b>\n";

        List<LeaderboardEntryResponse> leaderboardEntries = competitionReadService.getLeaderboardEntriesForCompetition(full);

        String body;
        if (!leaderboardEntries.isEmpty()) {
            body = ResponseUtils.formatLeaderboardResponse(leaderboardEntries);

            LeaderboardEntryResponse winner = leaderboardEntries.stream()
                    .filter(e -> e.getScore() != null).min((e1, e2) -> Double.compare(e2.getScore(), e1.getScore()))
                    .orElse(null);

            if (winner != null) {
                body += "\n\n🏆 <b>Congratulations to the winner:</b> " +
                        escapeHtml(winner.getSporthubUsername()) +
                        (winner.getTelegramUsername() != null ? " (@" + escapeHtml(winner.getTelegramUsername()) + ")" : " (no telegram)") +
                        " 🎉";
            }
        } else {
            body = "• No activity completed during this competition.\n❗ <i>No points awarded.</i>\n";
        }

        String message = title + leaderboardTitle + body;
        telegramService.sendMessage(full.getChatId(), message);
    }

    /**
     * Sends a notification when a milestone is reached in a competition, indicating the user's progress towards the goal.
     *
     * @param username     The username of the user who reached the milestone.
     * @param competition  The competition in which the milestone was reached.
     * @param progressPercent The percentage progress the user has made toward the goal.
     */
    @Override
    public void notifyMilestoneReached(String username, Competition competition, double progressPercent) {
        Competition full = competitionReadService.getCompetitionById(competition.getId());

        List<Chat> chats = userChatService.getChatsByChatId(full.getChatId());
        if (chats.isEmpty()) return;

        String message;
        if (progressPercent >= 100) {
            message = "🎯 <b>Competition goal reached</b> by <b>%s</b> in competition <code>%s</code>! 🎉"
                    .formatted(escapeHtml(username), escapeHtml(full.getName()));
        } else {
            message = "📈 <b>%s</b> has reached <b>%.0f%%</b> of the goal in competition <code>%s</code>."
                    .formatted(escapeHtml(username), progressPercent, escapeHtml(full.getName()));
        }

        chats.stream()
                .filter(chat -> chat.getUser() != null && username.equals(chat.getUser().getUsername()))
                .forEach(chat -> telegramService.sendMessage(chat.getChatId(), message));
    }

    /**
     * Escapes HTML special characters to prevent XSS vulnerabilities.
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

    /**
     * Generates a message when a user reaches a goal, including the goal type, target value, and progress.
     *
     * @param user        The user who reached the goal.
     * @param goal        The goal that was reached.
     * @param currentValue The current value the user has achieved towards the goal.
     * @return The formatted message.
     */
    private String generateGoalMessage(User user, Goal goal, double currentValue) {
        return String.format(
                "🎯 Congratulations %s!\nYou've reached your goal of %.2f %s for the %s period.\nCurrent progress: %.2f.",
                escapeHtml(user.getUsername()),
                goal.getTargetValue(),
                goal.getGoalType().name(),
                goal.getPeriod().name(),
                currentValue
        );
    }

    private String generateActivityMessage(User user, StravaActivity activity) {
        String name = user.getUsername();
        String activityName = activity.getName() != null ? activity.getName() : "Unnamed Activity";
        String distance = String.format("%.2f", activity.getDistanceMeters() / 1000) + " km";
        String duration = activity.getElapsedTimeSeconds() / 60 + " min";
        String description = activity.getDescription() != null ? activity.getDescription() : "No description";

        return "\uD83D\uDCAA <b>" + escapeHtml(name) + "</b> has just completed an activity on Strava!\n\n"
                + "📌 <b>Name:</b> " + escapeHtml(activityName) + "\n"
                + "📏 <b>Distance:</b> " + distance + "\n"
                + "⏱ <b>Duration:</b> " + duration + "\n"
                + "📝 <b>Description:</b> " + escapeHtml(description);
    }
}