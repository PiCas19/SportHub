package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HelpCommandTest {

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @InjectMocks
    private HelpCommand helpCommand;

    private TelegramCommandInput input;
    private final String chatId = "test-chat-id";
    private final String senderName = "test-user";
    private final String messageId = "12345";

    @BeforeEach
    void setUp() {
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[0]);
    }

    @Test
    void getCommandName_shouldReturnHelp() {
        assertEquals("/help", helpCommand.getCommandName());
    }

    @Test
    void isAllowedInChat_shouldAllowPrivateGroupAndChannel() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        assertTrue(helpCommand.isAllowedInChat(chatId));

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        assertTrue(helpCommand.isAllowedInChat(chatId));

        when(chatTypeFactory.isChatType(chatId, ChatType.CHANNEL)).thenReturn(true);
        assertTrue(helpCommand.isAllowedInChat(chatId));

        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(false);
        when(chatTypeFactory.isChatType(chatId, ChatType.CHANNEL)).thenReturn(false);
        assertFalse(helpCommand.isAllowedInChat(chatId));
    }

    @Test
    void execute_whenPrivateChat_shouldReturnPrivateHelp() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);

        String result = helpCommand.execute(input);

        assertTrue(result.contains("🏆 Personal Commands (Private Chat)"));
        assertTrue(result.contains("/start_notification"));
        assertTrue(result.contains("/unregister"));
        assertTrue(result.contains("/show_registered_chats"));
        assertTrue(result.contains("/show_activities"));
        assertTrue(result.contains("/set_goal"));
        assertTrue(result.contains("/delete_goal"));
    }

    @Test
    void execute_whenChannelChat_shouldReturnGroupHelp() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(false);
        when(chatTypeFactory.isChatType(chatId, ChatType.CHANNEL)).thenReturn(true);

        String result = helpCommand.execute(input);

        assertTrue(result.contains("Group & Channel Commands"));
        verify(chatTypeFactory).isChatType(chatId, ChatType.CHANNEL);
    }

    @Test
    void execute_whenUnknownChatType_shouldReturnError() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(false);
        when(chatTypeFactory.isChatType(chatId, ChatType.CHANNEL)).thenReturn(false);

        String result = helpCommand.execute(input);

        assertEquals("❌ Unknown chat type.", result);
    }

    @Test
    void isAllowedInChat_shouldReturnTrueForGroupChat() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);

        assertTrue(helpCommand.isAllowedInChat(chatId));

        verify(chatTypeFactory).isChatType(chatId, ChatType.PRIVATE);
        verify(chatTypeFactory).isChatType(chatId, ChatType.GROUP);
        verify(chatTypeFactory, never()).isChatType(chatId, ChatType.CHANNEL);
    }

    @Test
    void shouldUseGroupBranchWhenGroupChat() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);

        helpCommand.execute(input);

        verify(chatTypeFactory, never()).isChatType(chatId, ChatType.CHANNEL);
    }


}