package ch.supsi.sporthub.backend.repository.jpa;

import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.model.CompetitionLeaderboardEntry;
import ch.supsi.sporthub.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing and managing {@link CompetitionLeaderboardEntry} entities.
 * Provides methods to retrieve leaderboard data by competition or user.
 */
public interface CompetitionLeaderboardEntryRepository extends JpaRepository<CompetitionLeaderboardEntry, Long> {

    /**
     * Retrieves all leaderboard entries for a specific competition.
     *
     * @param competition the competition whose leaderboard entries are to be retrieved
     * @return a list of {@link CompetitionLeaderboardEntry}
     */
    List<CompetitionLeaderboardEntry> findByCompetition(Competition competition);

    /**
     * Retrieves the leaderboard entry for a specific user in a specific competition.
     *
     * @param competition the competition
     * @param user the user
     * @return an optional containing the leaderboard entry if found
     */
    Optional<CompetitionLeaderboardEntry> findByCompetitionAndUser(Competition competition, User user);

    /**
     * Deletes all leaderboard entries for a given user by user ID.
     *
     * @param userId the ID of the user
     */
    void deleteByUserId(Long userId);

    /**
     * Checks whether a leaderboard entry exists for the given user ID.
     *
     * @param userId the ID of the user
     * @return true if an entry exists, false otherwise
     */
    boolean existsByUserId(Long userId);
}