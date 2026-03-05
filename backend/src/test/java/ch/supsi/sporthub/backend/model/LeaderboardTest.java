package ch.supsi.sporthub.backend.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderboardTest {

    @Test
    void testLeaderboardConstructorAndFields() {
        Leaderboard leaderboard = new Leaderboard("group123", "March Challenge");

        assertNull(leaderboard.getId());
        assertEquals("group123", leaderboard.getChatId());
        assertEquals("March Challenge", leaderboard.getName());
        assertNotNull(leaderboard.getScores());
        assertTrue(leaderboard.getScores().isEmpty());

        leaderboard.getScores().put("user1", 100);
        leaderboard.getScores().put("user2", 150);

        Map<String, Integer> scores = leaderboard.getScores();
        assertEquals(2, scores.size());
        assertEquals(100, scores.get("user1"));
        assertEquals(150, scores.get("user2"));
    }

    @Test
    void testLeaderboardSetters() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setId(1L);
        leaderboard.setChatId("testChat");
        leaderboard.setName("Leaderboard Test");

        assertEquals(1L, leaderboard.getId());
        assertEquals("testChat", leaderboard.getChatId());
        assertEquals("Leaderboard Test", leaderboard.getName());
    }
}
