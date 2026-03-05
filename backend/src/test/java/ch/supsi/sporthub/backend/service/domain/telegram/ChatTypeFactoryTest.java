package ch.supsi.sporthub.backend.service.domain.telegram;

import ch.supsi.sporthub.backend.model.ChatType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatTypeFactoryTest {

    private ChatTypeFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ChatTypeFactory();
    }

    @Test
    void testIsPrivateChatType() {
        assertTrue(factory.isChatType("123456789", ChatType.PRIVATE));
        assertFalse(factory.isChatType("-123456789", ChatType.PRIVATE));
    }

    @Test
    void testIsGroupChatType() {
        assertTrue(factory.isChatType("-123456789", ChatType.GROUP));
        assertFalse(factory.isChatType("123456789", ChatType.GROUP));
    }

    @Test
    void testIsChannelChatType() {
        assertTrue(factory.isChatType("-100987654321", ChatType.CHANNEL));
        assertFalse(factory.isChatType("-987654321", ChatType.CHANNEL));
    }

    @Test
    void testIsChatType_NullStrategy() {
        assertFalse(factory.isChatType("123456789", null));
    }
}
