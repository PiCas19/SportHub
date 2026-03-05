package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.Chat;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Command that displays the list of registered chats for a user in a private chat.
 * The command allows a user to see all the chats they have registered for, either by providing a username
 * or by using the command without arguments to check their own registration.
 * This command is only available in private chats.
 */
@Component
public class ShowRegisteredChatsCommand implements ITelegramCommand {

    private final IChatTypeFactory chatTypeFactory;
    private final IUserChatService userChatService;

    /**
     * Constructs a {@link ShowRegisteredChatsCommand} with the provided services.
     *
     * @param userChatService service for handling user and chat interactions.
     * @param chatTypeFactory service for checking chat types.
     */
    public ShowRegisteredChatsCommand(IUserChatService userChatService, IChatTypeFactory chatTypeFactory) {
        this.userChatService = userChatService;
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Executes the command to retrieve and display the list of registered chats for the user.
     * The command checks if a username is provided in the arguments, and retrieves the registered chats for that user.
     * If no username is provided, it fetches the registered chats for the user from the current chat.
     *
     * @param input The input containing the chat ID and command arguments.
     * @return A message listing the registered chats for the user or an error message if no registered chats are found.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();
        String[] args = input.getArgs();

        User user;

        if (args.length == 1) {
            String usernameArg = args[0];
            user = userChatService.findUserInChatByUsername(chatId, usernameArg)
                    .orElseThrow(() -> new RuntimeException("❌ No user found with username <b>" + usernameArg + "</b>."));
        } else {
            user = userChatService.getUserFromChat(chatId);
            if (user == null) {
                return "❌ You are not registered in this chat.\nUse <code>/show_registered_chats &lt;your_username&gt;</code> if your private chat was removed.";
            }
        }

        List<Chat> registeredChats = userChatService.getChatsForUser(user);
        if (registeredChats.isEmpty()) {
            return "ℹ️ You have no registered chats.";
        }

        String chatList = registeredChats.stream()
                .map(chat -> "• <code>" + chat.getChatId() + "</code> (" + chat.getChatType().name() + ")")
                .collect(Collectors.joining("\n"));

        return "📜 <b>Registered chats for " + user.getUsername() + ":</b>\n\n" + chatList;
    }

    /**
     * Returns the name of the command, which is used to identify it in the Telegram bot.
     *
     * @return The command name, "/show_registered_chats".
     */
    @Override
    public String getCommandName() {
        return "/show_registered_chats";
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
