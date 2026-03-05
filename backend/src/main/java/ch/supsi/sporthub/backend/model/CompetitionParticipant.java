package ch.supsi.sporthub.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a user who is participating in a specific competition.
 * Each entry associates a user with a competition and stores the timestamp of when they joined.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "competition_participants")
public class CompetitionParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime joinedAt;
}