package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.Chat;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.service.impl.ChatValidationServiceImpl;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command that handles the registration of a user for notifications and group participation.
 * The user must provide their username and optional group IDs. The command checks if the user is already registered,
 * and either completes their registration or notifies them of any issues.
 * This command is available only in private chats.
 */
@Component
public class StartCommand implements ITelegramCommand {

    private final IChatTypeFactory chatTypeFactory;
    private final IUserChatService userChatService;
    private final ChatValidationServiceImpl chatValidationService;

    /**
     * Constructs a {@link StartCommand} with the provided services.
     *
     * @param userChatService service for handling user and chat interactions.
     * @param chatTypeFactory service for checking chat types.
     * @param chatValidationService service for validating chat IDs and leaderboards.
     */
    public StartCommand(IUserChatService userChatService, IChatTypeFactory chatTypeFactory, ChatValidationServiceImpl chatValidationService) {
        this.userChatService = userChatService;
        this.chatTypeFactory = chatTypeFactory;
        this.chatValidationService = chatValidationService;
    }

    /**
     * Executes the command to register a user for notifications and add them to groups.
     * The user must provide their username and optionally provide group IDs.
     * The command checks if the user is already registered in the chat or group and handles registration accordingly.
     * If the username is not found or the group IDs are invalid, appropriate error messages are returned.
     *
     * @param input The input containing the chat ID, sender's name, and command arguments.
     * @return A message detailing the registration status, including any errors or successful registrations.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();
        String telegramName = input.getSenderName();
        String[] args = input.getArgs();

        if (args.length < 1) {
            return "❌ <b>Error:</b> You must specify your username.\n<b>Correct usage:</b> /start_notification <code>&lt;username&gt; [group_id1 group_id2 ...]</code>";
        }

        String username = args[0];
        User user = userChatService.findUserByUsername(username).orElse(null);
        if (user == null) {
            return "❌ <b>Error:</b> The user <b>" + username + "</b> does not exist on SportHub.\nPlease register on SportHub first, then try again.";
        }

        boolean isPrivate = chatTypeFactory.isChatType(chatId, ChatType.PRIVATE);

        List<Chat> existingChatsWithSameId = userChatService.getChatsByChatId(chatId);
        boolean privateChatAlreadyRegistered = isPrivate && existingChatsWithSameId.stream()
                .anyMatch(chat -> chat.getUser().equals(user));

        if (isPrivate) {
            boolean someoneElseAlreadyRegistered = existingChatsWithSameId.stream()
                    .anyMatch(chat -> !chat.getUser().equals(user));
            if (someoneElseAlreadyRegistered) {
                return "❌ <b>Error:</b> This private chat is already registered with another user.";
            }
        }

        List<String> newGroupIds = new ArrayList<>();
        List<String> alreadyRegisteredGroupIds = new ArrayList<>();
        List<String> invalidGroupIds = new ArrayList<>();
        List<String> addedLeaderboards = new ArrayList<>();

        for (int i = 1; i < args.length; i++) {
            String groupId = args[i];
            if (!groupId.matches("-?\\d+")) {
                invalidGroupIds.add(groupId);
                continue;
            }

            if (!chatValidationService.verifyChatId(groupId)) {
                invalidGroupIds.add(groupId);
                continue;
            }

            List<Chat> chats = userChatService.getChatsByChatId(groupId);
            boolean alreadyRegistered = chats.stream()
                    .anyMatch(chat -> user.equals(chat.getUser()));

            if (alreadyRegistered) {
                alreadyRegisteredGroupIds.add(groupId);
            } else {
                newGroupIds.add(groupId);
            }
        }

        if (isPrivate && !privateChatAlreadyRegistered) {
            Chat privateChat = new Chat();
            privateChat.setChatId(chatId);
            privateChat.setChatType(ChatType.PRIVATE);
            privateChat.setUser(user);
            privateChat.setTelegramUserName(telegramName);
            userChatService.save(privateChat);
        }

        for (String groupId : newGroupIds) {
            Chat groupChat = new Chat();
            groupChat.setChatId(groupId);
            groupChat.setChatType(resolveChatType(groupId));
            groupChat.setUser(user);
            groupChat.setTelegramUserName(telegramName);

            addedLeaderboards.addAll(chatValidationService.updateLeaderboards(groupId, username));
            userChatService.save(groupChat);
        }

        StringBuilder response = new StringBuilder();

        if (!privateChatAlreadyRegistered && isPrivate) {
            response.append("<b>✅ Registration complete!</b>\n\n")
                    .append("📢 You will receive updates in your private chat and the following groups:\n");
        } else if (isPrivate) {
            response.append("⚠️ <b>Attention:</b> You are already registered in this private chat.\n");
            if (!newGroupIds.isEmpty()) {
                response.append("👉 You have added the following new groups:\n");
            }
        } else {
            response.append("<b>✅ Group registration complete!</b>\n");
        }

        List<String> registeredList = new ArrayList<>();
        if (!privateChatAlreadyRegistered && isPrivate) {
            registeredList.add("• <code>" + chatId + "</code>");
        }
        registeredList.addAll(newGroupIds.stream()
                .map(id -> "• <code>" + id + "</code>")
                .toList());

        if (!registeredList.isEmpty()) {
            response.append(String.join("\n", registeredList));
        }

        if (!addedLeaderboards.isEmpty()) {
            response.append("\n\n📊 <b>You have been added to the following leaderboards:</b>\n");
            response.append(addedLeaderboards.stream()
                    .map(name -> "• 🏆 <b>" + name + "</b>")
                    .collect(Collectors.joining("\n")));
        }

        if (!invalidGroupIds.isEmpty()) {
            response.append("\n\n❌ <b>Error:</b> The following group IDs are invalid:\n")
                    .append(invalidGroupIds.stream()
                            .map(id -> "• <code>" + id + "</code>")
                            .collect(Collectors.joining("\n")));
        }

        if (!alreadyRegisteredGroupIds.isEmpty()) {
            response.append("\n⚠️ <b>Attention:</b> You are already registered in the following groups:\n")
                    .append(alreadyRegisteredGroupIds.stream()
                            .map(id -> "• <code>" + id + "</code>")
                            .collect(Collectors.joining("\n")));
        }

        return response.toString();
    }

    /**
     * Resolves the chat type based on the chat ID.
     *
     * @param chatId The ID of the chat.
     * @return The chat type (PRIVATE, GROUP, or CHANNEL).
     */
    private ChatType resolveChatType(String chatId) {
        if (chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)) return ChatType.PRIVATE;
        if (chatTypeFactory.isChatType(chatId, ChatType.GROUP)) return ChatType.GROUP;
        if (chatTypeFactory.isChatType(chatId, ChatType.CHANNEL)) return ChatType.CHANNEL;
        return ChatType.PRIVATE;
    }

    /**
     * Returns the name of the command, which is used to identify it in the Telegram bot.
     *
     * @return The command name, "/start_notification".
     */
    @Override
    public String getCommandName() {
        return "/start_notification";
    }

    /**
     * Determines whether the command can be executed in the given chat.
     * This command can only be executed in private chats.
     *
     * @param chatId The ID of the chat where the command is being used.
     * @return True if the command is allowed in the chat, false otherwise.
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return chatTypeFactory.isChatType(chatId, ChatType.PRIVATE);
    }
}