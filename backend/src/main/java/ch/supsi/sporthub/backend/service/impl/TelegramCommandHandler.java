package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.service.api.ICommandHandler;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.service.domain.telegram.commands.CommandFactory;
import org.springframework.stereotype.Service;

import static java.lang.Integer.parseInt;

/**
 * Implementation of the ICommandHandler interface that processes incoming Telegram commands.
 * This service receives a command, validates its structure, and delegates the execution to the appropriate command handler.
 */
@Service
public class TelegramCommandHandler implements ICommandHandler {
    private final CommandFactory commandFactory;

    /**
     * Constructor for the TelegramCommandHandler class.
     * Initializes the command handler with the provided CommandFactory for retrieving specific command handlers.
     *
     * @param commandFactory The factory used to retrieve command handlers based on command names.
     */
    public TelegramCommandHandler(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Handles the incoming command by parsing the command text, validating it, and executing the corresponding command.
     * The method ensures that the command is recognized, allows the chat context, and executes the appropriate action.
     *
     * @param chatId     The unique identifier of the chat where the command was sent.
     * @param senderName The name of the sender who issued the command.
     * @param messageId  The unique identifier of the message containing the command.
     * @param commandText The full command text issued by the sender (including the command and any arguments).
     * @return A string response indicating the result of the command execution.
     */
    @Override
    public String handleCommand(String chatId, String senderName, String messageId, String commandText) {
        if (commandText == null || commandText.trim().isEmpty()) {
            return "❌ Unrecognized command.\nℹ️ Use /help to see the available commands.";
        }

        String[] parts = commandText.trim().split("\\s+", 2);
        String commandName = parts[0];
        String[] args = (parts.length > 1 && !parts[1].trim().isEmpty()) ? parts[1].split("\\s+") : new String[0];

        ITelegramCommand command = commandFactory.getCommand(commandName);
        if (command == null) {
            return "❌ Unrecognized command.\nℹ️ Use /help to see the available commands.";
        }

        if (!command.isAllowedInChat(chatId)) {
            return "⚠️ This command can be used in this chat. See the /help command";
        }

        return command.execute(new TelegramCommandInput(chatId, senderName, messageId, args));
    }

}