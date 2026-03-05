package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.*;
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
public class DeleteGoalCommandTest {

    @Mock
    private IGoalService goalService;

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @Mock
    private IUserChatService userChatService;

    @InjectMocks
    private DeleteGoalCommand deleteGoalCommand;

    private TelegramCommandInput input;
    private final String chatId = "test-chat-id";
    private final String senderName = "test-user";
    private final String messageId = "12345";

    @BeforeEach
    void setUp() {
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[0]);
    }

    @Test
    void getCommandName_shouldReturnCorrectCommand() {
        assertEquals("/delete_goal", deleteGoalCommand.getCommandName());
    }

    @Test
    void isAllowedInChat_whenPrivateChat_shouldReturnTrue() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        assertTrue(deleteGoalCommand.isAllowedInChat(chatId));
    }

    @Test
    void isAllowedInChat_whenNotPrivateChat_shouldReturnFalse() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        assertFalse(deleteGoalCommand.isAllowedInChat(chatId));
    }

    @Test
    void execute_whenNoArgs_shouldReturnError() {
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[0]);
        String result = deleteGoalCommand.execute(input);
        assertEquals("❌ Error: please specify the goal ID to delete. Example: /delete_goal id=1", result);
    }

    @Test
    void execute_whenUserNotFound_shouldReturnError() {
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"id=1"});
        when(userChatService.getUserFromChat(chatId)).thenReturn(null);

        String result = deleteGoalCommand.execute(input);
        assertEquals("❌ No registered user found for this chat. Register with /start_notification.", result);
    }

    @Test
    void execute_whenInvalidGoalIdFormat_shouldReturnError() {
        User testUser = new User();
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"invalid"});
        when(userChatService.getUserFromChat(chatId)).thenReturn(testUser);

        String result = deleteGoalCommand.execute(input);
        assertEquals("❌ Error: invalid goal ID format. Example: /delete_goal id=1", result);
    }

    @Test
    void execute_whenInvalidKeyFormat_shouldReturnError() {
        User testUser = new User();
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"wrong=1"});
        when(userChatService.getUserFromChat(chatId)).thenReturn(testUser);

        String result = deleteGoalCommand.execute(input);
        assertEquals("❌ Error: invalid goal ID format. Example: /delete_goal id=1", result);
    }

    @Test
    void execute_whenGoalDeletedSuccessfully_shouldReturnSuccess() {
        User testUser = new User();
        Long goalId = 1L;
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"id=" + goalId});
        when(userChatService.getUserFromChat(chatId)).thenReturn(testUser);
        when(goalService.deleteGoalForUser(goalId, testUser)).thenReturn(true);

        String result = deleteGoalCommand.execute(input);
        assertEquals("✅ Goal with ID " + goalId + " deleted successfully.", result);
    }

    @Test
    void execute_whenGoalNotFound_shouldReturnError() {
        User testUser = new User();
        Long goalId = 1L;
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"id=" + goalId});
        when(userChatService.getUserFromChat(chatId)).thenReturn(testUser);
        when(goalService.deleteGoalForUser(goalId, testUser)).thenReturn(false);

        String result = deleteGoalCommand.execute(input);
        assertEquals("❌ No goal found with ID " + goalId + " for the user.", result);
    }
    @Test
    void execute_whenValidGoalIdFormat_shouldParseSuccessfully() {
        User testUser = new User();
        Long goalId = 123L;
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"id=" + goalId});
        when(userChatService.getUserFromChat(chatId)).thenReturn(testUser);
        when(goalService.deleteGoalForUser(goalId, testUser)).thenReturn(true);

        String result = deleteGoalCommand.execute(input);
        assertEquals("✅ Goal with ID " + goalId + " deleted successfully.", result);
    }

    @Test
    void execute_whenInvalidGoalIdNumber_shouldReturnError() {
        User testUser = new User();
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"id=abc"});
        when(userChatService.getUserFromChat(chatId)).thenReturn(testUser);

        String result = deleteGoalCommand.execute(input);
        assertEquals("❌ Error: invalid goal ID format. Example: /delete_goal id=1", result);
    }

    @Test
    void execute_whenWrongParameterName_shouldReturnError() {
        User testUser = new User();
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"goal=123"});
        when(userChatService.getUserFromChat(chatId)).thenReturn(testUser);

        String result = deleteGoalCommand.execute(input);
        assertEquals("❌ Error: invalid goal ID format. Example: /delete_goal id=1", result);
    }

    @Test
    void execute_whenMissingEqualsSign_shouldReturnError() {
        User testUser = new User();
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"id123"});
        when(userChatService.getUserFromChat(chatId)).thenReturn(testUser);

        String result = deleteGoalCommand.execute(input);
        assertEquals("❌ Error: invalid goal ID format. Example: /delete_goal id=1", result);
    }
}