package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.config.TelegramProperties;
import ch.supsi.sporthub.backend.dto.response.telegram.ChatInfoResponse;
import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import ch.supsi.sporthub.backend.service.api.ICommandHandler;
import ch.supsi.sporthub.backend.service.api.ILeaderboardService;
import ch.supsi.sporthub.backend.service.api.ITelegramService;
import ch.supsi.sporthub.backend.utils.TelegramUpdateParser;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the ITelegramService interface that handles interactions with the Telegram API.
 * This service manages chat updates, sends messages, retrieves chat information, and handles user commands.
 */
@Service
public class TelegramServiceImpl implements ITelegramService {

    private final IBaseApiService baseApiService;
    private final TelegramProperties telegramProperties;
    private final ICommandHandler commandHandler;
    private final ILeaderboardService leaderboardService;
    private final Map<String, String> groupChatIds = new ConcurrentHashMap<>();
    private final ChatValidationServiceImpl chatValidationService;

    /**
     * Constructor for the TelegramServiceImpl class.
     * Initializes the service with the required dependencies to interact with Telegram and manage chat interactions.
     *
     * @param baseApiService       The base API service for making HTTP requests.
     * @param telegramProperties   The configuration properties for Telegram.
     * @param leaderboardService   The service for managing leaderboards.
     * @param commandHandler       The handler for processing Telegram commands.
     * @param chatValidationService The service for validating chat IDs.
     */
    public TelegramServiceImpl(IBaseApiService baseApiService, TelegramProperties telegramProperties,  ILeaderboardService leaderboardService, ICommandHandler commandHandler, ChatValidationServiceImpl chatValidationService) {
        this.baseApiService = baseApiService;
        this.telegramProperties = telegramProperties;
        this.commandHandler = commandHandler;
        this.leaderboardService = leaderboardService;
        this.chatValidationService = chatValidationService;
    }

    /**
     * Sends a message to a specific chat on Telegram.
     *
     * @param chatId The chat ID where the message should be sent.
     * @param text   The text content of the message.
     * @return A response indicating the success or failure of the message send operation.
     */
    @Override
    public String sendMessage(String chatId, String text) {
        if (chatId == null || chatId.isEmpty()) {
            return "Error: chatId is missing";
        }
        if (text == null || text.isEmpty()) {
            System.out.println("DEBUG - Error: text is empty.");
            return "Error: text is empty";
        }

        String endpoint = "/bot" + telegramProperties.getBotToken() + "/sendMessage";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("chat_id", chatId);
        requestBody.put("text", text);
        requestBody.put("parse_mode", "HTML");

        try {
            ResponseEntity<String> response = baseApiService.postJson(
                    telegramProperties.getBaseUrl(), endpoint, requestBody, String.class
            );
            return response.getBody();
        } catch (WebClientResponseException e) {
            return "Failed to send message. Error: " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Failed to send message due to unexpected error: " + e.getMessage();
        }
    }


    /**
     * Retrieves updates from Telegram related to messages or events involving the bot.
     *
     * @return A string response containing the raw update data from Telegram.
     */
    @Override
    public String getChatUpdates() {
        String endpoint = "/bot" + telegramProperties.getBotToken() + "/getUpdates";
        ResponseEntity<String> response = baseApiService.get(
                telegramProperties.getBaseUrl(),
                endpoint,
                String.class
        );
        return response.getBody();
    }

    /**
     * Retrieves the bot's link to allow users to open the bot on Telegram.
     *
     * @return A string containing the link to the bot on Telegram.
     */
    @Override
    public String getBotLink() {
        return telegramProperties.getBotUrlLink() + telegramProperties.getBotUsername();
    }

    /**
     * Retrieves the bot's group invite link to allow users to join the group.
     *
     * @return A string containing the invite link to the bot's group on Telegram.
     */
    @Override
    public String getGroupInviteLink() {
        return telegramProperties.getBotUrlLink() + telegramProperties.getBotUsername() + "?startgroup=true";
    }

