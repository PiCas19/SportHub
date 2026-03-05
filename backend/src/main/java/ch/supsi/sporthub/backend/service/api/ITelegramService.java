package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.dto.response.telegram.ChatInfoResponse;

/**
 * Interface for interacting with the Telegram Bot API.
 * Provides methods for sending messages, handling updates, retrieving chat information, and more.
 */
public interface ITelegramService {

    /**
     * Sends a message to the specified Telegram chat.
     *
     * @param chatId the ID of the chat where the message will be sent
     * @param text the text content of the message
     * @return the response or message ID from the Telegram API
     */
    String sendMessage(String chatId, String text);

    /**
     * Retrieves updates from the Telegram bot (e.g., new messages, callbacks).
     *
     * @return a raw JSON string representing the updates from Telegram
     */
    String getChatUpdates();

    /**
     * Returns the public invite link for the bot.
     *
     * @return the bot invite link (e.g., <a href="https://t.me/yourBotName">...</a>)
     */
    String getBotLink();

    /**
     * Returns the invite link for a group managed by the bot.
     *
     * @return the group invite link
     */
    String getGroupInviteLink();

    /**
     * Handles an incoming update payload from Telegram.
     *
     * @param update the raw JSON string representing the update
     * @return a response string (e.g., status or acknowledgment)
     */
    String handleUpdate(String update);

    /**
     * Retrieves detailed information about a Telegram chat.
     *
     * @param chatId the ID of the chat to query
     * @return a {@link ChatInfoResponse} containing metadata about the chat
     */
    ChatInfoResponse getChatInfo(String chatId);

    /**
     * Verifies the format or validity of a chat ID.
     *
     * @param chatId the ID to verify
     * @return true if the chat ID is valid or accessible; false otherwise
     */
    boolean verifyChatId(String chatId);

    /**
     * Retrieves the Telegram username associated with a given chat ID.
     *
     * @param chatId the chat ID from which to extract the username
     * @return the Telegram username as a String
     */
    String getTelegramUsername(String chatId);
}