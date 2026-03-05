package ch.supsi.sporthub.backend.repository.impl.telegram;

import ch.supsi.sporthub.backend.repository.api.ChatTypeStrategy;

/**
 * Strategy implementation for identifying Telegram group chats.
 * Telegram group chat IDs typically start with a single "-" but not with "-100" (which indicates a channel).
 */
public class GroupChatStrategy implements ChatTypeStrategy {

    /**
     * Determines whether the given chat ID belongs to a Telegram group chat.
     * It matches chat IDs that start with "-" but are not channels.
     *
     * @param chatId the chat ID to evaluate
     * @return true if the chat ID starts with "-" and is not a channel; false otherwise
     */
    @Override
    public boolean matches(String chatId) {
        return chatId.startsWith("-");
    }
}