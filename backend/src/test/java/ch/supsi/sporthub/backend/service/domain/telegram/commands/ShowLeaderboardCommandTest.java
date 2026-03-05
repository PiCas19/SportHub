package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.Leaderboard;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.ILeaderboardService;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.utils.ResponseUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShowLeaderboardCommandTest {

    @Mock
    private ILeaderboardService leaderboardService;

    @Mock
    private IUserChatService userChatService;

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @InjectMocks
    private ShowLeaderboardCommand showLeaderboardCommand;

    private final String testChatId = "chat123";
    private final String senderName = "testSender";
    private final String telegramMessageId = "msg123";
    private final String[] args = new String[0];

    @Test
    void testExecute_NoUsers() {
        TelegramCommandInput commandInput = new TelegramCommandInput(testChatId, senderName, telegramMessageId, args);
        when(userChatService.getUsersInChat(testChatId)).thenReturn(Collections.emptyList());
        String result = showLeaderboardCommand.execute(commandInput);
        assertEquals("ℹ️ No users are registered in this group.", result);
        verify(userChatService).getUsersInChat(testChatId);
        verifyNoMoreInteractions(leaderboardService);
    }

    @Test
    void testExecute_NoLeaderboard() {
        TelegramCommandInput commandInput = new TelegramCommandInput(testChatId, senderName, telegramMessageId, args);
        List<User> users = Collections.singletonList(new User());
        when(userChatService.getUsersInChat(testChatId)).thenReturn(users);
        when(leaderboardService.getLeaderboardById(testChatId)).thenReturn(Optional.empty());
        String result = showLeaderboardCommand.execute(commandInput);
        assertEquals("❌ No leaderboard found for this group.", result);
        verify(userChatService).getUsersInChat(testChatId);
        verify(leaderboardService).getLeaderboardById(testChatId);
    }

    @Test
    void testExecute_NoScores() {
        TelegramCommandInput commandInput = new TelegramCommandInput(testChatId, senderName, telegramMessageId, args);
        List<User> users = Collections.singletonList(new User());
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setScores(null);

        when(userChatService.getUsersInChat(testChatId)).thenReturn(users);
        when(leaderboardService.getLeaderboardById(testChatId)).thenReturn(Optional.of(leaderboard));
        String result = showLeaderboardCommand.execute(commandInput);
        assertEquals("ℹ️ No users have points yet.", result);
        verify(userChatService).getUsersInChat(testChatId);
        verify(leaderboardService).getLeaderboardById(testChatId);
    }

    @Test
    void testExecute_EmptyScores() {
        TelegramCommandInput commandInput = new TelegramCommandInput(testChatId, senderName, telegramMessageId, args);
        List<User> users = Collections.singletonList(new User());
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setScores(new HashMap<>());

        when(userChatService.getUsersInChat(testChatId)).thenReturn(users);
        when(leaderboardService.getLeaderboardById(testChatId)).thenReturn(Optional.of(leaderboard));
        String result = showLeaderboardCommand.execute(commandInput);
        assertEquals("ℹ️ No users have points yet.", result);
        verify(userChatService).getUsersInChat(testChatId);
        verify(leaderboardService).getLeaderboardById(testChatId);
    }

    @Test
    void testExecute_NoUsersWithPoints() {
        TelegramCommandInput commandInput = new TelegramCommandInput(testChatId, senderName, telegramMessageId, args);
        User user = new User();
        user.setUsername("userA");
        List<User> users = Collections.singletonList(user);

        Leaderboard leaderboard = new Leaderboard();
        Map<String, Integer> scores = new HashMap<>();
        scores.put("userB", 100);
        leaderboard.setScores(scores);

        when(userChatService.getUsersInChat(testChatId)).thenReturn(users);
        when(leaderboardService.getLeaderboardById(testChatId)).thenReturn(Optional.of(leaderboard));
        String result = showLeaderboardCommand.execute(commandInput);
        assertEquals("ℹ️ No users with points in this group.", result);
        verify(userChatService).getUsersInChat(testChatId);
        verify(leaderboardService).getLeaderboardById(testChatId);
    }

    @Test
    void testExecute_SuccessfulLeaderboard() {
        TelegramCommandInput commandInput = new TelegramCommandInput(testChatId, senderName, telegramMessageId, args);
        User user1 = new User();
        user1.setUsername("userA");
        User user2 = new User();
        user2.setUsername("userB");
        List<User> users = Arrays.asList(user1, user2);

        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setName("Test Leaderboard");
        Map<String, Integer> scores = new HashMap<>();
        scores.put("userA", 100);
        scores.put("userB", 50);
        leaderboard.setScores(scores);

        List<LeaderboardEntryResponse> entries = Arrays.asList(
                new LeaderboardEntryResponse(100.0, "userA", "@userA_telegram"),
                new LeaderboardEntryResponse(50.0, "userB", "@userB_telegram")
        );

        when(userChatService.getUsersInChat(testChatId)).thenReturn(users);
        when(leaderboardService.getLeaderboardById(testChatId)).thenReturn(Optional.of(leaderboard));
        when(userChatService.getTelegramUsernameForUserInChat(eq(testChatId), eq(user1))).thenReturn("@userA_telegram");
        when(userChatService.getTelegramUsernameForUserInChat(eq(testChatId), eq(user2))).thenReturn("@userB_telegram");

        try (MockedStatic<ResponseUtils> responseUtilsMock = Mockito.mockStatic(ResponseUtils.class)) {
            responseUtilsMock.when(() -> ResponseUtils.formatLeaderboardResponse(any())).thenReturn("Formatted Leaderboard");
            String result = showLeaderboardCommand.execute(commandInput);
            assertEquals("🏆 <b>Leaderboard: Test Leaderboard</b>\n\nFormatted Leaderboard", result);
            verify(userChatService).getUsersInChat(testChatId);
            verify(leaderboardService).getLeaderboardById(testChatId);
            verify(userChatService).getTelegramUsernameForUserInChat(eq(testChatId), eq(user1));
            verify(userChatService).getTelegramUsernameForUserInChat(eq(testChatId), eq(user2));
            responseUtilsMock.verify(() -> ResponseUtils.formatLeaderboardResponse(any()));
        }
    }

    @Test
    void testExecute_WithHtmlEscaping() {
        TelegramCommandInput commandInput = new TelegramCommandInput(testChatId, senderName, telegramMessageId, args);
        User user = new User();
        user.setUsername("userA");
        List<User> users = Collections.singletonList(user);

        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setName("Test & <Leaderboard>");
        Map<String, Integer> scores = new HashMap<>();
        scores.put("userA", 100);
        leaderboard.setScores(scores);

        when(userChatService.getUsersInChat(testChatId)).thenReturn(users);
        when(leaderboardService.getLeaderboardById(testChatId)).thenReturn(Optional.of(leaderboard));
        when(userChatService.getTelegramUsernameForUserInChat(eq(testChatId), eq(user))).thenReturn("@userA_telegram");

        try (MockedStatic<ResponseUtils> responseUtilsMock = Mockito.mockStatic(ResponseUtils.class)) {
            responseUtilsMock.when(() -> ResponseUtils.formatLeaderboardResponse(any())).thenReturn("Formatted Leaderboard");
            String result = showLeaderboardCommand.execute(commandInput);
            assertEquals("🏆 <b>Leaderboard: Test &amp; &lt;Leaderboard&gt;</b>\n\nFormatted Leaderboard", result);
        }
    }

    @Test
    void testExecute_NullLeaderboardName() {
        TelegramCommandInput commandInput = new TelegramCommandInput(testChatId, senderName, telegramMessageId, args);
        User user = new User();
        user.setUsername("userA");
        List<User> users = Collections.singletonList(user);

        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setName(null);
        Map<String, Integer> scores = new HashMap<>();
        scores.put("userA", 100);
        leaderboard.setScores(scores);

        when(userChatService.getUsersInChat(testChatId)).thenReturn(users);
        when(leaderboardService.getLeaderboardById(testChatId)).thenReturn(Optional.of(leaderboard));
        when(userChatService.getTelegramUsernameForUserInChat(eq(testChatId), eq(user))).thenReturn("@userA_telegram");

        try (MockedStatic<ResponseUtils> responseUtilsMock = Mockito.mockStatic(ResponseUtils.class)) {
            responseUtilsMock.when(() -> ResponseUtils.formatLeaderboardResponse(any())).thenReturn("Formatted Leaderboard");
            String result = showLeaderboardCommand.execute(commandInput);
            assertEquals("🏆 <b>Leaderboard: </b>\n\nFormatted Leaderboard", result);
        }
    }

    @Test
    void testGetCommandName() {
        String commandName = showLeaderboardCommand.getCommandName();
        assertEquals("/show_leaderboard", commandName);
    }


    @Test
    void testIsAllowedInChat_Channel() {
        when(chatTypeFactory.isChatType(testChatId, ChatType.GROUP)).thenReturn(false);
        when(chatTypeFactory.isChatType(testChatId, ChatType.CHANNEL)).thenReturn(true);
        boolean result = showLeaderboardCommand.isAllowedInChat(testChatId);
        assertTrue(result);
        verify(chatTypeFactory).isChatType(testChatId, ChatType.GROUP);
        verify(chatTypeFactory).isChatType(testChatId, ChatType.CHANNEL);
    }

    @Test
    void testIsAllowedInChat_NotAllowed() {
        when(chatTypeFactory.isChatType(testChatId, ChatType.GROUP)).thenReturn(false);
        when(chatTypeFactory.isChatType(testChatId, ChatType.CHANNEL)).thenReturn(false);
        boolean result = showLeaderboardCommand.isAllowedInChat(testChatId);
        assertFalse(result);
        verify(chatTypeFactory).isChatType(testChatId, ChatType.GROUP);
        verify(chatTypeFactory).isChatType(testChatId, ChatType.CHANNEL);
    }
    @Test
    void testIsAllowedInChat_Group() {
        when(chatTypeFactory.isChatType(testChatId, ChatType.GROUP)).thenReturn(true);
        boolean result = showLeaderboardCommand.isAllowedInChat(testChatId);
        assertTrue(result);
        verify(chatTypeFactory).isChatType(testChatId, ChatType.GROUP);
        verify(chatTypeFactory, never()).isChatType(testChatId, ChatType.CHANNEL);
    }
}