    /**
     * Handles an incoming update from Telegram, parses the command, and takes the appropriate action.
     *
     * @param update The raw update data received from Telegram.
     * @return A string response indicating the result of handling the update.
     */
    @Override
    public String handleUpdate(String update) {
        String chatId = TelegramUpdateParser.getChatId(update);
        String senderName = TelegramUpdateParser.getTelegramUsername(update);
        String text = TelegramUpdateParser.getMessageText(update);
        String chatTitle = TelegramUpdateParser.getChatTitle(update);
        String messageId = TelegramUpdateParser.getMessageId(update);
        if (chatId == null) {
            return "Error: chatId is missing.";
        }

        if (TelegramUpdateParser.isBotAddedToGroup(update)) {
            groupChatIds.put(chatId, chatId);
            createLeaderboardIfNotExists(chatId,chatTitle);
            return sendWelcomeMessageToGroup(chatId, chatTitle);
        }

        if (TelegramUpdateParser.isPrivateChat(update) && "/start".equalsIgnoreCase(text.trim())) {
            return sendRegistrationInstructions(chatId);
        }

        String response = commandHandler.handleCommand(chatId, senderName, messageId, text);
        return sendMessage(chatId, response);
    }

    /**
     * Retrieves information about a specific chat, including title, description, member count, and admins.
     *
     * @param chatId The chat ID for which to retrieve information.
     * @return A ChatInfoResponse containing details about the chat.
     */
    @Override
    public ChatInfoResponse getChatInfo(String chatId) {
        String endpointGetChat = "/bot" + telegramProperties.getBotToken() + "/getChat?chat_id=" + chatId;
        ResponseEntity<String> responseChat = baseApiService.get(telegramProperties.getBaseUrl(), endpointGetChat, String.class);
        String endpointGetCount = "/bot" + telegramProperties.getBotToken() + "/getChatMemberCount?chat_id=" + chatId;
        ResponseEntity<String> responseCount = baseApiService.get(telegramProperties.getBaseUrl(), endpointGetCount, String.class);
        String endpointGetAdmins = "/bot" + telegramProperties.getBotToken() + "/getChatAdministrators?chat_id=" + chatId;
        ResponseEntity<String> responseAdmins = baseApiService.get(telegramProperties.getBaseUrl(), endpointGetAdmins, String.class);
        String chatTitle = TelegramUpdateParser.getChatTitleFromResponse(responseChat.getBody());
        int memberCount = TelegramUpdateParser.getChatMemberCount(responseCount.getBody());
        String chatType = TelegramUpdateParser.getChatType(responseChat.getBody());
        String description  = TelegramUpdateParser.getChatDescription(responseChat.getBody());
        String photoSmall   = TelegramUpdateParser.getChatPhotoSmall(responseChat.getBody());
        String photoBig     = TelegramUpdateParser.getChatPhotoBig(responseChat.getBody());

        String photoSmallUrl = getFileUrlFromTelegram(photoSmall);
        String photoBigUrl   = getFileUrlFromTelegram(photoBig);

        List<String> admins = TelegramUpdateParser.getChatAdministrators(responseAdmins.getBody());
        return new ChatInfoResponse("Chat info", chatTitle, memberCount, chatId, 0L, description, photoSmallUrl, photoBigUrl, admins, chatType);
    }

