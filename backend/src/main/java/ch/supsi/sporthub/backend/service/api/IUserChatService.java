package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.model.Chat;
import ch.supsi.sporthub.backend.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing user-chat associations and interactions within Telegram chats.
 */
public interface IUserChatService {

    /**
     * Retrieves a list of users who are members of a specific chat.
     *
     * @param chatId the ID of the chat
     * @return list of users in the chat
     */
    List<User> getUsersInChat(String chatId);

    /**
     * Searches for a user in a specific chat by their username.
     *
     * @param chatId the ID of the chat
     * @param username the username to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findUserInChatByUsername(String chatId, String username);

    /**
     * Gets the user associated with a private chat.
     *
     * @param chatId the chat ID
     * @return the user associated with the chat
     */
    User getUserFromChat(String chatId);

    /**
     * Retrieves all chat entries that match the given chat ID.
     *
     * @param chatId the ID of the chat
     * @return a list of matching chat entries
     */
    List<Chat> getChatsByChatId(String chatId);

    /**
     * Retrieves all chat entries associated with a specific user.
     *
     * @param user the user whose chats should be retrieved
     * @return a list of chats
     */
    List<Chat> getChatsForUser(User user);

    /**
     * Persists a chat entity to the database.
     *
     * @param chat the chat to save
     */
    void save(Chat chat);

    /**
     * Finds a user by their Telegram username.
     *
     * @param username the Telegram username
     * @return an Optional containing the user if found
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Checks if the given user has a private chat with the specified chat ID.
     *
     * @param chatId the chat ID
     * @param user the user
     * @return true if a private chat exists, false otherwise
     */
    boolean hasPrivateChat(String chatId, User user);

    /**
     * Retrieves a chat entity by its chat ID.
     *
     * @param chatId the ID of the chat
     * @return an Optional containing the chat if found
     */
    Optional<Chat> getChatByChatId(String chatId);

    /**
     * Removes a private chat associated with the given chat ID.
     *
     * @param chatId the ID of the private chat to remove
     */
    void removePrivateChat(String chatId);

    /**
     * Removes a group chat from the system.
     *
     * @param groupChat the group chat to remove
     */
    void removeGroupChat(Chat groupChat);

    /**
     * Removes a group chat associated with the specified user.
     *
     * @param user the user
     * @param chatId the chat ID of the group
     * @return true if removal was successful, false otherwise
     */
    boolean removeGroupChatForUser(User user, String chatId);

    /**
     * Retrieves the Telegram username of a specific user within a given chat.
     *
     * @param chatId the ID of the chat
     * @param user the user whose Telegram username to retrieve
     * @return the Telegram username
     */
    String getTelegramUsernameForUserInChat(String chatId, User user);
}
