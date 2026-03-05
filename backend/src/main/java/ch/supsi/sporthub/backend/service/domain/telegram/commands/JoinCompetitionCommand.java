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
 * This class represents the command to allow a user to join a competition through Telegram.
 * It implements the {@link ITelegramCommand} interface and provides the logic to handle the joining of competitions.
 * The command expects a username and competition name as arguments and ensures the user is registered and the competition exists within the chat.
 * If successful, the user will be added to the competition.
 */
@Component
public class JoinCompetitionCommand implements ITelegramCommand {

    private final ICompetitionWriteService competitionWriteService;
    private final IUserChatService userChatService;
    private final IChatTypeFactory chatTypeFactory;

    /**
     * Constructs a {@link JoinCompetitionCommand} with the provided services.
     *
     * @param competitionWriteService service for competition-related operations.
     * @param userChatService service for handling user chat interactions.
     * @param chatTypeFactory service for checking chat types.
     */
    public JoinCompetitionCommand(ICompetitionWriteService competitionWriteService,
                                  IUserChatService userChatService,
                                  IChatTypeFactory chatTypeFactory) {
        this.competitionWriteService = competitionWriteService;
        this.userChatService = userChatService;
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Executes the command to join a competition.
     * The method checks if the user is registered, if the competition exists, and adds the user to the competition if all conditions are met.
     *
     * @param input the input containing the chat ID and arguments for the command.
     * @return a message indicating whether the user successfully joined the competition or an error message.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();
        String[] args = input.getArgs();

        if (args.length == 0) {
            return "❌ <b>Usage:</b> <code>/join_competition username=&lt;your_username&gt; competition='Competition Name'</code>";
        }

        Map<String, String> params = ArgumentParser.parseArgs(args);
        String username = params.get("username");
        String competitionName = params.get("competition");

        if (username == null || competitionName == null) {
            return "❌ Error: Missing required parameters: username and competition.";
        }

        Optional<User> userOpt = userChatService.findUserInChatByUsername(chatId, username);
        if (userOpt.isEmpty()) {
            return "❌ Error: The user " + username + " is not registered in this chat. Please register first using /start_notification.";
        }

        Optional<Competition> competitionOpt = competitionWriteService.findCompetitionByNameAndChatId(competitionName, chatId);
        if (competitionOpt.isEmpty()) {
            return "❌ Error: Competition " + competitionName + " not found in this chat.";
        }

        User user = userOpt.get();
        Competition competition = competitionOpt.get();

        try {
            competitionWriteService.joinCompetition(competition.getId(), user);
            return "✅ Success: You have joined the competition " + competition.getName() + ".";
        } catch (RuntimeException e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Returns the name of the command.
     *
     * @return the command name, "/join_competition".
     */
    @Override
    public String getCommandName() {
        return "/join_competition";
    }

    /**
     * Checks if the command is allowed to be executed in the given chat.
     * The command can only be executed in group chats.
     *
     * @param chatId the ID of the chat.
     * @return true if the command is allowed in the chat, false otherwise.
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return chatTypeFactory.isChatType(chatId, ChatType.GROUP);
    }
}