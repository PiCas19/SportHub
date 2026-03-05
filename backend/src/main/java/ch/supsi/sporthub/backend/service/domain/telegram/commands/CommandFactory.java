package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory class for managing and retrieving Telegram commands.
 * This class stores a collection of available commands and provides a mechanism to retrieve a specific command by its name.
 * If the command is not found, a default (unknown) command is returned.
 */
@Component
public class CommandFactory {

    private final Map<String,ITelegramCommand> commands = new HashMap<>();
    private final ITelegramCommand defaultCommand;

    /**
     * Constructs a new CommandFactory with the given list of commands and the default unknown command.
     *
     * @param commandList A list of commands that should be available in the system.
     * @param unknownCommand The command to return when an unknown command is requested.
     */
    public CommandFactory(List<ITelegramCommand> commandList, UnknownCommand unknownCommand) {
        this.defaultCommand = unknownCommand;

        for (ITelegramCommand command : commandList) {
            commands.put(command.getCommandName(), command);
        }
    }

    /**
     * Retrieves the command associated with the given command name.
     * If the command name is not found, the default command is returned.
     *
     * @param commandName The name of the command to retrieve.
     * @return The command associated with the given name, or the default command if not found.
     */
    public ITelegramCommand getCommand(String commandName) {
        return commands.getOrDefault(commandName, defaultCommand);
    }
}