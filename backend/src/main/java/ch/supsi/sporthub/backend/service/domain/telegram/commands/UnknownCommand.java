package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.springframework.stereotype.Component;

/**
 * Command that handles unrecognized commands in the Telegram bot.
 * When a user sends an unknown command, this class provides a response indicating that the command is not recognized.
 * The message differs depending on whether the command is sent in a private chat or a group chat.
 */
@Component
public class UnknownCommand implements ITelegramCommand {

    private final IChatTypeFactory chatTypeFactory;

    /**
     * Constructs a {@link UnknownCommand} with the provided {@link IChatTypeFactory}.
     *
     * @param chatTypeFactory service for determining the chat type (private, group, or channel).
     */
    public UnknownCommand(IChatTypeFactory chatTypeFactory) {
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Executes the command to respond to unrecognized commands.
     * If the command is sent in a private chat, it returns a message informing the user of the unrecognized command.
     * If the command is sent in a group chat, it provides additional information suggesting that the user contact in private for more options.
     *
     * @param input The input containing the chat ID and command arguments.
     * @return A message informing the user about the unrecognized command and offering options.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();

        if (chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)) {
            return "❌ Unrecognized command. Use /help to see the available commands.";
        } else {
            return """
                ❌ Unrecognized command.
                ℹ️ Use /help to see the available commands.
                📩 Contact me in private chat for more options!
                """;
        }
    }

    /**
     * Returns the name of the command, which is used to identify it in the Telegram bot.
     * In this case, it is a default command used for unrecognized inputs.
     *
     * @return The command name, "default".
     */
    @Override
    public String getCommandName() {
        return "default";
    }

    /**
     * Determines whether the command can be executed in the given chat.
     * This command can be executed in any chat (private, group, or channel).
     *
     * @param chatId The ID of the chat where the command is being used.
     * @return True, as this command can be executed in any chat type.
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return true;
    }
}