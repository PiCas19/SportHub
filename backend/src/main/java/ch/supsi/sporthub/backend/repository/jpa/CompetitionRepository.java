package ch.supsi.sporthub.backend.repository.jpa;

import ch.supsi.sporthub.backend.model.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


/**
 * Repository interface for accessing {@link Competition} entities.
 * Provides custom queries for filtering competitions by chat IDs and unique name/chatId pairs.
 */
public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    /**
     * Retrieves all distinct competitions associated with the provided list of chat IDs.
     *
     * @param chatIds a list of chat IDs to filter by
     * @return a list of {@link Competition} entities
     */
    @Query("SELECT DISTINCT c FROM Competition c WHERE c.chatId IN :chatIds")
    List<Competition> findByChatIdIn(@Param("chatIds") List<String> chatIds);

    /**
     * Finds a competition by its name and associated chat ID.
     *
     * @param name the name of the competition
     * @param chatId the associated chat ID
     * @return an {@link Optional} containing the matching competition, if found
     */
    Optional<Competition> findByNameAndChatId(String name, String chatId);

    /**
     * Checks whether a competition with the given name and chat ID exists.
     *
     * @param name the name of the competition
     * @param chatId the associated chat ID
     * @return true if a matching competition exists, false otherwise
     */
    boolean existsByNameAndChatId(String name, String chatId);
}