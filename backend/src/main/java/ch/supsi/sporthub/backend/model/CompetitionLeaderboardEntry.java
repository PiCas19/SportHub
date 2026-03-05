package ch.supsi.sporthub.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing an entry in the leaderboard for a specific competition.
 * Each entry links a user to a competition with an associated score.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "competition_leaderboard")
public class CompetitionLeaderboardEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double score = 0.0;
}