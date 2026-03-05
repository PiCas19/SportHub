package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.supsi.sporthub.backend.model.Chat;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.ILeaderboardService;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UnregisterCommandTest {

    @Mock
    private IUserChatService userChatService;

    @Mock
    private ILeaderboardService leaderboardService;

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @InjectMocks
    private UnregisterCommand unregisterCommand;

    private final String chatId = "12345";
    private final String username = "testuser";
    private final String telegramMessageId = "msg123";
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername(username);
    }

    private Chat createChat(String chatId, User user) {
        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setUser(user);
        chat.setChatType(ChatType.PRIVATE);
        chat.setTelegramUserName("testuser");
        return chat;
    }

    @Test
    void execute_WithNoUserFound_ShouldReturnErrorMessage() {
        TelegramCommandInput input = new TelegramCommandInput(
                chatId, "sender", telegramMessageId, new String[]{}
        );

        when(userChatService.getChatByChatId(chatId)).thenReturn(Optional.empty());

        String result = unregisterCommand.execute(input);

        assertEquals("❌ You are not registered or no valid user could be found for this operation.", result);
    }

    @Test
    void execute_WithUsernameAndAll_ShouldRemoveAllRegistrations() {
        String[] args = {username, "all"};
        TelegramCommandInput input = new TelegramCommandInput(
                chatId, "sender", telegramMessageId, args
        );

        Chat groupChat = createChat("67890", testUser);
        groupChat.setChatType(ChatType.GROUP);

        when(userChatService.findUserByUsername(username)).thenReturn(Optional.of(testUser));
        when(userChatService.hasPrivateChat(chatId, testUser)).thenReturn(true);
        when(userChatService.getChatsForUser(testUser)).thenReturn(Collections.singletonList(groupChat));
        when(chatTypeFactory.isChatType("67890", ChatType.GROUP)).thenReturn(true);

        String result = unregisterCommand.execute(input);

        assertTrue(result.contains("✅ You are no longer registered in your private chat."));
        assertTrue(result.contains("📤 <b>You have been removed from the following groups:</b>"));
        verify(userChatService).removePrivateChat(chatId);
        verify(userChatService).removeGroupChat(groupChat);
        verify(leaderboardService).removeUserFromLeaderboard("67890", username);
    }

    @Test
    void execute_WithUsernameOnly_ShouldRemovePrivateAndGroups() {
        String[] args = {username};
        TelegramCommandInput input = new TelegramCommandInput(
                chatId, "sender", telegramMessageId, args
        );

        Chat privateChat = createChat(chatId, testUser);
        Chat groupChat = createChat("67890", testUser);
        groupChat.setChatType(ChatType.GROUP);

        when(userChatService.getChatByChatId(chatId)).thenReturn(Optional.of(privateChat));
        when(userChatService.hasPrivateChat(chatId, testUser)).thenReturn(true);
        when(userChatService.getChatsForUser(testUser)).thenReturn(Collections.singletonList(groupChat));

        String result = unregisterCommand.execute(input);

        assertTrue(result.contains("✅ You are no longer registered in your private chat."));
        assertTrue(result.contains("📤 <b>You have been removed from the following groups:</b>"));
    }

    @Test
    void execute_WithGroupIds_ShouldRemoveSpecificGroups() {
        String[] args = {"67890", "98765"};
        TelegramCommandInput input = new TelegramCommandInput(
                chatId, "sender", telegramMessageId, args
        );

        Chat privateChat = createChat(chatId, testUser);

        when(userChatService.getChatByChatId(chatId)).thenReturn(Optional.of(privateChat));
        when(userChatService.removeGroupChatForUser(testUser, "67890")).thenReturn(true);
        when(userChatService.removeGroupChatForUser(testUser, "98765")).thenReturn(false);

        String result = unregisterCommand.execute(input);

        assertTrue(result.contains("📤 <b>You have been removed from the following groups:</b>"));
        assertTrue(result.contains("• <code>67890</code>"));
        assertTrue(result.contains("⚠️ <b>The following values were not recognized or not linked to you:</b>"));
        assertTrue(result.contains("• <code>98765</code>"));
    }

    @Test
    void execute_WithInvalidArgs_ShouldShowInfoMessage() {
        String[] args = {"invalid"};
        TelegramCommandInput input = new TelegramCommandInput(
                chatId, "sender", telegramMessageId, args
        );

        Chat privateChat = createChat(chatId, testUser);

        when(userChatService.getChatByChatId(chatId)).thenReturn(Optional.of(privateChat));

        String result = unregisterCommand.execute(input);

        assertTrue(result.contains("ℹ️ No registrations were removed."));
    }

    @Test
    void execute_WithAllFlagOnly_ShouldUseCurrentUser() {
        String[] args = {"all"};
        TelegramCommandInput input = new TelegramCommandInput(
                chatId, "sender", telegramMessageId, args
        );

        Chat privateChat = createChat(chatId, testUser);
        Chat groupChat = createChat("67890", testUser);
        groupChat.setChatType(ChatType.GROUP);

        when(userChatService.getChatByChatId(chatId)).thenReturn(Optional.of(privateChat));
        when(userChatService.hasPrivateChat(chatId, testUser)).thenReturn(true);
        when(userChatService.getChatsForUser(testUser)).thenReturn(Collections.singletonList(groupChat));
        when(chatTypeFactory.isChatType("67890", ChatType.GROUP)).thenReturn(true);

        String result = unregisterCommand.execute(input);

        assertTrue(result.contains("✅ You are no longer registered in your private chat."));
        assertTrue(result.contains("📤 <b>You have been removed from the following groups:</b>"));
    }

    @Test
    void getCommandName_ShouldReturnCorrectValue() {
        assertEquals("/unregister", unregisterCommand.getCommandName());
    }

    @Test
    void isAllowedInChat_ShouldAlwaysReturnTrue() {
        assertTrue(unregisterCommand.isAllowedInChat("any_chat_id"));
        assertTrue(unregisterCommand.isAllowedInChat(null));
    }
    @Test
    void execute_WithInvalidUsernameAndAll_ShouldUseCurrentUser() {
        String[] args = {"nonexistent", "all"};
        TelegramCommandInput input = new TelegramCommandInput(
                chatId, "sender", telegramMessageId, args
        );

        Chat privateChat = createChat(chatId, testUser);

        when(userChatService.findUserByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userChatService.getChatByChatId(chatId)).thenReturn(Optional.of(privateChat));
        when(userChatService.hasPrivateChat(chatId, testUser)).thenReturn(true);

        String result = unregisterCommand.execute(input);

        assertTrue(result.contains("✅ You are no longer registered in your private chat."));
    }

    @Test
    void execute_WithAllAsFirstArg_ShouldNotTreatAsUsername() {
        String[] args = {"all", "extra"};
        TelegramCommandInput input = new TelegramCommandInput(
                chatId, "sender", telegramMessageId, args
        );

        Chat privateChat = createChat(chatId, testUser);

        when(userChatService.getChatByChatId(chatId)).thenReturn(Optional.of(privateChat));
        when(userChatService.hasPrivateChat(chatId, testUser)).thenReturn(true);

        String result = unregisterCommand.execute(input);

        assertTrue(result.contains("✅ You are no longer registered in your private chat."));
    }
}