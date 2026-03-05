package ch.supsi.sporthub.backend.service.domain.telegram;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.repository.api.ChatTypeStrategy;
import ch.supsi.sporthub.backend.repository.impl.telegram.ChannelChatStrategy;
import ch.supsi.sporthub.backend.repository.impl.telegram.GroupChatStrategy;
import ch.supsi.sporthub.backend.repository.impl.telegram.PrivateChatStrategy;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory class responsible for determining the type of a chat (Private, Group, or Channel).
 * It uses the Strategy design pattern to delegate the logic of matching a chat ID to a specific chat type.
 * This class interacts with different strategies for each chat type: PrivateChatStrategy, GroupChatStrategy, and ChannelChatStrategy.
 */
@Component
public class ChatTypeFactory implements IChatTypeFactory {

    private final Map<ChatType, ChatTypeStrategy> strategies = new EnumMap<>(ChatType.class);

    /**
     * Constructs a {@link ChatTypeFactory} and initializes the strategies for each chat type.
     * The strategies are mapped to their respective chat types (Private, Group, and Channel).
     */
    public ChatTypeFactory() {
        strategies.put(ChatType.PRIVATE, new PrivateChatStrategy());
        strategies.put(ChatType.GROUP, new GroupChatStrategy());
        strategies.put(ChatType.CHANNEL, new ChannelChatStrategy());
    }

    /**
     * Determines whether the provided chat ID matches the specified chat type.
     * This method delegates the matching logic to the appropriate strategy based on the chat type.
     *
     * @param chatId The ID of the chat to check.
     * @param chatType The chat type (PRIVATE, GROUP, or CHANNEL) to match against.
     * @return True if the chat ID matches the specified chat type, false otherwise.
     */
    @Override
    public boolean isChatType(String chatId, ChatType chatType) {
        ChatTypeStrategy strategy = strategies.get(chatType);
        return strategy != null && strategy.matches(chatId);
    }
}