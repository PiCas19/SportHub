package ch.supsi.sporthub.backend.dto.response.auth;

import ch.supsi.sporthub.backend.dto.response.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response DTO for chat-related operations such as creation, deletion, or membership updates.
 * <p>
 * Extends {@link Response} to include additional status indicators relevant to chat management actions.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ChatManagementResponse extends Response {
    private boolean success;
    private int code;

    /**
     * Constructs a new ChatManagementResponse with the specified message, success status, and code.
     *
     * @param message a human-readable message describing the result of the operation
     * @param success true if the operation succeeded, false otherwise
     * @param code a numeric code representing the result
     */
    public ChatManagementResponse(String message, boolean success, int code) {
        super(message);
        this.success = success;
        this.code = code;
    }

}