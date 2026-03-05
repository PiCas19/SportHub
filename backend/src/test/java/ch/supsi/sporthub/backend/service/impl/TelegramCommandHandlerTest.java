package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.service.domain.telegram.commands.CommandFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TelegramCommandHandlerTest {

    private CommandFactory commandFactory;
    private TelegramCommandHandler handler;

    @BeforeEach
    void setUp() {
        commandFactory = mock(CommandFactory.class);
        handler = new TelegramCommandHandler(commandFactory);
    }

    @Test
    void testHandleCommand_NullCommand() {
        String result = handler.handleCommand("123", "user", "1", null);
        assertTrue(result.contains("Unrecognized command"));
    }

    @Test
    void testHandleCommand_EmptyCommand() {
        String result = handler.handleCommand("123", "user", "1", "   ");
        assertTrue(result.contains("Unrecognized command"));
    }

    @Test
    void testHandleCommand_UnknownCommand() {
        when(commandFactory.getCommand("/unknown"))
                .thenReturn(null);
        String result = handler.handleCommand("123", "user", "1", "/unknown");
        assertTrue(result.contains("Unrecognized command"));
    }

    @Test
    void testHandleCommand_NotAllowedInChat() {
        ITelegramCommand command = mock(ITelegramCommand.class);
        when(commandFactory.getCommand("/cmd"))
                .thenReturn(command);
        when(command.isAllowedInChat("123"))
                .thenReturn(false);

        String result = handler.handleCommand("123", "user", "1", "/cmd");
        assertTrue(result.contains("⚠️ This command can be used in this chat. See the /help command"));
    }

    @Test
    void testHandleCommand_Valid() {
        ITelegramCommand command = mock(ITelegramCommand.class);
        when(commandFactory.getCommand("/start"))
                .thenReturn(command);
        when(command.isAllowedInChat("123"))
                .thenReturn(true);
        when(command.execute(any(TelegramCommandInput.class)))
                .thenReturn("✅ OK");

        String result = handler.handleCommand("123", "user", "1", "/start");
        assertEquals("✅ OK", result);
    }

    @Test
    void testHandleCommand_WithArguments() {
        ITelegramCommand command = mock(ITelegramCommand.class);
        when(commandFactory.getCommand("/set"))
                .thenReturn(command);
        when(command.isAllowedInChat("123"))
                .thenReturn(true);
        when(command.execute(any(TelegramCommandInput.class)))
                .thenReturn("Success with args");

        String result = handler.handleCommand("123", "user", "1", "/set arg1 arg2");
        assertEquals("Success with args", result);
    }
}