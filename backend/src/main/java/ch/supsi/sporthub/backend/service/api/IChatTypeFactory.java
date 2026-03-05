package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.model.ChatType;

/**
 * Interface for determining the type of chat based on its identifier.
 * Implementations of this interface should provide logic to check whether
 * a given chat ID matches a specific {@link ChatType}.
 */
public interface IChatTypeFactory {

    /**
     * Checks if the given chat ID corresponds to the specified {@link ChatType}.
     *
     * @param chatId   the identifier of the chat
     * @param chatType the expected type of the chat
     * @return true if the chat ID matches the given chat type, false otherwise
     */
    boolean isChatType(String chatId, ChatType chatType);
}