    /**
     * Retrieves the file URL from Telegram based on the provided file ID.
     *
     * @param fileId The file ID to retrieve the file URL.
     * @return A string URL pointing to the file hosted by Telegram.
     */
    private String getFileUrlFromTelegram(String fileId) {
        if (fileId == null || fileId.isEmpty()) {
            return null;
        }

        try {
            String endpoint = "/bot" + telegramProperties.getBotToken() + "/getFile?file_id=" + fileId;
            ResponseEntity<String> response = baseApiService.get(
                    "https://api.telegram.org", endpoint, String.class
            );
            String filePath = TelegramUpdateParser.getFilePathFromGetFileResponse(response.getBody());

            if (filePath != null && !filePath.isEmpty()) {
                return "https://api.telegram.org/file/bot" + telegramProperties.getBotToken() + "/" + filePath;
            }
        } catch (Exception e) {
            System.err.println("Errore nel recupero dell'URL per file_id " + fileId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Verifies the validity of a given chat ID using the chat validation service.
     *
     * @param chatId The chat ID to validate.
     * @return true if the chat ID is valid, false otherwise.
     */
    @Override
    public boolean verifyChatId(String chatId) {
       return chatValidationService.verifyChatId(chatId);
    }

    /**
     * Retrieves the Telegram username of a specific chat based on its chat ID.
     *
     * @param chatId The chat ID for which to retrieve the username.
     * @return The Telegram username associated with the chat, or null if not found.
     */
    @Override
    public String getTelegramUsername(String chatId) {
        if (chatId == null || chatId.isEmpty()) {
            return null;
        }
        String endpoint = "/bot" + telegramProperties.getBotToken() + "/getChat?chat_id=" + chatId;
        try {
            ResponseEntity<String> response = baseApiService.get(telegramProperties.getBaseUrl(), endpoint, String.class);
            String responseBody = response.getBody();
            return TelegramUpdateParser.getTelegramUsernameFromResult(responseBody);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Sends a welcome message to a group when the bot is added to the group.
     *
     * @param chatId   The chat ID of the group.
     * @param chatTitle The title of the group.
     * @return A string response indicating the result of sending the welcome message.
     */
    private String sendWelcomeMessageToGroup(String chatId, String chatTitle) {
        String botUsername = telegramProperties.getBotUsername();
        String privateChatLink = "https://t.me/" + botUsername;

        String message = "<b>👋 Hello everyone!</b>\n\n"
                + "I am the <b>SportHub Bot! 🏆</b> and I can now send updates "
                + "about members' sports activities.\n\n"
                + "✅ <b>To register and receive notifications in this group:</b>\n"
                + "1️⃣ Start a private chat with me 👉 <a href=\"" + privateChatLink + "\">🔗 Click here</a>\n"
                + "2️⃣ Once in private chat, use the command:\n"
                + "<code>/start_notification &lt;username&gt; " + chatId + "</code>\n\n"
                + "📌 <b>This group's ID:</b>\n"
                + "<code>" + chatId + "</code>\n\n"
                + "ℹ️ If you want to receive notifications in multiple groups, you can register more than one!\n"
                + "📩 If you have any issues, contact SportHub support or use /help to see available commands.\n\n"
                + "🏆 A general leaderboard has been created for this group: <b>" + chatTitle + "</b>.\n"
                + "To participate, make sure you are registered.";


        return sendMessage(chatId, message);
    }

    /**
     * Sends the registration instructions to a user in private chat.
     *
     * @param chatId The chat ID of the user.
     * @return A string response containing the registration instructions.
     */
    private String sendRegistrationInstructions(String chatId) {
        String message = "<b>🏆 Welcome to SportHub!</b>\n\n"
                + "To receive notifications about your sports activities on Telegram, follow these steps:\n\n"
                + "1️⃣ <b>Register on SportHub</b> if you haven't already.\n\n"
                + "2️⃣ <b>Use the command to connect your SportHub account to Telegram:</b>\n"
                + "/start_notification <code>&lt;username&gt; [group_chat_id1 group_chat_id2 ...]</code>\n\n"
                + "📌 <b>Registration examples:</b>\n\n"
                + "✅ <b>To receive notifications in private chat only:</b>\n"
                + "<code>/start_notification john_doe</code>\n\n"
                + "✅ <b>To also receive notifications in a specific group:</b>\n"
                + "<code>/start_notification john_doe -123456789</code>\n\n"
                + "ℹ️ <b>If you want to receive notifications in multiple groups</b>, simply add more group chat IDs separated by a space:\n"
                + "<code>/start_notification john_doe -123456789 -987654321</code>\n\n"
                + "ℹ️ After registration, you will automatically receive notifications about your activities! 🏃‍♂️🏋️‍♀️🚴\n\n"
                + "📩 If you have any issues, contact SportHub support or use /help to see available commands.";

        return sendMessage(chatId, message);
    }

    /**
     * Creates a leaderboard for the group chat if it doesn't already exist.
     *
     * @param chatId   The chat ID of the group.
     * @param chatTitle The title of the group.
     */
    private void createLeaderboardIfNotExists(String chatId, String chatTitle) {
        if (!leaderboardService.existsByName(chatTitle)) {
            leaderboardService.createLeaderboard(chatId, chatTitle);
        }
    }

}