package ch.supsi.sporthub.backend.dto.response.telegram;


import ch.supsi.sporthub.backend.dto.response.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Response class that encapsulates detailed information about a Telegram chat.
 * <p>
 * This response includes metadata such as chat title, ID, member count, photos,
 * list of administrators, and the chat type (e.g., group, supergroup, channel).
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatInfoResponse extends Response {
    private String title;
    private int memberCount;
    private String chatId;
    private Long id;
    private String chatType;
    private String description;
    private String photoBig;
    private String photoSmall;
    private List<String> admins;

    /**
     * Constructs a new {@code ChatInfoResponse} with all available chat information.
     *
     * @param message      response message or status
     * @param title        title of the chat
     * @param memberCount  number of members in the chat
     * @param chatId       unique string identifier for the chat
     * @param id           Telegram-specific chat ID
     * @param description  optional description of the chat
     * @param photoSmall   small profile picture reference
     * @param photoBig     large profile picture reference
     * @param admins       list of admin usernames or IDs
     * @param chatType     type of the chat (e.g., group, supergroup, channel)
     */
    public ChatInfoResponse(String message, String title, int memberCount, String chatId, Long id, String description, String photoSmall, String photoBig, List<String> admins, String chatType) {
        super(message);
        this.title = title;
        this.memberCount = memberCount;
        this.chatId = chatId;
        this.id = id;
        this.description = description;
        this.photoSmall = photoSmall;
        this.photoBig = photoBig;
        this.admins = admins;
        this.chatType = chatType;
    }
}