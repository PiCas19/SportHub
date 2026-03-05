package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

public class CommandFactoryTest {

    private CommandFactory commandFactory;
    private ITelegramCommand mockCommand1;
    private ITelegramCommand mockCommand2;
    private UnknownCommand unknownCommand;

    private IChatTypeFactory mockChatTypeFactory;

    @BeforeEach
    void setUp() {
        mockChatTypeFactory = mock(IChatTypeFactory.class);
        mockCommand1 = mock(ITelegramCommand.class);
        mockCommand2 = mock(ITelegramCommand.class);

        unknownCommand = new UnknownCommand(mockChatTypeFactory);
        when(mockCommand1.getCommandName()).thenReturn("command1");
        when(mockCommand2.getCommandName()).thenReturn("command2");
        List<ITelegramCommand> commandList = Arrays.asList(mockCommand1, mockCommand2);
        commandFactory = new CommandFactory(commandList, unknownCommand);
    }

    @Test
    void testConstructorPopulatesCommands() {
        assertNotNull(commandFactory);
        assertNotNull(commandFactory.getCommand("command1"));
        assertNotNull(commandFactory.getCommand("command2"));
        assertSame(unknownCommand, commandFactory.getCommand("nonExistingCommand"));
    }

    @Test
    void testGetCommandWithExistingCommand() {
        ITelegramCommand command = commandFactory.getCommand("command1");
        assertSame(mockCommand1, command, "Should return the correct command for command1");
    }

    @Test
    void testGetCommandWithNonExistingCommand() {
        ITelegramCommand command = commandFactory.getCommand("nonExistingCommand");
        assertSame(unknownCommand, command, "Should return the default unknownCommand for non-existing commands");
    }
}
