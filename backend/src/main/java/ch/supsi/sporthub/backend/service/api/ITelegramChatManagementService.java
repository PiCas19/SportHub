package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.dto.response.auth.ChatManagementResponse;
import ch.supsi.sporthub.backend.dto.response.telegram.ChatInfoResponse;
import ch.supsi.sporthub.backend.model.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Service interface for managing Telegram chat integration for users.
 * Provides operations for retrieving, adding, and removing chat associations.
 */
public interface ITelegramChatManagementService {

    /**
     * Retrieves a list of group chat information associated with the given user.
     *
     * @param user the user whose group chats should be fetched
     * @return a list of {@link ChatInfoResponse} representing the user's group chats
     */
    List<ChatInfoResponse> getUserGroupChatIds(User user);

    /**
     * Removes a chat association for the given user based on the chat ID.
     *
     * @param user the user from whom the chat will be removed
     * @param id the ID of the chat to remove
     */
    void removeChat(User user, Long id);

    /**
     * Adds a new Telegram chat to the specified user.
     *
     * @param user the user to whom the chat will be associated
     * @param chatId the Telegram chat ID to associate
     * @return a {@link ResponseEntity} containing a {@link ChatManagementResponse} result
     */
    ResponseEntity<ChatManagementResponse> addChat(User user, String chatId);
}