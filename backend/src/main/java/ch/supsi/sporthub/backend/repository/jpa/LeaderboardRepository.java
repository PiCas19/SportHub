package ch.supsi.sporthub.backend.repository.jpa;
import ch.supsi.sporthub.backend.model.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for managing {@link Leaderboard} entities.
 * Provides query methods to find leaderboards by chat ID and name.
 */
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {

    /**
     * Checks whether a leaderboard exists with the given name.
     *
     * @param name the name of the leaderboard
     * @return true if a leaderboard with the name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Finds a leaderboard by the associated chat ID.
     *
     * @param chatId the identifier of the chat
     * @return an {@link Optional} containing the leaderboard if found
     */
    Optional<Leaderboard> findByChatId(String chatId);
}