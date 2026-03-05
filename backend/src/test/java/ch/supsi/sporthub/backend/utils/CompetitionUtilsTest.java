package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.service.api.INotificationService;
import ch.supsi.sporthub.backend.service.api.ITelegramService;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CompetitionUtilsTest {

    @Test
    void testIsPrivateChat_withValidPositiveChatId() {
        assertTrue(CompetitionUtils.isPrivateChat("123456"), "Chat ID positivo deve essere una chat privata");
    }

    @Test
    void testIsPrivateChat_withNegativeChatId() {
        assertFalse(CompetitionUtils.isPrivateChat("-123456"), "Chat ID negativo non deve essere una chat privata");
    }

    @Test
    void testIsPrivateChat_withNonNumericChatId() {
        assertFalse(CompetitionUtils.isPrivateChat("invalidChatId"), "Chat ID non numerico non deve essere una chat privata");
    }

    @Test
    void testSendLeaderboardToGroup() {
        ITelegramService telegramService = mock(ITelegramService.class);
        Competition competition = mock(Competition.class);
        when(competition.getChatId()).thenReturn("123456789");

        List<LeaderboardEntryResponse> leaderboardEntries = List.of(
                new LeaderboardEntryResponse(50.0, "user1", "tele_user1"),
                new LeaderboardEntryResponse(75.0, "user2", "tele_user2"),
                new LeaderboardEntryResponse(30.0, "user3", null)
        );

        CompetitionUtils.sendLeaderboardToGroup(competition, leaderboardEntries, telegramService);

        String expectedMessage = "\uD83D\uDCCA <b>Leaderboard of the competition:</b>\n\n" +
                "• <b>user2</b> (@tele_user2): 75.0 points\n" +
                "• <b>user1</b> (@tele_user1): 50.0 points\n" +
                "• <b>user3</b> (no telegram): 30.0 points\n";

        verify(telegramService).sendMessage(eq("123456789"), eq(expectedMessage));
    }

    @Test
    void testCheckAndNotifyMilestones_FirstMilestone() {
        INotificationService notificationService = mock(INotificationService.class);
        Competition competition = mock(Competition.class);

        String username = "test-user";
        String chatId = "123456";
        double targetValue = 100.0;
        double currentProgress = 25.0;

        Map<String, Map<String, Set<Double>>> userMilestoneTracker = new HashMap<>();

        CompetitionUtils.checkAndNotifyMilestones(currentProgress, competition, username, chatId, targetValue, notificationService, userMilestoneTracker);

        verify(notificationService).notifyMilestoneReached(username, competition, 25.0);
    }

    @Test
    void testCheckAndNotifyMilestones_MilestoneAlreadyNotified() {
        INotificationService notificationService = mock(INotificationService.class);
        Competition competition = mock(Competition.class);

        String username = "test-user";
        String chatId = "123456";
        double targetValue = 100.0;
        double currentProgress = 25.0;

        Map<String, Map<String, Set<Double>>> userMilestoneTracker = new HashMap<>();
        Map<String, Set<Double>> chatMap = new HashMap<>();
        Set<Double> notified = new HashSet<>();
        notified.add(0.25);
        chatMap.put(chatId, notified);
        userMilestoneTracker.put(username, chatMap);

        CompetitionUtils.checkAndNotifyMilestones(currentProgress, competition, username, chatId, targetValue, notificationService, userMilestoneTracker);

        verify(notificationService, never()).notifyMilestoneReached(anyString(), any(), anyDouble());
    }

    @Test
    void testCheckAndNotifyMilestones_MultipleMilestones() {
        INotificationService notificationService = mock(INotificationService.class);
        Competition competition = mock(Competition.class);

        String username = "test-user";
        String chatId = "123456";
        double targetValue = 100.0;
        double currentProgress = 80.0;

        Map<String, Map<String, Set<Double>>> userMilestoneTracker = new HashMap<>();

        CompetitionUtils.checkAndNotifyMilestones(currentProgress, competition, username, chatId, targetValue, notificationService, userMilestoneTracker);

        verify(notificationService).notifyMilestoneReached(username, competition, 25.0);
        verify(notificationService).notifyMilestoneReached(username, competition, 50.0);
        verify(notificationService).notifyMilestoneReached(username, competition, 75.0);
        verify(notificationService, never()).notifyMilestoneReached(username, competition, 100.0);
    }
}