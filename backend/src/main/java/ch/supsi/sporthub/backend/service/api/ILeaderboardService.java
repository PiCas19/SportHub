package ch.supsi.sporthub.backend.service.api;
import ch.supsi.sporthub.backend.model.Leaderboard;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing leaderboards within the application.
 * Provides methods for creating, retrieving, updating, and deleting leaderboard data.
 */
public interface ILeaderboardService {

    /**
     * Creates a new leaderboard for a given chat with the specified name.
     *
     * @param chatId the identifier of the chat associated with the leaderboard
     * @param name   the name of the leaderboard
     */
    void createLeaderboard(String chatId, String name);

    /**
     * Retrieves a leaderboard by its associated chat ID.
     *
     * @param chatId the identifier of the chat
     * @return an {@link Optional} containing the leaderboard if found, or empty if not
     */
    Optional<Leaderboard> getLeaderboardById(String chatId);

    /**
     * Checks if a leaderboard with the specified name already exists.
     *
     * @param name the name of the leaderboard
     * @return true if the leaderboard exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Removes a user from the leaderboard associated with the given chat ID.
     *
     * @param chatId   the identifier of the chat
     * @param username the username to remove from the leaderboard
     */
    void removeUserFromLeaderboard(String chatId, String username);

    /**
     * Persists the given leaderboard to the data source.
     *
     * @param leaderboard the leaderboard to save
     */
    void save(Leaderboard leaderboard);

    /**
     * Retrieves all existing leaderboards.
     *
     * @return a list of all {@link Leaderboard} instances
     */
    List<Leaderboard> getAllLeaderboards();
}