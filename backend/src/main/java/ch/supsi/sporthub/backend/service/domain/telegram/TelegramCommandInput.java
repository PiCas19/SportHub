package ch.supsi.sporthub.backend.service.domain.telegram;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents the input for a Telegram command.
 * This class holds the necessary data for processing a command, including the chat ID, sender's name,
 * the message ID from Telegram, and any arguments passed with the command.
 */
@Data
@AllArgsConstructor
public class TelegramCommandInput {
    private final String chatId;
    private final String senderName;
    private final String telegramMessageId;
    private final String[] args;
}