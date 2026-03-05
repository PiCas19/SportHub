package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.model.Chat;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.repository.jpa.ChatRepository;
import ch.supsi.sporthub.backend.repository.jpa.UserRepository;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the IUserChatService interface that handles interactions with user-chat data.
 * This service provides methods for managing user chats, retrieving users in chats, and saving/removing chats.
 */
@Service
public class UserChatServiceImpl implements IUserChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    /**
     * Constructor for the UserChatServiceImpl class.
     * Initializes the service with the required repositories for accessing and managing chats and users.
     *
     * @param chatRepository   The repository for accessing and managing chat data.
     * @param userRepository   The repository for accessing and managing user data.
     */
    public UserChatServiceImpl(ChatRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a list of users that are in a specified chat based on its chat ID.
     *
     * @param chatId The ID of the chat whose users are to be retrieved.
     * @return A list of users in the specified chat.
     */
    @Override
    public List<User> getUsersInChat(String chatId) {
        return chatRepository.findByChatId(chatId).stream()
                .map(Chat::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Finds a specific user in a chat by their username.
     *
     * @param chatId   The ID of the chat to search within.
     * @param username The username of the user to find.
     * @return An optional containing the user if found, otherwise an empty optional.
     */
    @Override
    public Optional<User> findUserInChatByUsername(String chatId, String username) {
        return chatRepository.findByChatId(chatId).stream()
                .map(Chat::getUser)
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    /**
     * Retrieves the user associated with a specific chat based on the chat ID.
     *
     * @param chatId The ID of the chat whose associated user is to be retrieved.
     * @return The user associated with the chat, or null if no user is found.
     */
    @Override
    public User getUserFromChat(String chatId) {
        return chatRepository.findByChatId(chatId)
                .map(Chat::getUser)
                .orElse(null);
    }

    /**
     * Retrieves a list of chats associated with a given chat ID.
     *
     * @param chatId The ID of the chat whose associated chats are to be retrieved.
     * @return A list of chats with the specified chat ID.
     */
    @Override
    public List<Chat> getChatsByChatId(String chatId) {
        return chatRepository.findAllByChatId(chatId);
    }

    /**
     * Retrieves a list of chats associated with a given user.
     *
     * @param user The user whose associated chats are to be retrieved.
     * @return A list of chats associated with the specified user.
     */
    @Override
    public List<Chat> getChatsForUser(User user) {
        return chatRepository.findByUser(user);
    }

    /**
     * Saves a chat to the repository.
     *
     * @param chat The chat to be saved.
     */
    @Override
    public void save(Chat chat) {
        chatRepository.save(chat);
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user to be found.
     * @return An optional containing the user if found, otherwise an empty optional.
     */
    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Retrieves a chat based on its chat ID.
     *
     * @param chatId The ID of the chat to be retrieved.
     * @return An optional containing the chat if found, otherwise an empty optional.
     */
    @Override
    public Optional<Chat> getChatByChatId(String chatId) {
        return chatRepository.findByChatId(chatId);
    }

    /**
     * Removes a private chat based on its chat ID.
     *
     * @param chatId The ID of the private chat to be removed.
     */
    @Override
    public void removePrivateChat(String chatId) {
        chatRepository.findByChatId(chatId).ifPresent(chatRepository::delete);
    }

    /**
     * Removes a group chat from the repository.
     *
     * @param groupChat The group chat to be removed.
     */
    @Override
    public void removeGroupChat(Chat groupChat) {
        chatRepository.delete(groupChat);
    }

    /**
     * Removes a group chat associated with a user based on the chat ID.
     *
     * @param user    The user who wants to remove the group chat.
     * @param chatId  The ID of the group chat to be removed.
     * @return true if the chat was removed successfully, otherwise false.
     */
    @Override
    public boolean removeGroupChatForUser(User user, String chatId) {
        Optional<Chat> groupChatOpt = chatRepository.findByChatId(chatId);
        if (groupChatOpt.isPresent() && groupChatOpt.get().getUser().equals(user)) {
            chatRepository.delete(groupChatOpt.get());
            return true;
        }
        return false;
    }

    /**
     * Checks whether a user has a private chat with the specified chat ID.
     *
     * @param chatId The ID of the chat to be checked.
     * @param user   The user to check for the private chat.
     * @return true if the user has a private chat with the specified chat ID, otherwise false.
     */
    @Override
    public boolean hasPrivateChat(String chatId, User user) {
        return chatRepository.findByChatId(chatId)
                .map(chat -> chat.getUser().equals(user) && chat.getChatType() == ChatType.PRIVATE)
                .orElse(false);
    }

    /**
     * Retrieves the Telegram username of a user associated with a specific chat ID.
     *
     * @param chatId The ID of the chat to search in.
     * @param user   The user whose Telegram username is to be retrieved.
     * @return The Telegram username of the user in the chat, or null if not found.
     */
    @Override
    public String getTelegramUsernameForUserInChat(String chatId, User user) {
        return getChatsByChatId(chatId).stream()
                .filter(chat -> chat.getUser().getId().equals(user.getId()))
                .map(Chat::getTelegramUserName)
                .filter(telegramUsername -> telegramUsername != null && !telegramUsername.isEmpty())
                .findFirst()
                .orElse(null);
    }


}