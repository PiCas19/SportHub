package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.dto.request.SetGoalRequest;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SetGoalCommandTest {

    @Mock
    private IGoalService goalService;

    @Mock
    private IUserChatService userChatService;

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @InjectMocks
    private SetGoalCommand setGoalCommand;

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
        assertEquals("/set_goal", setGoalCommand.getCommandName());
    }

    @Test
    void isAllowedInChat_whenPrivateChat_shouldReturnTrue() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        assertTrue(setGoalCommand.isAllowedInChat(chatId));
    }

    @Test
    void isAllowedInChat_whenNotPrivateChat_shouldReturnFalse() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        assertFalse(setGoalCommand.isAllowedInChat(chatId));
    }

    @Test
    void execute_whenUserNotRegistered_shouldReturnError() {
        when(userChatService.getUserFromChat(chatId)).thenReturn(null);

        String result = setGoalCommand.execute(input);

        assertEquals("❌ No registered users found for this chat. Please register using /start_notification.", result);
    }

    @Test
    void execute_whenMissingParameters_shouldReturnError() {
        User user = new User();
        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"target=100"});

        String result = setGoalCommand.execute(input);

        assertTrue(result.contains("❌ Error: missing parameters"));
        assertTrue(result.contains("target"));
        assertTrue(result.contains("type"));
        assertTrue(result.contains("period"));
        assertTrue(result.contains("sport"));
    }

    @Test
    void execute_whenInvalidTargetFormat_shouldReturnError() {
        User user = new User();
        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"target=invalid", "type=DISTANCE", "period=WEEKLY", "sport=RUN"});

        String result = setGoalCommand.execute(input);

        assertEquals("❌ Error: invalid number format for <code>target</code>.", result);
    }

    @Test
    void execute_whenInvalidEnumValue_shouldReturnError() {
        User user = new User();
        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"target=100", "type=INVALID", "period=WEEKLY", "sport=RUN"});

        String result = setGoalCommand.execute(input);

        assertEquals("❌ Error: invalid value for one of the parameters (type, period, sport).", result);
    }

    @Test
    void execute_whenValidInput_shouldCreateGoal() {
        User user = new User();
        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"target=100", "type=DISTANCE", "period=WEEKLY", "sport=RUN"});

        String result = setGoalCommand.execute(input);

        assertTrue(result.startsWith("✅ Goal set successfully:"));
        assertTrue(result.contains("Target=100"));
        assertTrue(result.contains("Type=DISTANCE"));
        assertTrue(result.contains("Period=WEEKLY"));
        assertTrue(result.contains("Sport=RUN"));

        ArgumentCaptor<SetGoalRequest> requestCaptor = ArgumentCaptor.forClass(SetGoalRequest.class);
        verify(goalService).createGoal(requestCaptor.capture(), eq(user));

        SetGoalRequest request = requestCaptor.getValue();
        assertEquals(100.0, request.getTargetValue());
        assertEquals("DISTANCE", request.getGoalType());
        assertEquals(Period.WEEKLY, request.getPeriod());
        assertEquals("RUN", request.getSportType());
    }

    @Test
    void execute_whenServiceThrowsException_shouldPropagate() {
        User user = new User();
        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"target=100", "type=DISTANCE", "period=WEEKLY", "sport=RUN"});

        doThrow(new RuntimeException("Database error"))
                .when(goalService)
                .createGoal(any(), any());

        assertThrows(RuntimeException.class, () -> setGoalCommand.execute(input));
    }
}