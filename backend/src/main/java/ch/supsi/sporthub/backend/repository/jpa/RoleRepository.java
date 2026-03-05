package ch.supsi.sporthub.backend.repository.jpa;

import ch.supsi.sporthub.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link Role} entities.
 * Provides method to retrieve roles by their name.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Retrieves a role entity by its name.
     *
     * @param name the name of the role
     * @return an {@link Optional} containing the role if found, or empty if not
     */
    Optional<Role> findByName(String name);
}