package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.springframework.stereotype.Component;

/**
 * Command that provides a help menu with available commands for private, group, and channel chats.
 * This command customizes the help message based on the type of chat the command is invoked in.
 */
@Component
public class HelpCommand implements ITelegramCommand {

    private final IChatTypeFactory chatTypeFactory;

    /**
     * Constructs a {@link HelpCommand} with the provided {@link IChatTypeFactory}.
     *
     * @param chatTypeFactory service for determining the chat type (private, group, or channel).
     */
    public HelpCommand(IChatTypeFactory chatTypeFactory) {
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Executes the command to display the appropriate help message based on the chat type.
     * It provides different help messages for private chats, group chats, and channels.
     *
     * @param input The input containing the command arguments and chat ID.
     * @return A help message specific to the chat type (private, group, or channel).
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();

        if (chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)) {
            return getPrivateChatHelp();
        } else if (chatTypeFactory.isChatType(chatId, ChatType.GROUP) || chatTypeFactory.isChatType(chatId, ChatType.CHANNEL)) {
            return getGroupChatHelp();
        } else {
            return "❌ Unknown chat type.";
        }
    }

    /**
     * Returns the help message for private chats.
     * This message includes personal commands that users can execute in private chats.
     *
     * @return The help message for private chats.
     */
    private String getPrivateChatHelp() {
        return """
    <b>🏆 Personal Commands (Private Chat)</b>
    
    🔔 /start_notification - Register for notifications
    <b>Usage:</b> <code>/start_notification &lt;username&gt; [group_chat_id1 group_chat_id2 ...]</code>
    <b>Example:</b> <code>/start_notification john_doe -123456789 -987654321</code>
    
    ❌ /unregister - Remove your registration
    <b>Usage:</b>
    • <code>/unregister &lt;your_username&gt;</code> – remove from private chat
    • <code>/unregister &lt;group_id&gt;</code> – remove from specific group
    • <code>/unregister all</code> – remove from private and all groups

    📜 /show_registered_chats - Show all registered chats
    <b>Usage:</b> <code>/show_registered_chats [username]</code>
    
    📈 /show_activities - Show your Strava activities (all or filtered)
    <b>Usage:</b> <code>/show_activities [key=value ...]</code>
    <b>Filters:</b>
    • <code>min</code> - Minimum distance (km)
    • <code>max</code> - Maximum distance (km)
    • <code>duration</code> - Minimum duration (min)
    • <code>type</code> - Activity type (e.g., run, ride)
    • <code>count</code> - Number of latest activities to show
    • <code>format</code> - Custom summary format
    <b>Example:</b> <code>/show_activities min=5 max=20 count=3</code>
    
    🎯 /set_goal - Set your personal goal
    <b>Usage:</b> <code>/set_goal target=&lt;value&gt; type=&lt;DISTANCE|CALORIES|DURATION|ACTIVITIES&gt; period=&lt;DAILY|WEEKLY|MONTHLY|QUARTERLY|YEARLY&gt; sport=&lt;SPORT_TYPE&gt;</code>
    <b>Example:</b> <code>/set_goal target=10 type=DISTANCE period=WEEKLY sport=RUN</code>
    
    📊 /show_goals - Show your current goals
    <b>Usage:</b> <code>/show_goals</code>
 
    ❌ /delete_goal - Delete a specific goal
    <b>Usage:</b> <code>/delete_goal id=&lt;goal_id&gt;</code>
    <b>Example:</b> <code>/delete_goal id=1</code>

    ℹ️ /help - Show this menu
    <b>Usage:</b> <code>/help</code>
    """;
    }

    /**
     * Returns the help message for group chats and channels.
     * This message includes commands that are available in group chats and channels, such as competition-related commands.
     *
     * @return The help message for group chats and channels.
     */
    private String getGroupChatHelp() {
        return """
    <b>📢 Group & Channel Commands</b>

    🏆 /show_leaderboard - Show the current leaderboard
    <b>Usage:</b> <code>/show_leaderboard</code>

    🏁 /competition_history - Show past competitions
    <b>Usage:</b> <code>/competition_history</code>

    🎯 /create_competition - Create a new competition
    <b>Usage:</b> <code>/create_competition name="Name" target=100 goalType=DISTANCE endDate=YYYY-MM-DD sportType=RUN max=50 deadline=YYYY-MM-DD</code>
    <b>Example:</b> <code>/create_competition name="Spring Run" target=100 goalType=DISTANCE endDate=2025-04-30 sportType=RUN max=50 deadline=2025-04-20</code>

    ✅ /join_competition - Join a competition
    <b>Usage:</b> <code>/join_competition username=&lt;your_username&gt; competition="Competition Name"</code>
    
    🚪 /leave_competition - Leave a competition
    <b>Usage:</b> <code>/leave_competition username=&lt;your_username&gt; competition="Competition Name"</code>

    📍 /check_location - Check activities near a location
    <b>Usage:</b> <code>/check_location lat=&lt;latitude&gt; lon=&lt;longitude&gt; [radius=&lt;km&gt;]</code>
    OR <code>/check_location location="Location Name" [radius=&lt;km&gt;]</code>
    <b>Example:</b> <code>/check_location lat=46.2044 lon=6.1432 radius=1.5</code>

    ℹ️ /help - Show this menu
    <b>Usage:</b> <code>/help</code>
    """;
    }

    /**
     * Returns the name of the command, which is used to identify it in the Telegram bot.
     *
     * @return The command name, "/help".
     */
    @Override
    public String getCommandName() {
        return "/help";
    }

    /**
     * Determines whether the command can be executed in the given chat.
     * This command can be executed in private chats, group chats, and channels.
     *
     * @param chatId The ID of the chat where the command is being used.
     * @return True if the command is allowed in the chat, false otherwise.
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)
                || chatTypeFactory.isChatType(chatId, ChatType.GROUP)
                || chatTypeFactory.isChatType(chatId, ChatType.CHANNEL);
    }
}