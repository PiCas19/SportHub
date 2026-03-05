package ch.supsi.sporthub.backend.repository.impl.telegram;

import ch.supsi.sporthub.backend.repository.api.ChatTypeStrategy;

/**
 * Strategy implementation for identifying Telegram channel chats.
 * Telegram channel chat IDs typically start with the prefix "-100".
 */
public class ChannelChatStrategy implements ChatTypeStrategy {

    /**
     * Determines whether the given chat ID belongs to a Telegram channel.
     *
     * @param chatId the chat ID to evaluate
     * @return true if the chat ID starts with "-100", indicating a channel; false otherwise
     */
    @Override
    public boolean matches(String chatId) {
        return chatId.startsWith("-100");
    }
}