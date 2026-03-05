package ch.supsi.sporthub.backend.dto.request.auth;

import ch.supsi.sporthub.backend.dto.request.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AddChatRequest represents the request body used to add a chat to a user's account.
 * It extends the {@link Request} class and includes a `chatId` field, which is the unique identifier of the chat.
 * This request is typically used when associating a Telegram chat with the user in the system.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddChatRequest extends Request {
    private String chatId;
}