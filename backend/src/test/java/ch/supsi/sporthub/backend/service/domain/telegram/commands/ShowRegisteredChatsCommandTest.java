package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.Chat;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShowRegisteredChatsCommandTest {

    @Mock
    private IUserChatService userChatService;

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @InjectMocks
    private ShowRegisteredChatsCommand showRegisteredChatsCommand;

    private TelegramCommandInput input;
    private final String chatId = "test-chat-id";
    private final String username = "test-user";
    private final String messageId = "12345";
    private User testUser;

    @BeforeEach
    void setUp() {
        input = new TelegramCommandInput(chatId, username, messageId, new String[0]);
        testUser = new User();
        testUser.setUsername(username);
    }

    @Test
    void getCommandName_shouldReturnCorrectCommand() {
        assertEquals("/show_registered_chats", showRegisteredChatsCommand.getCommandName());
    }

    @Test
    void isAllowedInChat_whenPrivateChat_shouldReturnTrue() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        assertTrue(showRegisteredChatsCommand.isAllowedInChat(chatId));
    }

    @Test
    void isAllowedInChat_whenNotPrivateChat_shouldReturnFalse() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        assertFalse(showRegisteredChatsCommand.isAllowedInChat(chatId));
    }

    @Test
    void execute_whenNoArgsAndUserNotRegistered_shouldReturnError() {
        when(userChatService.getUserFromChat(chatId)).thenReturn(null);

        String result = showRegisteredChatsCommand.execute(input);

        assertEquals("❌ You are not registered in this chat.\nUse <code>/show_registered_chats &lt;your_username&gt;</code> if your private chat was removed.", result);
        verify(userChatService).getUserFromChat(chatId);
        verifyNoMoreInteractions(userChatService);
    }

    @Test
    void execute_whenNoArgsAndNoRegisteredChats_shouldReturnMessage() {
        when(userChatService.getUserFromChat(chatId)).thenReturn(testUser);
        when(userChatService.getChatsForUser(testUser)).thenReturn(Collections.emptyList());

        String result = showRegisteredChatsCommand.execute(input);

        assertEquals("ℹ️ You have no registered chats.", result);
        verify(userChatService).getUserFromChat(chatId);
        verify(userChatService).getChatsForUser(testUser);
    }

    @Test
    void execute_whenNoArgsAndHasRegisteredChats_shouldReturnFormattedList() {
        Chat chat1 = new Chat(1L, "chat1", ChatType.PRIVATE, testUser, "telegramUser1");
        Chat chat2 = new Chat(2L, "chat2", ChatType.GROUP, testUser, "telegramUser1");
        List<Chat> chats = List.of(chat1, chat2);

        when(userChatService.getUserFromChat(chatId)).thenReturn(testUser);
        when(userChatService.getChatsForUser(testUser)).thenReturn(chats);

        String result = showRegisteredChatsCommand.execute(input);

        assertTrue(result.startsWith("📜 <b>Registered chats for " + username + ":</b>"));
        assertTrue(result.contains("• <code>chat1</code> (PRIVATE)"));
        assertTrue(result.contains("• <code>chat2</code> (GROUP)"));
        verify(userChatService).getUserFromChat(chatId);
        verify(userChatService).getChatsForUser(testUser);
    }

    @Test
    void execute_whenUsernameArgAndUserNotFound_shouldThrowException() {
        String usernameArg = "unknown-user";
        input = new TelegramCommandInput(chatId, username, messageId, new String[]{usernameArg});

        when(userChatService.findUserInChatByUsername(chatId, usernameArg)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            showRegisteredChatsCommand.execute(input);
        });

        assertEquals("❌ No user found with username <b>" + usernameArg + "</b>.", exception.getMessage());
        verify(userChatService).findUserInChatByUsername(chatId, usernameArg);
        verifyNoMoreInteractions(userChatService);
    }

    @Test
    void execute_whenUsernameArgAndNoRegisteredChats_shouldReturnMessage() {
        String usernameArg = "other-user";
        input = new TelegramCommandInput(chatId, username, messageId, new String[]{usernameArg});
        User otherUser = new User();
        otherUser.setUsername(usernameArg);

        when(userChatService.findUserInChatByUsername(chatId, usernameArg)).thenReturn(Optional.of(otherUser));
        when(userChatService.getChatsForUser(otherUser)).thenReturn(Collections.emptyList());

        String result = showRegisteredChatsCommand.execute(input);

        assertEquals("ℹ️ You have no registered chats.", result);
        verify(userChatService).findUserInChatByUsername(chatId, usernameArg);
        verify(userChatService).getChatsForUser(otherUser);
    }

    @Test
    void execute_whenUsernameArgAndHasRegisteredChats_shouldReturnFormattedList() {
        String usernameArg = "other-user";
        input = new TelegramCommandInput(chatId, username, messageId, new String[]{usernameArg});
        User otherUser = new User();
        otherUser.setUsername(usernameArg);
        Chat chat1 = new Chat(1L, "chat1", ChatType.PRIVATE, otherUser, "telegramUser1");
        Chat chat2 = new Chat(2L, "chat2", ChatType.CHANNEL, otherUser, "telegramUser1");
        List<Chat> chats = List.of(chat1, chat2);

        when(userChatService.findUserInChatByUsername(chatId, usernameArg)).thenReturn(Optional.of(otherUser));
        when(userChatService.getChatsForUser(otherUser)).thenReturn(chats);

        String result = showRegisteredChatsCommand.execute(input);

        assertTrue(result.startsWith("📜 <b>Registered chats for " + usernameArg + ":</b>"));
        assertTrue(result.contains("• <code>chat1</code> (PRIVATE)"));
        assertTrue(result.contains("• <code>chat2</code> (CHANNEL)"));
        verify(userChatService).findUserInChatByUsername(chatId, usernameArg);
        verify(userChatService).getChatsForUser(otherUser);
    }

}