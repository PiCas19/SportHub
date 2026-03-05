package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseUtilsTest {

    @Test
    void testFormatLeaderboardResponse_EmptyList() {
        List<LeaderboardEntryResponse> entries = List.of();
        String result = ResponseUtils.formatLeaderboardResponse(entries);
        assertEquals("", result);
    }

    @Test
    void testFormatLeaderboardResponse_Top3Medals() {
        List<LeaderboardEntryResponse> entries = List.of(
                new LeaderboardEntryResponse(100.0, "Alice", "alice_tele"),
                new LeaderboardEntryResponse(90.0, "Bob", "bob_tele" ),
                new LeaderboardEntryResponse(80.0, "Charlie", "charlie_tele")
        );

        String result = ResponseUtils.formatLeaderboardResponse(entries);

        assertTrue(result.contains("🥇 Alice (@alice_tele) - 100 Points"));
        assertTrue(result.contains("🥈 Bob (@bob_tele) - 90 Points"));
        assertTrue(result.contains("🥉 Charlie (@charlie_tele) - 80 Points"));
    }

    @Test
    void testFormatLeaderboardResponse_MoreThanThree() {
        List<LeaderboardEntryResponse> entries = List.of(
                new LeaderboardEntryResponse(100.0, "A", "a_tele"),
                new LeaderboardEntryResponse(90.0, "B", "b_tele"),
                new LeaderboardEntryResponse(80.0, "C", "c_tele"),
                new LeaderboardEntryResponse(70.0, "D", "d_tele"),
                new LeaderboardEntryResponse(60.0, "E", "e_tele")
        );

        String result = ResponseUtils.formatLeaderboardResponse(entries);

        assertTrue(result.contains("🥇 A (@a_tele) - 100 Points"));
        assertTrue(result.contains("🥈 B (@b_tele) - 90 Points"));
        assertTrue(result.contains("🥉 C (@c_tele) - 80 Points"));
        assertTrue(result.contains("4. D (@d_tele) - 70 Points"));
        assertTrue(result.contains("5. E (@e_tele) - 60 Points"));
    }

    @Test
    void testFormatLeaderboardResponse_SortedDescending() {
        List<LeaderboardEntryResponse> entries = List.of(
                new LeaderboardEntryResponse(10.0, "X", "x_tele"),
                new LeaderboardEntryResponse(50.0,"Y", "y_tele"),
                new LeaderboardEntryResponse(30.0, "Z", "z_tele")
        );

        String result = ResponseUtils.formatLeaderboardResponse(entries);

        int indexY = result.indexOf("Y (@y_tele) - 50 Points");
        int indexZ = result.indexOf("Z (@z_tele) - 30 Points");
        int indexX = result.indexOf("X (@x_tele) - 10 Points");

        assertTrue(indexY < indexZ);
        assertTrue(indexZ < indexX);
    }

    @Test
    void testFormatLeaderboardResponse_TelegramNull() {
        List<LeaderboardEntryResponse> entries = List.of(
                new LeaderboardEntryResponse(75.0, "NoTelegramUser", null)
        );

        String result = ResponseUtils.formatLeaderboardResponse(entries);

        assertTrue(result.contains("NoTelegramUser (no telegram) - 75 Points"));
    }

    @Test
    void testFormatLeaderboardResponse_ScoreNull() {
        List<LeaderboardEntryResponse> entries = List.of(
                new LeaderboardEntryResponse(null, "NullScoreUser", "nullscore_tele")
        );

        String result = ResponseUtils.formatLeaderboardResponse(entries);

        assertTrue(result.contains("NullScoreUser (@nullscore_tele) - 0 Points"));
    }

    @Test
    void testFormatLeaderboardResponse_TelegramAndScoreNull() {
        List<LeaderboardEntryResponse> entries = List.of(
                new LeaderboardEntryResponse(null, "BothNullUser", null)
        );

        String result = ResponseUtils.formatLeaderboardResponse(entries);

        assertTrue(result.contains("BothNullUser (no telegram) - 0 Points"));
    }

    @Test
    void testFormatLeaderboardResponse_SortingHandlesNulls() {
        List<LeaderboardEntryResponse> entries = List.of(
                new LeaderboardEntryResponse(50.0, "UserWithScore", "tele1"),
                new LeaderboardEntryResponse(null, "UserNoScore", "tele2"),
                new LeaderboardEntryResponse(10.0, "UserLowScore", "tele3")
        );

        String result = ResponseUtils.formatLeaderboardResponse(entries);
        int indexUserWithScore = result.indexOf("UserWithScore (@tele1) - 50 Points");
        int indexUserLowScore = result.indexOf("UserLowScore (@tele3) - 10 Points");
        int indexUserNoScore = result.indexOf("UserNoScore (@tele2) - 0 Points");

        assertTrue(indexUserWithScore < indexUserLowScore);
        assertTrue(indexUserLowScore < indexUserNoScore);
    }

}
