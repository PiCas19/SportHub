package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;

import java.util.List;
import java.util.Map;


/**
 * Utility class for formatting responses related to leaderboard data.
 * Provides methods to format leaderboard entries into a human-readable string format.
 */
public class ResponseUtils {

    /**
     * Formats a list of leaderboard entries into a string representation, sorted by score.
     * The leaderboard entries are displayed with their rank, username, telegram handle (if available),
     * and score (rounded to an integer).
     *
     * The entries are sorted in descending order based on the score, and special emoji symbols are added
     * for the top three ranks (🥇, 🥈, 🥉). For other ranks, the rank number is displayed.
     *
     * @param entries The list of leaderboard entries to format.
     * @return A string representation of the leaderboard, formatted for display in a user-friendly way.
     */
    public static String formatLeaderboardResponse(List<LeaderboardEntryResponse> entries) {
        StringBuilder response = new StringBuilder();

        List<LeaderboardEntryResponse> sorted = entries.stream()
                .sorted((a, b) -> Double.compare(
                        b.getScore() != null ? b.getScore() : 0,
                        a.getScore() != null ? a.getScore() : 0
                ))
                .toList();

        int rank = 1;
        for (LeaderboardEntryResponse entry : sorted) {
            String username = entry.getSporthubUsername();
            String telegram = (entry.getTelegramUsername() != null) ? "(@" + entry.getTelegramUsername() + ")" : "(no telegram)";
            double score = (entry.getScore() != null) ? entry.getScore() : 0.0;

            response.append(switch (rank) {
                case 1 -> "🥇 ";
                case 2 -> "🥈 ";
                case 3 -> "🥉 ";
                default -> rank + ". ";
            });

            response.append(username).append(" ").append(telegram)
                    .append(" - ").append((int) score)
                    .append(" Points\n");

            rank++;
        }

        return response.toString();
    }

}