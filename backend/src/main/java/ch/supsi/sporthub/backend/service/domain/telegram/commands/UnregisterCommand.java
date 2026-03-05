package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.Chat;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.api.ILeaderboardService;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command that handles the unregistration of a user from private chats and groups.
 * The user can unregister from specific groups or all groups at once. This command also removes the user from the associated leaderboards.
 * This command is available in all chat types (private, group, or channel).
 */
@Component
public class UnregisterCommand implements ITelegramCommand {

    private final IUserChatService userChatService;
    private final ILeaderboardService leaderboardService;
    private final IChatTypeFactory chatTypeFactory;

    /**
     * Constructs a {@link UnregisterCommand} with the provided services.
     *
     * @param userChatService service for handling user and chat interactions.
     * @param leaderboardService service for managing leaderboards.
     * @param chatTypeFactory service for determining the chat type.
     */
    public UnregisterCommand(IUserChatService userChatService,
                             ILeaderboardService leaderboardService,
                             IChatTypeFactory chatTypeFactory) {
        this.userChatService = userChatService;
        this.leaderboardService = leaderboardService;
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Executes the unregistration process for the user.
     * The user can unregister from their private chat and/or from specific groups.
     * If no valid user is found or the user is not registered, an error message is returned.
     *
     * @param input The input containing the chat ID, sender's name, and command arguments.
     * @return A message detailing the unregistration status, including any groups or private chats the user was removed from.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();
        String[] args = input.getArgs();

        User user = null;
        if (args.length >= 2 && Arrays.asList(args).contains("all") && !args[0].equalsIgnoreCase("all")) {
            String providedUsername = args[0];
            Optional<User> userOpt = userChatService.findUserByUsername(providedUsername);
            if (userOpt.isPresent()) {
                user = userOpt.get();
            }
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        if (user == null) {
            Optional<Chat> privateChatOpt = userChatService.getChatByChatId(chatId);
            user = privateChatOpt.map(Chat::getUser).orElse(null);
        }

        if (user == null) {
            return "❌ You are not registered or no valid user could be found for this operation.";
        }

        boolean hasPrivateChat = userChatService.hasPrivateChat(chatId, user);
        return unregisterUser(user, chatId, args, hasPrivateChat);
    }

    /**
     * Unregisters the user from the specified chats (private and/or group).
     * If "all" is specified in the arguments, the user is unregistered from both the private chat and all associated groups.
     *
     * @param user The user to unregister.
     * @param chatId The chat ID from which the user is unregistering.
     * @param args The command arguments, including group IDs or "all".
     * @param hasPrivateChat Whether the user has a private chat registered.
     * @return A message detailing which chats the user has been removed from.
     */
    private String unregisterUser(User user, String chatId, String[] args, boolean hasPrivateChat) {
        String registeredUsername = user.getUsername();
        boolean removedPrivate = false;
        List<String> removedGroups = new ArrayList<>();
        List<String> notFoundGroups = new ArrayList<>();

        boolean isAll = Arrays.asList(args).contains("all");
        if (isAll) {
            if (hasPrivateChat) {
                userChatService.removePrivateChat(chatId);
                removedPrivate = true;
            }

            List<Chat> groupChats = userChatService.getChatsForUser(user).stream()
                    .filter(c -> chatTypeFactory.isChatType(c.getChatId(), ChatType.GROUP))
                    .toList();

            for (Chat groupChat : groupChats) {
                String groupId = groupChat.getChatId();
                leaderboardService.removeUserFromLeaderboard(groupId, registeredUsername);
                userChatService.removeGroupChat(groupChat);
                removedGroups.add(groupId);
            }

        } else {
            for (String arg : args) {
                if (arg.equalsIgnoreCase(registeredUsername)) {
                    userChatService.removePrivateChat(chatId);
                    removedPrivate = true;
                    removeUserFromGroups(user, registeredUsername, removedGroups);
                    continue;
                }

                if (arg.matches("-?\\d+")) {
                    boolean isRemoved = userChatService.removeGroupChatForUser(user, arg);
                    if (isRemoved) {
                        leaderboardService.removeUserFromLeaderboard(arg, registeredUsername);
                        removedGroups.add(arg);
                    } else {
                        notFoundGroups.add(arg);
                    }
                } else {
                    notFoundGroups.add(arg);
                }
            }
        }

        StringBuilder response = new StringBuilder();

        if (removedPrivate) {
            response.append("✅ You are no longer registered in your private chat.\n");
        }

        if (!removedGroups.isEmpty()) {
            response.append("\n📤 <b>You have been removed from the following groups:</b>\n");
            response.append(removedGroups.stream()
                    .map(id -> "• <code>" + id + "</code>")
                    .collect(Collectors.joining("\n")));
        }

        if (!notFoundGroups.isEmpty()) {
            response.append("\n\n⚠️ <b>The following values were not recognized or not linked to you:</b>\n");
            response.append(notFoundGroups.stream()
                    .map(id -> "• <code>" + id + "</code>")
                    .collect(Collectors.joining("\n")));
        }

        if (!removedPrivate && removedGroups.isEmpty()) {
            response.append("ℹ️ No registrations were removed.");
        }

        return response.toString();
    }

    /**
     * Removes the user from all groups they are registered in.
     *
     * @param user The user to remove.
     * @param username The username to be removed.
     * @param removedGroups The list to track removed groups.
     */
    private void removeUserFromGroups(User user, String username, List<String> removedGroups) {
        List<Chat> groupChats = userChatService.getChatsForUser(user);
        for (Chat groupChat : groupChats) {
            String groupId = groupChat.getChatId();
            leaderboardService.removeUserFromLeaderboard(groupId, username);
            userChatService.removeGroupChat(groupChat);
            removedGroups.add(groupId);
        }
    }

    /**
     * Returns the name of the command, which is used to identify it in the Telegram bot.
     *
     * @return The command name, "/unregister".
     */
    @Override
    public String getCommandName() {
        return "/unregister";
    }

    /**
     * Determines whether the command can be executed in the given chat.
     * This command can be executed in any chat type.
     *
     * @param chatId The ID of the chat where the command is being used.
     * @return True, as this command can be executed in any chat type.
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return true;
    }
}