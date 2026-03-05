package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.Leaderboard;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.ILeaderboardService;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.utils.ResponseUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Command that retrieves and displays the leaderboard for a group or channel in Telegram.
 * The leaderboard includes the scores of all users in the group who have points.
 * This command is available for both group chats and channels.
 */
@Component
public class ShowLeaderboardCommand implements ITelegramCommand {

    private final ILeaderboardService leaderboardService;
    private final IUserChatService userChatService;
    private final IChatTypeFactory chatTypeFactory;

    /**
     * Constructs a {@link ShowLeaderboardCommand} with the provided services.
     *
     * @param leaderboardService service for handling leaderboard-related operations.
     * @param userChatService service for handling user chat interactions.
     * @param chatTypeFactory service for checking chat types.
     */
    public ShowLeaderboardCommand(ILeaderboardService leaderboardService,
                                  IUserChatService userChatService,
                                  IChatTypeFactory chatTypeFactory) {
        this.leaderboardService = leaderboardService;
        this.userChatService = userChatService;
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Executes the command to retrieve and display the leaderboard for the group or channel.
     * The leaderboard shows users and their scores. If no leaderboard or scores are found,
     * an appropriate message is returned.
     *
     * @param input The input containing the chat ID and command arguments.
     * @return A message with the leaderboard or an error message if no leaderboard or scores exist.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();

        List<User> users = userChatService.getUsersInChat(chatId);
        if (users.isEmpty()) {
            return "ℹ️ No users are registered in this group.";
        }

        Optional<Leaderboard> leaderboardOpt = leaderboardService.getLeaderboardById(chatId);
        if (leaderboardOpt.isEmpty()) {
            return "❌ No leaderboard found for this group.";
        }

        Leaderboard leaderboard = leaderboardOpt.get();
        Map<String, Integer> scores = leaderboard.getScores();
        if (scores == null || scores.isEmpty()) {
            return "ℹ️ No users have points yet.";
        }

        List<LeaderboardEntryResponse> leaderboardEntries = new ArrayList<>();
        for (User user : users) {
            if (scores.containsKey(user.getUsername())) {
                String telegramUsername = userChatService.getTelegramUsernameForUserInChat(chatId, user);
                leaderboardEntries.add(new LeaderboardEntryResponse(
                        scores.get(user.getUsername()).doubleValue(),
                        user.getUsername(),
                        telegramUsername
                ));
            }
        }

        if (leaderboardEntries.isEmpty()) {
            return "ℹ️ No users with points in this group.";
        }

        return "🏆 <b>Leaderboard: " +
                escapeHtml(leaderboard.getName()) +
                "</b>\n\n" +
                ResponseUtils.formatLeaderboardResponse(leaderboardEntries);
    }

    /**
     * Returns the name of the command, which is used to identify it in the Telegram bot.
     *
     * @return The command name, "/show_leaderboard".
     */
    @Override
    public String getCommandName() {
        return "/show_leaderboard";
    }

    /**
     * Determines whether the command can be executed in the given chat.
     * This command can be executed in group chats or channels.
     *
     * @param chatId The ID of the chat where the command is being used.
     * @return True if the command is allowed in the chat, false otherwise.
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return chatTypeFactory.isChatType(chatId, ChatType.GROUP) ||
                chatTypeFactory.isChatType(chatId, ChatType.CHANNEL);
    }


    /**
     * Escapes HTML characters in a given text to prevent HTML injection and ensure proper formatting.
     *
     * @param text The text to be escaped.
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