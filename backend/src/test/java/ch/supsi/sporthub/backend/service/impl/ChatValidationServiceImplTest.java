package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.config.TelegramProperties;
import ch.supsi.sporthub.backend.model.Leaderboard;
import ch.supsi.sporthub.backend.repository.jpa.LeaderboardRepository;
import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatValidationServiceImplTest {

    @Mock
    private IBaseApiService baseApiService;

    @Mock
    private TelegramProperties telegramProperties;

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @InjectMocks
    private ChatValidationServiceImpl chatValidationService;

    @Test
    void verifyChatId_validChatId_returnsTrue() {
        String chatId = "12345";
        String botToken = "testBotToken";
        String baseUrl = "https://api.telegram.org";
        String endpoint = "/bot" + botToken + "/getChat?chat_id=" + chatId;
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{}", HttpStatus.OK);

        when(telegramProperties.getBotToken()).thenReturn(botToken);
        when(telegramProperties.getBaseUrl()).thenReturn(baseUrl);
        when(baseApiService.get(baseUrl, endpoint, String.class)).thenReturn(responseEntity);

        assertTrue(chatValidationService.verifyChatId(chatId));

        verify(telegramProperties).getBotToken();
        verify(telegramProperties).getBaseUrl();
        verify(baseApiService).get(baseUrl, endpoint, String.class);
    }

    @Test
    void verifyChatId_invalidChatId_returnsFalse() {
        String chatId = "invalid";
        String botToken = "testBotToken";
        String baseUrl = "https://api.telegram.org";
        String endpoint = "/bot" + botToken + "/getChat?chat_id=" + chatId;
        ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        when(telegramProperties.getBotToken()).thenReturn(botToken);
        when(telegramProperties.getBaseUrl()).thenReturn(baseUrl);
        when(baseApiService.get(baseUrl, endpoint, String.class)).thenReturn(responseEntity);

        assertFalse(chatValidationService.verifyChatId(chatId));

        verify(telegramProperties).getBotToken();
        verify(telegramProperties).getBaseUrl();
        verify(baseApiService).get(baseUrl, endpoint, String.class);
    }

    @Test
    void verifyChatId_exceptionThrown_returnsFalse() {
        String chatId = "error";
        String botToken = "testBotToken";
        String baseUrl = "https://api.telegram.org";
        String endpoint = "/bot" + botToken + "/getChat?chat_id=" + chatId;

        when(telegramProperties.getBotToken()).thenReturn(botToken);
        when(telegramProperties.getBaseUrl()).thenReturn(baseUrl);
        when(baseApiService.get(baseUrl, endpoint, String.class)).thenThrow(new RuntimeException("Test Exception"));

        assertFalse(chatValidationService.verifyChatId(chatId));

        verify(telegramProperties).getBotToken();
        verify(telegramProperties).getBaseUrl();
        verify(baseApiService).get(baseUrl, endpoint, String.class);
    }

    @Test
    void updateLeaderboards_leaderboardExistsAndUserNotPresent_addsUserAndReturnsName() {
        String groupId = "group1";
        String username = "testUser";
        String leaderboardName = "Test Leaderboard";
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setChatId(groupId);
        leaderboard.setName(leaderboardName);
        leaderboard.setScores(new HashMap<>());

        when(leaderboardRepository.findByChatId(groupId)).thenReturn(Optional.of(leaderboard));
        when(leaderboardRepository.save(leaderboard)).thenReturn(leaderboard);

        List<String> addedLeaderboards = chatValidationService.updateLeaderboards(groupId, username);

        assertEquals(1, addedLeaderboards.size());
        assertEquals(leaderboardName, addedLeaderboards.get(0));
        assertTrue(leaderboard.getScores().containsKey(username));
        assertEquals(0, leaderboard.getScores().get(username));

        verify(leaderboardRepository).findByChatId(groupId);
        verify(leaderboardRepository).save(leaderboard);
    }

    @Test
    void updateLeaderboards_leaderboardExistsAndUserAlreadyPresent_returnsEmptyList() {
        String groupId = "group1";
        String username = "existingUser";
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setChatId(groupId);
        HashMap<String, Integer> scores = new HashMap<>();
        scores.put(username, 10);
        leaderboard.setScores(scores);

        when(leaderboardRepository.findByChatId(groupId)).thenReturn(Optional.of(leaderboard));

        List<String> addedLeaderboards = chatValidationService.updateLeaderboards(groupId, username);

        assertTrue(addedLeaderboards.isEmpty());
        verify(leaderboardRepository).findByChatId(groupId);
        verify(leaderboardRepository, never()).save(any());
    }

    @Test
    void updateLeaderboards_leaderboardNotExists_returnsEmptyList() {
        String groupId = "nonExistentGroup";
        String username = "testUser";

        when(leaderboardRepository.findByChatId(groupId)).thenReturn(Optional.empty());

        List<String> addedLeaderboards = chatValidationService.updateLeaderboards(groupId, username);

        assertTrue(addedLeaderboards.isEmpty());
        verify(leaderboardRepository).findByChatId(groupId);
        verify(leaderboardRepository, never()).save(any());
    }
}