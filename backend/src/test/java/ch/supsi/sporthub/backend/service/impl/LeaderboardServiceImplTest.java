package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.model.Leaderboard;
import ch.supsi.sporthub.backend.repository.jpa.LeaderboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LeaderboardServiceImplTest {

    private LeaderboardRepository leaderboardRepository;
    private LeaderboardServiceImpl leaderboardService;
    private String chatId = "testChat";
    private String leaderboardName = "Test Leaderboard";
    private String usernameToRemove = "userToRemove";
    private Leaderboard leaderboard;

    @BeforeEach
    void setUp() {
        leaderboardRepository = mock(LeaderboardRepository.class);
        leaderboardService = new LeaderboardServiceImpl(leaderboardRepository);
        leaderboard = new Leaderboard(chatId, leaderboardName);
        leaderboard.getScores().put("user1", 10);
        leaderboard.getScores().put(usernameToRemove, 5);
    }

    @Test
    void testCreateLeaderboard() {
        leaderboardService.createLeaderboard("group123", "March Challenge");
        verify(leaderboardRepository, times(1)).save(new Leaderboard("group123", "March Challenge"));
    }

    @Test
    void testGetLeaderboardById_exists() {
        when(leaderboardRepository.findByChatId("group123")).thenReturn(Optional.of(new Leaderboard("group123", "Test Leaderboard")));

        Optional<Leaderboard> result = leaderboardService.getLeaderboardById("group123");
        assertTrue(result.isPresent());
        assertEquals("Test Leaderboard", result.get().getName());
        verify(leaderboardRepository).findByChatId("group123");
    }

    @Test
    void testGetLeaderboardById_notExists() {
        when(leaderboardRepository.findByChatId("nonExistentId")).thenReturn(Optional.empty());
        Optional<Leaderboard> result = leaderboardService.getLeaderboardById("nonExistentId");
        assertFalse(result.isPresent());
        verify(leaderboardRepository).findByChatId("nonExistentId");
    }

    @Test
    void testExistsByName_true() {
        when(leaderboardRepository.existsByName("Monthly Leaderboard")).thenReturn(true);
        assertTrue(leaderboardService.existsByName("Monthly Leaderboard"));
        verify(leaderboardRepository).existsByName("Monthly Leaderboard");
    }

    @Test
    void testExistsByName_false() {
        when(leaderboardRepository.existsByName("NonExistent Leaderboard")).thenReturn(false);
        assertFalse(leaderboardService.existsByName("NonExistent Leaderboard"));
        verify(leaderboardRepository).existsByName("NonExistent Leaderboard");
    }

    @Test
    void testSave() {
        Leaderboard leaderboardToSave = new Leaderboard("anotherChat", "Another Leaderboard");
        leaderboardService.save(leaderboardToSave);
        verify(leaderboardRepository).save(leaderboardToSave);
    }

    @Test
    void testRemoveUserFromLeaderboard_userExists() {
        when(leaderboardRepository.findByChatId(chatId)).thenReturn(Optional.of(leaderboard));
        leaderboardService.removeUserFromLeaderboard(chatId, usernameToRemove);
        verify(leaderboardRepository).findByChatId(chatId);
        verify(leaderboardRepository).save(leaderboard);
        assertFalse(leaderboard.getScores().containsKey(usernameToRemove));
    }

    @Test
    void testRemoveUserFromLeaderboard_userNotExists() {
        when(leaderboardRepository.findByChatId(chatId)).thenReturn(Optional.of(leaderboard));
        leaderboardService.removeUserFromLeaderboard(chatId, "nonExistentUser");
        verify(leaderboardRepository).findByChatId(chatId);
        verify(leaderboardRepository).save(leaderboard);
        assertTrue(leaderboard.getScores().containsKey("user1"));
        assertTrue(leaderboard.getScores().containsKey(usernameToRemove));
    }

    @Test
    void testRemoveUserFromLeaderboard_leaderboardNotExists() {
        when(leaderboardRepository.findByChatId(chatId)).thenReturn(Optional.empty());
        leaderboardService.removeUserFromLeaderboard(chatId, usernameToRemove);
        verify(leaderboardRepository).findByChatId(chatId);
        verify(leaderboardRepository, never()).save(any());
    }

    @Test
    void testGetAllLeaderboards() {
        Leaderboard leaderboard2 = new Leaderboard("anotherChat", "Another Leaderboard");
        List<Leaderboard> allLeaderboards = Arrays.asList(leaderboard, leaderboard2);
        when(leaderboardRepository.findAll()).thenReturn(allLeaderboards);
        List<Leaderboard> result = leaderboardService.getAllLeaderboards();
        assertEquals(2, result.size());
        assertTrue(result.contains(leaderboard));
        assertTrue(result.contains(leaderboard2));
        verify(leaderboardRepository).findAll();
    }
}