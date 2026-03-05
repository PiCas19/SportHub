package ch.supsi.sporthub.backend.repository.jpa;

import ch.supsi.sporthub.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * Provides methods for common queries based on username and email.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user by their username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email the email to check for existence
     * @return {@code true} if a user with the email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);
}