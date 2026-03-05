package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.dto.request.SetGoalRequest;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.api.IGoalService;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.utils.ArgumentParser;
import ch.supsi.sporthub.backend.utils.CommandValidatorUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Command that allows a user to set a personal goal in a Telegram chat.
 * The goal includes parameters such as target value, goal type, period, and sport type.
 * This command is available only in private chats.
 */
@Component
public class SetGoalCommand implements ITelegramCommand {

    private final IGoalService goalService;
    private final IUserChatService userChatService;
    private final IChatTypeFactory chatTypeFactory;


    /**
     * Constructs a {@link SetGoalCommand} with the provided services.
     *
     * @param goalService service for handling goal creation.
     * @param userChatService service for handling user chat interactions.
     * @param chatTypeFactory service for checking chat types.
     */
    public SetGoalCommand(IGoalService goalService,
                          IUserChatService userChatService,
                          IChatTypeFactory chatTypeFactory) {
        this.goalService = goalService;
        this.userChatService = userChatService;
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Executes the command to set a personal goal for the user.
     * The command expects parameters for the target value, goal type, period, and sport type.
     * If all parameters are valid, the goal is created successfully.
     * If there are errors in the parameters, an appropriate error message is returned.
     *
     * @param input The input containing the chat ID and command arguments.
     * @return A message indicating whether the goal was set successfully or an error message.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();
        String[] args = input.getArgs();

        User user = userChatService.getUserFromChat(chatId);
        if (user == null) {
            return "❌ No registered users found for this chat. Please register using /start_notification.";
        }

        Map<String, String> params = ArgumentParser.parseArgs(args);

        if (!CommandValidatorUtils.hasRequiredParams(params, "target", "type", "period", "sport")) {
            return "❌ Error: missing parameters. Required: <code>target</code>, <code>type</code>, <code>period</code>, <code>sport</code>";
        }

        try {
            Double targetValue = Double.parseDouble(params.get("target"));
            GoalType goalType = GoalType.valueOf(params.get("type").toUpperCase());
            Period period = Period.valueOf(params.get("period").toUpperCase());
            SportType sportType = SportType.valueOf(params.get("sport").toUpperCase());

            SetGoalRequest request = new SetGoalRequest();
            request.setTargetValue(targetValue);
            request.setGoalType(goalType.name());
            request.setPeriod(period);
            request.setSportType(sportType.name());

            goalService.createGoal(request, user);

            return "✅ Goal set successfully: " +
                    "Target=" + targetValue +
                    ", Type=" + goalType +
                    ", Period=" + period +
                    ", Sport=" + sportType + ".";

        } catch (NumberFormatException e) {
            return "❌ Error: invalid number format for <code>target</code>.";
        } catch (IllegalArgumentException e) {
            return "❌ Error: invalid value for one of the parameters (type, period, sport).";
        }
    }

    /**
     * Returns the name of the command, which is used to identify it in the Telegram bot.
     *
     * @return The command name, "/set_goal".
     */
    @Override
    public String getCommandName() {
        return "/set_goal";
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