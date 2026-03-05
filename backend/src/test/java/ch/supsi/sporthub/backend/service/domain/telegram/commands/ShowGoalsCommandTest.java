package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.Goal;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.model.GoalType;
import ch.supsi.sporthub.backend.model.Period;
import ch.supsi.sporthub.backend.service.api.IGoalService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShowGoalsCommandTest {

    @Mock
    private IGoalService goalService;

    @Mock
    private IUserChatService userChatService;

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @InjectMocks
    private ShowGoalsCommand showGoalsCommand;

    private TelegramCommandInput input;
    private final String chatId = "test-chat-id";
    private final String username = "test-user";
    private final String messageId = "12345";

    @BeforeEach
    void setUp() {
        input = new TelegramCommandInput(chatId, username, messageId, new String[0]);
    }

    @Test
    void getCommandName_shouldReturnCorrectCommand() {
        assertEquals("/show_goals", showGoalsCommand.getCommandName());
    }

    @Test
    void isAllowedInChat_whenPrivateChat_shouldReturnTrue() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        assertTrue(showGoalsCommand.isAllowedInChat(chatId));
    }

    @Test
    void isAllowedInChat_whenNotPrivateChat_shouldReturnFalse() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        assertFalse(showGoalsCommand.isAllowedInChat(chatId));
    }

    @Test
    void execute_whenUserNotFound_shouldReturnErrorMessage() {
        when(userChatService.getUserFromChat(chatId)).thenReturn(null);

        String result = showGoalsCommand.execute(input);

        assertEquals("❌ No registered users found for this chat. Register with /start_notification.", result);
        verify(userChatService).getUserFromChat(chatId);
        verifyNoInteractions(goalService);
    }

    @Test
    void execute_whenNoGoals_shouldReturnNoGoalsMessage() {
        User user = new User();
        user.setUsername(username);

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(goalService.getGoalsForUser(user)).thenReturn(Collections.emptyList());

        String result = showGoalsCommand.execute(input);

        assertEquals("ℹ️ You didn't set any goals.", result);
        verify(userChatService).getUserFromChat(chatId);
        verify(goalService).getGoalsForUser(user);
    }

    @Test
    void execute_whenGoalsExist_shouldReturnFormattedGoalsList() {
        User user = new User();
        user.setUsername(username);

        Goal goal1 = createTestGoal(1L, 100.0, GoalType.ACTIVITIES, Period.DAILY);
        Goal goal2 = createTestGoal(2L, 50.0, GoalType.DISTANCE, Period.WEEKLY);
        List<Goal> goals = List.of(goal1, goal2);

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(goalService.getGoalsForUser(user)).thenReturn(goals);

        String result = showGoalsCommand.execute(input);
        assertTrue(result.startsWith("🎯 <b>Objectives imposed:</b>\n\n"));
        assertTrue(result.contains("ID: 1 - Target: 100.0 - Type: ACTIVITIES - Period: DAILY"));
        assertTrue(result.contains("ID: 2 - Target: 50.0 - Type: DISTANCE - Period: WEEKLY"));
        verify(userChatService).getUserFromChat(chatId);
        verify(goalService).getGoalsForUser(user);
    }

    @Test
    void execute_whenMultipleGoals_shouldFormatAllGoals() {
        User user = new User();
        user.setUsername(username);

        Goal goal1 = createTestGoal(1L, 100.0, GoalType.ACTIVITIES, Period.DAILY);
        Goal goal2 = createTestGoal(2L, 50.0, GoalType.DISTANCE, Period.WEEKLY);
        Goal goal3 = createTestGoal(3L, 30.0, GoalType.CALORIES, Period.MONTHLY);
        List<Goal> goals = List.of(goal1, goal2, goal3);

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(goalService.getGoalsForUser(user)).thenReturn(goals);

        String result = showGoalsCommand.execute(input);

        String[] lines = result.split("\n");
        assertEquals(5, lines.length);
        assertTrue(lines[2].contains("ID: 1"));
        assertTrue(lines[3].contains("ID: 2"));
        assertTrue(lines[4].contains("ID: 3"));
    }

    private Goal createTestGoal(Long id, Double targetValue, GoalType goalType, Period period) {
        Goal goal = new Goal();
        goal.setId(id);
        goal.setTargetValue(targetValue);
        goal.setGoalType(goalType);
        goal.setPeriod(period);
        return goal;
    }
}