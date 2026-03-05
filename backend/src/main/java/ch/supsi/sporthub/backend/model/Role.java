package ch.supsi.sporthub.backend.model;

import jakarta.persistence.Entity;
import lombok.*;
import jakarta.persistence.*;

/**
 * Entity representing a user role within the application.
 * Roles are used to manage permissions and access control (e.g., USER, ADMIN).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}