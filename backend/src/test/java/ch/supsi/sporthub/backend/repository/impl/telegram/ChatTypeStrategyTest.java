package ch.supsi.sporthub.backend.repository.impl.telegram;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatTypeStrategyTest {

    @Test
    void testChannelChatStrategy() {
        ChannelChatStrategy strategy = new ChannelChatStrategy();
        assertTrue(strategy.matches("-1001234567890"));
        assertFalse(strategy.matches("-1234567890"));
        assertFalse(strategy.matches("1234567890"));
    }

    @Test
    void testGroupChatStrategy() {
        GroupChatStrategy strategy = new GroupChatStrategy();
        assertTrue(strategy.matches("-1234567890"));
        assertTrue(strategy.matches("-100987654321"));
        assertFalse(strategy.matches("1234567890"));
    }

    @Test
    void testPrivateChatStrategy() {
        PrivateChatStrategy strategy = new PrivateChatStrategy();
        assertTrue(strategy.matches("1234567890"));
        assertFalse(strategy.matches("-1234567890"));
        assertFalse(strategy.matches("-100987654321"));
    }
}
