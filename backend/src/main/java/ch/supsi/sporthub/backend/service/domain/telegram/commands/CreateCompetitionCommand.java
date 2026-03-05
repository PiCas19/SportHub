package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.dto.request.CreateCompetitionRequest;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.utils.ArgumentParser;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

/**
 * Command for creating a new competition in a specific group chat.
 * This command parses the input, validates required parameters, and creates a competition based on the provided details.
 */
@Component
public class CreateCompetitionCommand implements ITelegramCommand {

    private final ICompetitionWriteService competitionWriteService;
    private final IChatTypeFactory chatTypeFactory;

    private static final Set<String> ALLOWED_KEYS = Set.of("name", "target", "goaltype", "enddate", "max", "deadline", "sporttype");

    /**
     * Constructs a new CreateCompetitionCommand with the given chat type factory and competition write service.
     *
     * @param chatTypeFactory The service responsible for determining chat types.
     * @param competitionWriteService The service used to create and manage competitions.
     */
    public CreateCompetitionCommand(IChatTypeFactory chatTypeFactory, ICompetitionWriteService competitionWriteService) {
        this.chatTypeFactory = chatTypeFactory;
        this.competitionWriteService = competitionWriteService;
    }

    /**
     * Executes the command to create a new competition based on the input parameters.
     * It validates the input, checks for missing or invalid parameters, and creates a competition.
     *
     * @param input The input containing the command arguments and chat ID.
     * @return A string response indicating the success or failure of the command.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String[] args = input.getArgs();

        if (args.length == 0) {
            return getUsageMessage();
        }

        Map<String, String> params = ArgumentParser.parseArgs(args);

        List<String> missingParams = new ArrayList<>();
        for (String required : ALLOWED_KEYS) {
            if (!params.containsKey(required)) {
                missingParams.add(required);
            }
        }

        if (!missingParams.isEmpty()) {
            return "❌ <b>Error:</b> Missing required parameters: <code>"
                    + String.join(", ", missingParams) + "</code>\n\n"
                    + getUsageMessage();
        }

        // Check for duplicate competition name
        String competitionName = params.get("name");
        if (competitionWriteService.existsCompetitionByNameAndChatId(competitionName, input.getChatId())) {
            return "❌ <b>Error:</b> Competition <code>" + competitionName
                    + "</code> already exists in this chat.";
        }

        try {
            CreateCompetitionRequest req = new CreateCompetitionRequest();
            req.setChatId(input.getChatId());
            req.setName(competitionName);
            req.setTargetValue(Double.parseDouble(params.get("target")));
            req.setGoalType(params.get("goaltype"));
            req.setSportType(params.get("sporttype"));
            req.setMaxParticipants(Integer.parseInt(params.get("max")));
            req.setDeadline(LocalDate.parse(params.get("deadline")));
            req.setEndDate(LocalDate.parse(params.get("enddate")));

            competitionWriteService.createCompetition(req, null);

            return "✅ <b>Competition created successfully:</b> <code>" + req.getName() + "</code>";
        } catch (IllegalArgumentException e) {
            return "❌ <b>Error:</b> Invalid parameter format. " + e.getMessage();
        } catch (Exception e) {
            return "❌ <b>Unexpected error:</b> " + e.getMessage();
        }
    }

    /**
     * Returns the usage message for the command.
     * This message provides the correct syntax for using the command.
     *
     * @return The usage message.
     */
    private String getUsageMessage() {
        return "❌️ <b>Usage:</b> <code>/create_competition name='Name' target=100 goalType=DISTANCE endDate=YYYY-MM-DD sportType=RUN max=50 deadline=YYYY-MM-DD</code>";
    }

    /**
     * Returns the name of the command, which is used for identifying it in Telegram.
     *
     * @return The command name, "/create_competition".
     */
    @Override
    public String getCommandName() {
        return "/create_competition";
    }

    /**
     * Determines if the command is allowed to be executed in the given chat.
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