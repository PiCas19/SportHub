package ch.supsi.sporthub.backend.repository.jpa;

import ch.supsi.sporthub.backend.model.StravaToken;
import ch.supsi.sporthub.backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * Repository interface for managing {@link StravaToken} entities.
 * Provides methods to retrieve and delete tokens based on user or athlete ID.
 */
public interface StravaTokenRepository extends JpaRepository<StravaToken, Long> {

    /**
     * Finds a Strava token by the associated user.
     *
     * @param user the user whose token is to be retrieved
     * @return an {@link Optional} containing the token if found
     */
    Optional<StravaToken> findByUser(User user);

    /**
     * Finds a Strava token by the associated athlete ID.
     *
     * @param athleteId the athlete ID linked to the token
     * @return an {@link Optional} containing the token if found
     */
    Optional<StravaToken> findByAthleteId(Long athleteId);

    /**
     * Deletes the Strava token associated with the given user.
     *
     * @param user the user whose token is to be deleted
     */
    @Transactional
    void deleteByUser(User user);
}