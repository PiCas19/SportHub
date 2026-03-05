package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UnknownCommandTest {

    @Mock
    private IChatTypeFactory chatTypeFactory;

    private UnknownCommand unknownCommand;

    @BeforeEach
    void setUp() {
        unknownCommand = new UnknownCommand(chatTypeFactory);
    }

    @Test
    void execute_InPrivateChat_ShouldReturnPrivateChatMessage() {
        String chatId = "12345";
        TelegramCommandInput input = new TelegramCommandInput(chatId, "testuser", "msg123", new String[]{});

        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        String result = unknownCommand.execute(input);
        assertEquals("❌ Unrecognized command. Use /help to see the available commands.", result);
        verify(chatTypeFactory).isChatType(chatId, ChatType.PRIVATE);
    }

    @Test
    void execute_InNonPrivateChat_ShouldReturnNonPrivateChatMessage() {
        String chatId = "67890";
        TelegramCommandInput input = new TelegramCommandInput(chatId, "testuser", "msg123", new String[]{});

        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        String result = unknownCommand.execute(input);
        String expected = """
                ❌ Unrecognized command.
                ℹ️ Use /help to see the available commands.
                📩 Contact me in private chat for more options!
                """;
        assertEquals(expected, result);
        verify(chatTypeFactory).isChatType(chatId, ChatType.PRIVATE);
    }

    @Test
    void getCommandName_ShouldReturnDefault() {
        String commandName = unknownCommand.getCommandName();
        assertEquals("default", commandName);
    }

    @Test
    void isAllowedInChat_ShouldAlwaysReturnTrue() {
        assertTrue(unknownCommand.isAllowedInChat("12345"));
        assertTrue(unknownCommand.isAllowedInChat("group_chat"));
        assertTrue(unknownCommand.isAllowedInChat(null));
    }
}