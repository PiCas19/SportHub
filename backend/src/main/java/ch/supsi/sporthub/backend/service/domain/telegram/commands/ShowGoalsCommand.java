package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.Goal;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.IGoalService;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Command that retrieves and displays the goals set by a user in a private Telegram chat.
 * It fetches the list of goals associated with the user and presents them in a readable format.
 * This command is only available in private chats.
 */
@Component
public class ShowGoalsCommand implements ITelegramCommand {

    private final IGoalService goalService;
    private final IChatTypeFactory chatTypeFactory;
    private final IUserChatService userChatService;

    /**
     * Constructs a {@link ShowGoalsCommand} with the provided services.
     *
     * @param goalService service for handling goal-related operations.
     * @param userChatService service for handling user chat interactions.
     * @param chatTypeFactory service for checking chat types.
     */
    public ShowGoalsCommand(IGoalService goalService,
                            IUserChatService userChatService,
                            IChatTypeFactory chatTypeFactory) {
        this.goalService = goalService;
        this.userChatService = userChatService;
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Executes the command to retrieve and display the goals set by the user.
     * The command checks if the user is registered in the chat and if they have set any goals.
     * If goals are found, it returns a list of goals with details. If no goals are set, it informs the user.
     *
     * @param input The input containing the chat ID and command arguments.
     * @return A message listing the goals or an error message if no goals are found.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();

        User user = userChatService.getUserFromChat(chatId);
        if (user == null) {
            return "❌ No registered users found for this chat. Register with /start_notification.";
        }

        List<Goal> goals = goalService.getGoalsForUser(user);
        if (goals.isEmpty()) {
            return "ℹ️ You didn't set any goals.";
        }

        StringBuilder response = new StringBuilder("🎯 <b>Objectives imposed:</b>\n\n");
        for (Goal goal : goals) {
            response.append("ID: ").append(goal.getId())
                    .append(" - Target: ").append(goal.getTargetValue().toString())
                    .append(" - Type: ").append(goal.getGoalType().name())
                    .append(" - Period: ").append(goal.getPeriod().name())
                    .append("\n");
        }

        return response.toString();
    }

    /**
     * Returns the name of the command, which is used to identify it in the Telegram bot.
     *
     * @return The command name, "/show_goals".
     */
    @Override
    public String getCommandName() {
        return "/show_goals";
    }

    /**
     * Determines whether the command can be executed in the given chat.
     * This command can only be executed in private chats.
     *
     * @param chatId The ID of the chat where the command is being used.
     * @return True if the command is allowed in the chat, false otherwise.
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return chatTypeFactory.isChatType(chatId, ChatType.PRIVATE);
    }
}