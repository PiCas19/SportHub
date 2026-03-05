package ch.supsi.sporthub.backend.repository.jpa;

import ch.supsi.sporthub.backend.model.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for accessing and managing {@link UserImage} entities.
 * Provides methods to perform CRUD operations and custom queries on user profile images.
 */
public interface UserImageRepository extends JpaRepository<UserImage, Long> {

    /**
     * Retrieves a {@link UserImage} by the associated user's ID.
     *
     * @param userId the ID of the user whose image is to be retrieved
     * @return an {@link Optional} containing the image if found, or empty otherwise
     */
    Optional<UserImage> findByUserId(Long userId);
}