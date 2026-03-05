package ch.supsi.sporthub.backend.repository.api;

/**
 * Strategy interface for determining whether a given chat ID matches
 * a specific chat type (e.g., private, group, or channel).
 * Implementations of this interface encapsulate the logic to detect
 * if a chat ID corresponds to a specific {@link ch.supsi.sporthub.backend.model.ChatType}.
 */
public interface ChatTypeStrategy {

    /**
     * Determines whether the given chat ID matches the specific chat type
     * handled by this strategy implementation.
     *
     * @param chatId the ID of the chat to check
     * @return true if the chat ID matches the chat type; false otherwise
     */
    boolean matches(String chatId);
}