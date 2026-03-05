package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveCompetitionCommandTest {

    @Mock
    private ICompetitionWriteService competitionWriteService;

    @Mock
    private IUserChatService userChatService;

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @InjectMocks
    private LeaveCompetitionCommand leaveCompetitionCommand;

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
        assertEquals("/leave_competition", leaveCompetitionCommand.getCommandName());
    }

    @Test
    void isAllowedInChat_whenGroupChat_shouldReturnTrue() {
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        assertTrue(leaveCompetitionCommand.isAllowedInChat(chatId));
    }

    @Test
    void isAllowedInChat_whenNotGroupChat_shouldReturnFalse() {
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(false);
        assertFalse(leaveCompetitionCommand.isAllowedInChat(chatId));
    }

    @Test
    void execute_whenNotEnoughArgs_shouldReturnUsageMessage() {
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"arg1"});
        String result = leaveCompetitionCommand.execute(input);
        assertTrue(result.contains("❌ <b>Usage:</b>"));
        assertTrue(result.contains("/leave_competition username="));
    }

    @Test
    void execute_whenMissingUsername_shouldReturnError() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"competition=TestComp", "other=param"});

        String result = leaveCompetitionCommand.execute(input);

        assertTrue(result.contains("❌ <b>Error:</b> Missing required parameters"));
        assertTrue(result.contains("username"));
    }

    @Test
    void execute_whenMissingCompetition_shouldReturnError() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"username=testuser", "invalid=param"});

        String result = leaveCompetitionCommand.execute(input);

        assertTrue(result.contains("❌ <b>Error:</b> Missing required parameters"));
        assertTrue(result.contains("competition"));
    }

    @Test
    void execute_whenUserNotFound_shouldReturnError() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"username=testuser", "competition=TestComp"});

        when(userChatService.findUserInChatByUsername(chatId, "testuser"))
                .thenReturn(Optional.empty());

        String result = leaveCompetitionCommand.execute(input);

        assertTrue(result.contains("❌ <b>Error:</b>"));
        assertTrue(result.contains("not registered"));
        assertTrue(result.contains("/start_notification"));
    }

    @Test
    void execute_whenCompetitionNotFound_shouldReturnError() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"username=testuser", "competition=TestComp"});

        User user = new User();
        when(userChatService.findUserInChatByUsername(chatId, "testuser"))
                .thenReturn(Optional.of(user));
        when(competitionWriteService.findCompetitionByNameAndChatId("TestComp", chatId))
                .thenReturn(Optional.empty());

        String result = leaveCompetitionCommand.execute(input);

        assertTrue(result.contains("❌ <b>Error:</b>"));
        assertTrue(result.contains("not found"));
    }

    @Test
    void execute_whenLeaveFails_shouldReturnError() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"username=testuser", "competition=TestComp"});

        User user = new User();
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setName("TestComp");

        when(userChatService.findUserInChatByUsername(chatId, "testuser"))
                .thenReturn(Optional.of(user));
        when(competitionWriteService.findCompetitionByNameAndChatId("TestComp", chatId))
                .thenReturn(Optional.of(competition));

        doThrow(new RuntimeException("Not participating"))
                .when(competitionWriteService)
                .leaveCompetition(competition.getId(), user);

        String result = leaveCompetitionCommand.execute(input);

        assertTrue(result.contains("❌ <b>Error:</b>"));
        assertTrue(result.contains("Not participating"));
    }

    @Test
    void execute_whenSuccess_shouldReturnConfirmation() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"username=testuser", "competition=TestComp"});

        User user = new User();
        Competition competition = new Competition();
        competition.setName("TestComp");
        competition.setId(1L);

        when(userChatService.findUserInChatByUsername(chatId, "testuser"))
                .thenReturn(Optional.of(user));
        when(competitionWriteService.findCompetitionByNameAndChatId("TestComp", chatId))
                .thenReturn(Optional.of(competition));

        String result = leaveCompetitionCommand.execute(input);

        assertTrue(result.contains("✅ <b>Success:</b>"));
        assertTrue(result.contains("TestComp"));

        verify(competitionWriteService).leaveCompetition(competition.getId(), user);
    }

    @Test
    void execute_whenCompetitionNameHasSpaces_shouldWorkCorrectly() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"username=testuser", "competition=Test Competition"});

        User user = new User();
        Competition competition = new Competition();
        competition.setName("Test Competition");
        competition.setId(1L);

        when(userChatService.findUserInChatByUsername(chatId, "testuser"))
                .thenReturn(Optional.of(user));
        when(competitionWriteService.findCompetitionByNameAndChatId("Test Competition", chatId))
                .thenReturn(Optional.of(competition));

        String result = leaveCompetitionCommand.execute(input);

        assertTrue(result.contains("✅ <b>Success:</b>"));
        assertTrue(result.contains("Test Competition"));
    }
}