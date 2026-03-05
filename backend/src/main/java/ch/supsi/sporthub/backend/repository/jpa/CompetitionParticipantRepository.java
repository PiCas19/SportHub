package ch.supsi.sporthub.backend.repository.jpa;

import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.model.CompetitionParticipant;
import ch.supsi.sporthub.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link CompetitionParticipant} entities.
 * Provides methods to handle participation data in competitions.
 */
public interface CompetitionParticipantRepository extends JpaRepository<CompetitionParticipant, Long> {

    /**
     * Retrieves all participants of a given competition.
     *
     * @param competition the competition to retrieve participants for
     * @return a list of {@link CompetitionParticipant}
     */
    List<CompetitionParticipant> findByCompetition(Competition competition);

    /**
     * Checks whether a user is already a participant in a competition.
     *
     * @param competition the competition
     * @param user the user
     * @return true if the user is participating, false otherwise
     */
    boolean existsByCompetitionAndUser(Competition competition, User user);

    /**
     * Removes a specific user from a specific competition.
     *
     * @param competition the competition
     * @param user the user
     */
    void deleteByCompetitionAndUser(Competition competition, User user);

    /**
     * Deletes all competition participation records for a given user ID.
     *
     * @param id the user's ID
     */
    void deleteByUserId(Long id);

    /**
     * Checks if the user is participating in any competition.
     *
     * @param userId the user's ID
     * @return true if user has any participation record, false otherwise
     */
    boolean existsByUserId(Long userId);
}