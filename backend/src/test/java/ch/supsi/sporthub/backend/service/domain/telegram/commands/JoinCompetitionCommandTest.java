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
public class JoinCompetitionCommandTest {

    @Mock
    private ICompetitionWriteService competitionWriteService;

    @Mock
    private IUserChatService userChatService;

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @InjectMocks
    private JoinCompetitionCommand joinCompetitionCommand;

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
        assertEquals("/join_competition", joinCompetitionCommand.getCommandName());
    }

    @Test
    void isAllowedInChat_whenGroupChat_shouldReturnTrue() {
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        assertTrue(joinCompetitionCommand.isAllowedInChat(chatId));
    }

    @Test
    void isAllowedInChat_whenNotGroupChat_shouldReturnFalse() {
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(false);
        assertFalse(joinCompetitionCommand.isAllowedInChat(chatId));
    }

    @Test
    void execute_whenNotEnoughArgs_shouldReturnUsageMessage() {
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"arg1"});
        String result = joinCompetitionCommand.execute(input);
        assertTrue(result.contains("❌ Error: Missing required parameters: username and competition."));
    }

    @Test
    void execute_whenMissingParameters_shouldReturnError() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"wrong=param", "another=param"});

        String result = joinCompetitionCommand.execute(input);

        assertTrue(result.contains("❌ Error: Missing required parameters: username and competition."));
    }

    @Test
    void execute_whenUserNotRegistered_shouldReturnError() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"username=testuser", "competition=TestComp"});

        when(userChatService.findUserInChatByUsername(chatId, "testuser"))
                .thenReturn(Optional.empty());

        String result = joinCompetitionCommand.execute(input);

        assertTrue(result.contains("❌ Error: The user testuser is not registered in this chat."));
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

        String result = joinCompetitionCommand.execute(input);

        assertTrue(result.contains("❌ Error: Competition TestComp not found in this chat."));
    }

    @Test
    void execute_whenJoinFails_shouldReturnError() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"username=testuser", "competition=TestComp"});

        User user = new User();
        Competition competition = new Competition();
        competition.setId(1L);
        when(userChatService.findUserInChatByUsername(chatId, "testuser"))
                .thenReturn(Optional.of(user));
        when(competitionWriteService.findCompetitionByNameAndChatId("TestComp", chatId))
                .thenReturn(Optional.of(competition));
        doThrow(new RuntimeException("Competition full"))
                .when(competitionWriteService)
                .joinCompetition(competition.getId(), user);

        String result = joinCompetitionCommand.execute(input);

        assertTrue(result.contains("❌ Error: Competition full"));
    }

    @Test
    void execute_whenSuccess_shouldReturnConfirmation() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"username=testuser", "competition=TestComp"});

        User user = new User();
        user.setUsername("testuser");
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setName("TestComp");

        when(userChatService.findUserInChatByUsername(chatId, "testuser"))
                .thenReturn(Optional.of(user));
        when(competitionWriteService.findCompetitionByNameAndChatId("TestComp", chatId))
                .thenReturn(Optional.of(competition));

        String result = joinCompetitionCommand.execute(input);

        assertTrue(result.contains("✅ Success: You have joined the competition TestComp."));
        verify(competitionWriteService).joinCompetition(competition.getId(), user);
    }

    @Test
    void execute_whenCompetitionNameIsNull_shouldReturnError() {
        input = new TelegramCommandInput(chatId, senderName, messageId,
                new String[]{"username=testuser", "invalid_param=value"});

        String result = joinCompetitionCommand.execute(input);

        assertTrue(result.contains("❌ Error: Missing required parameters: username and competition."));
    }

    @Test
    void execute_whenNoArguments_shouldReturnUsageMessage() {
        String[] args = {};
        String result = joinCompetitionCommand.execute(input);
        input = new TelegramCommandInput(chatId, senderName, messageId, args);
        assertTrue(result.startsWith("❌"));
    }
}