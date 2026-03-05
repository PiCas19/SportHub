package ch.supsi.sporthub.backend.repository.jpa;

import ch.supsi.sporthub.backend.model.Chat;
import ch.supsi.sporthub.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Chat} entities.
 * Provides methods to query chats by chat ID and associated user.
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    /**
     * Finds a chat by its chat ID.
     *
     * @param chatId the unique identifier of the chat
     * @return an optional containing the chat if found, or empty otherwise
     */
    Optional<Chat> findByChatId(String chatId);

    /**
     * Retrieves a paginated list of chats by chat ID.
     *
     * @param chatId the chat ID to filter by
     * @param pageRequest the pagination and sorting information
     * @return a page of chats matching the chat ID
     */
    Page<Chat> findByChatId(String chatId, PageRequest pageRequest);

    /**
     * Retrieves all chats associated with a specific user.
     *
     * @param user the user whose chats should be returned
     * @return a list of chats linked to the user
     */
    List<Chat> findByUser(User user);

    /**
     * Finds all chats that match the given chat ID.
     *
     * @param chatId the ID of the chat
     * @return a list of all chats matching the chat ID
     */
    List<Chat> findAllByChatId(String chatId);


    /**
     * Finds a chat by chat ID and associated user.
     *
     * @param chatId the chat ID
     * @param user the user who owns the chat
     * @return an optional containing the matching chat if found
     */
    Optional<Chat> findByChatIdAndUser(String chatId, User user);
}