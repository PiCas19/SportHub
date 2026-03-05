package ch.supsi.sporthub.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Response class representing a single entry in a leaderboard.
 * <p>
 * Each entry includes the user's score and identifying usernames across platforms.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class LeaderboardEntryResponse extends Response {
    private Double score;
    private String sporthubUsername;
    private String telegramUsername;
}