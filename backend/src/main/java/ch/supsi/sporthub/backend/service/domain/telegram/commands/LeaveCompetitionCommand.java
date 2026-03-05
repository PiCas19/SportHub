package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.ICompetitionWriteService;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.utils.ArgumentParser;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Command that allows a user to leave a competition in a Telegram chat.
 * It checks the validity of the user's participation in the competition and removes them from the competition.
 * This command is only available in group chats.
 */
@Component
public class LeaveCompetitionCommand implements ITelegramCommand {

    private final ICompetitionWriteService competitionWriteService;
    private final IUserChatService userChatService;
    private final IChatTypeFactory chatTypeFactory;

    /**
     * Constructs a {@link LeaveCompetitionCommand} with the provided services.
     *
     * @param competitionWriteService service for competition-related operations.
     * @param userChatService service for handling user chat interactions.
     * @param chatTypeFactory service for checking chat types.
     */
    public LeaveCompetitionCommand(ICompetitionWriteService competitionWriteService,
                                   IUserChatService userChatService,
                                   IChatTypeFactory chatTypeFactory) {
        this.competitionWriteService = competitionWriteService;
        this.userChatService = userChatService;
        this.chatTypeFactory = chatTypeFactory;
    }


    /**
     * Executes the command to allow a user to leave a competition.
     * It verifies that the user is registered in the chat, checks if the competition exists,
     * and removes the user from the competition if possible.
     *
     * @param input The input containing the chat ID and command arguments.
     * @return A message indicating whether the user successfully left the competition or an error message.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();
        String[] args = input.getArgs();

        if (args.length < 2) {
            return "❌ <b>Usage:</b> <code>/leave_competition username=&lt;your_username&gt; competition='Competition Name'</code>";
        }

        Map<String, String> params = ArgumentParser.parseArgs(args);
        String username = params.get("username");
        String competitionName = params.get("competition");

        if (username == null || competitionName == null) {
            return "❌ <b>Error:</b> Missing required parameters: <code>username</code> and <code>competition</code>.";
        }

        Optional<User> userOpt = userChatService.findUserInChatByUsername(chatId, username);
        if (userOpt.isEmpty()) {
            return "❌ <b>Error:</b> The user <code>" + username + "</code> is not registered in this chat. Please register first using <code>/start_notification</code>.";
        }

        Optional<Competition> competitionOpt = competitionWriteService.findCompetitionByNameAndChatId(competitionName, chatId);
        if (competitionOpt.isEmpty()) {
            return "❌ <b>Error:</b> Competition <code>" + competitionName + "</code> not found in this chat.";
        }

        User user = userOpt.get();
        Competition competition = competitionOpt.get();

        try {
            competitionWriteService.leaveCompetition(competition.getId(), user);
            return "✅ <b>Success:</b> You have left the competition <code>" + competition.getName() + "</code>.";
        } catch (RuntimeException e) {
            return "❌ <b>Error:</b> " + e.getMessage();
        }
    }

    /**
     * Returns the name of the command, used to identify it in the Telegram bot.
     *
     * @return The command name, "/leave_competition".
     */
    @Override
    public String getCommandName() {
        return "/leave_competition";
    }

    /**
     * Determines whether the command can be executed in the given chat.
     * This command can only be executed in group chats.
     *
     * @param chatId The ID of the chat where the command is being used.
     * @return True if the command is allowed in the chat, false otherwise.
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return chatTypeFactory.isChatType(chatId, ChatType.GROUP);
    }
}