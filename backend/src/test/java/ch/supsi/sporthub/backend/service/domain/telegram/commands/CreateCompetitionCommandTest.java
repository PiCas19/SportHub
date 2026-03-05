package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.dto.request.CreateCompetitionRequest;
import ch.supsi.sporthub.backend.model.ChatType;
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
public class CreateCompetitionCommandTest {

    @Mock
    private ICompetitionWriteService competitionWriteService;

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @InjectMocks
    private CreateCompetitionCommand createCompetitionCommand;

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
        assertEquals("/create_competition", createCompetitionCommand.getCommandName());
    }

    @Test
    void isAllowedInChat_whenGroupChat_shouldReturnTrue() {
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        assertTrue(createCompetitionCommand.isAllowedInChat(chatId));
        verify(chatTypeFactory).isChatType(chatId, ChatType.GROUP);
    }

    @Test
    void isAllowedInChat_whenNotGroupChat_shouldReturnFalse() {
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(false);
        assertFalse(createCompetitionCommand.isAllowedInChat(chatId));
        verify(chatTypeFactory).isChatType(chatId, ChatType.GROUP);
    }

    @Test
    void execute_whenNoArgs_shouldReturnUsageMessage() {
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[0]);
        String result = createCompetitionCommand.execute(input);
        assertTrue(result.contains("❌"));
    }

    @Test
    void execute_whenMissingRequiredParameters_shouldListAllMissing() {
        String[] args = {"name=Test", "target=100"};
        input = new TelegramCommandInput(chatId, senderName, messageId, args);

        String result = createCompetitionCommand.execute(input);
        assertTrue(result.contains("❌ <b>Error:</b> Missing required parameters"));
        assertTrue(result.contains("goaltype"));
        assertTrue(result.contains("enddate"));
        assertTrue(result.contains("sporttype"));
        assertTrue(result.contains("max"));
        assertTrue(result.contains("deadline"));
    }

    @Test
    void execute_whenDuplicateCompetition_shouldReturnError() {
        String[] args = completeValidArgs();
        input = new TelegramCommandInput(chatId, senderName, messageId, args);

        when(competitionWriteService.existsCompetitionByNameAndChatId("Spring Run", chatId))
                .thenReturn(true);

        String result = createCompetitionCommand.execute(input);
        assertTrue(result.contains("❌ <b>Error:</b> Competition"));
        assertTrue(result.contains("already exists"));
        verify(competitionWriteService, never()).createCompetition(any(), any());
    }

    @Test
    void execute_whenInvalidNumberFormat_shouldReturnError() {
        String[] args = {
                "name=Test",
                "target=invalid",
                "goaltype=DISTANCE",
                "enddate=2025-07-01",
                "sporttype=RUN",
                "max=50",
                "deadline=2025-06-01"
        };
        input = new TelegramCommandInput(chatId, senderName, messageId, args);

        String result = createCompetitionCommand.execute(input);
        assertTrue(result.contains("❌ <b>Error:</b> Invalid parameter format"));
    }

    @Test
    void execute_whenInvalidDateFormat_shouldReturnError() {
        String[] args = {
                "name=Test",
                "target=100",
                "goaltype=DISTANCE",
                "enddate=invalid-date",
                "sporttype=RUN",
                "max=50",
                "deadline=2025-06-01"
        };
        input = new TelegramCommandInput(chatId, senderName, messageId, args);

        String result = createCompetitionCommand.execute(input);
        assertTrue(result.contains("❌ <b>Unexpected error:</b> Text 'invalid-date' could not be parsed at index 0"));
    }

    @Test
    void execute_withValidArgs_shouldCreateCompetition() {
        String[] args = completeValidArgs();
        input = new TelegramCommandInput(chatId, senderName, messageId, args);

        when(competitionWriteService.existsCompetitionByNameAndChatId("Spring Run", chatId))
                .thenReturn(false);

        String result = createCompetitionCommand.execute(input);
        assertTrue(result.contains("✅ <b>Competition created successfully:"));
        verify(competitionWriteService).createCompetition(any(CreateCompetitionRequest.class), isNull());
    }

    @Test
    void execute_whenServiceThrowsIllegalArgument_shouldReturnError() {
        String[] args = completeValidArgs();
        input = new TelegramCommandInput(chatId, senderName, messageId, args);

        when(competitionWriteService.existsCompetitionByNameAndChatId(any(), any())).thenReturn(false);
        doThrow(new IllegalArgumentException("Invalid sport type"))
                .when(competitionWriteService).createCompetition(any(), any());

        String result = createCompetitionCommand.execute(input);
        assertTrue(result.contains("❌ <b>Error:</b> Invalid parameter format"));
    }

    @Test
    void execute_whenServiceThrowsUnexpectedException_shouldReturnError() {
        String[] args = completeValidArgs();
        input = new TelegramCommandInput(chatId, senderName, messageId, args);

        when(competitionWriteService.existsCompetitionByNameAndChatId(any(), any())).thenReturn(false);
        doThrow(new RuntimeException("Database error"))
                .when(competitionWriteService).createCompetition(any(), any());

        String result = createCompetitionCommand.execute(input);
        assertTrue(result.contains("❌ <b>Unexpected error:</b> Database error"));
    }

    @Test
    void execute_whenNoArguments_shouldReturnUsageMessage() {
        String[] args = {};
        String result = createCompetitionCommand.execute(input);
        input = new TelegramCommandInput(chatId, senderName, messageId, args);
        assertTrue(result.startsWith("❌"));
    }

    private String[] completeValidArgs() {
        return new String[]{
                "name=\"Spring Run\"",
                "target=100",
                "goaltype=DISTANCE",
                "enddate=2025-07-01",
                "sporttype=RUN",
                "max=50",
                "deadline=2025-06-01"
        };
    }
}