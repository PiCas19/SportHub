package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;

/**
 * Interface representing a command that can be executed via Telegram.
 * Extends {@link ICommand} to support command execution logic.
 */
public interface ITelegramCommand extends ICommand<TelegramCommandInput, String> {

    /**
     * Returns the name of the Telegram command.
     *
     * @return the command name (e.g., "/start", "/help")
     */
    String getCommandName();

    /**
     * Determines whether the command is allowed to be used in the specified chat.
     *
     * @param chatId the ID of the chat to validate
     * @return true if the command is allowed in the chat; false otherwise
     */
    boolean isAllowedInChat(String chatId);
}