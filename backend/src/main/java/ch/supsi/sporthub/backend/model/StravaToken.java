package ch.supsi.sporthub.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

/**
 * Entity representing an OAuth token issued by Strava for a specific user.
 * This entity stores access and refresh tokens along with their expiration date.
 * It is used to interact with the Strava API on behalf of the user.
 */
@Data
@Entity
@Table(name = "strava_tokens")
public class StravaToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "athlete_id", nullable = false, unique = true)
    private Long athleteId;

    @Column(name = "access_token", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}