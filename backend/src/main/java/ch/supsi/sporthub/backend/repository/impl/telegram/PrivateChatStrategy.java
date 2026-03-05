package ch.supsi.sporthub.backend.repository.impl.telegram;

import ch.supsi.sporthub.backend.repository.api.ChatTypeStrategy;

/**
 * Strategy implementation for identifying Telegram private chats.
 * Private chat IDs in Telegram do not start with a dash ("-").
 */
public class PrivateChatStrategy implements ChatTypeStrategy {

    /**
     * Checks if the given chat ID corresponds to a private chat.
     * In Telegram, private chat IDs are numeric and do not begin with a dash.
     *
     * @param chatId the chat ID to evaluate
     * @return true if the chat ID does not start with "-", indicating a private chat
     */
    @Override
    public boolean matches(String chatId) {
        return !chatId.startsWith("-");
    }
}