package ch.supsi.sporthub.backend.service.api;

/**
 * Interface for handling incoming text-based commands in a chat context.
 * <p>
 * Implementations of this interface process user-issued commands and return a response
 * to be sent back to the user or system.
 * </p>
 */
public interface ICommandHandler {

    /**
     * Handles a command received in a chat and produces a response.
     *
     * @param chatId      the identifier of the chat where the command was received
     * @param senderName  the display name or username of the sender who issued the command
     * @param messageId   the ID of the original message triggering the command (for reference or reply)
     * @param commandText the raw command text to process
     * @return a response string to be sent back as a reply or notification
     */
    String handleCommand(String chatId, String senderName, String messageId, String commandText);
}