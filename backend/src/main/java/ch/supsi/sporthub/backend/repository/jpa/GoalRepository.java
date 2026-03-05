package ch.supsi.sporthub.backend.repository.jpa;

import ch.supsi.sporthub.backend.model.Goal;
import ch.supsi.sporthub.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Goal} entities.
 * Provides methods to query goals by user, load goals with user data, and perform deletion and existence checks.
 */
public interface GoalRepository extends JpaRepository<Goal, Long> {

    /**
     * Finds all goals associated with a specific user.
     *
     * @param user the user whose goals are to be retrieved
     * @return a list of {@link Goal} objects
     */
    List<Goal> findByUser(User user);


    /**
     * Finds a goal by its ID and associated user.
     *
     * @param id the ID of the goal
     * @param user the user who owns the goal
     * @return an {@link Optional} containing the goal if found
     */
    Optional<Goal> findByIdAndUser(Long id, User user);

    /**
     * Retrieves all goals along with their associated users using eager loading.
     *
     * @return a list of {@link Goal} entities with fetched user associations
     */
    @Query("SELECT g FROM Goal g JOIN FETCH g.user")
    List<Goal> findAllWithUser();

    /**
     * Deletes all goals associated with a specific user ID.
     *
     * @param userId the ID of the user whose goals should be deleted
     */
    void deleteByUserId(Long userId);

    /**
     * Checks whether any goals exist for a given user ID.
     *
     * @param userId the user ID to check
     * @return true if at least one goal exists, false otherwise
     */
    boolean existsByUserId(Long userId);
}