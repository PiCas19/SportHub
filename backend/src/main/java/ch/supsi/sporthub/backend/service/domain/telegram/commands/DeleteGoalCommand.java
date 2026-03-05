package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.IGoalService;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.springframework.stereotype.Component;

/**
 * Command for deleting a user's goal by goal ID.
 * This command verifies that the user is registered in the chat, checks if the provided goal ID is valid,
 * and then deletes the specified goal for the user.
 */
@Component
public class DeleteGoalCommand implements ITelegramCommand {

    private final IGoalService goalService;
    private final IChatTypeFactory chatTypeFactory;
    private final IUserChatService userChatService;

    /**
     * Constructs a new DeleteGoalCommand with the provided dependencies.
     *
     * @param goalService The service responsible for managing goals.
     * @param userChatService The service responsible for retrieving user data from the chat.
     * @param chatTypeFactory The service that determines the type of chat (private or group).
     */
    public DeleteGoalCommand(IGoalService goalService,
                             IUserChatService userChatService,
                             IChatTypeFactory chatTypeFactory) {
        this.goalService = goalService;
        this.userChatService = userChatService;
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Executes the command to delete a goal for a user.
     * It checks if the user is registered, validates the goal ID, and deletes the goal if found.
     *
     * @param input The input containing the command arguments and chat ID.
     * @return A message indicating the success or failure of the goal deletion.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();
        String[] args = input.getArgs();

        if (args.length < 1) {
            return "❌ Error: please specify the goal ID to delete. Example: /delete_goal id=1";
        }

        User user = userChatService.getUserFromChat(chatId);
        if (user == null) {
            return "❌ No registered user found for this chat. Register with /start_notification.";
        }

        Long goalId = parseGoalId(args);
        if (goalId == null) {
            return "❌ Error: invalid goal ID format. Example: /delete_goal id=1";
        }

        boolean deleted = goalService.deleteGoalForUser(goalId, user);
        if (deleted) {
            return "✅ Goal with ID " + goalId + " deleted successfully.";
        } else {
            return "❌ No goal found with ID " + goalId + " for the user.";
        }
    }

    /**
     * Parses the goal ID from the command arguments.
     * It expects the format "id=<goalId>" in the arguments.
     *
     * @param args The command arguments.
     * @return The parsed goal ID, or null if the format is invalid.
     */
    private Long parseGoalId(String[] args) {
        for (String token : args) {
            if (token.contains("=")) {
                String[] parts = token.split("=", 2);
                String key = parts[0].trim().toLowerCase();
                String value = parts[1].trim();
                if ("id".equals(key)) {
                    try {
                        return Long.parseLong(value);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Returns the name of the command, which is used to identify it in the Telegram bot.
     *
     * @return The command name, "/delete_goal".
     */
    @Override
    public String getCommandName() {
        return "/delete_goal";
    }


    /**
     * Determines whether the command can be executed in the given chat.
     * This command can only be executed in private chats.
     *
     * @param chatId The ID of the chat where the command is being executed.
     * @return True if the command is allowed in a private chat, false otherwise.
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return chatTypeFactory.isChatType(chatId, ChatType.PRIVATE);
    }
}