package ch.supsi.sporthub.backend.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing and extracting data from Telegram updates.
 * This class provides methods to extract various details from the Telegram update JSON structure.
 */
public class TelegramUpdateParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extracts the chat ID from the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return The chat ID or {@code null} if not found or invalid.
     */
    public static String getChatId(String update) {
        if (update == null || update.isEmpty()) {
            return null;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            return jsonNode.path("message").path("chat").path("id").asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the file path from a response to a "get file" request.
     *
     * @param json The JSON response from Telegram's getFile API.
     * @return The file path or {@code null} if not found or invalid.
     */
    public static String getFilePathFromGetFileResponse(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            return jsonNode.path("result").path("file_path").asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the number of members in a chat.
     *
     * @param update The Telegram update JSON string.
     * @return The number of chat members or 0 if not found or invalid.
     */
    public static int getChatMemberCount(String update) {
        if (update == null || update.isEmpty()) {
            return 0;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            return jsonNode.path("result").asInt();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Extracts the chat title from the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return The chat title or {@code null} if not found or invalid.
     */
    public static String getChatTitleFromResponse(String update) {
        if (update == null || update.isEmpty()) {
            return null;
        }
        try {
            JsonNode chatNode = objectMapper.readTree(update);
            JsonNode resultNode = chatNode.path("result");
            return resultNode.path("title").asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the chat type (private, group, or channel) from the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return The chat type or {@code null} if not found or invalid.
     */
    public static String getChatType(String update) {
        if (update == null || update.isEmpty()) {
            return null;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            return jsonNode.path("result").path("type").asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the chat description from the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return The chat description or {@code null} if not found or invalid.
     */
    public static String getChatDescription(String update) {
        if (update == null || update.isEmpty()) {
            return null;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            return jsonNode.path("result").path("description").asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the small chat photo file ID from the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return The small photo file ID or {@code null} if not found or invalid.
     */
    public static String getChatPhotoSmall(String update) {
        if (update == null || update.isEmpty()) {
            return null;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            return jsonNode.path("result").path("photo").path("small_file_id").asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the big chat photo file ID from the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return The big photo file ID or {@code null} if not found or invalid.
     */
    public static String getChatPhotoBig(String update) {
        if (update == null || update.isEmpty()) {
            return null;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            return jsonNode.path("result").path("photo").path("big_file_id").asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the list of chat administrators from the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return A list of admin usernames (or names if no username) or an empty list if not found or invalid.
     */
    public static List<String> getChatAdministrators(String update) {
        List<String> admins = new ArrayList<>();
        if (update == null || update.isEmpty()) {
            return admins;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            JsonNode resultArray = jsonNode.path("result");
            if (resultArray.isArray()) {
                for (JsonNode adminNode : resultArray) {
                    JsonNode userNode = adminNode.path("user");
                    String username = userNode.path("username").asText();
                    if (username == null || username.isEmpty()) {
                        String firstName = userNode.path("first_name").asText();
                        admins.add(firstName + " (no link available)");
                    } else {
                        admins.add("https://t.me/" + username);
                    }
                }
            }
        } catch (Exception e) {
            return List.of();
        }
        return admins;
    }

    /**
     * Extracts the chat title from the message within the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return The chat title or {@code null} if not found or invalid.
     */
    public static String getChatTitle(String update) {
        if (update == null || update.isEmpty()) {
            return null;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            return jsonNode.path("message").path("chat").path("title").asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the message text from the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return The message text or {@code null} if not found or invalid.
     */
    public static String getMessageText(String update) {
        if (update == null || update.isEmpty()) {
            return null;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            return jsonNode.path("message").path("text").asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if the bot has been added to a group.
     *
     * @param update The Telegram update JSON string.
     * @return {@code true} if the bot is added, {@code false} otherwise.
     */
    public static boolean isBotAddedToGroup(String update) {
        if (update == null || update.isEmpty()) {
            return false;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            JsonNode newChatMember = jsonNode.path("message").path("new_chat_member");
            return newChatMember.has("is_bot") && newChatMember.get("is_bot").asBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the update is from a private chat.
     *
     * @param update The Telegram update JSON string.
     * @return {@code true} if it's a private chat, {@code false} otherwise.
     */
    public static boolean isPrivateChat(String update) {
        if (update == null || update.isEmpty()) {
            return false;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            String chatType = jsonNode.path("message").path("chat").path("type").asText();
            return "private".equals(chatType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the username from the result node of the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return The username or "unknown" if not found or invalid.
     */
    public static String getTelegramUsernameFromResult(String update) {
        if (update == null || update.isEmpty()) {
            return "unknown";
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            JsonNode resultNode = jsonNode.path("result");
            String username = resultNode.path("username").asText();
            return username.isEmpty() ? "unknown" : username;
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Extracts the username from the message sender in the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return The username or "unknown" if not found or invalid.
     */
    public static String getTelegramUsername(String update) {
        if (update == null || update.isEmpty()) {
            return "unknown";
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            String username = jsonNode.path("message").path("from").path("username").asText();
            return username.isEmpty() ? "unknown" : username;
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Extracts the message ID from the update JSON.
     *
     * @param update The Telegram update JSON string.
     * @return The message ID or {@code null} if not found or invalid.
     */
    public static String getMessageId(String update) {
        if (update == null || update.isEmpty()) {
            return null;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(update);
            return jsonNode.path("message").path("message_id").asText();
        } catch (Exception e) {
            return null;
        }
    }
}